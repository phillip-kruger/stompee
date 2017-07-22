package com.github.phillipkruger.stompee;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import javax.inject.Inject;
import javax.json.Json;
import javax.json.JsonArray;
import javax.json.JsonArrayBuilder;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import javax.json.JsonWriter;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import lombok.extern.java.Log;
 
/** 
 * Simple Servlet for other data needed by the screen.
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
@Log
@WebServlet(value="/servlet/stompee", name="StompeeServlet") 
public class StompeeServlet extends HttpServlet {
    
    private static final String ACTION = "action";
    
    private static final String GET_ALL_LOGGER_NAMES = "getAllLoggerNames";
    private static final String GET_LOGGER_LEVEL = "getLoggerLevel";
    private static final String GET_DEFAULT_SETTINGS = "getDefaultSettings";
    private static final String NAME = "name";
    private static final String LEVEL = "level";
    private static final String CONTENT_TYPE = "application/json";
    private final StompeeUtil stompeeUtil = new StompeeUtil();
    
    @Inject
    private StompeeProperties stompeeProperties;
    
    @Override
    public void service(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        res.setContentType(CONTENT_TYPE);
        
        String action = req.getParameter(ACTION);
        if(GET_ALL_LOGGER_NAMES.equalsIgnoreCase(action)){
            getAllLoggerNames(req, res);
        }else if(GET_LOGGER_LEVEL.equalsIgnoreCase(action)){
            getLoggerLevel(req, res);
        }else if(GET_DEFAULT_SETTINGS.equalsIgnoreCase(action)){
            getDefaultSettings(res);
        }
    }

    private void getDefaultSettings(ServletResponse res) throws IOException, ServletException {
        JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
        String loggerName = stompeeProperties.getProperty("logger", null);
        if(loggerName!=null && !loggerName.isEmpty())objectBuilder.add(NAME, loggerName);
        String loggerLevel = stompeeProperties.getProperty("level", null);
        if(loggerLevel!=null && !loggerLevel.isEmpty())objectBuilder.add(LEVEL, loggerLevel);
        writeObject(res, objectBuilder.build());
    }
    
    private void getLoggerLevel(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        String name = req.getParameter(NAME);
        Level level = stompeeUtil.getLevel(name);
        if(level!=null){
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add(LEVEL, level.getName());
            writeObject(res, objectBuilder.build());
        }
    }
    
    private void getAllLoggerNames(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        
        List<String> loggers = stompeeUtil.getAllLoggerNames();
        loggers.forEach((name) -> {
            arrayBuilder.add(name);
        });
        writeArray(res,arrayBuilder.build());
    }
    
    private void writeObject(ServletResponse res,JsonObject o) throws IOException{
        try(StringWriter stringWriter = new StringWriter(); 
            JsonWriter jsonWriter = Json.createWriter(stringWriter)){
            jsonWriter.writeObject(o);
            res.getWriter().write(stringWriter.toString());
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        res.getWriter().flush();
    }
    
    private void writeArray(ServletResponse res,JsonArray a) throws IOException{
        try(StringWriter stringWriter = new StringWriter(); 
            JsonWriter jsonWriter = Json.createWriter(stringWriter)){
            jsonWriter.writeArray(a);
            res.getWriter().write(stringWriter.toString());
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        
        res.getWriter().flush();
    }
    
}