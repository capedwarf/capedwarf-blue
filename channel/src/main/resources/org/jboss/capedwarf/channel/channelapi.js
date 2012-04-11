function handleChannelMessage(token, type, msg) {
    var socket = CapedwarfChannelManager.instance.getSocket(token);

    if (type == "message") {
        socket.handleMessage(msg);
    } else if (type == "open") {
        socket.handleOpen();
    } else if (type == "close") {
        socket.handleClose();
    } else if (type == "error") {
        socket.handleError();
    }
}


var win = window;
var undef = void 0;

//    var browserSupportsWebSocket = "WebSocket" in window;
var browserSupportsWebSocket = false;

assign = function (name, value) {
    var c = name.split(".");
    if (!(c[0] in win) && win.execScript) {
        win.execScript("var " + c[0]);
    }
    for (var f; c.length && (f = c.shift());) {
        if (!c.length && value !== undef) {
            win[f] = value;
        } else {
            win = win[f] ? win[f] : win[f] = {};
        }
    }
};



