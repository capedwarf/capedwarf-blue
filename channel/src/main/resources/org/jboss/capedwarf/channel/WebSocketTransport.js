

WebSocketTransport = function(socket) {
    this.socket = socket;
};

WebSocketTransport.prototype.start = function() {
    var s = this.socket;
    var loc = window.location;
    var url = (loc.protocol === "https:" ? "wss://" : "ws://") + loc.host + contextPath + "/_ah/channel_ws?token=" + s.token;
    this.webSocket = new WebSocket(url);
    this.webSocket.onopen = function() {
        s.handleOpen();
    };
    this.webSocket.onmessage = function(msg) {
        s.handleMessage(msg.data);
    };
    this.webSocket.onclose = function() {
        s.handleClose();
    };
    this.webSocket.onerror = function() {
        s.handleClose();
    }
};


WebSocketTransport.prototype.close = function() {
    this.webSocket.send("close");
};


