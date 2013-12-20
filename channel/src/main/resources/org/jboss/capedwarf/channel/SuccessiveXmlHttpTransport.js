
SuccessiveXmlHttpTransport = function(socket) {
    this.socket = socket;
    this.requestIndex = 0;
};

SuccessiveXmlHttpTransport.prototype.start = function() {
    this.sendRequest("");
};

SuccessiveXmlHttpTransport.prototype.sendRequest = function(ackList) {
    var reqIndex = this.requestIndex++;
    var t = this;
    var xhr = new XMLHttpRequest();
    xhr.onreadystatechange = function() {
        const REQUEST_NOT_INITIALIZED = 0;
        const SERVER_CONNECTION_ESTABLISHED = 1;
        const REQUEST_RECEIVED = 2;
        const PROCESSING_REQUEST = 3;
        const REQUEST_FINISHED = 4;
        const HTTP_OK = 200;
        const HTTP_NOT_FOUND = 404;

        if (xhr.readyState == REQUEST_FINISHED) {
            var ackIds = "";
            if (xhr.status == HTTP_OK) {
                var lines = xhr.responseText.split(/\r\n|\r|\n/g);
                for (var i = 0; i < lines.length; i++) {
                    var line = lines[i];
                    var tokens = line.split(/:_:/g);
                    var type = tokens[0];
                    var messageId = tokens[1];
                    var message = tokens[2];
                    if (type != undefined) {
                        t.socket.processMessage(type, message);
                        if (messageId != "" && messageId != undefined) {
                            ackIds = (ackIds == "" ? "" : (ackIds + ",")) + messageId;
                        }
                    }
                }
            }
            if (xhr.status != HTTP_NOT_FOUND && t.socket.readyState != goog.appengine.Socket.ReadyState.CLOSED) {
                setTimeout(function () {
                    t.socket.transport.sendRequest(ackIds);
                }, 1);
            }
        }
    };
    xhr.open("POST", contextPath + "/_ah/channel?transport=SuccessiveXmlHttp&token=" + this.socket.token + "&requestIndex=" + reqIndex + "&ackIds=" + ackList, true);
    xhr.send();
};

SuccessiveXmlHttpTransport.prototype.close = function() {
    var request = new XMLHttpRequest();
    request.open("POST", contextPath + "/_ah/channel?action=closeChannel&token=" + this.socket.token, true);
    request.send();
};

