
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
        if (xhr.readyState == 4) {
            var lines = xhr.responseText.split(/\r\n|\r|\n/g);
            var ackIds = "";
            for (var i=0; i<lines.length; i++) {
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

            if (socket.readyState != goog.appengine.Socket.ReadyState.CLOSED) {
                setTimeout(function() {
                    socket.transport.sendRequest(ackIds);
                }, 1);
            }
        }
    };
    xhr.open("POST", "/_ah/channel?transport=SuccessiveXmlHttp&token=" + this.socket.token + "&requestIndex=" + reqIndex + "&ackIds=" + ackList, true);
    xhr.send();
};

SuccessiveXmlHttpTransport.prototype.close = function() {
    var request = new XMLHttpRequest();
    request.open("POST", "/_ah/channel?action=closeChannel&token=" + this.socket.token, true);
    request.send();
};

