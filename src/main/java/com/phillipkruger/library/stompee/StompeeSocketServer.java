package com.phillipkruger.library.stompee;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
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
@ServerEndpoint("/websocket/stompee") 
public class StompeeSocketServer {
   
    private final RandomNameGenerator randomNameGenerator = new RandomNameGenerator();
    
    @OnOpen
    public void onOpen(Session session){
        String appName = getAppName();
        systemMessage(appName,session);
    }
    
    @OnClose
    public void onClose(Session session){
        stop(session);
    }
    
    @OnMessage
    public void onMessage(String message, Session session){
        if("start".equalsIgnoreCase(message)){
            start(session);
        } else if("stop".equalsIgnoreCase(message)){
            stop(session);
        } else {
            // TODO: Log levels
        }
    }
    
    private void start(Session session){
        String name = getName(session);
        if(name == null){
            name = randomNameGenerator.generateName();
            registerHandler(session,name);
            SESSIONS.put(session.getId(), session);
            loggerMessage("Started " + name,session);
        }else{
            loggerMessage(name + " is already running",session);
        }
    }
    
    private void stop(Session session){
        String name = getName(session);
        if(name == null){
            loggerMessage("Can not stop, not running",session);
        }else{
            loggerMessage("Stopped " + name + "",session);
            unregisterHandler(session);
            SESSIONS.remove(session.getId());
        }
    }
    
    private void loggerMessage(String message,Session session){
        loggerMessage(Level.INFO,message,session);
    }
    
    private void loggerMessage(Level level,String message,Session session){
        LogRecord logRecored = new LogRecord(level, message);                                                                      
        try {
            Formatter formatter = getFormatter(session);
            if(formatter!=null){
                session.getBasicRemote().sendText(formatter.format(logRecored));
            }else {
                session.getBasicRemote().sendText(message);
            }
        }catch (IllegalStateException | IOException ex) {
            log.severe(ex.getMessage());
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
            String appName = (String) ic.lookup("java:app/AppName");
            return appName;
        } catch (NamingException ex) {
            return "Unknown";
        }
    }
    
    private Handler registerHandler(Session session,String name){
        Handler handler = new StompeeHandler(session);
        // TODO: Pass in the name
        Logger logger = Logger.getLogger("");
        logger.addHandler(handler);
        session.getUserProperties().put(HANDLER, handler);
        session.getUserProperties().put(NAME, name);
        return handler;
    }
    
    private void unregisterHandler(Session session){
        Handler handler = getHandler(session);
        if(handler!=null){
            // TODO: Pass in the name
            Logger logger = Logger.getLogger("");
            logger.removeHandler(handler);
        }
        session.getUserProperties().remove(NAME);
        session.getUserProperties().remove(HANDLER);
    }
    
    private Handler getHandler(Session session){
        Object o = session.getUserProperties().get(HANDLER);
        if(o!=null){
            return (Handler)o;
        }
        return null;
    }
    
    private Formatter getFormatter(Session session){
        Handler handler = getHandler(session);
        if(handler!=null){
            return handler.getFormatter();
        }else{
            return null;
        }
    }
    
    private String getName(Session session){
        Object o = session.getUserProperties().get(NAME);
        if(o==null)return null;
        return (String)o;
    }
    
    private static final String NAME = "name";
    private static final String HANDLER = "handler";
    private static final Map<String,Session> SESSIONS = new ConcurrentHashMap<>();
}