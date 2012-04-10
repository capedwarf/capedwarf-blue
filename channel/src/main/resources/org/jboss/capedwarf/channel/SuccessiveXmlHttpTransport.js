
SuccessiveXmlHttpTransport = function(socket) {
    this.socket = socket;
    this.xhr = new XMLHttpRequest();
    this.requestIndex = 0;
    var x = this.xhr;
    var t = this;
    this.xhr.onreadystatechange = function() {
        if (x.readyState == 4) {
            eval(x.responseText);
            if (socket.readyState != goog.appengine.Socket.ReadyState.CLOSED) {
                t.sendRequest();
            }
        }
    };
};

SuccessiveXmlHttpTransport.prototype.start = function() {
    this.sendRequest();
};

SuccessiveXmlHttpTransport.prototype.sendRequest = function() {
    this.xhr.open("POST", "/_ah/channel?transport=SuccessiveXmlHttp&token=" + this.socket.token + "&requestIndex=" + this.requestIndex++, true);
    this.xhr.send();
};

SuccessiveXmlHttpTransport.prototype.close = function() {
    var request = new XMLHttpRequest();
    request.open("POST", "/_ah/channel?action=closeChannel&token=" + this.socket.token, true);
    request.send();
};

