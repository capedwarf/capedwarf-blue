

WebSocketTransport = function(socket) {
    this.socket = socket;
};

WebSocketTransport.prototype.start = function() {
    var s = this.socket;
    this.webSocket = new WebSocket("ws://localhost:8080/_ah/channel_ws?token=" + s.token);
    this.webSocket.onopen = function() {
        s.handleOpen();
    }
    this.webSocket.onmessage = function(msg) {
        s.handleMessage(msg.data);
    }
    this.webSocket.onclose = function() {
        s.handleClose();
    }
    this.webSocket.onerror = function() {
        s.handleClose();
    }
};


WebSocketTransport.prototype.close = function() {
    this.webSocket.send("close");
};


