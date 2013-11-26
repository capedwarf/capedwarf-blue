var win = window;
var undef = void 0;

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



