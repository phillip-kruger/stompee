package com.github.phillipkruger.stompee;

import java.io.IOException;
import java.io.StringWriter;
import java.util.List;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonArrayBuilder;
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
    
    private static final String NAME = "name";
    private static final String GET_ALL_LOGGER_NAMES = "getAllLoggerNames";
    private static final String GET_LOGGER_LEVEL = "getLoggerLevel";
    private static final String LEVEL = "level";
    private static final String CONTENT_TYPE = "application/json";
    
    private final StompeeUtil stompeeUtil = new StompeeUtil();
    
    @Override
    public void service(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        res.setContentType(CONTENT_TYPE);
        
        String action = req.getParameter(ACTION);
        if(GET_ALL_LOGGER_NAMES.equalsIgnoreCase(action)){
            getAllLoggerNames(req, res);
        }else if(GET_LOGGER_LEVEL.equalsIgnoreCase(action)){
            getLoggerLevel(req, res);
        }
    }

    private void getLoggerLevel(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        String name = req.getParameter(NAME);
        Level level = stompeeUtil.getLevel(name);
        if(level!=null){
            JsonObjectBuilder objectBuilder = Json.createObjectBuilder();
            objectBuilder.add(LEVEL, level.getName());
            try(StringWriter stringWriter = new StringWriter(); 
                JsonWriter jsonWriter = Json.createWriter(stringWriter)){
                jsonWriter.writeObject(objectBuilder.build());
                res.getWriter().write(stringWriter.toString());
            } catch (IOException ex) {
                log.log(Level.SEVERE, null, ex);
            }
            res.getWriter().flush();
        }
    }
    
    private void getAllLoggerNames(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        
        JsonArrayBuilder arrayBuilder = Json.createArrayBuilder();
        
        List<String> loggers = stompeeUtil.getAllLoggerNames();
        loggers.forEach((name) -> {
            arrayBuilder.add(name);
        });
        
        try(StringWriter stringWriter = new StringWriter(); 
            JsonWriter jsonWriter = Json.createWriter(stringWriter)){
            jsonWriter.writeArray(arrayBuilder.build());
            res.getWriter().write(stringWriter.toString());
        } catch (IOException ex) {
            log.log(Level.SEVERE, null, ex);
        }
        
        res.getWriter().flush();
    }
    
}