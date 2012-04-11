
LongIFrameTransport = function (socket) {
    this.socket = socket;
};

LongIFrameTransport.prototype.start = function () {
    var iframe = document.createElement("iframe");
    iframe.src = "/_ah/channel?transport=" + transport + "&token=" + this.socket.token;
    iframe.style.display = "none";
    iframe.style.position = "absolute";
    iframe.style.visibility = "hidden";
    iframe.style.width = iframe.style.height = "100%";
    iframe.onload = function () {
        document.body.removeChild(iframe);
        document.body.appendChild(iframe);
    };
    document.body.appendChild(iframe);
};

LongIFrameTransport.prototype.close = function() {
    var request = new XMLHttpRequest();
    request.open("POST", "/_ah/channel?action=closeChannel&token=" + this.socket.token, true);
    request.send();
};
