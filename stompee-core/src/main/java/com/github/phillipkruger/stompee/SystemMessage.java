package com.github.phillipkruger.stompee;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonObjectBuilder;

/**
 * To create a system message (not a log message)
 * @author Phillip Kruger (phillip.kruger@gmail.com)
 */
public abstract class SystemMessage {
    
    protected abstract JsonObject toJsonObject();
    protected abstract String getMessageType();
    
    @Override
    public String toString() {
        return toJsonObject().toString();
    }
    
    protected JsonObjectBuilder getJsonObjectBuilder(){
        JsonObjectBuilder builder = Json.createObjectBuilder();
        builder.add(MESSAGE_TYPE, getMessageType());
        return builder;
    }
    
    private static final String MESSAGE_TYPE = "messageType";
}
