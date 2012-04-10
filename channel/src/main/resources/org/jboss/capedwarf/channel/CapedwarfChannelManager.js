
CapedwarfChannelManager = function() {
    this.sockets = {};
};

CapedwarfChannelManager.prototype.getSocket = function(token) {
    return this.sockets[token];
};

CapedwarfChannelManager.prototype.registerSocket = function(token, socket) {
    this.sockets[token] = socket;
};

CapedwarfChannelManager.prototype.unregisterSocket = function(token) {
    this.sockets[token] = undefined;
};

CapedwarfChannelManager.instance = new CapedwarfChannelManager();
