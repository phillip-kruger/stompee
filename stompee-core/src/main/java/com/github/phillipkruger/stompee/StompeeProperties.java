package com.github.phillipkruger.stompee;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.logging.Level;
import javax.annotation.PostConstruct;
import javax.enterprise.context.ApplicationScoped;
import lombok.extern.java.Log;

/**
 * Default Properties
 * @author Phillip Kruger (stompee@phillip-kruger.com)
 */
@Log
@ApplicationScoped
public class StompeeProperties {
    private final Properties props = new Properties();
    private final String PROPERTIES_FILE_NAME = "stompee.properties";
    
    @PostConstruct
    public void init(){
        // Properties
        try (InputStream propertiesStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(PROPERTIES_FILE_NAME)){
            if(propertiesStream!=null){
                props.load(propertiesStream);
            }else{
                log.log(Level.FINEST, "Can not load stompee properties [stompee.properties]");
            }
        } catch (NullPointerException | IOException ex) {
            log.log(Level.FINEST, "Can not load stompee properties [stompee.properties] - {0}", ex.getMessage());
        }
    }
    
    public boolean hasProperties(){
        return this.props!=null && !this.props.isEmpty();
    }
    
    public String getProperty(String key, String defaultValue){
        if(hasProperties() && props.containsKey(key)){
            return props.getProperty(key);
        }
        return defaultValue;
    }
    
    public Map<String,String> getProperties(){
        if(hasProperties()){
            return new HashMap<>((Map)props);
        }
        return null;
    }
}
