package com.phillipkruger.library.stompee;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * To create a system message (not a log message)
 * @author Phillip Kruger (phillip.kruger@momentum.co.za)
 */
@AllArgsConstructor @NoArgsConstructor
public class SystemMessage {
    
    @Getter @Setter
    private String applicationName;

    @Override
    public String toString() {
        return toJsonObject().toString();
    }
    
    private JsonObject toJsonObject(){
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(MESSAGE_TYPE, SYSTEM);
        if(applicationName!=null)builder.add(APPLICATION_NAME, applicationName);
        
        return builder.build();
    }
    
    private static final String SYSTEM = "system";
    private static final String MESSAGE_TYPE = "messageType";
    private static final String APPLICATION_NAME = "applicationName";
}
