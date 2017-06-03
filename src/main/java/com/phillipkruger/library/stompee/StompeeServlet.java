package com.phillipkruger.library.stompee;

import java.io.IOException;
import java.util.Enumeration;
import java.util.logging.LogManager;
import javax.servlet.GenericServlet;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import lombok.extern.java.Log;
 
/** 
 * Simple Servlet for other data needed by the screen.
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@Log
@WebServlet(value="/servlet/stompee", name="StompeeServlet") 
public class StompeeServlet extends GenericServlet {

    @Override
    public void service(ServletRequest req, ServletResponse res) throws IOException, ServletException {
        //res.setContentType("application/json");
        LogManager manager = LogManager.getLogManager();
        Enumeration<String> names = manager.getLoggerNames();
        while(names.hasMoreElements()){
            String name = names.nextElement();
            res.getWriter().println(name);
        }
        res.getWriter().flush();
    }
    
}