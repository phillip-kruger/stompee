package com.github.phillipkruger.stompee;

import java.io.IOException;
import java.io.StringReader;
import java.util.Enumeration;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import java.util.logging.MemoryHandler;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.websocket.OnClose;
import javax.websocket.OnMessage;
import javax.websocket.OnOpen;
import javax.websocket.Session;
import javax.websocket.server.ServerEndpoint;
import lombok.extern.java.Log;
 
/** 
 * Websocket server that can distribute log messages.
 * If there is no subscribers, the logging fall back to normal file.
 * 
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
@Log
@ServerEndpoint("/socket/stompee") 
public class StompeeSocket {
    private final StompeeUtil stompeeUtil = new StompeeUtil();
    
    @Inject
    private StompeeProperties stompeeProperties;
    
    @OnOpen
    public void onOpen(Session session){
        String appName = getAppName();
        startupMessage(appName,session);
    }
    
    @OnClose
    public void onClose(Session session){
        stop(session);
    }
    
    @OnMessage
    public void onMessage(String message, Session session) {
        if(message!=null && !message.isEmpty()){
            JsonObject jo = toJsonObject(message);
            
            String action = jo.getString(ACTION);
            if(START.equalsIgnoreCase(action)){
                String loggerName = jo.getString(LOGGER);
                if(stompeeUtil.validLogger(loggerName))start(session,loggerName);
            } else if(STOP.equalsIgnoreCase(action)){
                stop(session);
            } else if(SET_LOG_LEVEL.equalsIgnoreCase(action)){
                String levelName = jo.getString(LOG_LEVEL);
                setLogLevel(session,levelName); 
            } else if(SET_EXCEPTIONS_ONLY.equalsIgnoreCase(action)){
                Boolean exceptionsOnly = jo.getBoolean(Settings.EXCEPTIONS_ONLY);
                setExceptionsOnly(session,exceptionsOnly);
            } else if(SET_FILTER.equalsIgnoreCase(action)){
                String filter = jo.getString(Settings.FILTER);
                setFilter(session, filter);
            }
        }
    }
    
    private void start(Session session,String logger) {
        String uuid = getUuid(session);
        if(uuid == null){
            uuid = UUID.randomUUID().toString();
            registerHandler(session,uuid,logger);
            SESSIONS.put(session.getId(), session);
            // Set the default level
            String levelName = stompeeProperties.getProperty("level", null);
            if(levelName!=null && !levelName.isEmpty())setLogLevel(session,levelName); 
        }
    }
    
    private void stop(Session session) {
        String name = getUuid(session);
        if(name != null){
            unregisterHandler(session);
            SESSIONS.remove(session.getId());
        }
    }
    
    private void setLogLevel(Session session,String levelName){
        String loggerName = (String)session.getUserProperties().get(LOGGER_NAME);
        
        Level level = levelName == null ? null : Level.parse(levelName);
        Logger logger = Logger.getLogger(loggerName);
        logger.setLevel(level);

        LogManager logManager = LogManager.getLogManager();
        Enumeration<String> loggerNames = logManager.getLoggerNames();
        String prefix = loggerName + DOT;
        while (loggerNames.hasMoreElements()) {
            String aLoggerName = loggerNames.nextElement();
            if (aLoggerName.startsWith(prefix)) {
                Logger.getLogger(aLoggerName).setLevel(level);
            }
        }
    }
    
    private void setExceptionsOnly(Session session,Boolean exceptionsOnly){
        session.getUserProperties().put(Settings.EXCEPTIONS_ONLY, exceptionsOnly);
    }
    
    private void setFilter(Session session,String filter){
        if(filter!=null && !filter.isEmpty()){
            session.getUserProperties().put(Settings.FILTER, filter);
        }else{
            session.getUserProperties().remove(Settings.FILTER);
        }
    }
    
    private void startupMessage(String message,Session session){
        String startupMessage = new StartupMessage(message).toString();
        try {
            session.getBasicRemote().sendText(startupMessage);
        }catch (IllegalStateException | IOException ex) {
            log.severe(ex.getMessage());
        }
    }
    
    private String getAppName(){
        try {
            InitialContext ic = new InitialContext();
            String appName = (String) ic.lookup(JNDI_APP_NAME);
            return appName;
        } catch (NamingException ex) {
            return UNKNOWN;
        }
    }
          
    private void registerHandler(Session session,String uuid,String loggerName){
        
        Handler handler = new MemoryHandler(new StompeeHandler(session,loggerName),1000,Level.FINEST);
        
        Logger logger = stompeeUtil.getLogger(loggerName);
        if(logger!=null){
            logger.addHandler(handler);
            
            session.getUserProperties().put(HANDLER, handler);
            session.getUserProperties().put(ID, uuid);
            session.getUserProperties().put(LOGGER_NAME, loggerName);           
            session.getUserProperties().put(LOG_LEVEL, stompeeUtil.getLevel(logger).getName());
        }
    }
    
    private void unregisterHandler(Session session){
        Handler handler = getHandler(session);
        String loggerName = (String)session.getUserProperties().get(LOGGER_NAME);
        if(handler!=null){
            Logger logger = stompeeUtil.getLogger(loggerName);
            if(logger!=null)logger.removeHandler(handler); 
        }
        // Restore original level
        String originalLevel = (String)session.getUserProperties().remove(LOG_LEVEL);
        setLogLevel(session, originalLevel);
        
        session.getUserProperties().remove(ID);
        session.getUserProperties().remove(HANDLER);
        session.getUserProperties().remove(LOGGER_NAME);
        session.getUserProperties().remove(Settings.EXCEPTIONS_ONLY);
        session.getUserProperties().remove(Settings.FILTER);
    }
    
    private Handler getHandler(Session session){
        Object o = session.getUserProperties().get(HANDLER);
        if(o!=null){
            return (Handler)o;
        }
        return null;
    }
    
    private String getUuid(Session session){
        Object o = session.getUserProperties().get(ID);
        if(o==null)return null;
        return (String)o;
    }
    
    private JsonObject toJsonObject(String message){
        try(StringReader sr = new StringReader(message);
            JsonReader reader = Json.createReader(sr)){
            return reader.readObject();
        }
    }
    
    private static final String ID = "uuid";
    private static final String HANDLER = "handler";
    private static final String JNDI_APP_NAME = "java:app/AppName";
    private static final String UNKNOWN = "Unknown";
    private static final String START = "start";
    private static final String STOP = "stop";
    private static final String SET_LOG_LEVEL = "setLogLevel";
    private static final String SET_EXCEPTIONS_ONLY = "setExceptionsOnly";
    private static final String SET_FILTER = "setFilter";
    private static final String LOGGER_NAME = "loggerName";
    private static final String LOG_LEVEL = "logLevel";
    private static final String ACTION = "action";
    private static final String LOGGER = "logger";
    
    private static final String DOT = ".";
    private static final Map<String,Session> SESSIONS = new ConcurrentHashMap<>();
    //private static final Set<Session> sessions = Collections.synchronizedSet(new HashSet<Session>());
}