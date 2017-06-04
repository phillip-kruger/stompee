package com.phillipkruger.library.stompee;

import java.io.IOException;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;
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
    public void onMessage(String message, Session session){
        
        JsonObject jo = toJsonObject(message);

        String action = jo.getString(ACTION);
        String loggerName = jo.getString(LOGGER);
        
        if(START.equalsIgnoreCase(action)){
            start(session,loggerName);
        } else if(STOP.equalsIgnoreCase(action)){
            stop(session);
        }
    }
    
    private void start(Session session,String logger){
        String uuid = getUuid(session);
        if(uuid == null){
            uuid = UUID.randomUUID().toString();
            Map<Level,Boolean> levelMap = registerHandler(session,uuid,logger);
            SESSIONS.put(session.getId(), session);
            // TODO: Reply with the current log levels
            logLevelMessage(levelMap,session);
        }
    }
    
    private void stop(Session session){
        String name = getUuid(session);
        if(name != null){
            unregisterHandler(session);
            SESSIONS.remove(session.getId());
            // TODO: Restore the original log levels
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
    
    private void logLevelMessage(Map<Level,Boolean> message,Session session){
        String initLogLevelMessage = new InitialLogLevelMessage(message).toString();
        try {
            session.getBasicRemote().sendText(initLogLevelMessage);
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
          
    private Map<Level,Boolean> registerHandler(Session session,String uuid,String loggerName){
        Handler handler = new StompeeHandler(session,loggerName);
        
        Logger logger = getLogger(loggerName);
        logger.addHandler(handler);
        Map<Level,Boolean> levelMap = getLoggerMap(logger);
        session.getUserProperties().put(HANDLER, handler);
        session.getUserProperties().put(ID, uuid);
        session.getUserProperties().put(LOGGER_NAME, loggerName);
        session.getUserProperties().put(LEVEL_MAP, levelMap);
        return levelMap;
    }
    
    private void unregisterHandler(Session session){
        Handler handler = getHandler(session);
        String loggerName = (String)session.getUserProperties().get(LOGGER_NAME);
        if(handler!=null){
            Logger logger = getLogger(loggerName);
            logger.removeHandler(handler); 
        }
        session.getUserProperties().remove(ID);
        session.getUserProperties().remove(HANDLER);
        session.getUserProperties().remove(LOGGER_NAME);
        session.getUserProperties().remove(LEVEL_MAP);
        // TODO: Restore level map
    }
    
    private Logger getLogger(String loggerName){
        Logger logger; 
        if(loggerName==null || loggerName.isEmpty()){        
            logger = Logger.getLogger(DEFAULT_LOGGER);
        }else{
            logger = Logger.getLogger(loggerName);
        }
        return logger;
    }
    
    private Handler getHandler(Session session){
        Object o = session.getUserProperties().get(HANDLER);
        if(o!=null){
            return (Handler)o;
        }
        return null;
    }
    
    private Map<Level,Boolean> getLoggerMap(Logger logger){
        Map<Level,Boolean> levelMap = new HashMap<>();
        levelMap.put(Level.CONFIG, logger.isLoggable(Level.CONFIG));
        levelMap.put(Level.FINE, logger.isLoggable(Level.FINE));
        levelMap.put(Level.FINER, logger.isLoggable(Level.FINER));
        levelMap.put(Level.FINEST, logger.isLoggable(Level.FINEST));
        levelMap.put(Level.INFO, logger.isLoggable(Level.INFO));
        levelMap.put(Level.SEVERE, logger.isLoggable(Level.SEVERE));
        levelMap.put(Level.WARNING, logger.isLoggable(Level.WARNING));
        return levelMap;
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
    private static final String DEFAULT_LOGGER = "";
    private static final String LOGGER_NAME = "loggerName";
    private static final String LEVEL_MAP = "levelMap";
    private static final String ACTION = "action";
    private static final String LOGGER = "logger";
    private static final Map<String,Session> SESSIONS = new ConcurrentHashMap<>();
}