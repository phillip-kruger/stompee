/* 
 * Javascript file for stompee.html
 */

var contextRoot = getContextRoot();

function getContextRoot() {
    var base = document.getElementsByTagName('base')[0];
    if (base && base.href && (base.href.length > 0)) {
        base = base.href;
    } else {
        base = document.URL;
    }
    
    var u = base.substr(0,base.indexOf("/", base.indexOf("/", base.indexOf("//") + 2) + 1));
    var u = u.substr(u.indexOf("//")+2);
    var contextRoot = u.substr(u.indexOf("/"));
    return contextRoot;
}

var webSocket;
var messages = document.getElementById("messages");

$('document').ready(function(){
    openSocket();
    $('table').tablesort();
});

window.onbeforeunload = function() {
    closeSocket();
};

function openSocket(){
    // Ensures only one connection is open at a time
    if(webSocket !== undefined && webSocket.readyState !== WebSocket.CLOSED){
        writeResponse("Already connected...");
        return;
    }
    // Create a new instance of the websocket
    var loc = window.location, new_uri;
    if (loc.protocol === "https:") {
        new_uri = "wss:";
    } else {
        new_uri = "ws:";
    }
    new_uri += "//" + loc.host;
    new_uri += contextRoot + "/socket/stompee";
    webSocket = new WebSocket(new_uri);

    /**
     * Binds functions to the listeners for the websocket.
     */
    webSocket.onopen = function(event){
        // For reasons I can't determine, onopen gets called twice
        // and the first time event.data is undefined.
        // Leave a comment if you know the answer.
        if(event.data === undefined)
            return;
        
        writeResponse(event.data);
    };

    webSocket.onmessage = function(event){
        try{
            // JSON Message
            var json = JSON.parse(event.data);
            
            switch(json.messageType) {
                case "log":
                    var timestamp = new Date(json.timestamp);
                    var timestring = timestamp.toLocaleTimeString();
                    var level = getClassLogLevel(json.level);
                    var icon = getExceptionIcon(json);
                    var tid = json.threadId;
                    var msg = json.message;
                    var sourceClassName = json.sourceClassName;
                    var sourceMethodName = json.sourceMethodName;
                    var sequenceNumber = json.sequenceNumber;
                    
                    writeResponse("<tr class='" + level + "'>\n\
                                    <td data-tooltip='" + json.level + "' data-position='top left'>" + sequenceNumber + "</td>\n\
                                    <td>" + tid + "</td>\n\
                                    <td>" + timestring + "</td>\n\
                                    <td>" + sourceClassName + "</td>\n\
                                    <td>" + sourceMethodName + "</td>\n\
                                    <td>" + msg +"</td>\n\
                                    <td>" + icon + "</td>\n\
                                    </tr>");
                    
                    if(json.stacktrace){
                        for (var i in json.stacktrace) {
                            var stacktrace = enhanceStacktrace(json.loggerName, json.stacktrace[i]);
                            writeResponse("<tr style='display: none;' id='" + sequenceNumber + "'><td>" + sequenceNumber + "</td><td class='active' colspan='6'>" + stacktrace + "</td></tr>");
                        }
                    }
                    
                    break;
                case "system":
                    $("#applicationName").html("<h2>" + json.applicationName + "</h2>");
                    break;
            }
        }catch(e){
            // Unknown message ?
            console.log("Unknown message " + event.data);
        }
    };

    webSocket.onclose = function(){
        writeResponse("Connection closed");
    };

    $('pre code').each(function(i, block) {
        hljs.highlightBlock(block);
    });
}

function getClassLogLevel(level){
    if(level == "WARNING")return "warning";
    if(level == "SEVERE")return "error";
    if(level == "INFO")return "positive";
    if(level == "FINE")return "blue";
    if(level == "FINER")return "blue"; // TODO: Find better colors
    if(level == "FINEST")return "blue"; // TODO: Find better colors
    return level;
}

function getExceptionIcon(json){
    if(json.stacktrace){
        return "<i style='cursor:pointer;' class='warning sign icon' onclick='toggleException(" + json.sequenceNumber + ")'></i>";
    }
    return "";
}

function enhanceStacktrace(loggerName, stacktrace){
    var lines = stacktrace.split('\n');
    for(var i = 0;i < lines.length;i++){
        
        console.log(">> " + loggerName + " = " + lines[i]);
    }
    return stacktrace;
}

function toggleException(sequenceNumber){
    var element = document.getElementById(sequenceNumber);
    var result_style = element.style;
    
    if(result_style.display == ''){
        result_style.display = "none";
    }else{
        result_style.display = '';
    }
}

function closeSocket(){
    webSocket.close();
}

function writeResponse(text){
    messages.innerHTML += text;
}

function startLog(){
    var loggerName = $("#loggerName").val();
    $("#startIcon").addClass("disabled");
    $("#startIcon").prop("disabled", true);
    $("#loggerName").addClass("disabled");
    $("#loggerName").prop("disabled", true);
    
    var msg = createJsonMessage("start",loggerName);
    webSocket.send(msg);
    
    $("#stopIcon").removeClass("disabled");
    $("#stopIcon").prop("disabled", false);
}

function stopLog(){
    var loggerName = $("#loggerName").val();
    $("#stopIcon").addClass("disabled");
    $("#stopIcon").prop("disabled", true);
    
    var msg = createJsonMessage("stop",loggerName);
    webSocket.send(msg);
    
    $("#startIcon").removeClass("disabled");
    $("#startIcon").prop("disabled", false);
    $("#loggerName").removeClass("disabled");
    $("#loggerName").prop("disabled", false);
}

function createJsonMessage(doAction,loggerName){
    var msg = {
        action: doAction,
        logger: loggerName
    };
    
    return JSON.stringify(msg);
    
}

function clearScreen(){
    messages.innerHTML = "";
}

$("#loggerName").on('keyup', function (e) {
    if (e.keyCode == 13) {
        startLog();
    }
});

