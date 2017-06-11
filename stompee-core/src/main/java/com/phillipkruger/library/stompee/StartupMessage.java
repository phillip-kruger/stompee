package com.phillipkruger.library.stompee;

import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * To create a startup message, to send system state
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
@AllArgsConstructor @NoArgsConstructor
public class StartupMessage extends SystemMessage {
    
    @Getter @Setter
    private String applicationName;

    @Override
    protected JsonObject toJsonObject(){
        JsonObjectBuilder builder = getJsonObjectBuilder();
        if(applicationName!=null)builder.add(APPLICATION_NAME, applicationName);
        return builder.build();
    }
    
    @Override
    protected String getMessageType() {
        return STARTUP_MESSAGE;
    }
    
    private static final String STARTUP_MESSAGE = "startupMessage";
    private static final String APPLICATION_NAME = "applicationName";
}
