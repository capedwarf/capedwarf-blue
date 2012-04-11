
CapedwarfChannel = function (token) {
    this.token = token;
};
CapedwarfChannel.prototype.open = function (optionalHandler) {
    optionalHandler = optionalHandler || new CapedwarfSocket.Handler;
    return new CapedwarfSocket(this.token, optionalHandler);
};



assign("goog.appengine.Channel", CapedwarfChannel);
assign("goog.appengine.Channel.prototype.open", CapedwarfChannel.prototype.open);

