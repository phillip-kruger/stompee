package com.phillipkruger.library.stompee;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Formatter;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import lombok.AllArgsConstructor;
import lombok.extern.java.Log;

/**
 * Formatting log records into a json format
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@Log
@AllArgsConstructor
public class JsonFormatter extends Formatter {
    private final String loggerName;
    
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
        builder.add(MESSAGE_TYPE, LOG);
        if(logRecord.getLoggerName()!=null)builder.add(LOGGER_NAME, loggerName);
        if(logRecord.getLevel()!=null)builder.add(LEVEL, logRecord.getLevel().getName());
        if(logRecord.getMessage()!=null)builder.add(MESSAGE, formattedMessage);
        
        if(logRecord.getSourceClassName()!=null){
            builder.add(SOURCE_CLASS_NAME_FULL, logRecord.getSourceClassName());
            builder.add(SOURCE_CLASS_NAME, getJustClassName(logRecord.getSourceClassName()));
        }
        if(logRecord.getSourceMethodName()!=null)builder.add(SOURCE_METHOD_NAME, logRecord.getSourceMethodName());
        if(logRecord.getThrown()!=null)builder.add(STACKTRACE, getStacktraces(logRecord.getThrown()));
        builder.add(THREAD_ID, logRecord.getThreadID());
        builder.add(TIMESTAMP, logRecord.getMillis());
        builder.add(SEQUENCE_NUMBER, logRecord.getSequenceNumber());
        return builder.build();
    }
    
    
    private JsonArray getStacktraces(Throwable t){
        List<String> traces = new LinkedList<>();
        addStacktrace(traces, t);
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        traces.forEach((trace) -> {
            arrayBuilder.add(trace);
        });
        return arrayBuilder.build();
    }
    
    private void addStacktrace(List<String> traces,Throwable t){
        traces.add(getStacktrace(t));
        if(t.getCause()!=null)addStacktrace(traces, t.getCause());
    }
    
    private String getStacktrace(Throwable t){
        try(StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw)){
            t.printStackTrace(pw);
            return sw.toString();
        } catch (IOException ex) {
            log.log(Level.WARNING, "Can not create stacktrace [{0}]", ex.getMessage());
            return null;
        }
    }
   
    private String getJustClassName(String fullName){
        int lastDot = fullName.lastIndexOf(DOT) + 1;
        return fullName.substring(lastDot);
    }
    
    private static final String LOG = "log";
    private static final String MESSAGE_TYPE = "messageType";
    private static final String LOGGER_NAME = "loggerName";
    private static final String LEVEL = "level";
    private static final String MESSAGE = "message";
    private static final String SOURCE_CLASS_NAME_FULL = "sourceClassNameFull";
    private static final String SOURCE_CLASS_NAME = "sourceClassName";
    private static final String SOURCE_METHOD_NAME = "sourceMethodName";
    private static final String THREAD_ID = "threadId";
    private static final String TIMESTAMP = "timestamp";
    private static final String STACKTRACE = "stacktrace";
    private static final String SEQUENCE_NUMBER = "sequenceNumber";
    private static final String DOT = ".";
}
