
CapedwarfSocket = function (token, handler) {
    CapedwarfChannelManager.instance.registerSocket(token, this);

    this.readyState = goog.appengine.Socket.ReadyState.CONNECTING;
    this.token = token;

    this.onopen = handler.onopen;
    this.onmessage = handler.onmessage;
    this.onerror = handler.onerror;
    this.onclose = handler.onclose;

    if (!document.body) {
        throw "document.body is not defined -- do not create socket from script in <head>.";
    }

//    var transportType = browserSupportsWebSocket ? "WebSocket" : "SuccessiveXmlHttp";
    var transportType = "SuccessiveXmlHttp";

    var socket = this;
    setTimeout(function () {
        socket.connect(transportType);
    }, 1);
};

CapedwarfSocket.prototype.connect = function(transportType) {
    this.transport = this.getTransport(transportType);
    this.transport.start();
};

CapedwarfSocket.prototype.getTransport = function(transportType) {
    if (transportType == "LongIFrame") {
        return new LongIFrameTransport(this);
    } else if (transportType == "SuccessiveXmlHttp") {
        return new SuccessiveXmlHttpTransport(this);
    } else if (transportType == "WebSocket") {
        return new WebSocketTransport(this);
    } else {
        throw "Unknown transport type: " + transportType;
    }
};



CapedwarfSocket.prototype.beforeunload_ = function () {
    this.close();
};

CapedwarfSocket.prototype.handleOpen = function () {
    this.readyState = goog.appengine.Socket.ReadyState.OPEN;
    this.onopen();
};

CapedwarfSocket.prototype.handleMessage = function (msg) {
    this.onmessage({data:msg});
};

CapedwarfSocket.prototype.handleClose = function () {
    this.readyState = goog.appengine.Socket.ReadyState.CLOSED;
    this.onclose();
};

CapedwarfSocket.prototype.handleError = function () {
    this.onerror();
};

CapedwarfSocket.prototype.close = function () {
    this.readyState = goog.appengine.Socket.ReadyState.CLOSING;
    this.transport.close();
};

var noop = function() {
};

CapedwarfSocket.Handler = function () {
};
CapedwarfSocket.Handler.prototype.onopen = noop;
CapedwarfSocket.Handler.prototype.onmessage = noop;
CapedwarfSocket.Handler.prototype.onerror = noop;
CapedwarfSocket.Handler.prototype.onclose = noop;




assign("goog.appengine.Socket", CapedwarfSocket);
assign("goog.appengine.Socket.prototype.send", CapedwarfSocket.prototype.send);
assign("goog.appengine.Socket.prototype.close", CapedwarfSocket.prototype.close);
assign("goog.appengine.Socket.ReadyState", {CONNECTING:0,OPEN:1,CLOSING:2,CLOSED:3});
assign("goog.appengine.Socket.ReadyState.CONNECTING", 0);
assign("goog.appengine.Socket.ReadyState.OPEN", 1);
assign("goog.appengine.Socket.ReadyState.CLOSING", 2);
assign("goog.appengine.Socket.ReadyState.CLOSED", 3);

goog.appengine.Socket = CapedwarfSocket;
goog.appengine.Socket.ReadyState = {CONNECTING:0, OPEN:1, CLOSING:2, CLOSED:3};



