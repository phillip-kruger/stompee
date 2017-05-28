package com.phillipkruger.library.stompee;

import java.io.IOException;
import java.util.Iterator;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.logging.Handler;
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
public class StompeeSocketServer extends Handler {
   
    private final JsonFormatter formatter = new JsonFormatter();
    
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
    
    @Override
    public void publish(LogRecord logRecord) {
        String msg = getFormatter().format(logRecord);
        log.severe("stompee >>>>>>>>>>>>>>> " + msg);
        logMessage(msg);
    }
 
    private void systemMessage(String message){
        systemMessage(Level.INFO,message);
    }
    
    private void systemMessage(Level level,String message){
        LogRecord logRecored = new LogRecord(level, message);
        logMessage(formatter.format(logRecored));
    }
    
    private void logMessage(String logline){
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
        Logger logger = Logger.getGlobal(); // TODO: Allow passing in of log name  
        //logger.setUseParentHandlers(false);
        logger.addHandler(this);
    }
    
    private void unregisterListener(){
        Logger logger = Logger.getGlobal(); // TODO: Allow passing in of log name  
        //logger.setUseParentHandlers(true);
        logger.removeHandler(this);
    }
    
    @Override
    public void flush() {} 

    @Override
    public void close() throws SecurityException {}
    
    private static final Queue<Session> SESSIONS = new ConcurrentLinkedQueue<>();
}