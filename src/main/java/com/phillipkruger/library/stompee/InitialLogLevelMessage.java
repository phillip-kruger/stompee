package com.phillipkruger.library.stompee;

import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * To create a initial log level message
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@AllArgsConstructor @NoArgsConstructor
public class InitialLogLevelMessage extends SystemMessage {
    
    @Getter @Setter
    private Map<Level,Boolean> loggerMap;

    @Override
    protected JsonObject toJsonObject(){
        JsonObjectBuilder builder = getJsonObjectBuilder();
        builder.add(LOG_LEVELS, getLoggerMapJson());
        return builder.build();
    }
    
    private JsonObject getLoggerMapJson(){
        JsonObjectBuilder loggerJson = Json.createObjectBuilder();
        Map<Level,Boolean> m = getLoggerMap();
        Set<Map.Entry<Level, Boolean>> entries = m.entrySet();
        
        entries.forEach((entry) -> {
            loggerJson.add(entry.getKey().getName(), entry.getValue());
        });
        
        return loggerJson.build();
    }
    
    @Override
    protected String getMessageType() {
        return INIT_LOG_LEVEL_MESSAGE;
    }
    
    private static final String INIT_LOG_LEVEL_MESSAGE = "initialLogLevelMessage";
    private static final String LOG_LEVELS = "logLevels";
}
