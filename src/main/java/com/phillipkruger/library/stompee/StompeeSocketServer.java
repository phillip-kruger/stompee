package com.phillipkruger.library.stompee;

import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import javax.websocket.OnClose;
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
public class StompeeSocketServer implements LogServer {
   
    private final JsonFormatter formatter = new JsonFormatter();
    private StompeeHandler stompeeHandler;
    
    @OnOpen
    public void onOpen(Session session){
        SESSIONS.add(session);
        if(SESSIONS.size()==1)registerListener();
        
        systemMessage("Log viewer [" + session.getId() + "] joined"); // TODO: Change to use user Id once we have it
    }
    
    @OnClose
    public void onClose(Session session){
        systemMessage("Log viewer [" + session.getId() + "] left");
        SESSIONS.remove(session);
        if(SESSIONS.isEmpty())unregisterListener();
    }
    
    private void systemMessage(String message){
        systemMessage(Level.INFO,message);
    }
    
    private void systemMessage(Level level,String message){
        LogRecord logRecored = new LogRecord(level, message);
        logMessage(formatter.format(logRecored));
    }
    
    @Override
    public void logMessage(String logline){
        Iterator<Session> iterator = SESSIONS.iterator();
        while (iterator.hasNext()) {
            try {
                Session next = iterator.next();
                next.getBasicRemote().sendText(logline);   
            }catch (IllegalStateException | IOException ex) {
                log.severe(ex.getMessage());
            }
        }
    }
    
    
    private void registerListener(){
        stompeeHandler = new StompeeHandler(this);
        stompeeHandler.setFormatter(formatter);
        Logger logger = Logger.getLogger("com.phillipkruger.example.stompee.ExampleService"); // TODO: Allow passing in of log name  
        //logger.setUseParentHandlers(false);
        logger.addHandler(stompeeHandler);
    }
    
    private void unregisterListener(){
        Logger logger = Logger.getLogger("com.phillipkruger.example.stompee.ExampleService"); // TODO: Allow passing in of log name  
        //logger.setUseParentHandlers(true);
        logger.removeHandler(stompeeHandler);
        stompeeHandler = null;
    }
    
    private static final Queue<Session> SESSIONS = new ConcurrentLinkedQueue<>();
}