package com.phillipkruger.library.stompee;

import java.io.IOException;
import java.io.StringReader;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
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
        systemMessage(appName,session);
    }
    
    @OnClose
    public void onClose(Session session){
        String loggerName = (String)session.getUserProperties().get(LOGGER_NAME);
        stop(session,loggerName);
    }
    
    @OnMessage
    public void onMessage(String message, Session session){
        
        JsonObject jo = toJsonObject(message);

        String action = jo.getString("action");
        String loggerName = jo.getString("logger");
        
        if(START.equalsIgnoreCase(action)){
            start(session,loggerName);
        } else if(STOP.equalsIgnoreCase(action)){
            stop(session,loggerName);
        }
    }
    
    private void start(Session session,String logger){
        String uuid = getUuid(session);
        if(uuid == null){
            uuid = UUID.randomUUID().toString();
            registerHandler(session,uuid,logger);
            SESSIONS.put(session.getId(), session);
        }
    }
    
    private void stop(Session session,String logger){
        String name = getUuid(session);
        if(name != null){
            unregisterHandler(session,logger);
            SESSIONS.remove(session.getId());
        }
    }
    
    private void systemMessage(String message,Session session){
        String systemMessage = new SystemMessage(message).toString();
        try {
            session.getBasicRemote().sendText(systemMessage);
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
          
    private Handler registerHandler(Session session,String uuid,String loggerName){
        Handler handler = new StompeeHandler(session,loggerName);
        
        Logger logger = getLogger(loggerName);
        logger.addHandler(handler);
        session.getUserProperties().put(HANDLER, handler);
        session.getUserProperties().put(ID, uuid);
        session.getUserProperties().put(LOGGER_NAME, loggerName);
        return handler;
    }
    
    private void unregisterHandler(Session session,String loggerName){
        Handler handler = getHandler(session);
        
        if(handler!=null){
            Logger logger = getLogger(loggerName);
            logger.removeHandler(handler); // TODO: What if someone else is looking at the log ? With this name ?
        }
        session.getUserProperties().remove(ID);
        session.getUserProperties().remove(HANDLER);
        session.getUserProperties().remove(LOGGER_NAME, loggerName);
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
    private static final Map<String,Session> SESSIONS = new ConcurrentHashMap<>();
}