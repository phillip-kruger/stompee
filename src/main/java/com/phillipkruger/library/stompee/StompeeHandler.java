package com.phillipkruger.library.stompee;

import java.util.logging.Handler;
import java.util.logging.LogRecord;
import lombok.AllArgsConstructor;
 
/** 
 * Log handler for Stompee
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@AllArgsConstructor
public class StompeeHandler extends Handler {
   
    private final LogServer logServer;
    
    @Override
    public void publish(LogRecord logRecord) {
        String msg = getFormatter().format(logRecord);
        logServer.logMessage(msg);
    }

    @Override
    public void flush() {}

    @Override
    public void close() throws SecurityException {}
 
}