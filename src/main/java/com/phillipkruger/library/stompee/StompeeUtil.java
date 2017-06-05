package com.phillipkruger.library.stompee;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Some util to help with logger.
 * @author Phillip Kruger (phillip.kruger@momentum.co.za)
 */
public class StompeeUtil {

    public Logger getLogger(String loggerName){
        Logger logger; 
        if(loggerName==null || loggerName.isEmpty()){        
            logger = Logger.getLogger(DEFAULT_LOGGER);
        }else{
            logger = Logger.getLogger(loggerName);
        }
        return logger;
    }
    
    public Level getLevel(String loggerName){
        Logger logger = getLogger(loggerName);
        return getLevel(logger);
    }
    
    public Level getLevel(Logger logger){
        Level level = logger.getLevel();
        if(level==null && logger.getParent()!=null)return getLevel(logger.getParent());
        if(level==null)return Level.OFF;
        return level;
    }
    
    private static final String DEFAULT_LOGGER = "";
}
