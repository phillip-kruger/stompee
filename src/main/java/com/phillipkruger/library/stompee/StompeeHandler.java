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
        if(session!=null && shouldLog(logRecord)){ 
            String message = getFormatter().format(logRecord);
            try {
                session.getBasicRemote().sendText(message);   
            }catch (Throwable ex) {
                try {session.close();} catch (IOException ex1) {}
            }
        }
    }

    private boolean shouldLog(LogRecord logRecord){
        Object exceptionsOnlyProperty = session.getUserProperties().get(EXCEPTIONS_ONLY);
        if(exceptionsOnlyProperty!=null){
            boolean exceptionsOnly = (Boolean)exceptionsOnlyProperty;
            if(exceptionsOnly && logRecord.getThrown()!=null)return true;
            return !exceptionsOnly;
        }
        return true;
    }
    
    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}

    private static final String EXCEPTIONS_ONLY = "exceptionsOnly";
   
}