package com.phillipkruger.library.stompee;

import java.io.IOException;
import java.util.logging.Handler;
import java.util.logging.LogRecord;
import javax.websocket.Session;
import lombok.extern.java.Log;
 
/** 
 * Log handler for Stompee
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@Log
public class StompeeHandler extends Handler {
   
    private final Session session;
    
    public StompeeHandler(Session session,String logger){
        this.session = session;
        setFormatter(new JsonFormatter(logger));
    }
    
    @Override
    public void publish(LogRecord logRecord) {
        if(session!=null){
            String message = getFormatter().format(logRecord);
            try {
                session.getBasicRemote().sendText(message);   
            }catch (IllegalStateException | IOException ex) {
                log.severe(ex.getMessage());
            }
        }
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}
   
}