

WebSocketTransport = function(socket) {
    this.socket = socket;
};

WebSocketTransport.prototype.start = function() {
    this.webSocket = new WebSocket("ws://localhost:8080/_ah/channel?transport=WebSocket&token=" + this.socket.token);
    this.webSocket.onopen = this.socket.handleOpen;
    this.webSocket.onmessage = this.socket.handleMessage;
    this.webSocket.onclose = this.socket.handleClose;
    this.webSocket.onerror = this.socket.handleError;
};


WebSocketTransport.prototype.close = function() {
    this.webSocket.send("close");
};


