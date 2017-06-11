package com.phillipkruger.library.stompee;

import java.util.logging.Level;
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
public class LogLevelMessage extends SystemMessage {
    
    @Getter @Setter
    private Level level;

    @Override
    protected JsonObject toJsonObject(){
        JsonObjectBuilder builder = getJsonObjectBuilder();
        builder.add(LOG_LEVEL, level.getName());
        return builder.build();
    }
    
    @Override
    protected String getMessageType() {
        return INIT_LOG_LEVEL_MESSAGE;
    }
    
    private static final String INIT_LOG_LEVEL_MESSAGE = "logLevelMessage";
    private static final String LOG_LEVEL = "logLevel";
}
