
function handleChannelMessage(token, type, msg) {
    var socket = CapedwarfChannelManager.instance.getSocket(token);

    if (type == "message") {
        socket.onmessage({data:msg});
    } else if (type == "open") {
        socket.onopen();
    } else if (type == "close") {
        socket.handleClose();
    } else if (type == "error") {
        socket.onerror();
    }
}

(function() {

    var win = this;
    var undef = void 0;

    assign = function(name, value) {
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


    CapedwarfChannel = function (token) {
        this.token = token;
    };
    CapedwarfChannel.prototype.open = function (optionalHandler) {
        optionalHandler = optionalHandler || new CapedwarfSocket.Handler;
        return new CapedwarfSocket(this.token, optionalHandler);
    };


    function connect(transport, socket, token) {
        if (transport == "LongIFrame") {
            var iframe = document.createElement("iframe");
            iframe.src = "/_ah/channel?transport=" + transport + "&token=" + token;
            iframe.style.display = "none";
            iframe.style.position = "absolute";
            iframe.style.visibility = "hidden";
            iframe.style.width = iframe.style.height = "100%";
            iframe.onload = function() {
                document.body.removeChild(iframe);
                document.body.appendChild(iframe);
            };

            document.body.appendChild(iframe);
        } else if (transport == "LongScript") {
            var script = document.createElement("script");
            script.language = "JavaScript";
            script.type = "text/javascript";
            script.src = "/_ah/channel?transport=" + transport + "&token=" + token;
            document.body.appendChild(script);
        } else if (transport == "SuccessiveXmlHttp") {
            var successiveXmlHttpTransport = new SuccessiveXmlHttpTransport(socket, token);
            successiveXmlHttpTransport.start();
        }
    }

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

        var transport = "SuccessiveXmlHttp";
//        var transport = "LongIFrame";

        var socket = this;
        setTimeout(function () {
            connect(transport, socket, token);
        }, 1);
    };

    SuccessiveXmlHttpTransport = function(socket, token) {
        this.socket = socket;
        this.token = token;
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
        this.xhr.open("POST", "/_ah/channel?transport=SuccessiveXmlHttp&token=" + this.token + "&requestIndex=" + this.requestIndex++, true);
        this.xhr.send();
    };


    CapedwarfSocket.prototype.beforeunload_ = function () {
        this.close();
    };

    CapedwarfSocket.prototype.handleClose = function () {
        this.readyState = goog.appengine.Socket.ReadyState.CLOSED;
        this.onclose();
    };

    CapedwarfSocket.prototype.close = function () {
        this.readyState = goog.appengine.Socket.ReadyState.CLOSING;
        var request = new XMLHttpRequest();
        request.open("POST", "/_ah/channel?action=closeChannel&token=" + this.token, true);
        request.send();
    };

    CapedwarfSocket.Handler = function () {
    };
    CapedwarfSocket.Handler.prototype.onopen = function () {
    };
    CapedwarfSocket.Handler.prototype.onmessage = function () {
    };
    CapedwarfSocket.Handler.prototype.onerror = function () {
    };
    CapedwarfSocket.Handler.prototype.onclose = function () {
    };

    assign("goog.appengine.Channel", CapedwarfChannel);
    assign("goog.appengine.Channel.prototype.open", CapedwarfChannel.prototype.open);
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

    CapedwarfChannelManager = function() {
        this.sockets = {};
    };

    CapedwarfChannelManager.prototype.getSocket = function(token) {
        return this.sockets[token];
    };

    CapedwarfChannelManager.prototype.registerSocket = function(token, socket) {
        this.sockets[token] = socket;
    };

    CapedwarfChannelManager.instance = new CapedwarfChannelManager();

})();


