package com.github.phillipkruger.stompee;

import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import lombok.extern.java.Log;
 
/** 
 * Simple Servlet for other data needed by the screen.
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@Log
@WebServlet(value="/servlet/stompee", name="StompeeServlet") 
public class StompeeServlet extends HttpServlet {
    
    private static final String ACTION = "action";
    
    private static final String NAME = "name";
    private static final String GET_ALL_LOGGER_NAMES = "getAllLoggerNames";
    private static final String GET_LOGGER_LEVEL = "getLoggerLevel";
    
    private final StompeeUtil stompeeUtil = new StompeeUtil();
    
    @Override
    public void service(ServletRequest req, ServletResponse res) throws IOException, ServletException {
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
            res.getWriter().print(level.getName());
            res.getWriter().flush();
        }
    }
    
    private void getAllLoggerNames(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        List<String> loggers = stompeeUtil.getAllLoggerNames();
        for(String name:loggers){
            res.getWriter().println(name);
        }
        res.getWriter().flush();
    }
    
}