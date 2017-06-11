/* 
 * Javascript file for stompee.html
 * Phillip Kruger (phillip.kruger@gmail.com)
 */

var contextRoot = getContextRoot();
var webSocket;
var messages = document.getElementById("messages");

    $('document').ready(function(){
        openSocket();
        $('table').tablesort();
        
        
        // Get all logger names 
        var url = contextRoot + "/servlet/stompee?action=getAllLoggerNames";
        var loggerNames = httpGet(url);
        var loggerNamesArray = loggerNames.split(/\r?\n/);
        
        var loggerNameMenu = $('#loggerNameMenu');
        for (var i in loggerNamesArray) {
            var $div = $("<div>", {"class": "item"});
            $div.append(loggerNamesArray[i]);
            loggerNameMenu.append($div);
        }
        
    });


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
            try {
                // JSON Message
                var json = JSON.parse(event.data);

                switch(json.messageType) {
                    case "log":
                        messageLog(json);
                        break;
                    case "startupMessage":
                        messageStartup(json);
                        break;
                }
            }catch(e){
                console.log("Unknown message " + e.message);
            }
        };

        webSocket.onclose = function(){
            writeResponse("Connection closed");
        };



        function messageLog(json){
            var timestamp = new Date(json.timestamp);
            var timestring = timestamp.toLocaleTimeString();
            var datestring = timestamp.toLocaleDateString();
            var level = getClassLogLevel(json.level);
            var tid = json.threadId;
            var msg = getMessage(json);
            var sourceClassName = json.sourceClassName;
            var sourceClassNameFull = json.sourceClassNameFull;
            var sourceMethodName = json.sourceMethodName;
            var sequenceNumber = json.sequenceNumber;

            writeResponse("<tr class='" + level + "'>\n\
                            <td data-tooltip='" + json.level + "' data-position='top left'>\n\
                                <a class='ui " + getLogLevelColor(json.level) + " empty circular label'></a> " + sequenceNumber + "\n\
                            </td>\n\
                            <td onclick='filterByThreadId(" + tid + ");'>" + tid + "</td>\n\
                            <td data-tooltip='" + datestring + "' data-position='top left'>" + timestring + "</td>\n\
                            <td data-tooltip='" + sourceClassNameFull + "' data-position='top left'>" + sourceClassName + "</td>\n\
                            <td>" + sourceMethodName + "</td>\n\
                            <td>" + msg + "</td>\n\
                         </tr>");

            if(json.stacktrace){
                for (var i in json.stacktrace) {
                    var stacktrace = enhanceStacktrace(json.loggerName, json.stacktrace[i]);
                    writeResponse("<tr style='display: none;' id='" + sequenceNumber + "'>\n\
                                    <td colspan='7'>\n\
                                        <div class='ui raised segment'>" + stacktrace + "</div>\n\
                                    </td>\n\
                                   </tr>");
                }
            }

        }

        function messageStartup(json){
            $("#applicationName").html("<h2>" + json.applicationName + "</h2>");
        }
    }

    function toggleLogLevel(level){
        var map = new Map();
        putMap(map,"logLevel",level);
        var msg = createJsonMessage("setLogLevel",map);
        webSocket.send(msg);
    }


    function toggleExceptionsOnly(){
        var map = new Map();
        putMap(map,"exceptionsOnly",$('#buttonExceptionsOnly').is(":checked"));
        var msg = createJsonMessage("setExceptionsOnly",map);
        webSocket.send(msg);
    }

    function getClassLogLevel(level){

        if(level === "WARNING") return "warning";
        if(level === "SEVERE") return "error";
        if(level === "INFO") return "positive";
        if(level === "FINE") return "blue";
        if(level === "FINER") return "blue"; // TODO: Find better colors
        if(level === "FINEST") return "blue"; // TODO: Find better colors
        return level;
    }

    function getLogLevelColor(level){

        if(level === "WARNING") return "orange";
        if(level === "SEVERE") return "red";
        if(level === "INFO") return "green";
        if(level === "FINE") return "teal";
        if(level === "FINER") return "blue";
        if(level === "FINEST") return "violet";
        return level;
    }

    function getMessage(json){
        if(json.stacktrace){
            return "<span style='cursor:pointer;' onclick='toggleException(" + json.sequenceNumber + ")'><i class='warning sign icon'></i>" + json.message + "</span>";
        }
        return json.message;
    }

    function enhanceStacktrace(loggerName, stacktrace){
        var enhanceStacktrace = [];
        var lines = stacktrace.split('\n');
        for(var i = 0;i < lines.length;i++){
            var line = lines[i].trim();
            if(line){
                var startWithAt = line.startsWith("at ");
                if(!startWithAt){
                    var parts = line.split(":");
                    line = "<a class='ui red ribbon label'>" + parts[0] + "</a><span><b>" + parts[1] + "</b></span>";
                }
            }

            var isMyClass = line.includes(loggerName);
            if(isMyClass && loggerName)line = '<span class="red text">' + line + '</span>';

            enhanceStacktrace.push(line + '<br/>');
        }
        var newStacktrace = enhanceStacktrace.join('');
        return newStacktrace;
    }

    function toggleException(sequenceNumber){
        var element = document.getElementById(sequenceNumber);
        var result_style = element.style;

        if(result_style.display === ''){
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
        var loggerName = $('#loggerDropdown').dropdown('get text');
        
        if(loggerName){
            $("#startIcon").addClass("disabled");
            $("#startIcon").prop("disabled", true);
            $("#loggerDropdown").addClass("disabled");
            $("#loggerDropdown").prop("disabled", true);
            
            var exceptionsOnly = $("#exceptionOnly").val();
            var map = new Map();
            putMap(map,"logger",loggerName);
            putMap(map,"exceptionsOnly",exceptionsOnly);
            var msg = createJsonMessage("start",map);
            webSocket.send(msg);

            $("#stopIcon").removeClass("disabled");
            $("#stopIcon").prop("disabled", false);
            $("#settingsIcon").removeClass("disabled");
            $("#settingsIcon").prop("disabled", false);
            
            $("#loggerNameDiv").removeClass("error");
        }else{
            $("#loggerNameDiv").addClass("error");
        }
    }

    function stopLog(){
        var loggerName = $('#loggerDropdown').dropdown('get text');
        
        if(loggerName){
            $("#stopIcon").addClass("disabled");
            $("#stopIcon").prop("disabled", true);
            $("#settingsIcon").addClass("disabled");
            $("#settingsIcon").prop("disabled", true);

            var map = new Map();
            putMap(map,"logger",loggerName);
            var msg = createJsonMessage("stop",map);
            webSocket.send(msg);

            $("#startIcon").removeClass("disabled");
            $("#startIcon").prop("disabled", false);
            $("#loggerDropdown").removeClass("disabled");
            $("#loggerDropdown").prop("disabled", false);
        }
    }

    function putMap(map, key, value) {
        map.set(key, value);
    }

    function map_to_object(map) {
        const out = Object.create(null)
        map.forEach((value, key) => {
          if (value instanceof Map) {
            out[key] = map_to_object(value)
          }
          else {
            out[key] = value
          }
        })
        return out
    }

    function createJsonMessage(doAction,params){
        putMap(params,"action",doAction);
        var o = map_to_object(params);
        return JSON.stringify(o);  
    }

    function clearScreen(){
        messages.innerHTML = "";
    }

    function showSettingsModal(){
        // Here get the current settings
        var loggerName = $('#loggerDropdown').dropdown('get text');
        if(loggerName){
            var url = contextRoot + "/servlet/stompee?action=getLoggerLevel&name=" + loggerName;
            var level = httpGet(url);

            messageLogLevel(level);

            $('#modalSettings')
                .modal({
                    onHide: function(){
                        filterMessages();
                    }
                })    
                .modal('setting', 'transition', 'vertical flip')
                .modal('show')
            ;
        }
    }

    function filterMessages(){
        var filter = $("#txtFilterMessages").val();
        
        var map = new Map();
        if(filter){
            putMap(map,"filter",filter);
        }else{
            putMap(map,"filter","");
        }
        var msg = createJsonMessage("setFilter",map);
        webSocket.send(msg);
    }

    function showAboutModal(){
        $('#modalAbout')
            .modal('setting', 'transition', 'vertical flip')
            .modal('show')
        ;
    }

    function filterByThreadId(threadId){
        //console.log("TODO: Filter by id " + threadId);
    }

    function messageLogLevel(level){
        if(level === "INFO")$("#buttonInfo").prop("checked", "checked");
        if(level === "FINE")$("#buttonFine").prop("checked", "checked");
        if(level === "FINER")$("#buttonFiner").prop("checked", "checked");
        if(level === "FINEST")$("#buttonFinest").prop("checked", "checked");
        if(level === "WARNING")$("#buttonWarning").prop("checked", "checked");
        if(level === "SEVERE")$("#buttonSevere").prop("checked", "checked");
        if(level === "CONFIG")$("#buttonConfig").prop("checked", "checked");
    }


    function httpGet(theUrl){
        var xmlHttp = new XMLHttpRequest();
        xmlHttp.open( "GET", theUrl, false ); // false for synchronous request
        xmlHttp.send( null );
        return xmlHttp.responseText;
    }

    $("#loggerDropdown").on('keyup', function (e) {
        if (e.keyCode == 13) {
            startLog();
        }
    });

    window.onbeforeunload = function() {
        closeSocket();
    };
    
    $('#loggerDropdown')
        .dropdown()
    ;