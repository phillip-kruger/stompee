package com.github.phillipkruger.stompee;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import javax.validation.constraints.NotNull;
import lombok.extern.java.Log;

/**
 * Some util to help with logger.
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
@Log
public class StompeeUtil {

    public Logger getLogger(@NotNull String loggerName) {
        if(validLogger(loggerName)){
            return Logger.getLogger(loggerName);
        }
        return null;
    }
    
    public Level getLevel(String loggerName){
        Logger logger = getLogger(loggerName);
        if(logger!=null){
            return getLevel(logger);
        }
        return null;
    }
    
    public Level getLevel(Logger logger){
        if(logger==null || logger.getName().isEmpty())return Level.INFO; // Not sure about this 
        Level level = logger.getLevel();
        if(level==null && logger.getParent()!=null)return getLevel(logger.getParent());
        return level;
    }
    
    public boolean validLogger(String name){
        return !name.isEmpty() && LOGGERS.contains(name);
    }
    
    public List<String> getAllLoggerNames(){
        return LOGGERS;
    }
    
    private static final List<String> LOGGERS = new ArrayList<>();
    static {
    
        LogManager manager = LogManager.getLogManager();
        Enumeration<String> names = manager.getLoggerNames();
        while(names.hasMoreElements()){
            String name = names.nextElement();
            if(name!=null && !name.isEmpty() && name.contains(".") ){
                LOGGERS.add(name);
                String[] parts = name.split("\\.");
                LinkedList<String> l = new LinkedList<>(Arrays.asList(parts));
                if(l.size()>2){
                    while(l.size()>1){
                        l.remove(l.size()-1);
                        String parentLogger = String.join(".", l);
                        if(!LOGGERS.contains(parentLogger) && parentLogger.contains("."))LOGGERS.add(parentLogger);
                    }
                }
                
            }
        }
    }
}
