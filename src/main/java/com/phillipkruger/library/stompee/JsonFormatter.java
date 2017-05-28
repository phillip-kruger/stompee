package com.phillipkruger.library.stompee;

import java.io.IOException;
import java.io.StringWriter;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import lombok.NoArgsConstructor;
import lombok.extern.java.Log;

/**
 * Formatting log records into a json format
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@Log
@NoArgsConstructor
public class JsonFormatter extends Formatter {
    
    @Override
    public String format(final LogRecord logRecord) {
        try(StringWriter stringWriter = new StringWriter(); 
            JsonWriter jsonWriter = Json.createWriter(stringWriter)){

            jsonWriter.writeObject(toJsonObject(logRecord));
            return stringWriter.toString();
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }

        return null;
    }
    
    private JsonObject toJsonObject(LogRecord logRecord){
        String formattedMessage = formatMessage(logRecord);
        JsonObjectBuilder builder = Json.createObjectBuilder();
        if(logRecord.getLoggerName()!=null)builder.add(LOGGER_NAME, logRecord.getLoggerName());
        if(logRecord.getLevel()!=null)builder.add(LEVEL, logRecord.getLevel().getName());
        if(logRecord.getMessage()!=null)builder.add(MESSAGE, formattedMessage);
        if(logRecord.getSourceClassName()!=null)builder.add(SOURCE_CLASS_NAME, logRecord.getSourceClassName());
        if(logRecord.getSourceMethodName()!=null)builder.add(SOURCE_METHOD_NAME, logRecord.getSourceMethodName());    
        builder.add(THREAD_ID, logRecord.getThreadID());
        builder.add(TIMESTAMP, logRecord.getMillis());
        //builder.add(HOSTNAME, serverInfo.getHostname());
        //builder.add(PORT, serverInfo.getHttpPort());
        return builder.build();
    }
    
    private static final String LOGGER_NAME = "loggerName";
    private static final String LEVEL = "level";
    private static final String MESSAGE = "message";
    private static final String SOURCE_CLASS_NAME = "sourceClassName";
    private static final String SOURCE_METHOD_NAME = "sourceMethodName";
    private static final String THREAD_ID = "threadId";
    private static final String TIMESTAMP = "timestamp";
    //private static final String PORT = "port";
    //private static final String HOSTNAME = "hostname";
}
