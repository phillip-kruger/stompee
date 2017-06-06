package com.phillipkruger.library.stompee;

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
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@Log
@ServerEndpoint("/socket/stompee") 
public class StompeeSocket {
   
    private final StompeeUtil stompeeUtil = new StompeeUtil();
    
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
                String levelName = jo.getString(LOGGER); // TODO: Change name to param ?
                // TODO: Validate level
                setLogLevel(session,levelName); 
            }
        }
    }
    
    private void start(Session session,String logger) {
        String uuid = getUuid(session);
        if(uuid == null){
            uuid = UUID.randomUUID().toString();
            registerHandler(session,uuid,logger);
            SESSIONS.put(session.getId(), session);
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
        String prefix = loggerName + ".";
        while (loggerNames.hasMoreElements()) {
            String aLoggerName = loggerNames.nextElement();
            if (aLoggerName.startsWith(prefix)) {
                Logger.getLogger(aLoggerName).setLevel(level);
            }
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
        Handler handler = new StompeeHandler(session,loggerName);
        
        Logger logger = stompeeUtil.getLogger(loggerName);
        if(logger!=null){
            logger.addHandler(handler);
        
            session.getUserProperties().put(HANDLER, handler);
            session.getUserProperties().put(ID, uuid);
            session.getUserProperties().put(LOGGER_NAME, loggerName);
            session.getUserProperties().put(LOG_LEVEL, logger.getLevel().getName());
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
    
    private static final String LOGGER_NAME = "loggerName";
    private static final String LOG_LEVEL = "logLevel";
    private static final String ACTION = "action";
    private static final String LOGGER = "logger";
    private static final Map<String,Session> SESSIONS = new ConcurrentHashMap<>();
}