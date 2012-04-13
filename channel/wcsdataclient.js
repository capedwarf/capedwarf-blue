(function () {
    function f(a) {
        throw a;
    }

    var i = void 0, j = !0, k = null, m = !1, r, ba = ba || {}, s = this, ca = function (a) {
        for (var a = a.split("."), b = s, c; c = a.shift();)if (t(b[c]))b = b[c]; else return k;
        return b
    }, da = function () {
    }, ea = function (a) {
        var b = typeof a;
        if ("object" == b)if (a) {
            if (a instanceof Array)return"array";
            if (a instanceof Object)return b;
            var c = Object.prototype.toString.call(a);
            if ("[object Window]" == c)return"object";
            if ("[object Array]" == c || "number" == typeof a.length && "undefined" != typeof a.splice && "undefined" != typeof a.propertyIsEnumerable && !a.propertyIsEnumerable("splice"))return"array";
            if ("[object Function]" == c || "undefined" != typeof a.call && "undefined" != typeof a.propertyIsEnumerable && !a.propertyIsEnumerable("call"))return"function"
        } else return"null"; else if ("function" == b && "undefined" == typeof a.call)return"object";
        return b
    }, u = function (a) {
        return a !== i
    }, fa = function (a) {
        return a === k
    }, t = function (a) {
        return a != k
    }, w = function (a) {
        return"array" == ea(a)
    }, ga = function (a) {
        var b = ea(a);
        return"array" == b || "object" == b && "number" == typeof a.length
    }, x = function (a) {
        return"string" == typeof a
    }, ha = function (a) {
        return"function" ==
            ea(a)
    }, ia = function (a) {
        var b = typeof a;
        return"object" == b && a != k || "function" == b
    }, la = function (a) {
        return a[ja] || (a[ja] = ++ka)
    }, ja = "closure_uid_" + Math.floor(2147483648 * Math.random()).toString(36), ka = 0, ma = function (a, b, c) {
        return a.call.apply(a.bind, arguments)
    }, na = function (a, b, c) {
        a || f(Error());
        if (2 < arguments.length) {
            var d = Array.prototype.slice.call(arguments, 2);
            return function () {
                var c = Array.prototype.slice.call(arguments);
                Array.prototype.unshift.apply(c, d);
                return a.apply(b, c)
            }
        }
        return function () {
            return a.apply(b,
                arguments)
        }
    }, y = function (a, b, c) {
        y = Function.prototype.bind && -1 != Function.prototype.bind.toString().indexOf("native code") ? ma : na;
        return y.apply(k, arguments)
    }, oa = function (a, b) {
        var c = Array.prototype.slice.call(arguments, 1);
        return function () {
            var b = Array.prototype.slice.call(arguments);
            b.unshift.apply(b, c);
            return a.apply(this, b)
        }
    }, z = Date.now || function () {
        return+new Date
    }, assign = function (a, b) {
        var c = a.split("."), d = s;
        !(c[0]in d) && d.execScript && d.execScript("var " + c[0]);
        for (var e; c.length && (e = c.shift());)!c.length &&
            u(b) ? d[e] = b : d = d[e] ? d[e] : d[e] = {}
    }, A = function (a, b) {
        function c() {
        }

        c.prototype = b.prototype;
        a.z = b.prototype;
        a.prototype = new c;
        a.prototype.constructor = a
    };
    Function.prototype.bind = Function.prototype.bind || function (a, b) {
        if (1 < arguments.length) {
            var c = Array.prototype.slice.call(arguments, 1);
            c.unshift(this, a);
            return y.apply(k, c)
        }
        return y(this, a)
    };
    var qa = function (a) {
        this.stack = Error().stack || "";
        a && (this.message = "" + a)
    };
    A(qa, Error);
    qa.prototype.name = "CustomError";
    var ra = function (a, b) {
        for (var c = 1; c < arguments.length; c++)var d = ("" + arguments[c]).replace(/\$/g, "$$$$"), a = a.replace(/\%s/, d);
        return a
    }, sa = /^[a-zA-Z0-9\-_.!~*'()]*$/, ta = function (a) {
        a = "" + a;
        return!sa.test(a) ? encodeURIComponent(a) : a
    }, za = function (a) {
        if (!ua.test(a))return a;
        -1 != a.indexOf("&") && (a = a.replace(va, "&amp;"));
        -1 != a.indexOf("<") && (a = a.replace(wa, "&lt;"));
        -1 != a.indexOf(">") && (a = a.replace(xa, "&gt;"));
        -1 != a.indexOf('"') && (a = a.replace(ya, "&quot;"));
        return a
    }, va = /&/g, wa = /</g, xa = />/g, ya = /\"/g, ua = /[&<>\"]/,
        Aa = function (a) {
            1024 < a.length && (a = a.substring(0, 1021) + "...");
            return a
        }, Ba = function () {
        return Math.floor(2147483648 * Math.random()).toString(36) + Math.abs(Math.floor(2147483648 * Math.random()) ^ z()).toString(36)
    };
    var Ca = function (a, b) {
        b.unshift(a);
        qa.call(this, ra.apply(k, b));
        b.shift()
    };
    A(Ca, qa);
    Ca.prototype.name = "AssertionError";
    var Da = function (a, b, c) {
        if (!a) {
            var d = Array.prototype.slice.call(arguments, 2), e = "Assertion failed";
            if (b)var e = e + (": " + b), g = d;
            f(new Ca("" + e, g || []))
        }
    }, Ea = function (a, b) {
        f(new Ca("Failure" + (a ? ": " + a : ""), Array.prototype.slice.call(arguments, 1)))
    };
    var B = Array.prototype, Fa = B.indexOf ? function (a, b, c) {
        Da(a.length != k);
        return B.indexOf.call(a, b, c)
    } : function (a, b, c) {
        c = c == k ? 0 : 0 > c ? Math.max(0, a.length + c) : c;
        if (x(a))return!x(b) || 1 != b.length ? -1 : a.indexOf(b, c);
        for (; c < a.length; c++)if (c in a && a[c] === b)return c;
        return-1
    }, Ga = B.forEach ? function (a, b, c) {
        Da(a.length != k);
        B.forEach.call(a, b, c)
    } : function (a, b, c) {
        for (var d = a.length, e = x(a) ? a.split("") : a, g = 0; g < d; g++)g in e && b.call(c, e[g], g, a)
    }, Ha = function (a, b) {
        for (var c = x(a) ? a.split("") : a, d = a.length - 1; 0 <= d; --d)d in c &&
        b.call(i, c[d], d, a)
    }, Ia = B.some ? function (a, b, c) {
        Da(a.length != k);
        return B.some.call(a, b, c)
    } : function (a, b, c) {
        for (var d = a.length, e = x(a) ? a.split("") : a, g = 0; g < d; g++)if (g in e && b.call(c, e[g], g, a))return j;
        return m
    }, Ja = B.every ? function (a, b, c) {
        Da(a.length != k);
        return B.every.call(a, b, c)
    } : function (a, b, c) {
        for (var d = a.length, e = x(a) ? a.split("") : a, g = 0; g < d; g++)if (g in e && !b.call(c, e[g], g, a))return m;
        return j
    }, Ka = function (a, b) {
        var c = Fa(a, b);
        0 <= c && (Da(a.length != k), B.splice.call(a, c, 1))
    }, La = function (a) {
        return B.concat.apply(B,
            arguments)
    }, Ma = function (a) {
        if (w(a))return La(a);
        for (var b = [], c = 0, d = a.length; c < d; c++)b[c] = a[c];
        return b
    }, Na = function (a, b, c) {
        Da(a.length != k);
        return 2 >= arguments.length ? B.slice.call(a, b) : B.slice.call(a, b, c)
    }, Pa = function (a, b) {
        if (!ga(a) || !ga(b) || a.length != b.length)return m;
        for (var c = a.length, d = Oa, e = 0; e < c; e++)if (!d(a[e], b[e]))return m;
        return j
    }, Oa = function (a, b) {
        return a === b
    };
    var Qa, Ra, Sa, Ta, Ua = function () {
        return s.navigator ? s.navigator.userAgent : k
    };
    Ta = Sa = Ra = Qa = m;
    var Va;
    if (Va = Ua()) {
        var Wa = s.navigator;
        Qa = 0 == Va.indexOf("Opera");
        Ra = !Qa && -1 != Va.indexOf("MSIE");
        Sa = !Qa && -1 != Va.indexOf("WebKit");
        Ta = !Qa && !Sa && "Gecko" == Wa.product
    }
    var Xa = Qa, C = Ra, Ya = Ta, E = Sa, Za;
    a:{
        var $a = "", ab;
        if (Xa && s.opera)var bb = s.opera.version, $a = "function" == typeof bb ? bb() : bb; else if (Ya ? ab = /rv\:([^\);]+)(\)|;)/ : C ? ab = /MSIE\s+([^\);]+)(\)|;)/ : E && (ab = /WebKit\/(\S+)/), ab)var cb = ab.exec(Ua()), $a = cb ? cb[1] : "";
        if (C) {
            var db, eb = s.document;
            db = eb ? eb.documentMode : i;
            if (db > parseFloat($a)) {
                Za = "" + db;
                break a
            }
        }
        Za = $a
    }
    var fb = Za, gb = {}, F = function (a) {
        var b;
        if (!(b = gb[a])) {
            b = 0;
            for (var c = ("" + fb).replace(/^[\s\xa0]+|[\s\xa0]+$/g, "").split("."), d = ("" + a).replace(/^[\s\xa0]+|[\s\xa0]+$/g, "").split("."), e = Math.max(c.length, d.length), g = 0; 0 == b && g < e; g++) {
                var h = c[g] || "", l = d[g] || "", p = RegExp("(\\d*)(\\D*)", "g"), n = RegExp("(\\d*)(\\D*)", "g");
                do {
                    var q = p.exec(h) || ["", "", ""], v = n.exec(l) || ["", "", ""];
                    if (0 == q[0].length && 0 == v[0].length)break;
                    b = ((0 == q[1].length ? 0 : parseInt(q[1], 10)) < (0 == v[1].length ? 0 : parseInt(v[1], 10)) ? -1 : (0 == q[1].length ?
                        0 : parseInt(q[1], 10)) > (0 == v[1].length ? 0 : parseInt(v[1], 10)) ? 1 : 0) || ((0 == q[2].length) < (0 == v[2].length) ? -1 : (0 == q[2].length) > (0 == v[2].length) ? 1 : 0) || (q[2] < v[2] ? -1 : q[2] > v[2] ? 1 : 0)
                } while (0 == b)
            }
            b = gb[a] = 0 <= b
        }
        return b
    }, hb = {}, ib = function (a) {
        return hb[a] || (hb[a] = C && !!document.documentMode && document.documentMode >= a)
    };
    var jb = k, kb = k, lb = k;
    var mb = function (a, b) {
        for (var c in a)b.call(i, a[c], c, a)
    }, nb = function (a) {
        var b = [], c = 0, d;
        for (d in a)b[c++] = a[d];
        return b
    }, ob = function (a) {
        var b = [], c = 0, d;
        for (d in a)b[c++] = d;
        return b
    }, pb = function (a, b, c) {
        b in a && f(Error('The object already contains the key "' + b + '"'));
        a[b] = c
    }, qb = function (a) {
        var b = {}, c;
        for (c in a)b[c] = a[c];
        return b
    }, rb = "constructor,hasOwnProperty,isPrototypeOf,propertyIsEnumerable,toLocaleString,toString,valueOf".split(","), sb = function (a, b) {
        for (var c, d, e = 1; e < arguments.length; e++) {
            d = arguments[e];
            for (c in d)a[c] = d[c];
            for (var g = 0; g < rb.length; g++)c = rb[g], Object.prototype.hasOwnProperty.call(d, c) && (a[c] = d[c])
        }
    };
    var tb, ub = !C || ib(9);
    !Ya && !C || C && ib(9) || Ya && F("1.9.1");
    C && F("9");
    var vb = function (a, b) {
        var c;
        c = a.className;
        c = x(c) && c.match(/\S+/g) || [];
        for (var d = Na(arguments, 1), e = c.length + d.length, g = c, h = 0; h < d.length; h++)0 <= Fa(g, d[h]) || g.push(d[h]);
        a.className = c.join(" ");
        return c.length == e
    };
    var xb = function (a) {
        return a ? new wb(9 == a.nodeType ? a : a.ownerDocument || a.document) : tb || (tb = new wb)
    }, yb = function (a, b) {
        var c = b && "*" != b ? b.toUpperCase() : "";
        return a.querySelectorAll && a.querySelector && (!E || "CSS1Compat" == document.compatMode || F("528")) && c ? a.querySelectorAll(c + "") : a.getElementsByTagName(c || "*")
    }, Ab = function (a, b) {
        mb(b, function (b, d) {
            "style" == d ? a.style.cssText = b : "class" == d ? a.className = b : "for" == d ? a.htmlFor = b : d in zb ? a.setAttribute(zb[d], b) : 0 == d.lastIndexOf("aria-", 0) ? a.setAttribute(d, b) : a[d] = b
        })
    },
        zb = {cellpadding:"cellPadding", cellspacing:"cellSpacing", colspan:"colSpan", rowspan:"rowSpan", valign:"vAlign", height:"height", width:"width", usemap:"useMap", frameborder:"frameBorder", maxlength:"maxLength", type:"type"}, Cb = function (a, b, c) {
        function d(c) {
            c && b.appendChild(x(c) ? a.createTextNode(c) : c)
        }

        for (var e = 2; e < c.length; e++) {
            var g = c[e];
            ga(g) && !(ia(g) && 0 < g.nodeType) ? Ga(Bb(g) ? Ma(g) : g, d) : d(g)
        }
    }, Db = function (a) {
        return a && a.parentNode ? a.parentNode.removeChild(a) : k
    }, Bb = function (a) {
        if (a && "number" == typeof a.length) {
            if (ia(a))return"function" ==
                typeof a.item || "string" == typeof a.item;
            if (ha(a))return"function" == typeof a.item
        }
        return m
    }, wb = function (a) {
        this.Oa = a || s.document || document
    };
    r = wb.prototype;
    r.Vi = function (a, b, c) {
        var d = this.Oa, e = arguments, g = e[0], h = e[1];
        if (!ub && h && (h.name || h.type)) {
            g = ["<", g];
            h.name && g.push(' name="', za(h.name), '"');
            if (h.type) {
                g.push(' type="', za(h.type), '"');
                var l = {};
                sb(l, h);
                h = l;
                delete h.type
            }
            g.push(">");
            g = g.join("")
        }
        g = d.createElement(g);
        h && (x(h) ? g.className = h : w(h) ? vb.apply(k, [g].concat(h)) : Ab(g, h));
        2 < e.length && Cb(d, g, e);
        return g
    };
    r.createElement = function (a) {
        return this.Oa.createElement(a)
    };
    r.createTextNode = function (a) {
        return this.Oa.createTextNode(a)
    };
    r.A = function () {
        return this.Oa.parentWindow || this.Oa.defaultView
    };
    r.appendChild = function (a, b) {
        a.appendChild(b)
    };
    r.removeNode = Db;
    r.contains = function (a, b) {
        if (a.contains && 1 == b.nodeType)return a == b || a.contains(b);
        if ("undefined" != typeof a.compareDocumentPosition)return a == b || Boolean(a.compareDocumentPosition(b) & 16);
        for (; b && a != b;)b = b.parentNode;
        return b == a
    };
    var Eb = function () {
    };
    Eb.prototype.vb = m;
    Eb.prototype.I = function () {
        this.vb || (this.vb = j, this.g())
    };
    var Fb = function (a, b) {
        a.Ee || (a.Ee = []);
        a.Ee.push(b)
    };
    Eb.prototype.g = function () {
        this.Ee && Gb.apply(k, this.Ee)
    };
    var G = function (a) {
        a && "function" == typeof a.I && a.I()
    }, Gb = function (a) {
        for (var b = 0, c = arguments.length; b < c; ++b) {
            var d = arguments[b];
            ga(d) ? Gb.apply(k, d) : G(d)
        }
    };
    var Hb = function (a) {
        Hb[" "](a);
        return a
    };
    Hb[" "] = da;
    var Ib = function (a, b) {
        try {
            return Hb(a[b]), j
        } catch (c) {
        }
        return m
    };
    !C || ib(9);
    var Jb = !C || ib(9);
    C && F("8");
    !E || F("528");
    Ya && F("1.9b") || C && F("8") || Xa && F("9.5") || E && F("528");
    Ya && !F("8") || C && F("9");
    var H = function (a, b) {
        this.type = a;
        this.currentTarget = this.target = b
    };
    A(H, Eb);
    H.prototype.g = function () {
        delete this.type;
        delete this.target;
        delete this.currentTarget
    };
    H.prototype.wc = m;
    H.prototype.ue = j;
    var Kb = function (a, b) {
        a && this.ud(a, b)
    };
    A(Kb, H);
    r = Kb.prototype;
    r.target = k;
    r.relatedTarget = k;
    r.offsetX = 0;
    r.offsetY = 0;
    r.clientX = 0;
    r.clientY = 0;
    r.screenX = 0;
    r.screenY = 0;
    r.button = 0;
    r.keyCode = 0;
    r.charCode = 0;
    r.ctrlKey = m;
    r.altKey = m;
    r.shiftKey = m;
    r.metaKey = m;
    r.re = k;
    r.ud = function (a, b) {
        var c = this.type = a.type;
        H.call(this, c);
        this.target = a.target || a.srcElement;
        this.currentTarget = b;
        var d = a.relatedTarget;
        d ? Ya && (Ib(d, "nodeName") || (d = k)) : "mouseover" == c ? d = a.fromElement : "mouseout" == c && (d = a.toElement);
        this.relatedTarget = d;
        this.offsetX = E || a.offsetX !== i ? a.offsetX : a.layerX;
        this.offsetY = E || a.offsetY !== i ? a.offsetY : a.layerY;
        this.clientX = a.clientX !== i ? a.clientX : a.pageX;
        this.clientY = a.clientY !== i ? a.clientY : a.pageY;
        this.screenX = a.screenX || 0;
        this.screenY = a.screenY || 0;
        this.button =
            a.button;
        this.keyCode = a.keyCode || 0;
        this.charCode = a.charCode || ("keypress" == c ? a.keyCode : 0);
        this.ctrlKey = a.ctrlKey;
        this.altKey = a.altKey;
        this.shiftKey = a.shiftKey;
        this.metaKey = a.metaKey;
        this.state = a.state;
        this.re = a;
        delete this.ue;
        delete this.wc
    };
    r.g = function () {
        Kb.z.g.call(this);
        this.relatedTarget = this.currentTarget = this.target = this.re = k
    };
    var Lb = function () {
    }, Mb = 0;
    r = Lb.prototype;
    r.key = 0;
    r.uc = m;
    r.og = m;
    r.ud = function (a, b, c, d, e, g) {
        ha(a) ? this.ij = j : a && a.handleEvent && ha(a.handleEvent) ? this.ij = m : f(Error("Invalid listener argument"));
        this.xd = a;
        this.Qi = b;
        this.src = c;
        this.type = d;
        this.capture = !!e;
        this.cg = g;
        this.og = m;
        this.key = ++Mb;
        this.uc = m
    };
    r.handleEvent = function (a) {
        return this.ij ? this.xd.call(this.cg || this.src, a) : this.xd.handleEvent.call(this.xd, a)
    };
    var Nb = {}, I = {}, Ob = {}, Pb = {}, Qb = function (a, b, c, d, e) {
        if (b) {
            if (w(b)) {
                for (var g = 0; g < b.length; g++)Qb(a, b[g], c, d, e);
                return k
            }
            var d = !!d, h = I;
            b in h || (h[b] = {j:0, xa:0});
            h = h[b];
            d in h || (h[d] = {j:0, xa:0}, h.j++);
            var h = h[d], l = la(a), p;
            h.xa++;
            if (h[l]) {
                p = h[l];
                for (g = 0; g < p.length; g++)if (h = p[g], h.xd == c && h.cg == e) {
                    if (h.uc)break;
                    return p[g].key
                }
            } else p = h[l] = [], h.j++;
            g = Rb();
            g.src = a;
            h = new Lb;
            h.ud(c, g, a, b, d, e);
            c = h.key;
            g.key = c;
            p.push(h);
            Nb[c] = h;
            Ob[l] || (Ob[l] = []);
            Ob[l].push(h);
            a.addEventListener ? (a == s || !a.Pi) && a.addEventListener(b,
                g, d) : a.attachEvent(b in Pb ? Pb[b] : Pb[b] = "on" + b, g);
            return c
        }
        f(Error("Invalid event type"))
    }, Rb = function () {
        var a = Sb, b = Jb ? function (c) {
            return a.call(b.src, b.key, c)
        } : function (c) {
            c = a.call(b.src, b.key, c);
            if (!c)return c
        };
        return b
    }, Tb = function (a, b, c, d, e) {
        if (w(b)) {
            for (var g = 0; g < b.length; g++)Tb(a, b[g], c, d, e);
            return k
        }
        a = Qb(a, b, c, d, e);
        Nb[a].og = j;
        return a
    }, Ub = function (a, b, c, d, e) {
        if (w(b))for (var g = 0; g < b.length; g++)Ub(a, b[g], c, d, e); else {
            d = !!d;
            a:{
                g = I;
                if (b in g && (g = g[b], d in g && (g = g[d], a = la(a), g[a]))) {
                    a = g[a];
                    break a
                }
                a =
                    k
            }
            if (a)for (g = 0; g < a.length; g++)if (a[g].xd == c && a[g].capture == d && a[g].cg == e) {
                Vb(a[g].key);
                break
            }
        }
    }, Vb = function (a) {
        if (!Nb[a])return m;
        var b = Nb[a];
        if (b.uc)return m;
        var c = b.src, d = b.type, e = b.Qi, g = b.capture;
        c.removeEventListener ? (c == s || !c.Pi) && c.removeEventListener(d, e, g) : c.detachEvent && c.detachEvent(d in Pb ? Pb[d] : Pb[d] = "on" + d, e);
        c = la(c);
        e = I[d][g][c];
        if (Ob[c]) {
            var h = Ob[c];
            Ka(h, b);
            0 == h.length && delete Ob[c]
        }
        b.uc = j;
        e.Si = j;
        Wb(d, g, c, e);
        delete Nb[a];
        return j
    }, Wb = function (a, b, c, d) {
        if (!d.ve && d.Si) {
            for (var e = 0, g =
                0; e < d.length; e++)d[e].uc ? d[e].Qi.src = k : (e != g && (d[g] = d[e]), g++);
            d.length = g;
            d.Si = m;
            0 == g && (delete I[a][b][c], I[a][b].j--, 0 == I[a][b].j && (delete I[a][b], I[a].j--), 0 == I[a].j && delete I[a])
        }
    }, Xb = function (a) {
        var b, c = 0, d = b == k;
        b = !!b;
        if (a == k)mb(Ob, function (a) {
            for (var e = a.length - 1; 0 <= e; e--) {
                var g = a[e];
                if (d || b == g.capture)Vb(g.key), c++
            }
        }); else if (a = la(a), Ob[a])for (var a = Ob[a], e = a.length - 1; 0 <= e; e--) {
            var g = a[e];
            if (d || b == g.capture)Vb(g.key), c++
        }
    }, Yb = function (a, b) {
        var c = la(a), d = Ob[c];
        if (d) {
            var e = u(b), g = u(i);
            return e &&
                g ? (d = I[b], !!d && !!d[i] && c in d[i]) : !e && !g ? j : Ia(d, function (a) {
                return e && a.type == b || g && a.capture == i
            })
        }
        return m
    }, $b = function (a, b, c, d, e) {
        var g = 1, b = la(b);
        if (a[b]) {
            a.xa--;
            a = a[b];
            a.ve ? a.ve++ : a.ve = 1;
            try {
                for (var h = a.length, l = 0; l < h; l++) {
                    var p = a[l];
                    p && !p.uc && (g &= Zb(p, e) !== m)
                }
            } finally {
                a.ve--, Wb(c, d, b, a)
            }
        }
        return Boolean(g)
    }, Zb = function (a, b) {
        var c = a.handleEvent(b);
        a.og && Vb(a.key);
        return c
    }, Sb = function (a, b) {
        if (!Nb[a])return j;
        var c = Nb[a], d = c.type, e = I;
        if (!(d in e))return j;
        var e = e[d], g, h;
        if (!Jb) {
            g = b || ca("window.event");
            var l = j in e, p = m in e;
            if (l) {
                if (0 > g.keyCode || g.returnValue != i)return j;
                a:{
                    var n = m;
                    if (0 == g.keyCode)try {
                        g.keyCode = -1;
                        break a
                    } catch (q) {
                        n = j
                    }
                    if (n || g.returnValue == i)g.returnValue = j
                }
            }
            n = new Kb;
            n.ud(g, this);
            g = j;
            try {
                if (l) {
                    for (var v = [], aa = n.currentTarget; aa; aa = aa.parentNode)v.push(aa);
                    h = e[j];
                    h.xa = h.j;
                    for (var D = v.length - 1; !n.wc && 0 <= D && h.xa; D--)n.currentTarget = v[D], g &= $b(h, v[D], d, j, n);
                    if (p) {
                        h = e[m];
                        h.xa = h.j;
                        for (D = 0; !n.wc && D < v.length && h.xa; D++)n.currentTarget = v[D], g &= $b(h, v[D], d, m, n)
                    }
                } else g = Zb(c, n)
            } finally {
                v && (v.length =
                    0), n.I()
            }
            return g
        }
        d = new Kb(b, this);
        try {
            g = Zb(c, d)
        } finally {
            d.I()
        }
        return g
    };
    var ac = function (a) {
        a = "" + a;
        if (/^\s*$/.test(a) ? 0 : /^[\],:{}\s\u2028\u2029]*$/.test(a.replace(/\\["\\\/bfnrtu]/g, "@").replace(/"[^"\\\n\r\u2028\u2029\x00-\x08\x10-\x1f\x80-\x9f]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, "]").replace(/(?:^|:|,)(?:[\s\u2028\u2029]*\[)+/g, "")))try {
            return eval("(" + a + ")")
        } catch (b) {
        }
        f(Error("Invalid JSON string: " + a))
    }, bc = function (a) {
        return eval("(" + a + ")")
    }, ec = function (a) {
        var b = [];
        cc(new dc, a, b);
        return b.join("")
    }, dc = function () {
        this.Ge = i
    }, cc = function (a, b, c) {
        switch (typeof b) {
            case "string":
                fc(b,
                    c);
                break;
            case "number":
                c.push(isFinite(b) && !isNaN(b) ? b : "null");
                break;
            case "boolean":
                c.push(b);
                break;
            case "undefined":
                c.push("null");
                break;
            case "object":
                if (b == k) {
                    c.push("null");
                    break
                }
                if (w(b)) {
                    var d = b.length;
                    c.push("[");
                    for (var e = "", g = 0; g < d; g++)c.push(e), e = b[g], cc(a, a.Ge ? a.Ge.call(b, "" + g, e) : e, c), e = ",";
                    c.push("]");
                    break
                }
                c.push("{");
                d = "";
                for (g in b)Object.prototype.hasOwnProperty.call(b, g) && (e = b[g], "function" != typeof e && (c.push(d), fc(g, c), c.push(":"), cc(a, a.Ge ? a.Ge.call(b, g, e) : e, c), d = ","));
                c.push("}");
                break;
            case "function":
                break;
            default:
                f(Error("Unknown type: " + typeof b))
        }
    }, gc = {'"':'\\"', "\\":"\\\\", "/":"\\/", "\u0008":"\\b", "\u000c":"\\f", "\n":"\\n", "\r":"\\r", "\t":"\\t", "\x0B":"\\u000b"}, hc = /\uffff/.test("\uffff") ? /[\\\"\x00-\x1f\x7f-\uffff]/g : /[\\\"\x00-\x1f\x7f-\xff]/g, fc = function (a, b) {
        b.push('"', a.replace(hc, function (a) {
            if (a in gc)return gc[a];
            var b = a.charCodeAt(0), e = "\\u";
            16 > b ? e += "000" : 256 > b ? e += "00" : 4096 > b && (e += "0");
            return gc[a] = e + b.toString(16)
        }), '"')
    };
    var ic = "StopIteration"in s ? s.StopIteration : Error("StopIteration"), jc = function () {
    };
    jc.prototype.next = function () {
        f(ic)
    };
    jc.prototype.Bd = function () {
        return this
    };
    var kc = function (a) {
        if (a instanceof jc)return a;
        if ("function" == typeof a.Bd)return a.Bd(m);
        if (ga(a)) {
            var b = 0, c = new jc;
            c.next = function () {
                for (; ;) {
                    b >= a.length && f(ic);
                    if (b in a)return a[b++];
                    b++
                }
            };
            return c
        }
        f(Error("Not implemented"))
    }, lc = function (a, b) {
        if (ga(a))try {
            Ga(a, b, i)
        } catch (c) {
            c !== ic && f(c)
        } else {
            a = kc(a);
            try {
                for (; ;)b.call(i, a.next(), i, a)
            } catch (d) {
                d !== ic && f(d)
            }
        }
    };
    var mc = function (a) {
        if ("function" == typeof a.P)a = a.P(); else if (ga(a) || x(a))a = a.length; else {
            var b = 0, c;
            for (c in a)b++;
            a = b
        }
        return a
    }, nc = function (a) {
        if ("function" == typeof a.da)return a.da();
        if (x(a))return a.split("");
        if (ga(a)) {
            for (var b = [], c = a.length, d = 0; d < c; d++)b.push(a[d]);
            return b
        }
        return nb(a)
    }, oc = function (a) {
        if ("function" == typeof a.Ma)return a.Ma();
        if ("function" != typeof a.da) {
            if (ga(a) || x(a)) {
                for (var b = [], a = a.length, c = 0; c < a; c++)b.push(c);
                return b
            }
            return ob(a)
        }
    }, pc = function (a, b, c) {
        if ("function" == typeof a.forEach)a.forEach(b,
            c); else if (ga(a) || x(a))Ga(a, b, c); else for (var d = oc(a), e = nc(a), g = e.length, h = 0; h < g; h++)b.call(c, e[h], d && d[h], a)
    }, qc = function (a, b) {
        if ("function" == typeof a.every)return a.every(b, i);
        if (ga(a) || x(a))return Ja(a, b, i);
        for (var c = oc(a), d = nc(a), e = d.length, g = 0; g < e; g++)if (!b.call(i, d[g], c && c[g], a))return m;
        return j
    };
    var J = function (a, b) {
        this.l = {};
        this.F = [];
        var c = arguments.length;
        if (1 < c) {
            c % 2 && f(Error("Uneven number of arguments"));
            for (var d = 0; d < c; d += 2)this.set(arguments[d], arguments[d + 1])
        } else a && this.hg(a)
    };
    r = J.prototype;
    r.j = 0;
    r.jb = 0;
    r.P = function () {
        return this.j
    };
    r.da = function () {
        rc(this);
        for (var a = [], b = 0; b < this.F.length; b++)a.push(this.l[this.F[b]]);
        return a
    };
    r.Ma = function () {
        rc(this);
        return this.F.concat()
    };
    r.W = function (a) {
        return sc(this.l, a)
    };
    r.pg = function (a) {
        for (var b = 0; b < this.F.length; b++) {
            var c = this.F[b];
            if (sc(this.l, c) && this.l[c] == a)return j
        }
        return m
    };
    r.Ub = function (a, b) {
        if (this === a)return j;
        if (this.j != a.P())return m;
        var c = b || tc;
        rc(this);
        for (var d, e = 0; d = this.F[e]; e++)if (!c(this.get(d), a.get(d)))return m;
        return j
    };
    var tc = function (a, b) {
        return a === b
    };
    J.prototype.Va = function () {
        return 0 == this.j
    };
    J.prototype.clear = function () {
        this.l = {};
        this.jb = this.j = this.F.length = 0
    };
    J.prototype.remove = function (a) {
        return sc(this.l, a) ? (delete this.l[a], this.j--, this.jb++, this.F.length > 2 * this.j && rc(this), j) : m
    };
    var rc = function (a) {
        if (a.j != a.F.length) {
            for (var b = 0, c = 0; b < a.F.length;) {
                var d = a.F[b];
                sc(a.l, d) && (a.F[c++] = d);
                b++
            }
            a.F.length = c
        }
        if (a.j != a.F.length) {
            for (var e = {}, c = b = 0; b < a.F.length;)d = a.F[b], sc(e, d) || (a.F[c++] = d, e[d] = 1), b++;
            a.F.length = c
        }
    };
    r = J.prototype;
    r.get = function (a, b) {
        return sc(this.l, a) ? this.l[a] : b
    };
    r.set = function (a, b) {
        sc(this.l, a) || (this.j++, this.F.push(a), this.jb++);
        this.l[a] = b
    };
    r.hg = function (a) {
        var b;
        a instanceof J ? (b = a.Ma(), a = a.da()) : (b = ob(a), a = nb(a));
        for (var c = 0; c < b.length; c++)this.set(b[c], a[c])
    };
    r.K = function () {
        return new J(this)
    };
    r.Bd = function (a) {
        rc(this);
        var b = 0, c = this.F, d = this.l, e = this.jb, g = this, h = new jc;
        h.next = function () {
            for (; ;) {
                e != g.jb && f(Error("The map has changed since the iterator was created"));
                b >= c.length && f(ic);
                var h = c[b++];
                return a ? h : d[h]
            }
        };
        return h
    };
    var sc = function (a, b) {
        return Object.prototype.hasOwnProperty.call(a, b)
    };
    var uc = function (a) {
        this.l = new J;
        a && this.hg(a)
    }, vc = function (a) {
        var b = typeof a;
        return"object" == b && a || "function" == b ? "o" + la(a) : b.substr(0, 1) + a
    };
    r = uc.prototype;
    r.P = function () {
        return this.l.P()
    };
    r.add = function (a) {
        this.l.set(vc(a), a)
    };
    r.hg = function (a) {
        for (var a = nc(a), b = a.length, c = 0; c < b; c++)this.add(a[c])
    };
    r.Wf = function (a) {
        for (var a = nc(a), b = a.length, c = 0; c < b; c++)this.remove(a[c])
    };
    r.remove = function (a) {
        return this.l.remove(vc(a))
    };
    r.clear = function () {
        this.l.clear()
    };
    r.Va = function () {
        return this.l.Va()
    };
    r.contains = function (a) {
        return this.l.W(vc(a))
    };
    r.da = function () {
        return this.l.da()
    };
    r.K = function () {
        return new uc(this)
    };
    r.Ub = function (a) {
        return this.P() == mc(a) && wc(this, a)
    };
    var wc = function (a, b) {
        var c = mc(b);
        if (a.P() > c)return m;
        !(b instanceof uc) && 5 < c && (b = new uc(b));
        return qc(a, function (a) {
            if ("function" == typeof b.contains)a = b.contains(a); else if ("function" == typeof b.pg)a = b.pg(a); else if (ga(b) || x(b))a = 0 <= Fa(b, a); else a:{
                for (var c in b)if (b[c] == a) {
                    a = j;
                    break a
                }
                a = m
            }
            return a
        })
    };
    uc.prototype.Bd = function () {
        return this.l.Bd(m)
    };
    var yc = function (a) {
        return xc(a || arguments.callee.caller, [])
    }, xc = function (a, b) {
        var c = [];
        if (0 <= Fa(b, a))c.push("[...circular reference...]"); else if (a && 50 > b.length) {
            c.push(zc(a) + "(");
            for (var d = a.arguments, e = 0; e < d.length; e++) {
                0 < e && c.push(", ");
                var g;
                g = d[e];
                switch (typeof g) {
                    case "object":
                        g = g ? "object" : "null";
                        break;
                    case "string":
                        break;
                    case "number":
                        g = "" + g;
                        break;
                    case "boolean":
                        g = g ? "true" : "false";
                        break;
                    case "function":
                        g = (g = zc(g)) ? g : "[fn]";
                        break;
                    default:
                        g = typeof g
                }
                40 < g.length && (g = g.substr(0, 40) + "...");
                c.push(g)
            }
            b.push(a);
            c.push(")\n");
            try {
                c.push(xc(a.caller, b))
            } catch (h) {
                c.push("[exception trying to get caller]\n")
            }
        } else a ? c.push("[...long stack...]") : c.push("[end]");
        return c.join("")
    }, zc = function (a) {
        if (Ac[a])return Ac[a];
        a = "" + a;
        if (!Ac[a]) {
            var b = /function ([^\(]+)/.exec(a);
            Ac[a] = b ? b[1] : "[Anonymous]"
        }
        return Ac[a]
    }, Ac = {};
    var Bc = function (a, b, c, d, e) {
        this.reset(a, b, c, d, e)
    };
    Bc.prototype.qd = 0;
    Bc.prototype.cj = k;
    Bc.prototype.bj = k;
    var Cc = 0;
    Bc.prototype.reset = function (a, b, c, d, e) {
        this.qd = "number" == typeof e ? e : Cc++;
        d || z();
        this.Ob = a;
        this.we = b;
        delete this.cj;
        delete this.bj
    };
    Bc.prototype.Kh = function () {
        return this.Ob
    };
    Bc.prototype.pj = function (a) {
        this.Ob = a
    };
    Bc.prototype.sj = function () {
        return this.we
    };
    var Dc = function (a) {
        this.Ba = a
    };
    Dc.prototype.Be = k;
    Dc.prototype.Ob = k;
    Dc.prototype.qg = k;
    Dc.prototype.tj = k;
    var Ec = function (a, b) {
        this.name = a;
        this.value = b
    };
    Ec.prototype.toString = function () {
        return this.name
    };
    var Fc = new Ec("SEVERE", 1E3), Gc = new Ec("WARNING", 900), Hc = new Ec("INFO", 800), Ic = new Ec("CONFIG", 700), Jc = new Ec("FINE", 500), Kc = new Ec("FINEST", 300), Lc = function (a) {
        s.console && (s.console.timeStamp ? s.console.timeStamp(a) : s.console.markTimeline && s.console.markTimeline(a));
        s.msWriteProfilerMark && s.msWriteProfilerMark(a)
    };
    Dc.prototype.getName = function () {
        return this.Ba
    };
    Dc.prototype.getParent = function () {
        return this.Be
    };
    Dc.prototype.pj = function (a) {
        this.Ob = a
    };
    Dc.prototype.Kh = function () {
        return this.Ob
    };
    var Mc = function (a) {
        if (a.Ob)return a.Ob;
        if (a.Be)return Mc(a.Be);
        Ea("Root logger has no level set.");
        return k
    };
    r = Dc.prototype;
    r.log = function (a, b, c) {
        a.value >= Mc(this).value && Nc(this, this.Lh(a, b, c))
    };
    r.Lh = function (a, b, c) {
        var d = new Bc(a, "" + b, this.Ba);
        if (c) {
            d.cj = c;
            var e;
            var g = arguments.callee.caller;
            try {
                var h;
                var l = ca("window.location.href");
                if (x(c))h = {message:c, name:"Unknown error", lineNumber:"Not available", fileName:l, stack:"Not available"}; else {
                    var p, n, q = m;
                    try {
                        p = c.lineNumber || c.Hm || "Not available"
                    } catch (v) {
                        p = "Not available", q = j
                    }
                    try {
                        n = c.fileName || c.filename || c.sourceURL || l
                    } catch (aa) {
                        n = "Not available", q = j
                    }
                    h = q || !c.lineNumber || !c.fileName || !c.stack ? {message:c.message, name:c.name, lineNumber:p, fileName:n,
                        stack:c.stack || "Not available"} : c
                }
                e = "Message: " + za(h.message) + '\nUrl: <a href="view-source:' + h.fileName + '" target="_new">' + h.fileName + "</a>\nLine: " + h.lineNumber + "\n\nBrowser stack:\n" + za(h.stack + "-> ") + "[end]\n\nJS stack traversal:\n" + za(yc(g) + "-> ")
            } catch (D) {
                e = "Exception trying to expose exception! You win, we lose. " + D
            }
            d.bj = e
        }
        return d
    };
    r.w = function (a, b) {
        this.log(Fc, a, b)
    };
    r.q = function (a, b) {
        this.log(Gc, a, b)
    };
    r.info = function (a, b) {
        this.log(Hc, a, b)
    };
    var K = function (a, b) {
        a.log(Jc, b, i)
    }, log = function (a, b) {
        a.log(Kc, b, i)
    }, Nc = function (a, b) {
        Lc("log:" + b.sj());
        for (var c = a; c;) {
            var d = c, e = b;
            if (d.tj)for (var g = 0, h = i; h = d.tj[g]; g++)h(e);
            c = c.getParent()
        }
    }, Oc = {}, Pc = k, M = function (a) {
        Pc || (Pc = new Dc(""), Oc[""] = Pc, Pc.pj(Ic));
        var b;
        if (!(b = Oc[a])) {
            b = new Dc(a);
            var c = a.lastIndexOf("."), d = a.substr(c + 1), c = M(a.substr(0, c));
            c.qg || (c.qg = {});
            c.qg[d] = b;
            b.Be = c;
            Oc[a] = b
        }
        return b
    };
    var Qc = function () {
        this.la = {}
    };
    A(Qc, Eb);
    r = Qc.prototype;
    r.ie = M("goog.messaging.AbstractChannel");
    r.T = function (a) {
        a && a()
    };
    r.ua = function () {
        return j
    };
    r.$ = function (a, b, c) {
        this.la[a] = {Xf:b, Di:!!c}
    };
    r.g = function () {
        Qc.z.g.call(this);
        G(this.ie);
        delete this.ie;
        delete this.la;
        delete this.ci
    };
    var Rc = RegExp("^(?:([^:/?#.]+):)?(?://(?:([^/?#]*)@)?([\\w\\d\\-\\u0100-\\uffff.%]*)(?::([0-9]+))?)?([^?#]+)?(?:\\?([^#]*))?(?:#(.*))?$"), Tc = function (a) {
        if (Sc) {
            Sc = m;
            var b = s.location;
            if (b) {
                var c = b.href;
                if (c && (c = (c = Tc(c)[3] || k) && decodeURIComponent(c)) && c != b.hostname)Sc = j, f(Error())
            }
        }
        return a.match(Rc)
    }, Sc = E, Uc = function (a) {
        var b = Tc(a), a = b[1], c = b[2], d = b[3], b = b[4], e = [];
        a && e.push(a, ":");
        d && (e.push("//"), c && e.push(c, "@"), e.push(d), b && e.push(":", b));
        return e.join("")
    };
    var N = function (a, b) {
        var c;
        a instanceof N ? (this.sc(b == k ? a.Na : b), Vc(this, a.aa), Wc(this, a.Ab), Xc(this, a.Q()), Yc(this, a.Ca), Zc(this, a.H), $c(this, a.U.K()), ad(this, a.zb)) : a && (c = Tc("" + a)) ? (this.sc(!!b), Vc(this, c[1] || "", j), Wc(this, c[2] || "", j), Xc(this, c[3] || "", j), Yc(this, c[4]), Zc(this, c[5] || "", j), this.Ac(c[6] || "", j), ad(this, c[7] || "", j)) : (this.sc(!!b), this.U = new bd(k, this, this.Na))
    };
    r = N.prototype;
    r.aa = "";
    r.Ab = "";
    r.Y = "";
    r.Ca = k;
    r.H = "";
    r.zb = "";
    r.nl = m;
    r.Na = m;
    r.toString = function () {
        if (this.wa)return this.wa;
        var a = [];
        this.aa && a.push(cd(this.aa, dd), ":");
        this.Y && (a.push("//"), this.Ab && a.push(cd(this.Ab, dd), "@"), a.push(x(this.Y) ? encodeURIComponent(this.Y) : k), this.Ca != k && a.push(":", "" + this.Ca));
        this.H && (this.Y && "/" != this.H.charAt(0) && a.push("/"), a.push(cd(this.H, "/" == this.H.charAt(0) ? ed : fd)));
        var b = "" + this.U;
        b && a.push("?", b);
        this.zb && a.push("#", cd(this.zb, gd));
        return this.wa = a.join("")
    };
    r.K = function () {
        return hd(this.aa, this.Ab, this.Y, this.Ca, this.H, this.U.K(), this.zb, this.Na)
    };
    var Vc = function (a, b, c) {
        id(a);
        delete a.wa;
        a.aa = c ? b ? decodeURIComponent(b) : "" : b;
        a.aa && (a.aa = a.aa.replace(/:$/, ""))
    }, Wc = function (a, b, c) {
        id(a);
        delete a.wa;
        a.Ab = c ? b ? decodeURIComponent(b) : "" : b
    };
    N.prototype.Q = function () {
        return this.Y
    };
    var Xc = function (a, b, c) {
        id(a);
        delete a.wa;
        a.Y = c ? b ? decodeURIComponent(b) : "" : b
    }, Yc = function (a, b) {
        id(a);
        delete a.wa;
        b ? (b = Number(b), (isNaN(b) || 0 > b) && f(Error("Bad port number " + b)), a.Ca = b) : a.Ca = k
    }, Zc = function (a, b, c) {
        id(a);
        delete a.wa;
        a.H = c ? b ? decodeURIComponent(b) : "" : b
    }, $c = function (a, b, c) {
        id(a);
        delete a.wa;
        b instanceof bd ? (a.U = b, a.U.eg = a, a.U.sc(a.Na)) : (c || (b = cd(b, jd)), a.U = new bd(b, a, a.Na));
        return a
    };
    N.prototype.Ac = function (a, b) {
        return $c(this, a, b)
    };
    var kd = function (a) {
        a = a.U;
        a.xc || (a.xc = a.toString() ? decodeURIComponent(a.toString()) : "");
        return a.xc
    };
    N.prototype.$e = function () {
        return this.U.toString()
    };
    var O = function (a, b, c) {
        id(a);
        delete a.wa;
        a.U.set(b, c)
    }, od = function (a, b, c) {
        id(a);
        delete a.wa;
        w(c) || (c = ["" + c]);
        a = a.U;
        ld(a);
        md(a);
        b = nd(a, b);
        if (a.W(b)) {
            var d = a.B.get(b);
            w(d) ? a.j -= d.length : a.j--
        }
        0 < c.length && (a.B.set(b, c), a.j += c.length)
    }, ad = function (a, b, c) {
        id(a);
        delete a.wa;
        a.zb = c ? b ? decodeURIComponent(b) : "" : b
    }, pd = function (a) {
        id(a);
        O(a, "zx", Ba());
        return a
    }, id = function (a) {
        a.nl && f(Error("Tried to modify a read-only Uri"))
    };
    N.prototype.sc = function (a) {
        this.Na = a;
        this.U && this.U.sc(a);
        return this
    };
    var qd = function (a) {
        return a instanceof N ? a.K() : new N(a, i)
    }, hd = function (a, b, c, d, e, g, h, l) {
        l = new N(k, l);
        a && Vc(l, a);
        b && Wc(l, b);
        c && Xc(l, c);
        d && Yc(l, d);
        e && Zc(l, e);
        g && $c(l, g);
        h && ad(l, h);
        return l
    }, rd = /^[a-zA-Z0-9\-_.!~*'():\/;?]*$/, cd = function (a, b) {
        var c = k;
        x(a) && (c = a, rd.test(c) || (c = encodeURI(a)), 0 <= c.search(b) && (c = c.replace(b, sd)));
        return c
    }, sd = function (a) {
        a = a.charCodeAt(0);
        return"%" + (a >> 4 & 15).toString(16) + (a & 15).toString(16)
    }, dd = /[#\/\?@]/g, fd = /[\#\?:]/g, ed = /[\#\?]/g, jd = /[\#\?@]/g, gd = /#/g, bd = function (a, b, c) {
        this.ab = a || k;
        this.eg = b || k;
        this.Na = !!c
    }, ld = function (a) {
        if (!a.B && (a.B = new J, a.j = 0, a.ab))for (var b = a.ab.split("&"), c = 0; c < b.length; c++) {
            var d = b[c].indexOf("="), e = k, g = k;
            0 <= d ? (e = b[c].substring(0, d), g = b[c].substring(d + 1)) : e = b[c];
            e = decodeURIComponent(e.replace(/\+/g, " "));
            e = nd(a, e);
            a.add(e, g ? decodeURIComponent(g.replace(/\+/g, " ")) : "")
        }
    };
    r = bd.prototype;
    r.B = k;
    r.j = k;
    r.P = function () {
        ld(this);
        return this.j
    };
    r.add = function (a, b) {
        ld(this);
        md(this);
        a = nd(this, a);
        if (this.W(a)) {
            var c = this.B.get(a);
            w(c) ? c.push(b) : this.B.set(a, [c, b])
        } else this.B.set(a, b);
        this.j++;
        return this
    };
    r.remove = function (a) {
        ld(this);
        a = nd(this, a);
        if (this.B.W(a)) {
            md(this);
            var b = this.B.get(a);
            w(b) ? this.j -= b.length : this.j--;
            return this.B.remove(a)
        }
        return m
    };
    r.clear = function () {
        md(this);
        this.B && this.B.clear();
        this.j = 0
    };
    r.Va = function () {
        ld(this);
        return 0 == this.j
    };
    r.W = function (a) {
        ld(this);
        a = nd(this, a);
        return this.B.W(a)
    };
    r.pg = function (a) {
        var b = this.da();
        return 0 <= Fa(b, a)
    };
    r.Ma = function () {
        ld(this);
        for (var a = this.B.da(), b = this.B.Ma(), c = [], d = 0; d < b.length; d++) {
            var e = a[d];
            if (w(e))for (var g = 0; g < e.length; g++)c.push(b[d]); else c.push(b[d])
        }
        return c
    };
    r.da = function (a) {
        ld(this);
        var b = [];
        if (a)a = nd(this, a), this.W(a) && (b = La(b, this.B.get(a))); else for (var a = this.B.da(), c = 0; c < a.length; c++)b = La(b, a[c]);
        return b
    };
    r.set = function (a, b) {
        ld(this);
        md(this);
        a = nd(this, a);
        if (this.W(a)) {
            var c = this.B.get(a);
            w(c) ? this.j -= c.length : this.j--
        }
        this.B.set(a, b);
        this.j++;
        return this
    };
    r.get = function (a, b) {
        ld(this);
        a = nd(this, a);
        if (this.W(a)) {
            var c = this.B.get(a);
            return w(c) ? c[0] : c
        }
        return b
    };
    r.toString = function () {
        if (this.ab)return this.ab;
        if (!this.B)return"";
        for (var a = [], b = 0, c = this.B.Ma(), d = 0; d < c.length; d++) {
            var e = c[d], g = ta(e), e = this.B.get(e);
            if (w(e))for (var h = 0; h < e.length; h++)0 < b && a.push("&"), a.push(g), "" !== e[h] && a.push("=", ta(e[h])), b++; else 0 < b && a.push("&"), a.push(g), "" !== e && a.push("=", ta(e)), b++
        }
        return this.ab = a.join("")
    };
    var md = function (a) {
        delete a.xc;
        delete a.ab;
        a.eg && delete a.eg.wa
    };
    bd.prototype.K = function () {
        var a = new bd;
        this.xc && (a.xc = this.xc);
        this.ab && (a.ab = this.ab);
        this.B && (a.B = this.B.K());
        return a
    };
    var nd = function (a, b) {
        var c = "" + b;
        a.Na && (c = c.toLowerCase());
        return c
    };
    bd.prototype.sc = function (a) {
        a && !this.Na && (ld(this), md(this), pc(this.B, function (a, c) {
            var d = c.toLowerCase();
            c != d && (this.remove(c), this.add(d, a))
        }, this));
        this.Na = a
    };
    var transports = {1:"NativeMessagingTransport", 2:"FrameElementMethodTransport", 3:"IframeRelayTransport", 4:"IframePollingTransport", 5:"FlashTransport", 6:"NixTransport"},
        ud = ["pu", "lru", "pru", "lpu", "ppu"], vd = {},
        xd = function (a) {
        for (var b = wd, c = b.length, d = ""; 0 < a--;)d += b.charAt(Math.floor(Math.random() * c));
        return d
    }, wd = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", P = M("goog.net.xpc");
    var yd = function (a) {
        this.ia = a || xb()
    };
    A(yd, Eb);
    yd.prototype.Pb = 0;
    yd.prototype.Ae = function () {
        return this.Pb
    };
    yd.prototype.A = function () {
        return this.ia.A()
    };
    yd.prototype.getName = function () {
        return transports[this.Pb] || ""
    };
    var zd = function (a, b) {
        this.ia = b || xb();
        this.b = a;
        this.nd = [];
        this.ok = y(this.Ik, this)
    };
    A(zd, yd);
    r = zd.prototype;
    r.Pb = 2;
    r.Tf = m;
    r.Ga = 0;
    r.T = function () {
        0 == Ad(this.b) ? (this.$a = this.b.jc, this.$a.XPC_toOuter = y(this.ni, this)) : this.mi()
    };
    r.mi = function () {
        var a = j;
        try {
            if (this.$a || (this.$a = this.A().frameElement), this.$a && this.$a.XPC_toOuter)this.Ff = this.$a.XPC_toOuter, this.$a.XPC_toOuter.XPC_toInner = y(this.ni, this), a = m, this.send("tp", "SETUP_ACK"), Bd(this.b)
        } catch (b) {
            P.w("exception caught while attempting setup: " + b)
        }
        a && (this.vi || (this.vi = y(this.mi, this)), this.A().setTimeout(this.vi, 100))
    };
    r.$f = function (a) {
        0 == Ad(this.b) && !this.b.ua() && "SETUP_ACK" == a ? (this.Ff = this.$a.XPC_toOuter.XPC_toInner, Bd(this.b)) : f(Error("Got unexpected transport message."))
    };
    r.ni = function (a, b) {
        !this.Tf && 0 == this.nd.length ? this.b.Za(a, b) : (this.nd.push({vk:a, Pf:b}), 1 == this.nd.length && (this.Ga = this.A().setTimeout(this.ok, 1)))
    };
    r.Ik = function () {
        for (; this.nd.length;) {
            var a = this.nd.shift();
            this.b.Za(a.vk, a.Pf)
        }
    };
    r.send = function (a, b) {
        this.Tf = j;
        this.Ff(a, b);
        this.Tf = m
    };
    r.g = function () {
        zd.z.g.call(this);
        this.$a = this.Ff = k
    };
    var Q = function (a, b) {
        this.ia = b || xb();
        this.b = a;
        this.gd = this.b.v.ppu;
        this.xk = this.b.v.lpu;
        this.ne = []
    }, Cd, Dd;
    A(Q, yd);
    Q.prototype.Pb = 4;
    Q.prototype.pe = 0;
    Q.prototype.pc = m;
    Q.prototype.qa = m;
    var Ed = function (a) {
        return"googlexpc_" + a.b.name + "_msg"
    }, Fd = function (a) {
        return"googlexpc_" + a.b.name + "_ack"
    };
    Q.prototype.T = function () {
        if (!this.vb) {
            K(P, "transport connect called");
            if (!this.qa) {
                K(P, "initializing...");
                var a = Ed(this);
                this.nc = Gd(this, a);
                this.yf = this.A().frames[a];
                a = Fd(this);
                this.mc = Gd(this, a);
                this.xf = this.A().frames[a];
                this.qa = j
            }
            if (!Hd(this, Ed(this)) || !Hd(this, Fd(this))) {
                log(P, "foreign frames not (yet) present");
                if (1 == Ad(this.b) && !this.nk)log(P, "innerPeerReconnect called"), this.b.name = xd(10), log(P, "switching channels: " + this.b.name), Id(this), this.qa = m, this.nk = Gd(this, "googlexpc_reconnect_" + this.b.name);
                else if (0 == Ad(this.b)) {
                    log(P, "outerPeerReconnect called");
                    for (var a = this.b.va.frames, b = a.length, c = 0; c < b; c++) {
                        var d;
                        try {
                            a[c] && a[c].name && (d = a[c].name)
                        } catch (e) {
                        }
                        if (d) {
                            var g = d.split("_");
                            if (3 == g.length && "googlexpc" == g[0] && "reconnect" == g[1]) {
                                this.b.name = g[2];
                                Id(this);
                                this.qa = m;
                                break
                            }
                        }
                    }
                }
                this.A().setTimeout(y(this.T, this), 100)
            } else K(P, "foreign frames present"), this.Sh = new Jd(this, this.b.va.frames[Ed(this)], y(this.mk, this)), this.Rh = new Jd(this, this.b.va.frames[Fd(this)], y(this.lk, this)), this.$h()
        }
    };
    var Gd = function (a, b) {
        log(P, "constructing sender frame: " + b);
        var iframe = document.createElement("iframe"), d = iframe.style;
        d.position = "absolute";
        d.top = "-10px";
        d.left = "10px";
        d.width = "1px";
        d.height = "1px";
        iframe.id = iframe.name = b;
        iframe.src = a.gd + "#INITIAL";
        a.A().document.body.appendChild(iframe);
        return iframe
    }, Id = function (a) {
        log(P, "deconstructSenderFrames called");
        a.nc && (a.nc.parentNode.removeChild(a.nc), a.nc = k, a.yf = k);
        a.mc && (a.mc.parentNode.removeChild(a.mc), a.mc = k, a.xf = k)
    }, Hd = function (a, b) {
        log(P, "checking for receive frame: " + b);
        try {
            var c = a.b.va.frames[b];
            if (!c || 0 != c.location.href.indexOf(a.xk))return m
        } catch (d) {
            return m
        }
        return j
    };
    Q.prototype.$h = function () {
        var a = this.b.va.frames;
        !a[Fd(this)] || !a[Ed(this)] ? (this.wi || (this.wi = y(this.$h, this)), this.A().setTimeout(this.wi, 100), K(P, "local frames not (yet) present")) : (this.li = new Kd(this.gd, this.yf), this.le = new Kd(this.gd, this.xf), K(P, "local frames ready"), this.A().setTimeout(y(function () {
            this.li.send("SETUP");
            this.pc = j;
            K(P, "SETUP sent")
        }, this), 100))
    };
    var Ld = function (a) {
        if (a.Rf && a.pi) {
            if (Bd(a.b), a.tc) {
                K(P, "delivering queued messages (" + a.tc.length + ")");
                for (var b = 0, c; b < a.tc.length; b++)c = a.tc[b], a.b.Za(c.Ck, c.Pf);
                delete a.tc
            }
        } else log(P, "checking if connected: ack sent:" + a.Rf + ", ack rcvd: " + a.pi)
    };
    Q.prototype.mk = function (a) {
        log(P, "msg received: " + a);
        if ("SETUP" == a)this.le && (this.le.send("SETUP_ACK"), log(P, "SETUP_ACK sent"), this.Rf = j, Ld(this)); else if (this.b.ua() || this.Rf) {
            var b = a.indexOf("|"), c = a.substring(0, b), a = a.substring(b + 1), b = c.indexOf(",");
            if (-1 == b) {
                var d;
                this.le.send("ACK:" + c);
                Md(this, a)
            } else if (d = c.substring(0, b), this.le.send("ACK:" + d), c = c.substring(b + 1).split("/"), b = parseInt(c[0], 10), c = parseInt(c[1], 10), 1 == b && (this.Zf = []), this.Zf.push(a), b == c)Md(this, this.Zf.join("")), delete this.Zf
        } else P.q("received msg, but channel is not connected")
    };
    Q.prototype.lk = function (a) {
        log(P, "ack received: " + a);
        "SETUP_ACK" == a ? (this.pc = m, this.pi = j, Ld(this)) : this.b.ua() ? this.pc ? parseInt(a.split(":")[1], 10) == this.pe ? (this.pc = m, Nd(this)) : P.q("got ack with wrong sequence") : P.q("got unexpected ack") : P.q("received ack, but channel not connected")
    };
    var Nd = function (a) {
        if (!a.pc && a.ne.length) {
            var b = a.ne.shift();
            ++a.pe;
            a.li.send(a.pe + b);
            log(P, "msg sent: " + a.pe + b);
            a.pc = j
        }
    }, Md = function (a, b) {
        var c = b.indexOf(":"), d = b.substr(0, c), c = b.substring(c + 1);
        a.b.ua() ? a.b.Za(d, c) : ((a.tc || (a.tc = [])).push({Ck:d, Pf:c}), log(P, "queued delivery"))
    };
    Q.prototype.De = 3800;
    Q.prototype.send = function (a, b) {
        var c = a + ":" + b;
        if (!C || b.length <= this.De)this.ne.push("|" + c); else for (var d = b.length, e = Math.ceil(d / this.De), g = 0, h = 1; g < d;)this.ne.push("," + h + "/" + e + "|" + c.substr(g, this.De)), h++, g += this.De;
        Nd(this)
    };
    Q.prototype.g = function () {
        Q.z.g.call(this);
        var a = Od;
        Ka(a, this.Sh);
        Ka(a, this.Rh);
        this.Sh = this.Rh = k;
        Db(this.nc);
        Db(this.mc);
        this.yf = this.xf = this.nc = this.mc = k
    };
    var Od = [], Pd = y(function () {
        var a = m;
        try {
            for (var b = 0, c = Od.length; b < c; b++) {
                var d;
                if (!(d = a)) {
                    var e = Od[b], g = e.Ii.location.href;
                    if (g != e.Hi) {
                        e.Hi = g;
                        var h = g.split("#")[1];
                        h && (h = h.substr(1), e.Fk(decodeURIComponent(h)));
                        d = j
                    } else d = m
                }
                a = d
            }
        } catch (l) {
            if (P.info("receive_() failed: " + l), b = Od[b].ba.b, P.info("Transport Error"), b.close(), !Od.length)return
        }
        b = z();
        a && (Cd = b);
        Dd = window.setTimeout(Pd, 1E3 > b - Cd ? 10 : 100)
    }, Q), Qd = function () {
        K(P, "starting receive-timer");
        Cd = z();
        Dd && window.clearTimeout(Dd);
        Dd = window.setTimeout(Pd,
            10)
    }, Kd = function (a, b) {
        this.gd = a;
        this.aj = b;
        this.mg = 0
    };
    Kd.prototype.send = function (a) {
        this.mg = ++this.mg % 2;
        a = this.gd + "#" + this.mg + encodeURIComponent(a);
        try {
            E ? this.aj.location.href = a : this.aj.location.replace(a)
        } catch (b) {
            P.w("sending failed", b)
        }
        Qd()
    };
    var Jd = function (a, b, c) {
        this.ba = a;
        this.Ii = b;
        this.Fk = c;
        this.Hi = this.Ii.location.href.split("#")[0] + "#INITIAL";
        Od.push(this);
        Qd()
    };
    var Sd = function (a, b) {
        this.ia = b || xb();
        this.b = a;
        this.tk = this.b.v.pru;
        this.ui = this.b.v.ifrid;
        E && Rd()
    };
    A(Sd, yd);
    if (E)var Td = [], Ud = 0, Rd = function () {
        Ud || (Ud = window.setTimeout(function () {
            Vd()
        }, 1E3))
    }, Vd = function (a) {
        for (var b = z(), a = a || 3E3; Td.length && b - Td[0].timestamp >= a;) {
            var c = Td.shift().Ek;
            Db(c);
            log(P, "iframe removed")
        }
        Ud = window.setTimeout(Wd, 1E3)
    }, Wd = function () {
        Vd()
    };
    var Xd = {};
    Sd.prototype.Pb = 3;
    Sd.prototype.T = function () {
        this.A().xpcRelay || (this.A().xpcRelay = Yd);
        this.send("tp", "SETUP")
    };
    var Yd = function (a, b) {
        var c = b.indexOf(":"), d = b.substr(0, c), e = b.substr(c + 1);
        if (!C || -1 == (c = d.indexOf("|")))var g = d; else {
            var g = d.substr(0, c), d = d.substr(c + 1), c = d.indexOf("+"), h = d.substr(0, c), c = parseInt(d.substr(c + 1), 10), l = Xd[h];
            l || (l = Xd[h] = {mj:[], nj:0, lj:0});
            -1 != d.indexOf("++") && (l.lj = c + 1);
            l.mj[c] = e;
            l.nj++;
            if (l.nj != l.lj)return;
            e = l.mj.join("");
            delete Xd[h]
        }
        vd[a].Za(g, decodeURIComponent(e))
    };
    Sd.prototype.$f = function (a) {
        "SETUP" == a ? (this.send("tp", "SETUP_ACK"), Bd(this.b)) : "SETUP_ACK" == a && Bd(this.b)
    };
    Sd.prototype.send = function (a, b) {
        var c = encodeURIComponent(b), d = c.length;
        if (C && 1800 < d)for (var e = Ba(), g = 0, h = 0; g < d; h++) {
            var l = c.substr(g, 1800), g = g + 1800;
            Zd(this, a, l, e + (g >= d ? "++" : "+") + h)
        } else Zd(this, a, c)
    };
    var Zd = function (a, b, c, d) {
        if (C) {
            var e = a.A().document.createElement("div");
            e.innerHTML = '<iframe onload="this.xpcOnload()"></iframe>';
            e = e.childNodes[0];
            e.xpcOnload = $d
        } else e = a.A().document.createElement("iframe"), E ? Td.push({timestamp:z(), Ek:e}) : Qb(e, "load", $d);
        var g = e.style;
        g.visibility = "hidden";
        g.width = e.style.height = "0px";
        g.position = "absolute";
        g = a.tk;
        g += "#" + a.b.name;
        a.ui && (g += "," + a.ui);
        g += "|" + b;
        d && (g += "|" + d);
        g += ":" + c;
        e.src = g;
        a.A().document.body.appendChild(e);
        log(P, "msg sent: " + g)
    }, $d = function () {
        log(P,
            "iframe-load");
        Db(this)
    };
    Sd.prototype.g = function () {
        Sd.z.g.call(this);
        E && Vd(0)
    };
    var R = function (a) {
        this.n = a;
        this.F = []
    };
    A(R, Eb);
    var ae = [], S = function (a, b, c, d, e, g) {
        w(c) || (ae[0] = c, c = ae);
        for (var h = 0; h < c.length; h++)a.F.push(Qb(b, c[h], d || a, e || m, g || a.n || a))
    }, be = function (a, b, c, d, e, g) {
        if (w(c))for (var h = 0; h < c.length; h++)be(a, b, c[h], d, e, g); else a.F.push(Tb(b, c, d || a, e, g || a.n || a))
    };
    R.prototype.Wf = function () {
        Ga(this.F, Vb);
        this.F.length = 0
    };
    R.prototype.g = function () {
        R.z.g.call(this);
        this.Wf()
    };
    R.prototype.handleEvent = function () {
        f(Error("EventHandler.handleEvent not implemented"))
    };
    var T = function () {
    };
    A(T, Eb);
    r = T.prototype;
    r.Pi = j;
    r.Yf = k;
    r.addEventListener = function (a, b, c, d) {
        Qb(this, a, b, c, d)
    };
    r.removeEventListener = function (a, b, c, d) {
        Ub(this, a, b, c, d)
    };
    r.dispatchEvent = function (a) {
        var b = a.type || a, c = I;
        if (b in c) {
            if (x(a))a = new H(a, this); else if (a instanceof H)a.target = a.target || this; else {
                var d = a, a = new H(b, this);
                sb(a, d)
            }
            var d = 1, e, c = c[b], b = j in c, g;
            if (b) {
                e = [];
                for (g = this; g; g = g.Yf)e.push(g);
                g = c[j];
                g.xa = g.j;
                for (var h = e.length - 1; !a.wc && 0 <= h && g.xa; h--)a.currentTarget = e[h], d &= $b(g, e[h], a.type, j, a) && a.ue != m
            }
            if (m in c)if (g = c[m], g.xa = g.j, b)for (h = 0; !a.wc && h < e.length && g.xa; h++)a.currentTarget = e[h], d &= $b(g, e[h], a.type, m, a) && a.ue != m; else for (e = this; !a.wc && e && g.xa; e =
                e.Yf)a.currentTarget = e, d &= $b(g, e, a.type, m, a) && a.ue != m;
            a = Boolean(d)
        } else a = j;
        return a
    };
    r.g = function () {
        T.z.g.call(this);
        Xb(this);
        this.Yf = k
    };
    var de = function (a, b) {
        this.zd = a || 1;
        this.td = b || ce;
        this.kg = y(this.dl, this);
        this.lg = z()
    };
    A(de, T);
    de.prototype.enabled = m;
    var ce = s.window;
    r = de.prototype;
    r.Ga = k;
    r.setInterval = function (a) {
        this.zd = a;
        this.Ga && this.enabled ? (this.stop(), this.start()) : this.Ga && this.stop()
    };
    r.dl = function () {
        if (this.enabled) {
            var a = z() - this.lg;
            0 < a && a < 0.8 * this.zd ? this.Ga = this.td.setTimeout(this.kg, this.zd - a) : (this.fg(), this.enabled && (this.Ga = this.td.setTimeout(this.kg, this.zd), this.lg = z()))
        }
    };
    r.fg = function () {
        this.dispatchEvent("tick")
    };
    r.start = function () {
        this.enabled = j;
        this.Ga || (this.Ga = this.td.setTimeout(this.kg, this.zd), this.lg = z())
    };
    r.stop = function () {
        this.enabled = m;
        this.Ga && (this.td.clearTimeout(this.Ga), this.Ga = k)
    };
    r.g = function () {
        de.z.g.call(this);
        this.stop();
        delete this.td
    };
    var ee = function (a, b, c) {
        ha(a) ? c && (a = y(a, c)) : a && "function" == typeof a.handleEvent ? a = y(a.handleEvent, a) : f(Error("Invalid listener argument"));
        return 2147483647 < b ? -1 : ce.setTimeout(a, b || 0)
    };
    var fe = function (a, b, c) {
        this.ia = c || xb();
        this.b = a;
        this.ki = b || "*";
        this.r = new R(this);
        this.ed = new de(100, this.A());
        S(this.r, this.ed, "tick", this.ji)
    };
    A(fe, yd);
    fe.prototype.qa = m;
    fe.prototype.Pb = 1;
    var ge = {}, he = function (a) {
        var b = a.re.data;
        if (!x(b))return m;
        var c = b.indexOf("|"), d = b.indexOf(":");
        if (-1 == c || -1 == d)return m;
        var e = b.substring(0, c), c = b.substring(c + 1, d), b = b.substring(d + 1);
        K(P, "messageReceived: channel=" + e + ", service=" + c + ", payload=" + b);
        if (d = vd[e])return d.Za(c, b, a.re.origin), j;
        for (var g in vd)if (a = vd[g], 1 == Ad(a) && !a.ua() && "tp" == c && "SETUP" == b)return K(P, "changing channel name to " + e), a.name = e, delete vd[g], vd[e] = a, a.Za(c, b), j;
        P.info('channel name mismatch; message ignored"');
        return m
    };
    r = fe.prototype;
    r.$f = function (a) {
        switch (a) {
            case "SETUP":
                this.send("tp", "SETUP_ACK");
                break;
            case "SETUP_ACK":
                Bd(this.b)
        }
    };
    r.T = function () {
        var a = this.A(), b = la(a), c = ge[b];
        "number" == typeof c || (c = 0);
        0 == c && Qb(a.postMessage ? a : a.document, "message", he, m, fe);
        ge[b] = c + 1;
        this.qa = j;
        this.ji()
    };
    r.ji = function () {
        this.b.ua() || this.vb ? this.ed.stop() : (this.ed.start(), this.send("tp", "SETUP"))
    };
    r.send = function (a, b) {
        var c = this.b.va;
        if (c) {
            var d = c.postMessage ? c : c.document;
            this.send = function (a, b) {
                K(P, "send(): payload=" + b + " to hostname=" + this.ki);
                d.postMessage(this.b.name + "|" + a + ":" + b, this.ki)
            };
            this.send(a, b)
        } else K(P, "send(): window not ready")
    };
    r.g = function () {
        fe.z.g.call(this);
        if (this.qa) {
            var a = this.A(), b = la(a), c = ge[b];
            ge[b] = c - 1;
            1 == c && Ub(a.postMessage ? a : a.document, "message", he, m, fe)
        }
        G(this.r);
        delete this.r;
        G(this.ed);
        delete this.ed;
        delete this.send
    };
    var ie = function (a, b) {
        this.ia = b || xb();
        this.b = a;
        this.fi = a.at || "";
        this.ii = a.rat || "";
        var c = this.A();
        if (!c.nix_setup_complete)try {
            c.execScript("Class GCXPC____NIXVBS_wrapper\n Private m_Transport\nPrivate m_Auth\nPublic Sub SetTransport(transport)\nIf isEmpty(m_Transport) Then\nSet m_Transport = transport\nEnd If\nEnd Sub\nPublic Sub SetAuth(auth)\nIf isEmpty(m_Auth) Then\nm_Auth = auth\nEnd If\nEnd Sub\nPublic Function GetAuthToken()\n GetAuthToken = m_Auth\nEnd Function\nPublic Sub SendMessage(service, payload)\n Call m_Transport.GCXPC____NIXJS_handle_message(service, payload)\nEnd Sub\nPublic Sub CreateChannel(channel)\n Call m_Transport.GCXPC____NIXJS_create_channel(channel)\nEnd Sub\nPublic Sub GCXPC____NIXVBS_container()\n End Sub\nEnd Class\n Function GCXPC____NIXVBS_get_wrapper(transport, auth)\nDim wrap\nSet wrap = New GCXPC____NIXVBS_wrapper\nwrap.SetTransport transport\nwrap.SetAuth auth\nSet GCXPC____NIXVBS_get_wrapper = wrap\nEnd Function",
                "vbscript"), c.nix_setup_complete = j
        } catch (d) {
            P.w("exception caught while attempting global setup: " + d)
        }
        this.GCXPC____NIXJS_handle_message = this.pk;
        this.GCXPC____NIXJS_create_channel = this.Wb
    };
    A(ie, yd);
    r = ie.prototype;
    r.Pb = 6;
    r.rc = m;
    r.pb = k;
    r.T = function () {
        0 == Ad(this.b) ? this.ti() : this.ri()
    };
    r.ti = function () {
        if (!this.rc) {
            var a = this.b.jc;
            try {
                a.contentWindow.opener = this.A().GCXPC____NIXVBS_get_wrapper(this, this.fi), this.rc = j
            } catch (b) {
                P.w("exception caught while attempting setup: " + b)
            }
            this.rc || this.A().setTimeout(y(this.ti, this), 100)
        }
    };
    r.ri = function () {
        if (!this.rc) {
            try {
                var a = this.A().opener;
                if (a && "GCXPC____NIXVBS_container"in a) {
                    this.pb = a;
                    if (this.pb.GetAuthToken() != this.ii) {
                        P.w("Invalid auth token from other party");
                        return
                    }
                    this.pb.CreateChannel(this.A().GCXPC____NIXVBS_get_wrapper(this, this.fi));
                    this.rc = j;
                    Bd(this.b)
                }
            } catch (b) {
                P.w("exception caught while attempting setup: " + b);
                return
            }
            this.rc || this.A().setTimeout(y(this.ri, this), 100)
        }
    };
    r.Wb = function (a) {
        ("unknown" != typeof a || !("GCXPC____NIXVBS_container"in a)) && P.w("Invalid NIX channel given to createChannel_");
        this.pb = a;
        this.pb.GetAuthToken() != this.ii ? P.w("Invalid auth token from other party") : Bd(this.b)
    };
    r.pk = function (a, b) {
        this.A().setTimeout(y(function () {
            this.b.Za(a, b, i)
        }, this), 1)
    };
    r.send = function (a, b) {
        "unknown" !== typeof this.pb && P.w("NIX channel not connected");
        this.pb.SendMessage(a, b)
    };
    r.g = function () {
        ie.z.g.call(this);
        this.pb = k
    };
    var ke = function (a, b) {
        this.la = {};
        for (var c = 0, d; d = ud[c]; c++)d in a && !/^https?:\/\//.test(a[d]) && f(Error("URI " + a[d] + " is invalid for field " + d));
        this.v = a;
        this.name = this.v.cn || xd(10);
        this.ia = b || xb();
        this.Wc = [];
        a.lpu = a.lpu || Uc(this.ia.A().location.href) + "/robots.txt";
        a.ppu = a.ppu || Uc(a.pu || "") + "/robots.txt";
        vd[this.name] = this;
        Qb(window, "unload", je);
        P.info("CrossPageChannel created: " + this.name)
    };
    A(ke, Qc);
    var le = /^%*tp$/, me = /^%+tp$/;
    r = ke.prototype;
    r.ba = k;
    r.h = 1;
    r.ua = function () {
        return 2 == this.h
    };
    r.va = k;
    r.jc = k;
    var pe = function (a) {
        var b = document.body, c = a.v.ifrid;
        c || (c = a.v.ifrid = "xpcpeer" + xd(4));
        var d = document.createElement("IFRAME");
        d.id = d.name = c;
        d.style.width = d.style.height = "100%";
        var e = oe(a);
        Ya || E ? (a.ag = j, window.setTimeout(y(function () {
            this.ag = m;
            b.appendChild(d);
            d.src = e.toString();
            P.info("peer iframe created (" + c + ")");
            this.dd && this.T(this.Mf)
        }, a), 1)) : (d.src = e.toString(), b.appendChild(d), P.info("peer iframe created (" + c + ")"))
    }, oe = function (a) {
        var b = a.v.pu;
        x(b) && (b = a.v.pu = new N(b));
        var c = {};
        c.cn = a.name;
        c.tp =
            a.v.tp;
        a.v.lru && (c.pru = a.v.lru);
        a.v.lpu && (c.ppu = a.v.lpu);
        a.v.ppu && (c.lpu = a.v.ppu);
        O(b, "xpc", ec(c));
        return b
    };
    ke.prototype.ag = m;
    ke.prototype.dd = m;
    ke.prototype.T = function (a) {
        this.Mf = a || da;
        if (this.ag)P.info("connect() deferred"), this.dd = j; else {
            this.dd = m;
            P.info("connect()");
            this.v.ifrid && (this.jc = x(this.v.ifrid) ? this.ia.Oa.getElementById(this.v.ifrid) : this.v.ifrid);
            this.jc && ((a = this.jc.contentWindow) || (a = window.frames[this.v.ifrid]), this.va = a);
            this.va || (window == top && f(Error("CrossPageChannel: Can't connect, peer window-object not set.")), this.va = window.parent);
            if (!this.ba) {
                if (!this.v.tp) {
                    var a = this.v, b;
                    if (ha(document.postMessage) || ha(window.postMessage) ||
                        C && window.postMessage)b = 1; else if (Ya)b = 2; else if (C && this.v.pru)b = 3; else {
                        var c;
                        if (c = C) {
                            c = m;
                            try {
                                b = window.opener, window.opener = {}, c = Ib(window, "opener"), window.opener = b
                            } catch (d) {
                            }
                        }
                        b = c ? 6 : 4
                    }
                    a.tp = b
                }
                switch (this.v.tp) {
                    case 1:
                        this.ba = new fe(this, this.v.ph, this.ia);
                        break;
                    case 6:
                        this.ba = new ie(this, this.ia);
                        break;
                    case 2:
                        this.ba = new zd(this, this.ia);
                        break;
                    case 3:
                        this.ba = new Sd(this, this.ia);
                        break;
                    case 4:
                        this.ba = new Q(this, this.ia)
                }
                this.ba ? P.info("Transport created: " + this.ba.getName()) : f(Error("CrossPageChannel: No suitable transport found!"))
            }
            for (this.ba.T(); 0 <
                this.Wc.length;)this.Wc.shift()()
        }
    };
    ke.prototype.close = function () {
        this.ua() && (this.h = 3, this.ba.I(), this.Mf = this.ba = k, this.dd = m, this.Wc.length = 0, P.info('Channel "' + this.name + '" closed'))
    };
    var Bd = function (a) {
        a.ua() || (a.h = 2, P.info('Channel "' + a.name + '" connected'), a.Mf())
    };
    ke.prototype.send = function (a, b) {
        this.ua() ? Boolean(this.va.closed) ? (P.w("Peer has disappeared."), this.close()) : (ia(b) && (b = ec(b)), this.ba.send(qe(a), b)) : P.w("Can't send. Channel not connected.")
    };
    ke.prototype.Za = function (a, b, c) {
        if (this.dd)this.Wc.push(y(this.Za, this, a, b, c)); else {
            var d = this.v.ph;
            if (/^[\s\xa0]*$/.test(c == k ? "" : "" + c) || /^[\s\xa0]*$/.test(d == k ? "" : "" + d) || c == this.v.ph)if (this.vb)P.q("CrossPageChannel::deliver_(): Disposed."); else if (!a || "tp" == a)this.ba.$f(b); else if (this.ua()) {
                if (a = a.replace(/%[0-9a-f]{2}/gi, decodeURIComponent), a = me.test(a) ? a.substring(1) : a, c = this.la[a], c || (this.ci ? c = {Xf:oa(this.ci, a), Di:ia(b)} : (this.ie.q('Unknown service name "' + a + '"'), c = k)), c) {
                    var e;
                    a:{
                        if ((d = c.Di) &&
                            x(b))try {
                            e = ac(b);
                            break a
                        } catch (g) {
                            this.ie.q("Expected JSON payload for " + a + ', was "' + b + '"');
                            e = k;
                            break a
                        } else if (!d && !x(b)) {
                            e = ec(b);
                            break a
                        }
                        e = b
                    }
                    t(e) && c.Xf(e)
                }
            } else P.info("CrossPageChannel::deliver_(): Not connected."); else P.q('Message received from unapproved origin "' + c + '" - rejected.')
        }
    };
    var qe = function (a) {
        le.test(a) && (a = "%" + a);
        return a.replace(/[%:|]/g, encodeURIComponent)
    }, Ad = function (a) {
        return window.parent == a.va ? 1 : 0
    };
    ke.prototype.g = function () {
        ke.z.g.call(this);
        this.close();
        this.jc = this.va = k;
        delete vd[this.name];
        this.Wc.length = 0
    };
    var je = function () {
        for (var a in vd) {
            var b = vd[a];
            b && b.I()
        }
    };
    var re = function (a, b) {
        C ? a.cssText = b : a[E ? "innerText" : "innerHTML"] = b
    };
    var se = function (a, b, c, d, e, g) {
        var d = new N(d || window.location.href), h = new N, e = e ? e : Math.floor(1E3 * Math.random()) + ".talkgadget.google.com";
        Xc(h, e);
        Zc(h, "/talkgadget/d");
        O(h, "token", a);
        g && Yc(h, g);
        var a = c || "wcs-iframe", c = "#" + a + " { display: none; }", l = xb(i), p = k;
        if (C)p = l.Oa.createStyleSheet(), re(p, c); else {
            var n = yb(l.Oa, "head")[0];
            n || (p = yb(l.Oa, "body")[0], n = l.Vi("head"), p.parentNode.insertBefore(n, p));
            p = l.Vi("style");
            re(p, c);
            l.appendChild(n, p)
        }
        c = {};
        l = new N;
        Xc(l, e);
        g && Yc(l, g);
        Zc(l, "/talkgadget/xpc_blank");
        "http" == d.aa || "https" == d.aa ? (Vc(h, d.aa), Vc(l, d.aa), g = new N, Vc(g, d.aa), Xc(g, d.Q()), 80 != d.Ca && Yc(g, d.Ca), Zc(g, b)) : (Vc(h, "http"), Vc(l, "http"), g = new N("http://www.google.com/xpc_blank"));
        c.lpu = g.toString();
        c.ppu = l.toString();
        c.ifrid = a;
        c.pu = h.toString();
        ke.call(this, c)
    };
    A(se, ke);
    assign("chat.WcsCrossPageChannel", se);
    var ue = function (a, b, c, d, e) {
        this.readyState = 0;
        this.dg = [];
        this.onopen = b.onopen;
        this.onmessage = b.onmessage;
        this.onerror = b.onerror;
        this.onclose = b.onclose;
        this.ja = c || new se(a, "/_ah/channel/xpc_blank");
        this.te = c ? d : "wcs-iframe";
        this.kd = e || new te(a);
        document.body || f("document.body is not defined -- do not create socket from script in <head>.");
        pe(this.ja);
        this.ja.$("opened", y(this.Pk, this));
        this.ja.$("onMessage", y(this.Ok, this));
        this.ja.$("onError", y(this.me, this));
        this.ja.$("onClosed", y(this.Ri, this));
        this.ja.T(y(function () {
                this.Uf()
            },
            this))
    };
    ue.prototype.send = function () {
        return m
    };
    ue.prototype.close = function () {
        this.Ri()
    };
    ue.prototype.gl = function () {
        for (var a = 0, b; b = this.dg[a]; a++)switch (b.type) {
            case 0:
                this.onopen(b.Ce);
                break;
            case 1:
                this.onmessage(b.Ce);
                break;
            case 2:
                this.onerror(b.Ce);
                break;
            case 3:
                this.onclose(b.Ce)
        }
        this.dg = []
    };
    var ve = function (a, b, c) {
        a.dg.push({type:b, Ce:c});
        window.setTimeout(y(a.gl, a), 1)
    };
    r = ue.prototype;
    r.Ok = function (a) {
        var a = ac(a), b = a.m, a = a.s, c = we(this.kd, b);
        Pa(c, a) && ve(this, 1, {data:b});
        this.kd.qd++
    };
    r.me = function (a) {
        a = ac(a);
        ve(this, 2, {description:a.d, code:a.c})
    };
    r.Uf = function () {
    };
    r.Pk = function () {
        this.readyState = 1;
        ve(this, 0, {})
    };
    r.Ri = function () {
        this.ja.close();
        this.readyState = 3;
        ve(this, 3, {});
        if (this.te) {
            var a = new wb, b = x(this.te) ? a.Oa.getElementById(this.te) : this.te;
            b && a.removeNode(b)
        }
    };
    var te = function (a) {
        for (; 0 != a.length % 4;)a += ".";
        this.qd = 0;
        try {
            if (!jb) {
                jb = {};
                kb = {};
                lb = {};
                for (var b = 0; 65 > b; b++)jb[b] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".charAt(b), kb[b] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.".charAt(b), lb[kb[b]] = b
            }
            for (var b = lb, c = [], d = 0; d < a.length;) {
                var e = b[a.charAt(d++)], g = d < a.length ? b[a.charAt(d)] : 0;
                ++d;
                var h = d < a.length ? b[a.charAt(d)] : 0;
                ++d;
                var l = d < a.length ? b[a.charAt(d)] : 0;
                ++d;
                (e == k || g == k || h == k || l == k) && f(Error());
                c.push(e <<
                    2 | g >> 4);
                64 != h && (c.push(g << 4 & 240 | h >> 2), 64 != l && c.push(h << 6 & 192 | l))
            }
            this.jj = c
        } catch (p) {
            p.message && f(Error("The provided token is invalid (" + p.name + ": " + p.message + ")")), f(Error("The provided token is invalid."))
        }
        this.za = new SHA1;
        this.bl = new G_HMAC(this.za, this.jj, this.jj.length)
    }, we = function (a, b) {
        for (var c = [], d = 0, e = 0; e < b.length; e++) {
            for (var g = b.charCodeAt(e); 255 < g;)c[d++] = g & 255, g >>= 8;
            c[d++] = g
        }
        c.push(a.qd);
        d = a.bl;
        d.reset();
        d.update(c);
        return d.zc()
    };
    assign("goog.appengine.Socket", ue);
    assign("goog.appengine.Socket.ReadyState", {CONNECTING:0, OPEN:1, Zm:2, CLOSED:3});
    assign("goog.appengine.Socket.ReadyState.CONNECTING", 0);
    assign("goog.appengine.Socket.ReadyState.OPEN", 1);
    assign("goog.appengine.Socket.ReadyState.CLOSING", 2);
    assign("goog.appengine.Socket.ReadyState.CLOSED", 3);
    assign("goog.appengine.Socket.prototype.send", ue.prototype.send);
    assign("goog.appengine.Socket.prototype.close", ue.prototype.close);
    var xe = [4, 8, 16, 32, 1E3];
    var U = function () {
    };
    U.prototype.o = function () {
    };
    var ye = function (a) {
        return a.a
    };
    U.prototype.toString = function () {
        return this.a.toString()
    };
    var ze = function (a) {
        var b = V[a[0]];
        b || f(Error("Unknown JsPb message type: " + a[0]));
        return new b(a)
    }, V = {};
    var Ae = function (a) {
        this.a = a || ["ci:cpc"]
    };
    A(Ae, U);
    V["ci:cpc"] = Ae;
    Ae.prototype.o = function () {
        return"ci:cpc"
    };
    var Be = function (a) {
        this.a = a || ["ci:csm"];
        this.a[15] = this.a[15] || []
    };
    A(Be, U);
    V["ci:csm"] = Be;
    Be.prototype.o = function () {
        return"ci:csm"
    };
    var Ce = function (a) {
        return a.a[11]
    }, De = function (a) {
        this.a = a || ["ci:csmmc"]
    };
    A(De, U);
    V["ci:csmmc"] = De;
    De.prototype.o = function () {
        return"ci:csmmc"
    };
    var Ee = function (a) {
        this.a = a || ["ci:cui:uss"]
    };
    A(Ee, U);
    V["ci:cui:uss"] = Ee;
    r = Ee.prototype;
    r.o = function () {
        return"ci:cui:uss"
    };
    r.Ui = function () {
        return this.a[1]
    };
    r.getName = function () {
        return this.a[2]
    };
    r.sb = function (a) {
        this.a[2] = a
    };
    r.Sb = function () {
        return this.a[3]
    };
    r.Bc = function (a) {
        this.a[3] = a
    };
    r.Pg = function () {
        return this.a[4]
    };
    r.Ef = function (a) {
        this.a[4] = a
    };
    var Fe = function (a) {
        this.a = a || ["ci:cui"];
        this.a[9] = this.a[9] || []
    };
    A(Fe, U);
    V["ci:cui"] = Fe;
    r = Fe.prototype;
    r.o = function () {
        return"ci:cui"
    };
    r.i = function () {
        return this.a[1]
    };
    r.O = function (a) {
        this.a[1] = a
    };
    r.gb = function () {
        return this.a[3]
    };
    r.Hc = function (a) {
        this.a[3] = a
    };
    r.Ui = function () {
        return this.a[7]
    };
    r.Gd = function () {
        return this.a[12]
    };
    var Ge = function (a) {
        this.a = a || ["ci:cro"]
    };
    A(Ge, U);
    V["ci:cro"] = Ge;
    Ge.prototype.o = function () {
        return"ci:cro"
    };
    var He = function (a) {
        a.Xh || (a.Xh = new Fe(a.a[1]));
        return a.Xh
    }, Ie = function (a) {
        a.Uh || (a.Uh = new Ae(a.a[2]));
        return a.Uh
    }, Je = function (a) {
        a.Wh || (a.Wh = new Be(a.a[3]));
        return a.Wh
    };
    /*
     Portions of this code are from MochiKit, received by
     The Closure Authors under the MIT license. All other code is Copyright
     2005-2009 The Closure Authors. All Rights Reserved.
     */
    var Me = function (a) {
        this.id = Ke++;
        this.Uk = [];
        this.la = {};
        this.gg = {};
        this.Sd = {};
        this.Kb = {};
        this.Wi = {};
        this.oi = {};
        this.Ai = a ? a.Ai : new T;
        this.yk = !a;
        this.yd = k;
        a ? (this.yd = a, this.Sd = a.Sd, this.Kb = a.Kb, this.gg = a.gg, this.Wi = a.Wi) : z();
        a = Le(this);
        this != a && (a.oe ? a.oe.push(this) : a.oe = [this])
    };
    A(Me, Eb);
    var Ke = 1, Le = function (a) {
        for (; a.yd;)a = a.yd;
        return a
    };
    Me.prototype.get = function (a) {
        var b = Ne(this, a);
        !b && this.er && (b = {BOGUS:"Service " + a + " was not registered"});
        b || f(new Oe(a));
        return b
    };
    var Ne = function (a, b) {
        for (var c = a; c; c = c.yd) {
            if (c.la[b])return c.la[b][0];
            if (c.oi[b])break
        }
        return(c = a.Sd[b]) ? (c = c(a), a.$(b, c), c) : k
    };
    Me.prototype.$ = function (a, b, c) {
        Da(!this.la[a], 'Service for "%s" is already registered', a);
        this.Uk.push(a);
        this.la[a] = [b, !c];
        b = Pe(this, this, a);
        for (c = 0; c < b.length; c++)b[c].Xf(k);
        delete this.gg[a]
    };
    var Pe = function (a, b, c) {
        var d = [], e = a.Kb[c];
        e && (Ha(e, function (a) {
            var c;
            a:{
                for (c = a.fl; c;) {
                    if (c == b) {
                        c = j;
                        break a
                    }
                    c = c.yd
                }
                c = m
            }
            c && (d.push(a.Im), Ka(e, a))
        }), 0 == e.length && delete a.Kb[c]);
        return d
    }, Qe = function (a, b) {
        a.Kb && pc(a.Kb, function (a, d, e) {
            Ha(a, function (d) {
                d.fl == b && Ka(a, d)
            });
            0 == a.length && delete e[d]
        })
    };
    Me.prototype.g = function () {
        if (Le(this) == this) {
            var a = this.oe;
            if (a)for (; a.length;)a[0].I()
        } else for (var a = Le(this).oe, b = 0; b < a.length; b++)if (a[b] == this) {
            a.splice(b, 1);
            break
        }
        for (var c in this.la)a = this.la[c], a[1] && "undefined" != typeof a[0].I && a[0].I();
        this.la = k;
        this.yk && this.Ai.I();
        Qe(this, this);
        this.Kb = k;
        G(this.Dk);
        this.oi = this.Dk = k;
        Me.z.g.call(this)
    };
    var Oe = function (a) {
        qa.call(this);
        this.id = a;
        this.message = 'Service for "' + a + '" is not registered'
    };
    A(Oe, qa);
    var Re = function (a) {
        Ge.call(this, a);
        this.Hg = "u";
        this.Se = "";
        this.version = "local";
        this.Lg = [];
        this.gh = "Google Talk";
        this.startTime = 0;
        this.Mg = this.Ig = this.debug = m;
        this.Yb = -1;
        this.Og = 1;
        u(Ce(Je(this))) || (Je(this).a[11] = "[b:-1,h:-1,j:-1,sn:-1,sf:-1,1:1]");
        a = Je(this);
        u(a.a[12]) || (Je(this).a[12] = "{p:1} {ac:5,aa:4,ab:3,ai:2,aw:1,ao:0} {wb:1}");
        a = Je(this);
        u(a.a[13]) || (Je(this).a[13] = "*");
        a = Je(this);
        u(a.a[14]) || (Je(this).a[14] = "");
        a = Ie(this);
        u(a.a[7]) || (Ie(this).a[7] = "")
    };
    A(Re, Ge);
    assign("GoogleChat_GetConfig", function (a, b, c, d, e, g, h, l, p, n, q, v, aa, D, ne, Ui, Vi, Wi, Xi, Yi, Zi, $i, aj, bj, cj, dj, ej, fj, gj, hj, ij, jj, kj, lj, mj, nj, oj, pj, qj, rj, sj, tj, uj, vj, wj, xj, yj, zj, Aj, Bj, Cj, Dj, Ej, Fj, Gj, Hj, Ij, Jj, Kj, Lj, Mj, Nj, Oj, Pj, Qj, Rj, Sj, Tj, Uj, Vj, Wj, Xj, Yj, Zj, $j, o) {
        o && (o = ac(o));
        o = new Re(o);
        o.hm = j;
        o.Se = a;
        o.hk = b;
        o.ik = c;
        o.Hg = d;
        o.Ql = e;
        o.version = g;
        o.jk = h;
        o.Nl = l;
        o.yl = p;
        o.mm = n;
        o.Lg = q;
        o.bm = v;
        o.xl = aa;
        o.gh = D;
        o.im = "aquasar" == D || "Google-Voice" == D;
        o.gm = "aquasar" == D || "hangout" == D;
        o.fm = "ChromeOS" == D;
        o.em = "aChromeExtension" == D;
        o.ym = ne;
        o.Gl = Ui;
        o.Fl = Vi;
        o.Hl = Wi;
        o.Ol = Xi;
        o.startTime = Yi;
        o.debug = Zi;
        o.Jl = $i;
        o.xm = aj;
        o.Ig = bj;
        o.sl = cj;
        o.wl = dj;
        o.tm = ej;
        o.nm = fj;
        o.jm = gj;
        o.wm = hj;
        o.Tl = ij;
        o.Vl = jj;
        o.Mg = kj;
        o.Yl = lj;
        o.am = mj;
        o.Zl = nj;
        o.Sl = oj;
        o.Xl = pj;
        o.Ul = qj;
        o.om = rj;
        o.um = sj;
        o.qm = tj;
        o.dm = uj;
        o.Il = vj;
        o.Kl = wj;
        o.pm = xj;
        o.vl = yj;
        o.Cl = zj;
        o.El = Aj;
        o.Bl = Bj;
        o.Bm = Cj;
        o.Al = Dj;
        o.Yb = Ej;
        o.Og = Fj;
        o.ul = Gj;
        o.Rl = Hj;
        o.km = Ij;
        o.Dm = Jj || k;
        o.Em = Kj;
        o.zm = Lj;
        o.Wl = Mj;
        o.Ll = Nj;
        o.$l = Oj;
        o.vm = Pj;
        o.tl = Qj;
        o.sm = Rj;
        o.zl = Sj;
        o.cm = Tj;
        o.Pl = Uj;
        o.Ml = Vj;
        o.rm = Wj;
        o.Dl = Xj;
        o.Cm = Yj;
        o.Am = Zj;
        o.lm = $j;
        return o
    });
    var Se = function () {
    };
    Se.prototype.vj = k;
    var Ue = function (a) {
        var b;
        if (!(b = a.vj))b = {}, Te(a) && (b[0] = j, b[1] = j), b = a.vj = b;
        return b
    };
    var Ve, We = function () {
    };
    A(We, Se);
    var Xe = function (a) {
        return(a = Te(a)) ? new ActiveXObject(a) : new XMLHttpRequest
    };
    We.prototype.tg = k;
    var Te = function (a) {
        if (!a.tg && "undefined" == typeof XMLHttpRequest && "undefined" != typeof ActiveXObject) {
            for (var b = ["MSXML2.XMLHTTP.6.0", "MSXML2.XMLHTTP.3.0", "MSXML2.XMLHTTP", "Microsoft.XMLHTTP"], c = 0; c < b.length; c++) {
                var d = b[c];
                try {
                    return new ActiveXObject(d), a.tg = d
                } catch (e) {
                }
            }
            f(Error("Could not create ActiveXObject. ActiveX might be disabled, or MSXML might not be installed"))
        }
        return a.tg
    };
    Ve = new We;
    var Ye = function (a) {
        this.headers = new J;
        this.ke = a || k
    };
    A(Ye, T);
    Ye.prototype.f = M("goog.net.XhrIo");
    var Ze = /^https?$/i, $e = [];
    r = Ye.prototype;
    r.ob = m;
    r.C = k;
    r.ge = k;
    r.If = "";
    r.bi = "";
    r.u = "";
    r.Kf = m;
    r.he = m;
    r.Jf = m;
    r.Ib = m;
    r.je = 0;
    r.Hb = k;
    r.ei = "";
    r.kk = m;
    r.send = function (a, b, c, d) {
        this.C && f(Error("[goog.net.XhrIo] Object is active with another request"));
        b = b ? b.toUpperCase() : "GET";
        this.If = a;
        this.u = "";
        this.bi = b;
        this.Kf = m;
        this.ob = j;
        this.C = this.ke ? Xe(this.ke) : Xe(Ve);
        this.ge = this.ke ? Ue(this.ke) : Ue(Ve);
        this.C.onreadystatechange = y(this.ai, this);
        try {
            K(this.f, af(this, "Opening Xhr")), this.Jf = j, this.C.open(b, a, j), this.Jf = m
        } catch (e) {
            K(this.f, af(this, "Error opening Xhr: " + e.message));
            bf(this, e);
            return
        }
        var a = c || "", g = this.headers.K();
        d && pc(d, function (a, b) {
            g.set(b,
                a)
        });
        "POST" == b && !g.W("Content-Type") && g.set("Content-Type", "application/x-www-form-urlencoded;charset=utf-8");
        pc(g, function (a, b) {
            this.C.setRequestHeader(b, a)
        }, this);
        this.ei && (this.C.responseType = this.ei);
        "withCredentials"in this.C && (this.C.withCredentials = this.kk);
        try {
            this.Hb && (ce.clearTimeout(this.Hb), this.Hb = k), 0 < this.je && (K(this.f, af(this, "Will abort after " + this.je + "ms if incomplete")), this.Hb = ce.setTimeout(y(this.Tc, this), this.je)), K(this.f, af(this, "Sending request")), this.he = j, this.C.send(a),
                this.he = m
        } catch (h) {
            K(this.f, af(this, "Send error: " + h.message)), bf(this, h)
        }
    };
    r.Tc = function () {
        "undefined" != typeof ba && this.C && (this.u = "Timed out after " + this.je + "ms, aborting", K(this.f, af(this, this.u)), this.dispatchEvent("timeout"), this.abort(8))
    };
    var bf = function (a, b) {
        a.ob = m;
        a.C && (a.Ib = j, a.C.abort(), a.Ib = m);
        a.u = b;
        cf(a);
        df(a)
    }, cf = function (a) {
        a.Kf || (a.Kf = j, a.dispatchEvent("complete"), a.dispatchEvent("error"))
    };
    Ye.prototype.abort = function () {
        this.C && this.ob && (K(this.f, af(this, "Aborting")), this.ob = m, this.Ib = j, this.C.abort(), this.Ib = m, this.dispatchEvent("complete"), this.dispatchEvent("abort"), df(this))
    };
    Ye.prototype.g = function () {
        this.C && (this.ob && (this.ob = m, this.Ib = j, this.C.abort(), this.Ib = m), df(this, j));
        Ye.z.g.call(this)
    };
    Ye.prototype.ai = function () {
        !this.Jf && !this.he && !this.Ib ? this.el() : ef(this)
    };
    Ye.prototype.el = function () {
        ef(this)
    };
    var ef = function (a) {
        if (a.ob && "undefined" != typeof ba)if (a.ge[1] && 4 == ff(a) && 2 == a.fb())K(a.f, af(a, "Local request error detected and ignored")); else if (a.he && 4 == ff(a))ce.setTimeout(y(a.ai, a), 0); else if (a.dispatchEvent("readystatechange"), 4 == ff(a)) {
            K(a.f, af(a, "Request complete"));
            a.ob = m;
            var statusCode = a.fb(), c;
            a:switch (statusCode) {
                case 200:
                case 201:
                case 202:
                case 204:
                case 304:
                case 1223:
                    c = j;
                    break a;
                default:
                    c = m
            }
            if (!c) {
                if (statusCode = 0 === statusCode)statusCode = Tc("" + a.If)[1] || k, !statusCode && self.location && (statusCode = self.location.protocol, statusCode = statusCode.substr(0, statusCode.length - 1)), statusCode = !Ze.test(statusCode ?
                    statusCode.toLowerCase() : "");
                c = statusCode
            }
            if (c)a.dispatchEvent("complete"), a.dispatchEvent("success"); else {
                var d;
                try {
                    d = 2 < ff(a) ? a.C.statusText : ""
                } catch (e) {
                    K(a.f, "Can not get status: " + e.message), d = ""
                }
                a.u = d + " [" + a.fb() + "]";
                cf(a)
            }
            df(a)
        }
    }, df = function (a, b) {
        if (a.C) {
            var c = a.C, d = a.ge[0] ? da : k;
            a.C = k;
            a.ge = k;
            a.Hb && (ce.clearTimeout(a.Hb), a.Hb = k);
            b || a.dispatchEvent("ready");
            try {
                c.onreadystatechange = d
            } catch (e) {
                a.f.w("Problem encountered resetting onreadystatechange: " + e.message)
            }
        }
    };
    Ye.prototype.Jb = function () {
        return!!this.C
    };
    var ff = function (a) {
        return a.C ? a.C.readyState : 0
    };
    Ye.prototype.fb = function () {
        try {
            return 2 < ff(this) ? this.C.status : -1
        } catch (a) {
            return this.f.q("Can not get status: " + a.message), -1
        }
    };
    var gf = function (a) {
        try {
            return a.C ? a.C.responseText : ""
        } catch (b) {
            return K(a.f, "Can not get responseText: " + b.message), ""
        }
    };
    Ye.prototype.Qh = function () {
        return x(this.u) ? this.u : "" + this.u
    };
    var af = function (a, b) {
        return b + " [" + a.bi + " " + a.If + " " + a.fb() + "]"
    };
    var hf = function (a, b) {
        this.wd = b;
        this.Lb = [];
        a > this.wd && f(Error("[goog.structs.SimplePool] Initial cannot be greater than max"));
        for (var c = 0; c < a; c++)this.Lb.push(this.vd())
    };
    A(hf, Eb);
    hf.prototype.wj = k;
    hf.prototype.kj = k;
    var jf = function (a) {
        return a.Lb.length ? a.Lb.pop() : a.vd()
    }, kf = function (a, b) {
        a.Lb.length < a.wd ? a.Lb.push(b) : a.Qf(b)
    };
    hf.prototype.vd = function () {
        return this.wj ? this.wj() : {}
    };
    hf.prototype.Qf = function (a) {
        if (this.kj)this.kj(a); else if (ia(a))if (ha(a.I))a.I(); else for (var b in a)delete a[b]
    };
    hf.prototype.g = function () {
        hf.z.g.call(this);
        for (var a = this.Lb; a.length;)this.Qf(a.pop());
        delete this.Lb
    };
    var nf = function () {
        this.ca = [];
        this.mb = new J;
        this.zi = this.Ud = this.Vd = this.Mc = 0;
        this.Cb = new J;
        this.yi = this.ff = 0;
        this.Lk = 1;
        this.Db = new hf(0, 4E3);
        this.Db.vd = function () {
            return new lf
        };
        this.jf = new hf(0, 50);
        this.jf.vd = function () {
            return new mf
        };
        var a = this;
        this.fc = new hf(0, 2E3);
        this.fc.vd = function () {
            return"" + a.Lk++
        };
        this.fc.Qf = function () {
        };
        this.xh = 3
    };
    nf.prototype.f = M("goog.debug.Trace");
    nf.prototype.nf = 1E3;
    var mf = function () {
        this.Zc = this.time = this.count = 0
    };
    mf.prototype.toString = function () {
        var a = [];
        a.push(this.type, " ", this.count, " (", Math.round(10 * this.time) / 10, " ms)");
        this.Zc && a.push(" [VarAlloc = ", this.Zc, "]");
        return a.join("")
    };
    var lf = function () {
    }, qf = function (a, b, c, d) {
        var e = [];
        -1 == c ? e.push("    ") : e.push(of(a.Xd - c));
        e.push(" ", pf(a.Xd - b));
        0 == a.Yc ? e.push(" Start        ") : 1 == a.Yc ? (e.push(" Done "), e.push(of(a.Tj - a.startTime), " ms ")) : e.push(" Comment      ");
        e.push(d, a);
        0 < a.bd && e.push("[VarAlloc ", a.bd, "] ");
        return e.join("")
    };
    lf.prototype.toString = function () {
        return this.type == k ? this.cd : "[" + this.type + "] " + this.cd
    };
    nf.prototype.reset = function (a) {
        this.xh = a;
        for (a = 0; a < this.ca.length; a++) {
            var b = this.Db.id;
            b && kf(this.fc, b);
            kf(this.Db, this.ca[a])
        }
        this.ca.length = 0;
        this.mb.clear();
        this.Mc = z();
        this.yi = this.ff = this.zi = this.Ud = this.Vd = 0;
        b = this.Cb.Ma();
        for (a = 0; a < b.length; a++) {
            var c = this.Cb.get(b[a]);
            c.count = 0;
            c.time = 0;
            c.Zc = 0;
            kf(this.jf, c)
        }
        this.Cb.clear()
    };
    var rf = function (a) {
        return(a = a.fr) && a.isTracing() ? a.totalVarAlloc : -1
    };
    nf.prototype.toString = function () {
        for (var a = [], b = -1, c = [], d = 0; d < this.ca.length; d++) {
            var e = this.ca[d];
            1 == e.Yc && c.pop();
            a.push(" ", qf(e, this.Mc, b, c.join("")));
            b = e.Xd;
            a.push("\n");
            0 == e.Yc && c.push("|  ")
        }
        if (0 != this.mb.P()) {
            var g = z();
            a.push(" Unstopped timers:\n");
            lc(this.mb, function (b) {
                a.push("  ", b, " (", g - b.startTime, " ms, started at ", pf(b.startTime), ")\n")
            })
        }
        b = this.Cb.Ma();
        for (d = 0; d < b.length; d++)c = this.Cb.get(b[d]), 1 < c.count && a.push(" TOTAL ", c, "\n");
        a.push("Total tracers created ", this.ff, "\n", "Total comments created ",
            this.yi, "\n", "Overhead start: ", this.Vd, " ms\n", "Overhead end: ", this.Ud, " ms\n", "Overhead comment: ", this.zi, " ms\n");
        return a.join("")
    };
    var of = function (a) {
        var a = Math.round(a), b = "";
        1E3 > a && (b = " ");
        100 > a && (b = "  ");
        10 > a && (b = "   ");
        return b + a
    }, pf = function (a) {
        a = Math.round(a);
        return("" + (100 + a / 1E3 % 60)).substring(1, 3) + "." + ("" + (1E3 + a % 1E3)).substring(1, 4)
    }, sf = new nf;
    M("goog.debug.ErrorReporter");
    var tf = function () {
        this.f = M("chat.client.ServerMessageDispatcher")
    };
    A(tf, T);
    tf.prototype.handle = function (a) {
        log(this.f, "received: " + a);
        return Yb(this, a[0]) ? (this.dispatchEvent(new uf(a[0], a)), j) : m
    };
    var uf = function (a, b) {
        H.call(this, a, b);
        this.Ha = b
    };
    A(uf, H);
    var vf = function (a) {
        this.a = a || ["csu"];
        this.a[4] = this.a[4] || [];
        this.a[6] = this.a[6] || []
    };
    A(vf, U);
    V.csu = vf;
    r = vf.prototype;
    r.o = function () {
        return"csu"
    };
    r.i = function () {
        return this.a[1]
    };
    r.O = function (a) {
        this.a[1] = a
    };
    r.Vc = function () {
        return this.a[3]
    };
    r.wh = function (a) {
        this.a[3] = a
    };
    var xf = function (a) {
        if (!a.Df) {
            a.Df = [];
            for (var b = 0; b < a.a[4].length; b++)a.Df[b] = new wf(a.a[4][b])
        }
        return a.Df
    }, wf = function (a) {
        this.a = a || []
    };
    A(wf, U);
    r = wf.prototype;
    r.Wa = function () {
        return this.a[0]
    };
    r.Ae = function () {
        return this.a[1]
    };
    r.ef = function () {
        return this.a[3]
    };
    r.vh = function () {
        return this.a[4]
    };
    r.sh = function () {
        return this.a[5]
    };
    r.th = function () {
        return this.a[7]
    };
    r.uh = function () {
        return this.a[8]
    };
    var zf = function (a, b, c) {
        this.Y = a || k;
        this.bb = (b ? b.replace(yf, "\\27") : b) || k;
        this.Ad = c || k
    }, Af = /(muvc-|named-)?private-chat-[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}/, Bf = /^([0-9]+\.)?voice\.google\.com$/, Cf = [".google.com", ".gmail.com"], Df = /fc-chat-.*/, yf = RegExp("'", "g"), Ff = function (a) {
        a = Ef(a);
        return t(a.bb) && "contact.talk.google.com" == a.Q()
    }, Gf = function (a) {
        var b = a.indexOf("/");
        return-1 == b ? a : a.substring(0, b)
    }, Ef = function (a) {
        var b = a.indexOf("/"), c = a.indexOf("@"), d = k;
        0 > b ? b = a.length : b + 1 <
            a.length && (d = a.substring(b + 1));
        var e = k;
        0 <= c && (c < b ? 0 < c && (e = a.substring(0, c)) : c = -1);
        return new zf(a.substring(c + 1, b), e, d)
    };
    zf.prototype.Q = function () {
        return this.Y
    };
    var Hf = function (a) {
        var b;
        if (!(b = "groupchat.google.com" == a.Q() && Af.test(a.bb)))if (!(b = "friendconnectchat.google.com" == a.Q() && Df.test(a.bb))) {
            b = a;
            x(b) && (b = Ef(b));
            var c = 0 == (b.Q() || "").lastIndexOf("conference.", 0), d = m;
            if (c)for (var e = 0; e < Cf.length; e++) {
                var g = b.Q() || "", h = Cf[e], l = g.length - h.length;
                if (0 <= l && g.indexOf(h, l) == l) {
                    d = j;
                    break
                }
            }
            if (!(b = c && d))x(a) && (a = Ef(a)), b = "muvc.google.com" == a.Q()
        }
        return b
    }, If = function (a) {
        return a.Ad == k ? a : new zf(a.Y, a.bb, k)
    };
    zf.prototype.Ub = function (a) {
        return this.Y == a.Y && this.bb == a.bb && this.Ad == a.Ad
    };
    zf.prototype.toString = function () {
        var a = [];
        this.bb != k && (a.push(this.bb), a.push("@"));
        a.push(this.Y);
        this.Ad != k && (a.push("/"), a.push(this.Ad));
        return a.join("")
    };
    var Jf = function (a, b, c) {
        this.jg = b;
        this.Aa = a;
        this.He = z();
        this.we = c
    };
    A(Jf, T);
    Jf.prototype.Ae = function () {
        return this.Aa
    };
    Jf.prototype.Wa = function () {
        return this.jg
    };
    var Kf = function (a, b, c, d, e, g, h, l) {
        Jf.call(this, "c", a, e);
        this.Zi = g;
        this.$i = b;
        this.ig = h;
        this.ic = d;
        this.Yi = c;
        this.cl = l || k
    };
    A(Kf, Jf);
    Kf.prototype.Ub = function (a) {
        return this.jg == a.jg && this.Aa == a.Aa && this.we == a.we && t(this.Zi) == t(a.Zi) && this.$i == a.$i && this.ig == a.ig && this.ic == a.ic && this.Yi == a.Yi
    };
    Kf.prototype.uh = function () {
        return this.cl
    };
    Kf.prototype.vh = function () {
        return!Hf(Ef(this.ic)) ? Gf(this.ic) : this.ic
    };
    Kf.prototype.th = function () {
        return this.ig
    };
    var Lf = function (a, b, c) {
        this.xb = a;
        this.mh = b;
        this.ng = c;
        this.gj = m;
        this.Ng = [];
        this.Md = new J;
        this.Bb = k
    };
    A(Lf, T);
    Lf.prototype.g = function () {
        Lf.z.g.call(this);
        this.Ng = [];
        this.Md.clear()
    };
    Lf.prototype.Vc = function () {
        return this.ng
    };
    Lf.prototype.i = function () {
        return this.xb
    };
    Lf.prototype.sj = function (a) {
        return this.Md.get(a)
    };
    var Mf = function (a, b, c) {
        Lf.call(this, a, b, c)
    };
    A(Mf, Lf);
    Mf.prototype.wh = function (a) {
        return this.ng != a ? (this.ng = a, this.dispatchEvent("e"), j) : m
    };
    var Nf = function (a) {
        a.gj || (a.gj = j, a.dispatchEvent("g"))
    }, Of = function (a) {
        H.call(this, a)
    };
    A(Of, H);
    var Pf = function () {
        this.Kd = new J
    };
    A(Pf, T);
    Pf.prototype.g = function () {
        Pf.z.g.call(this);
        this.Kd.clear()
    };
    var Qf = function (a) {
        this.Kd = new J;
        this.n = new R(this);
        S(this.n, a.get("ha"), "csu", this.Yk)
    };
    A(Qf, Pf);
    Qf.prototype.g = function () {
        Qf.z.g.call(this);
        G(this.n)
    };
    Qf.prototype.Yk = function (a) {
        var a = ze(a.Ha), b;
        if ("number" == typeof a.Vc())b = a.Vc(); else switch (a.Vc()) {
            case "i":
                b = 1;
                break;
            case "o":
                b = 4;
                break;
            case "m":
                b = 3;
                break;
            case "c":
                b = 2
        }
        var c = this.Kd.get(a.i()), d = m;
        if (c) {
            var e = c, g = a.a[2];
            e.mh != g && (e.mh = g, e.dispatchEvent("f"));
            e = c.Vc();
            if (c.wh(b) && (2 == e || 1 == e && 2 != b))d = j
        } else c = new Mf(a.i(), a.a[2], b), this.Kd.set(a.i(), c), 2 != b && (d = j);
        d && this.dispatchEvent(new Rf("h"));
        c.Bb = [];
        if (0 < xf(a).length)for (b = 0; b < xf(a).length; ++b) {
            d = xf(a)[b];
            e = new Kf(d.Wa(), d.a[2], d.ef(), d.vh(),
                d.sh(), d.a[6], d.th(), d.uh());
            a:{
                d = c;
                (g = d.Md.get(e.Wa())) || d.Ng.push(e.Wa());
                if (!g || !g.Ub(e))if (d.Md.set(e.Wa(), e), d.Bb != k)d.Bb.push(e); else {
                    d.dispatchEvent(new Of("d"));
                    Nf(d);
                    d = j;
                    break a
                }
                d.Bb == k && Nf(d);
                d = m
            }
            d && this.dispatchEvent(new Rf("i"))
        }
        a = c;
        0 < a.Bb.length ? (a.dispatchEvent(new Of("d")), a.Bb = k, Nf(a), a = j) : (a.Bb = k, Nf(a), a = m);
        a && this.dispatchEvent(new Rf("i"))
    };
    var Rf = function (a) {
        H.call(this, a)
    };
    A(Rf, H);
    var Sf = function (a) {
        this.f = M("chat.client.ExperimentManager");
        this.Qk = a.get("ga");
        this.tb = new uc(this.Qk.Lg)
    }, Tf = {ALPHA:"al", Km:"ap", Lm:"ad", Nm:"au", Mm:"acc", Om:"be", Pm:"bp", Qm:"cd", Rm:"cdb", Sm:"ccm", Tm:"cmp", Vm:"ch", qk:"chg", Wm:"chp", Um:"csm", Xm:"cc", Ym:"c3", $m:"cds", an:"ci", bn:"fca", dn:"crp", en:"fdc", fn:"dnd", Bn:"ft", Cn:"fs", Dn:"fse", En:"gcy", Fn:"gdp", Gn:"gdd", Hn:"ght", In:"doc", Jn:"gsd", Kn:"gdy", Ln:"gbc", Mn:"gdh", Nn:"geg", On:"gfm", Pn:"gfv", Qn:"ggd", Rn:"ghp", Tn:"gsw", Sn:"gsn", Un:"glv", Vn:"gmi", Wn:"gml",
        Xn:"gwf", Zn:"gpa", $n:"gph", Yn:"gpm", ao:"grc", bo:"gss", co:"sst", eo:"gse", fo:"spc", ho:"gst", io:"gwm", jo:"gs", ko:"gbh", lo:"gha", mo:"gvi", no:"gmj", oo:"gnf", po:"gob", qo:"gvr", ro:"ho", so:"hrs", to:"hab", uo:"ha", vo:"hap", wo:"hlb", xo:"har", yo:"hbl", Ao:"hbp", zo:"bic", Co:"hbs", Do:"hbi", Eo:"hbt", Bo:"hb", Fo:"hby", Ho:"hci", Go:"hcb", Io:"hcp", Jo:"csi", Ko:"hdc", Lo:"hdp", Mo:"her", No:"hrm", Oo:"hef", Po:"hei", Qo:"hfj", Ro:"hgc", hp:"hng", So:"hhs", To:"hht", Uo:"hhp", Wo:"his", Vo:"him", Xo:"hja", Yo:"hlp", Zo:"hlr", $o:"hls", ap:"hms",
        bp:"hmj", cp:"hmm", dp:"hmd", ep:"hm", gp:"nci", fp:"hnc", ip:"hph", jp:"hpt", kp:"hpo", lp:"hqs", mp:"hrk", np:"hss", op:"hst", pp:"hsa", qp:"hsb", rp:"hsr", sp:"hsg", up:"hpi", vp:"ht", wp:"htp", xp:"htr", yp:"hxs", zp:"htm", Ap:"hyb", Bp:"hot", Cp:"hn", Dp:"h5a", Ep:"is", Fp:"jlu", Gp:"jps", Hp:"tm", Ip:"lp", Jp:"lic", Lp:"ld", Kp:"le", Mp:"mef", Np:"mtg", Op:"mfd", Pp:"mfr", Qp:"mpi", Rp:"mc", Sp:"nah", Tp:"nh", Up:"nm", Vp:"nq", oq:"pam", Iq:"rcq", Xp:"nc", Wp:"nft", Yp:"ogc", Zp:"og", $p:"osm", aq:"orc", bq:"org", cq:"orw", dq:"occ", eq:"o0i", iq:"odc",
        gq:"ood", fq:"oci", hq:"ocs", jq:"ops", kq:"iud", lq:"opc", mq:"opf", nq:"pbr", pq:"po", qq:"pst", rq:"prr", sq:"pf", tq:"pr", uq:"pmr", vq:"ppi", wq:"qde", xq:"qha", Aq:"qia", zq:"qig", Bq:"qlp", Cq:"qmw", yq:"qhc", Dq:"qna", Eq:"qp", Fq:"qmr", Gq:"qrs", Hq:"qrh", Jq:"rtb", Kq:"rht", rk:"ss", Lq:"sf", Mq:"sch", Nq:"sc2", Oq:"sdm", Cj:"sie", Pq:"spa", Qq:"spi", Hd:"srt", Rq:"srb", Sq:"srf", Tq:"ssd", Uq:"ssn", Vq:"sp1", Wq:"sp2", Xq:"sur", br:"wv", cr:"vnr", Yq:"ump", Zq:"vd", $q:"vp", ar:"vs", dr:"yo"};
    var Vf = function (a) {
        this.Mb = a || Uf
    }, Uf = function (a, b) {
        return"" + a < "" + b ? -1 : "" + a > "" + b ? 1 : 0
    };
    r = Vf.prototype;
    r.oa = k;
    r.Mb = k;
    r.eb = k;
    r.cb = k;
    r.j = 0;
    r.add = function (a) {
        if (this.oa == k)return this.cb = this.eb = this.oa = new Wf(a), this.j = 1, j;
        var b = m;
        Xf(this, function (c) {
            var d = k;
            if (0 < this.Mb(c.value, a)) {
                if (d = c.left, c.left == k) {
                    var e = new Wf(a, c);
                    c.left = e;
                    c == this.eb && (this.eb = e);
                    b = j;
                    Yf(this, c)
                }
            } else 0 > this.Mb(c.value, a) && (d = c.right, c.right == k && (e = new Wf(a, c), c.right = e, c == this.cb && (this.cb = e), b = j, Yf(this, c)));
            return d
        });
        b && (this.j += 1);
        return b
    };
    r.remove = function (a) {
        var b = k;
        Xf(this, function (c) {
            var d = k;
            if (0 < this.Mb(c.value, a))d = c.left; else if (0 > this.Mb(c.value, a))d = c.right; else if (b = c.value, c.left != k || c.right != k) {
                var e = k, g;
                if (c.left != k) {
                    g = Zf(this, c.left);
                    if (g != c.left) {
                        if (g.parent.right = g.left)g.left.parent = g.parent;
                        g.left = c.left;
                        g.left.parent = g;
                        e = g.parent
                    }
                    g.parent = c.parent;
                    g.right = c.right;
                    g.right && (g.right.parent = g);
                    c == this.cb && (this.cb = g)
                } else {
                    g = $f(this, c.right);
                    if (g != c.right) {
                        if (g.parent.left = g.right)g.right.parent = g.parent;
                        g.right = c.right;
                        g.right.parent = g;
                        e = g.parent
                    }
                    g.parent = c.parent;
                    g.left = c.left;
                    g.left && (g.left.parent = g);
                    c == this.eb && (this.eb = g)
                }
                ag(c) ? c.parent.left = g : bg(c) ? c.parent.right = g : this.oa = g;
                Yf(this, e ? e : g)
            } else ag(c) ? (c.parent.left = k, c == this.eb && (this.eb = c.parent), Yf(this, c.parent)) : bg(c) ? (c.parent.right = k, c == this.cb && (this.cb = c.parent), Yf(this, c.parent)) : this.clear();
            return d
        });
        b && (this.j = this.oa ? this.j - 1 : 0);
        return b
    };
    r.clear = function () {
        this.cb = this.eb = this.oa = k;
        this.j = 0
    };
    r.contains = function (a) {
        var b = m;
        Xf(this, function (c) {
            var d = k;
            0 < this.Mb(c.value, a) ? d = c.left : 0 > this.Mb(c.value, a) ? d = c.right : b = j;
            return d
        });
        return b
    };
    r.P = function () {
        return this.j
    };
    r.da = function () {
        var a = [];
        cg(this, function (b) {
            a.push(b)
        });
        return a
    };
    var cg = function (a, b) {
        if (a.oa) {
            var c, d = c = $f(a);
            for (c = c.left ? c.left : c; d != k;)if (d.left != k && d.left != c && d.right != c)d = d.left; else {
                if (d.right != c && b(d.value))break;
                var e = d, d = d.right != k && d.right != c ? d.right : d.parent;
                c = e
            }
        }
    }, Xf = function (a, b, c) {
        for (c = c ? c : a.oa; c && c != k;)c = b.call(a, c)
    }, Yf = function (a, b) {
        Xf(a, function (a) {
            var b = a.left ? a.left.height : 0, e = a.right ? a.right.height : 0;
            1 < b - e ? (a.left.right && (!a.left.left || a.left.left.height < a.left.right.height) && dg(this, a.left), eg(this, a)) : 1 < e - b && (a.right.left && (!a.right.right ||
                a.right.right.height < a.right.left.height) && eg(this, a.right), dg(this, a));
            b = a.left ? a.left.height : 0;
            e = a.right ? a.right.height : 0;
            a.height = Math.max(b, e) + 1;
            return a.parent
        }, b)
    }, dg = function (a, b) {
        ag(b) ? (b.parent.left = b.right, b.right.parent = b.parent) : bg(b) ? (b.parent.right = b.right, b.right.parent = b.parent) : (a.oa = b.right, a.oa.parent = k);
        var c = b.right;
        b.right = b.right.left;
        b.right != k && (b.right.parent = b);
        c.left = b;
        b.parent = c
    }, eg = function (a, b) {
        ag(b) ? (b.parent.left = b.left, b.left.parent = b.parent) : bg(b) ? (b.parent.right =
            b.left, b.left.parent = b.parent) : (a.oa = b.left, a.oa.parent = k);
        var c = b.left;
        b.left = b.left.right;
        b.left != k && (b.left.parent = b);
        c.right = b;
        b.parent = c
    }, $f = function (a, b) {
        if (!b)return a.eb;
        var c = b;
        Xf(a, function (a) {
            var b = k;
            a.left && (b = c = a.left);
            return b
        }, b);
        return c
    }, Zf = function (a, b) {
        if (!b)return a.cb;
        var c = b;
        Xf(a, function (a) {
            var b = k;
            a.right && (b = c = a.right);
            return b
        }, b);
        return c
    }, Wf = function (a, b) {
        this.value = a;
        this.parent = b ? b : k
    };
    Wf.prototype.left = k;
    Wf.prototype.right = k;
    Wf.prototype.height = 1;
    var bg = function (a) {
        return!!a.parent && a.parent.right == a
    }, ag = function (a) {
        return!!a.parent && a.parent.left == a
    };
    var fg = 0;
    fg++;
    fg++;
    var gg = function (a, b) {
        this.f = M("chat.client.ServerMessageSender");
        this.Hf = a;
        this.Ei = b
    };
    A(gg, Eb);
    gg.prototype.g = function () {
        gg.z.g.call(this);
        this.Ei = this.Hf = k
    };
    gg.prototype.send = function (a) {
        log(this.f, "send: " + a.toString());
        this.Ei(a)
    };
    "ScriptEngine"in s && "JScript" == s.ScriptEngine() && (s.ScriptEngineMajorVersion(), s.ScriptEngineMinorVersion(), s.ScriptEngineBuildVersion());
    C && F(8);
    var hg = function (a) {
        this.a = a || ["p"]
    };
    A(hg, U);
    V.p = hg;
    r = hg.prototype;
    r.o = function () {
        return"p"
    };
    r.i = function () {
        return this.a[1]
    };
    r.O = function (a) {
        this.a[1] = a
    };
    r.Sc = function () {
        return this.a[3]
    };
    r.Rc = function (a) {
        this.a[3] = a
    };
    r.fb = function () {
        return this.a[4]
    };
    r.qh = function (a) {
        this.a[4] = a
    };
    r.nh = function () {
        return this.a[5]
    };
    r.oh = function (a) {
        this.a[5] = a
    };
    r.Qc = function () {
        return this.a[6]
    };
    r.Nd = function (a) {
        this.a[6] = a
    };
    var ig = function (a) {
        this.a = a || ["ru"];
        this.a[15] = this.a[15] || []
    };
    A(ig, U);
    V.ru = ig;
    r = ig.prototype;
    r.o = function () {
        return"ru"
    };
    r.i = function () {
        return this.a[1]
    };
    r.O = function (a) {
        this.a[1] = a
    };
    r.gb = function () {
        return this.a[18]
    };
    r.Hc = function (a) {
        this.a[18] = a
    };
    r.Sb = function () {
        return this.a[3]
    };
    r.Bc = function (a) {
        this.a[3] = a
    };
    r.Qg = function (a) {
        this.a[4] = a
    };
    r.Ag = function () {
        return this.a[5]
    };
    r.Tg = function (a) {
        this.a[5] = a
    };
    r.Sg = function (a) {
        this.a[6] = a
    };
    r.Ug = function (a) {
        this.a[7] = a
    };
    r.rb = function () {
        return this.a[8]
    };
    r.Ed = function (a) {
        this.a[8] = a
    };
    r.Wg = function (a) {
        this.a[9] = a
    };
    r.Bg = function () {
        return this.a[10]
    };
    r.Vg = function (a) {
        this.a[10] = a
    };
    r.Cg = function () {
        return this.a[11]
    };
    r.Ia = function () {
        return this.a[12]
    };
    r.Cc = function (a) {
        this.a[12] = a
    };
    r.Dc = function () {
        return this.a[13]
    };
    r.Qe = function (a) {
        this.a[13] = a
    };
    r.zg = function () {
        return this.a[14]
    };
    r.Pe = function () {
        return this.a[15]
    };
    r.Je = function (a) {
        this.a[20] = a
    };
    r.Dg = function () {
        return this.a[21]
    };
    r.Fg = function (a) {
        this.a[21] = a
    };
    r.Gd = function () {
        return this.a[22]
    };
    var jg = function (a) {
        this.a = a || ["vrq"];
        this.a[2] = this.a[2] || []
    };
    A(jg, U);
    V.vrq = jg;
    jg.prototype.o = function () {
        return"vrq"
    };
    jg.prototype.Wa = function () {
        return this.a[1]
    };
    var kg = function (a) {
        this.a = a || ["vrqr"];
        this.a[2] = this.a[2] || []
    };
    A(kg, U);
    V.vrqr = kg;
    kg.prototype.o = function () {
        return"vrqr"
    };
    kg.prototype.Wa = function () {
        return this.a[1]
    };
    var lg = function (a) {
        this.a = a || []
    };
    A(lg, U);
    lg.prototype.Jh = function () {
        return this.a[5]
    };
    lg.prototype.Mh = function () {
        return this.a[8]
    };
    var mg = function (a, b) {
        this.Gc = a;
        this.ad = b
    };
    r = mg.prototype;
    r.Gc = "";
    r.ad = "";
    r.Le = "";
    r.wb = 4;
    r.Aj = m;
    r.Me = m;
    r.Gg = "none";
    r.Ih = k;
    r.tf = k;
    r.$d = k;
    r.of = k;
    r.Ea = function () {
        return this.Gg
    };
    r.Ub = function (a) {
        return a.Le != this.Le || a.wb != this.wb || a.Ea() != this.Ea() || a.Gc != this.Gc || (7 == a.wb || 18 == a.wb) != (7 == this.wb || 18 == this.wb) || a.Me != this.Me ? m : j
    };
    r.i = function () {
        return new zf("voice.google.com", this.ad, k)
    };
    r.Jh = function () {
        return this.Ih
    };
    r.Mh = function () {
        return this.$d
    };
    r.mf = function (a, b) {
        var c = ("" + a.ad).toLowerCase(), d = ("" + b.ad).toLowerCase();
        return c < d ? -1 : c == d ? 0 : 1
    };
    var ng = function (a, b, c) {
        this.f = M("chat.model.Contact");
        this.Ja = a.get("ja");
        this.G = a.get("ga");
        this.xb = Ef(b);
        this.Gi = c;
        this.Id = this.Cd = 0;
        this.Fi = k;
        this.f.info("Creating contact for '" + b + "' (" + c + ")");
        this.ac = a.get("ka");
        this.Vb = a.get("ha");
        Qb(this.Vb, "vrqr", this.gi, m, this)
    };
    A(ng, T);
    ng.prototype.g = function () {
        ng.z.g.call(this);
        Ub(this.Vb, "vrqr", this.gi, m, this)
    };
    var og = function (a, b) {
        H.call(this, a);
        this.Ne = b
    };
    A(og, H);
    var pg = function (a) {
        switch (a) {
            case "u":
                return 1;
            case "i":
                return 3;
            case "b":
                return 4;
            default:
                return 5
        }
    };
    r = ng.prototype;
    r.Xb = "u";
    r.ll = 0;
    r.Fd = [];
    r.Oe = [];
    r.sg = "a";
    r.Eg = m;
    r.Ke = m;
    r.i = function () {
        return this.xb
    };
    r.Ia = function () {
        return this.Gi
    };
    r.Cc = function (a) {
        this.Gi = a
    };
    r.Qc = function () {
        return this.ll
    };
    r.cf = function () {
        return this.Fi
    };
    r.Ve = function (a) {
        this.Fi = a
    };
    r.Sc = function () {
        return this.Xb
    };
    r.sb = function (a) {
        var b = this.Ba != a;
        this.Ba = a;
        b && (delete this.Jm, W(this, "t"));
        return b
    };
    r.Bc = function (a) {
        var b = this.Ti != a;
        this.Ti = a;
        b && W(this, "n");
        return b
    };
    r.Sb = function () {
        var a = this.xb.toString(), b;
        if (!(b = this.G.Mg))if (b = Ef(a), !(b = t(b.bb) && ("bot.talk.google.com" == b.Q() || "prom.corp.google.com" == b.Q() || "appspot.com" == b.Q() || /promchat\.corp\.google\.com$/.test(b.Q()) || /appspotchat\.com$/.test(b.Q()))))if (b = a, x(b) && (b = Ef(b)), !(b = "public.talk.google.com" == b.Q()))if (!(b = Ff(a)))a = Ef(a), b = Bf.test(a.Q());
        return b ? "" : this.Ti
    };
    r.Hc = function (a) {
        var b = this.pd != a;
        this.pd = a;
        b && W(this, "q");
        return b
    };
    r.gb = function () {
        return u(this.pd) ? this.pd : k
    };
    var qg = function (a, b) {
        var c = a.ml != b;
        a.ml = b;
        c && W(a, "r");
        return c
    };
    r = ng.prototype;
    r.Ef = function (a) {
        var b = this.rg != a;
        this.rg = a;
        b && W(this, "j");
        return b
    };
    r.Pg = function () {
        return!t(this.rg) ? "images/nopicture" + (C ? ".gif" : ".png") : this.rg
    };
    r.Fg = function (a) {
        var b = this.yj != a;
        this.yj = a;
        b && W(this, "y");
        return b
    };
    r.Dg = function () {
        return this.yj
    };
    r.Ed = function (a) {
        var b = this.xj != a;
        this.xj = a;
        b && W(this, "o");
        return b
    };
    r.rb = function () {
        return this.xj
    };
    r.Qe = function (a) {
        var a = t(a) ? a : 1, b = this.Od != a;
        this.Od = a;
        b && W(this, "E");
        return b
    };
    r.Dc = function () {
        return this.Od
    };
    r.We = function (a) {
        var b = this.sg != a;
        this.sg = a;
        b && W(this, "m");
        return b
    };
    r.af = function () {
        return this.sg
    };
    r.Je = function (a) {
        return this.Id < a ? (this.Id = a, W(this, "s"), j) : m
    };
    r.toString = function () {
        return this.xb.toString()
    };
    var W = function (a, b) {
        a.dispatchEvent(new og(b, a))
    };
    ng.prototype.gi = function (a) {
        a = ze(a.Ha);
        if (a.Wa() == this.xb.toString()) {
            if (!a.qf) {
                a.qf = [];
                for (var b = 0; b < a.a[2].length; b++)a.qf[b] = new lg(a.a[2][b])
            }
            for (var a = a.qf, c = m, b = this.Fd, d = 0; d < a.length; d++) {
                var e = a[d];
                if (0 == e.a[1])for (var g = e.a[0], h = e.Jh(), l = e.a[7], p = e.Mh(), e = e.a[9], n = 0; n < b.length; n++)if (b[n].ad == g) {
                    g = b[n];
                    n = m;
                    g.Ih = h;
                    if (!t(g.tf) || g.tf != l)g.tf = l, n = j;
                    if (!t(g.$d) || g.$d != p)g.$d = p, n = j;
                    if (!t(g.of) || g.of != e)g.of = e, n = j;
                    c |= n;
                    break
                }
            }
            c && W(this, "z")
        }
    };
    var rg = function (a) {
        this.sd = a
    }, sg = function (a, b) {
        this.Rk = a;
        this.R = b
    }, tg = {jn:"1", on:"p", kn:"h", gn:"b", pn:"j", mn:"nv", vn:"ao", rn:"aw", un:"ai", sn:"ab", qn:"aa", tn:"ac", yn:"sn", zn:"st", xn:"sf", wn:"sb", An:"wb", hn:"bp", nn:"po", ln:"lct"}, ug = function (a, b) {
        var c = a.R.get("1", 0);
        switch (b.rb()) {
            case "P":
                c += a.R.get("p", 0);
                break;
            case "H":
                c += a.R.get("h", 0);
                break;
            case "B":
                c += a.R.get("b", 0)
        }
        b.Ke && (c += a.R.get("j", 0));
        b.Te && (c += a.R.get("nv", 0));
        switch (b.Sc()) {
            case "u":
                c += a.R.get("ao", 0);
                break;
            case "w":
                c += a.R.get("aw",
                    0);
                break;
            case "i":
                c += a.R.get("ai", 0);
                break;
            case "b":
                c += a.R.get("ab", 0);
                break;
            case "a":
                c += a.R.get("aa", 0);
                break;
            case "c":
                c += a.R.get("ac", 0)
        }
        switch (b.Dc()) {
            case 1:
                c += a.R.get("sn", 0);
                break;
            case 2:
                c += a.R.get("st", 0);
                break;
            case 3:
                c += a.R.get("sf", 0);
                break;
            case 4:
                c += a.R.get("sb", 0)
        }
        c += b.Cd * a.R.get("wb", 0);
        c += b.Id * a.R.get("lct", 0);
        Ff(b.xb.toString()) && (c += a.R.get("po", 0));
        return a.Rk ? 0 < c ? 1 : 0 : c
    }, vg = function (a) {
        for (var a = a.split(" "), b = [], c = 0; c < a.length; ++c)if (t(a[c]) && "" != a[c]) {
            var d, e = a[c], g = e.length - 1;
            "[" ==
                e.charAt(0) && "]" == e.charAt(g) ? d = j : "{" == e.charAt(0) && "}" == e.charAt(g) ? d = m : f(Error("evaluations must start with '[' or '{', end with ']' or '}', but now it's " + e.charAt(0) + " and " + e.charAt(g)));
            for (var e = e.slice(1, g).split(","), g = new J, h = 0; h < e.length; ++h) {
                var l = e[h].split(":");
                2 != l.length && f(Error("missing ':' between name and value."));
                var p = m, n;
                for (n in tg)tg[n] == l[0] && (p = j);
                p || f(Error("Invaid evaluation field: " + l[0]));
                p = parseInt(l[1], 10);
                isNaN(p) && f(Error("Invaid evaluation weight: " + l[1]));
                g.set(l[0],
                    p)
            }
            0 < g.P() && b.push(new sg(d, g))
        }
        return new rg(b)
    };
    rg.prototype.mf = function (a, b) {
        if (a == b)return 0;
        for (var c = 0; c < this.sd.length; ++c) {
            var d = ug(this.sd[c], a), e = ug(this.sd[c], b);
            if (d > e)return-1;
            if (d < e)return 1
        }
        c = b.Ba;
        if (t(a.Ba) && t(c) && (c = a.Ba.toLowerCase() == b.Ba.toLowerCase() ? 0 : a.Ba.toLowerCase() > b.Ba.toLowerCase() ? 1 : -1, 0 != c))return c;
        c = a.i().toString();
        d = b.i().toString();
        return c == d ? 0 : c > d ? 1 : -1
    };
    var wg = function () {
        this.Fh = m
    };
    A(wg, T);
    var yg = function (a) {
        this.Fh = m;
        this.kf = [];
        this.G = a.get("ga");
        this.df = (this.rh = 0 <= this.G.Yb) ? this.G.Yb : 10;
        this.Ej = vg(Ce(Je(this.G)));
        a = Je(this.G);
        this.yh = vg(a.a[14]);
        this.Dj = vg("{ac:5,aa:4,ab:3,ai:2,aw:1,ao:0}");
        this.bf = this.yh;
        this.Yd = [];
        this.cc = k;
        for (var a = Je(this.G).a[12].split("\n"), b = 0; b < a.length; ++b)this.Yd.push(vg(Ce(Je(this.G)) + " " + a[b]));
        this.Yd.push(vg(""));
        this.$b = m;
        xg(this)
    };
    A(yg, wg);
    yg.prototype.g = function () {
        yg.z.g.call(this);
        ce.clearTimeout(this.cc);
        this.cc = k
    };
    yg.prototype.Tb = function () {
        ce.clearTimeout(this.cc);
        this.cc = k;
        if (this.$b) {
            for (var a = [], b = 0; b < this.Da.length - 1; ++b) {
                var c = this.gc[b], d = 0, e = this;
                cg(this.Da[b], function (b) {
                    ++d;
                    var h;
                    if (h = d <= c) {
                        a:{
                            h = e.Ej;
                            for (var l = 0; l < h.sd.length; ++l)if (0 >= ug(h.sd[l], b)) {
                                h = m;
                                break a
                            }
                            h = j
                        }
                        h = h && t(b.Ia())
                    }
                    return h ? (a.push(b), m) : j
                })
            }
            this.$b = m;
            b = a.sort(y(this.bf.mf, this.bf));
            Pa(this.kf, b) || (this.kf = b, this.dispatchEvent("G"))
        }
        return this.kf
    };
    var zg = function (a) {
        for (var a = a.Tb(), b = [], c = 0; c < a.length; ++c)b.push(a[c].Ia());
        return b
    };
    yg.prototype.Mi = function (a, b) {
        for (var c = b ? b : 0; c < a.length; ++c)if (Ag(this, a[c]), 0 == (c + 1) % 25 && c + 1 < a.length) {
            ee(y(this.Mi, this, a, c + 1), 1);
            return
        }
        this.Tb()
    };
    var Bg = function (a) {
        var b = parseInt(a, 10);
        isNaN(b) && f(Error("Not a number: " + a));
        return b
    }, xg = function (a) {
        for (var b = Je(a.G).a[13], c = a.df, d = b.split(","), e = [], g = 0, h = -1, l = 0; l < d.length; ++l)if ("*" == d[l])-1 != h && f(Error("multiple '*' components found in spec: " + b)), h = l, e.push(0); else {
            var p = d[l].split("/");
            3 != p.length && f(Error("component specs must be '*' or of the form a/b/c: " + d[l]));
            e.push(Math.max(Bg(p[0]), Math.floor(c * Bg(p[1]) / Bg(p[2]))));
            g += e[l]
        }
        -1 == h && f(Error("no '*' component found in spec: " + b));
        g >
            c && f(Error("total component sizes adds to more than 100%"));
        e[h] = c - g;
        e.push(Number.MAX_VALUE);
        a.gc = e;
        a.Da = [];
        for (b = 0; b < a.gc.length; ++b)a.Da.push(new Vf(y(a.Yd[b].mf, a.Yd[b])))
    }, Ag = function (a, b) {
        a.$b = j;
        for (var c = 0; !(a.Da[c].add(b), a.Da[c].P() <= a.gc[c]); ++c)Cg(a, c, b) && (b = Dg(a, c))
    }, Eg = function (a, b) {
        a.$b = j;
        for (var c = 0; ; ++c)if (a.Da[c].P() <= a.gc[c]) {
            a.Da[c].remove(b);
            break
        } else if (Cg(a, c, b)) {
            var d = Dg(a, c);
            a.Da[c].remove(b);
            b = d
        } else a.Da[c].remove(b)
    }, Cg = function (a, b, c) {
        var d = m, e = a.gc[b], g = 0;
        cg(a.Da[b],
            function (a) {
                if (g < e && c == a)return d = j;
                ++g;
                return m
            });
        return d
    }, Dg = function (a, b) {
        var c = k, d = a.gc[b], e = 0;
        cg(a.Da[b], function (a) {
            if (e == d)return c = a, j;
            ++e;
            return m
        });
        return c
    }, Fg = function (a, b) {
        t(b) && Eg(a, b)
    }, Gg = function (a, b, c) {
        t(b) && (Ag(a, b), !a.Fh && c && fa(a.cc) && (a.cc = ee(a.Tb, 500, a)))
    };
    var Hg = function (a) {
        this.a = a || ["cs"]
    };
    A(Hg, U);
    V.cs = Hg;
    r = Hg.prototype;
    r.o = function () {
        return"cs"
    };
    r.i = function () {
        return this.a[1]
    };
    r.O = function (a) {
        this.a[1] = a
    };
    r.af = function () {
        return this.a[2]
    };
    r.We = function (a) {
        this.a[2] = a
    };
    var Ig = function (a) {
        this.a = a || ["cds"]
    };
    A(Ig, U);
    V.cds = Ig;
    Ig.prototype.o = function () {
        return"cds"
    };
    Ig.prototype.jh = function () {
        return this.a[1]
    };
    Ig.prototype.kh = function (a) {
        this.a[1] = a
    };
    var Jg = function (a) {
        this.a = a || ["cdr"];
        this.a[2] = this.a[2] || []
    };
    A(Jg, U);
    V.cdr = Jg;
    Jg.prototype.o = function () {
        return"cdr"
    };
    Jg.prototype.jh = function () {
        return this.a[1]
    };
    Jg.prototype.kh = function (a) {
        this.a[1] = a
    };
    var Kg = function (a) {
        this.a = a || []
    };
    A(Kg, U);
    r = Kg.prototype;
    r.i = function () {
        return this.a[0]
    };
    r.O = function (a) {
        this.a[0] = a
    };
    r.cf = function () {
        return this.a[1]
    };
    r.Ve = function (a) {
        this.a[1] = a
    };
    r.Sc = function () {
        return this.a[2]
    };
    r.Rc = function (a) {
        this.a[2] = a
    };
    r.Qc = function () {
        return this.a[3]
    };
    r.Nd = function (a) {
        this.a[3] = a
    };
    var Lg = function (a) {
        this.a = a || []
    };
    A(Lg, U);
    var Mg = function (a) {
        this.a = a || ["cgu"]
    };
    A(Mg, U);
    V.cgu = Mg;
    r = Mg.prototype;
    r.o = function () {
        return"cgu"
    };
    r.i = function () {
        return this.a[1]
    };
    r.O = function (a) {
        this.a[1] = a
    };
    r.gb = function () {
        return this.a[2]
    };
    r.Hc = function (a) {
        this.a[2] = a
    };
    r.Gd = function () {
        return this.a[3]
    };
    var Ng = function (a) {
        this.a = a || ["rf"]
    };
    A(Ng, U);
    V.rf = Ng;
    Ng.prototype.o = function () {
        return"rf"
    };
    var Og = function (a) {
        this.a = a || ["otr"];
        this.a[1] = this.a[1] || []
    };
    A(Og, U);
    V.otr = Og;
    Og.prototype.o = function () {
        return"otr"
    };
    var Qg = function (a) {
        if (!a.Cf) {
            a.Cf = [];
            for (var b = 0; b < a.a[1].length; b++)a.Cf[b] = new Pg(a.a[1][b])
        }
        return a.Cf
    }, Pg = function (a) {
        this.a = a || []
    };
    A(Pg, U);
    Pg.prototype.i = function () {
        return this.a[0]
    };
    Pg.prototype.O = function (a) {
        this.a[0] = a
    };
    Pg.prototype.ef = function () {
        return this.a[1]
    };
    var Rg = function (a) {
        this.a = a || ["rd"]
    };
    A(Rg, U);
    V.rd = Rg;
    Rg.prototype.o = function () {
        return"rd"
    };
    Rg.prototype.i = function () {
        return this.a[1]
    };
    Rg.prototype.O = function (a) {
        this.a[1] = a
    };
    var Sg = function (a) {
        this.a = a || ["vc"]
    };
    A(Sg, U);
    V.vc = Sg;
    r = Sg.prototype;
    r.o = function () {
        return"vc"
    };
    r.i = function () {
        return this.a[1]
    };
    r.O = function (a) {
        this.a[1] = a
    };
    r.getName = function () {
        return this.a[2]
    };
    r.sb = function (a) {
        this.a[2] = a
    };
    r.hi = function () {
        return this.a[3]
    };
    r.Rg = function (a) {
        this.a[3] = a
    };
    var Ug = function (a, b) {
        this.wd = a || k;
        this.bg = !!b;
        this.l = new J;
        this.k = new Tg("", i);
        this.k.next = this.k.ya = this.k
    }, Wg = function (a, b) {
        var c = a.l.get(b);
        c && a.bg && (c.remove(), Vg(a, c));
        return c
    };
    r = Ug.prototype;
    r.get = function (a, b) {
        var c = Wg(this, a);
        return c ? c.value : b
    };
    r.set = function (a, b) {
        var c = Wg(this, a);
        c ? c.value = b : (c = new Tg(a, b), this.l.set(a, c), Vg(this, c))
    };
    r.shift = function () {
        return Xg(this, this.k.next)
    };
    r.pop = function () {
        return Xg(this, this.k.ya)
    };
    r.remove = function (a) {
        return(a = this.l.get(a)) ? (this.removeNode(a), j) : m
    };
    r.removeNode = function (a) {
        a.remove();
        this.l.remove(a.key)
    };
    r.P = function () {
        return this.l.P()
    };
    r.Va = function () {
        return this.l.Va()
    };
    r.Ma = function () {
        return this.map(function (a, b) {
            return b
        })
    };
    r.da = function () {
        return this.map(function (a) {
            return a
        })
    };
    r.contains = function (a) {
        return this.some(function (b) {
            return b == a
        })
    };
    r.W = function (a) {
        return this.l.W(a)
    };
    r.clear = function () {
        Yg(this, 0)
    };
    r.forEach = function (a, b) {
        for (var c = this.k.next; c != this.k; c = c.next)a.call(b, c.value, c.key, this)
    };
    r.map = function (a, b) {
        for (var c = [], d = this.k.next; d != this.k; d = d.next)c.push(a.call(b, d.value, d.key, this));
        return c
    };
    r.some = function (a, b) {
        for (var c = this.k.next; c != this.k; c = c.next)if (a.call(b, c.value, c.key, this))return j;
        return m
    };
    r.every = function (a, b) {
        for (var c = this.k.next; c != this.k; c = c.next)if (!a.call(b, c.value, c.key, this))return m;
        return j
    };
    var Vg = function (a, b) {
        a.bg ? (b.next = a.k.next, b.ya = a.k, a.k.next = b, b.next.ya = b) : (b.ya = a.k.ya, b.next = a.k, a.k.ya = b, b.ya.next = b);
        a.wd != k && Yg(a, a.wd)
    }, Yg = function (a, b) {
        for (var c = a.l.P(); c > b; c--)a.removeNode(a.bg ? a.k.ya : a.k.next)
    }, Xg = function (a, b) {
        a.k != b && a.removeNode(b);
        return b.value
    }, Tg = function (a, b) {
        this.key = a;
        this.value = b
    };
    Tg.prototype.remove = function () {
        this.ya.next = this.next;
        this.next.ya = this.ya;
        delete this.ya;
        delete this.next
    };
    var Zg = function () {
        this.Ya = []
    };
    Zg.prototype.k = 0;
    Zg.prototype.nb = 0;
    var $g = function (a) {
        if (a.k != a.nb) {
            var b = a.Ya[a.k];
            delete a.Ya[a.k];
            a.k++;
            return b
        }
    };
    r = Zg.prototype;
    r.P = function () {
        return this.nb - this.k
    };
    r.Va = function () {
        return 0 == this.nb - this.k
    };
    r.clear = function () {
        this.nb = this.k = this.Ya.length = 0
    };
    r.contains = function (a) {
        return 0 <= Fa(this.Ya, a)
    };
    r.remove = function (a) {
        a = Fa(this.Ya, a);
        if (0 > a)return m;
        if (a == this.k)$g(this); else {
            var b = this.Ya;
            Da(b.length != k);
            B.splice.call(b, a, 1);
            this.nb--
        }
        return j
    };
    r.da = function () {
        return this.Ya.slice(this.k, this.nb)
    };
    M("talk.media.CallEvent");
    var ah = function (a) {
        this.a = a || ["ft:ccr"];
        this.a[1] = this.a[1] || []
    };
    A(ah, U);
    V["ft:ccr"] = ah;
    ah.prototype.o = function () {
        return"ft:ccr"
    };
    var bh = m, ch = function (a) {
        a.match(/[\d]+/g).length = 3
    };
    if (navigator.plugins && navigator.plugins.length) {
        var dh = navigator.plugins["Shockwave Flash"];
        dh && (bh = j, dh.description && ch(dh.description));
        navigator.plugins["Shockwave Flash 2.0"] && (bh = j)
    } else if (navigator.mimeTypes && navigator.mimeTypes.length) {
        var eh = navigator.mimeTypes["application/x-shockwave-flash"];
        (bh = eh && eh.enabledPlugin) && ch(eh.enabledPlugin.description)
    } else try {
        var fh = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.7"), bh = j;
        ch(fh.GetVariable("$version"))
    } catch (gh) {
        try {
            fh = new ActiveXObject("ShockwaveFlash.ShockwaveFlash.6"),
                bh = j
        } catch (hh) {
            try {
                fh = new ActiveXObject("ShockwaveFlash.ShockwaveFlash"), bh = j, ch(fh.GetVariable("$version"))
            } catch (ih) {
            }
        }
    }
    ;
    var jh = function () {
    }, kh = k;
    kh == k && (kh = new jh);
    M("talk.media.Client3DDetector");
    jh.prototype.reset = function () {
    };
    M("talk.media.FlashDetector");
    M("talk.media.RendererDetector");
    new uc([4, 2]);
    M("talk.media.CallManager");
    var lh = function (a) {
        this.G = a.get("ga");
        var b = this.G.Ig ? "u" : this.G.Hg;
        this.f = M("chat.model.Me");
        this.ac = a.get("ka");
        this.ld = new uc;
        this.Jd = "a" == b || "b" == b ? b : "a";
        this.Hk = "";
        this.Kk = j
    };
    A(lh, T);
    lh.prototype.g = function () {
        lh.z.g.call(this);
        this.ld.clear()
    };
    lh.prototype.Rc = function (a, b) {
        return("a" == a || "b" == a) && this.Jd != a ? (this.Jd = a, this.dispatchEvent("$"), (!u(b) || b) && this.ac.Hf(["pu", this.Jd, this.Hk, this.ld.contains("invisible")]), j) : m
    };
    var mh = function (a) {
        if (a.qj()) {
            if (a.Kk || a.ld.contains("u"))return"u";
            if (a.ld.contains("i"))return"i"
        } else return k;
        return"a"
    };
    lh.prototype.qj = function () {
        return this.ld.contains("initialized")
    };
    var nh = function (a) {
        lh.call(this, a)
    };
    A(nh, lh);
    var oh = function (a) {
        this.a = a || ["sus"];
        this.a[1] = this.a[1] || []
    };
    A(oh, U);
    V.sus = oh;
    oh.prototype.o = function () {
        return"sus"
    };
    var ph = function (a) {
        this.a = a || []
    };
    A(ph, U);
    ph.prototype.getName = function () {
        return this.a[0]
    };
    ph.prototype.sb = function (a) {
        this.a[0] = a
    };
    var qh = function (a) {
        this.f = M("chat.model.Me");
        this.ac = a.get("ka");
        this.Jg = new nh(a);
        Fb(this, this.Jg);
        this.lc = new J;
        var b = a.get("ga");
        this.Ja = a.get("ja");
        this.sk = He(b).i();
        this.pd = He(b).gb();
        He(b);
        this.Re = a;
        this.Ja.tb.contains(Tf.rk);
        this.Ja.tb.contains(Tf.qk)
    };
    A(qh, T);
    qh.prototype.g = function () {
        qh.z.g.call(this);
        this.lc.clear()
    };
    qh.prototype.i = function () {
        return this.sk
    };
    qh.prototype.gb = function () {
        return this.pd
    };
    var sh = function (a, b, c) {
        var d = !a.lc.W(b);
        c != a.lc.get(b) && (a.lc.set(b, c), a.dispatchEvent(new rh(b, c, m, d)), a.dispatchEvent(new rh(b, c, m, d, ["ga", b].join("_"))))
    }, th = function (a) {
        qh.call(this, a);
        this.Nh = m;
        this.r = new R(this);
        S(this.r, a.get("ha"), "sus", this.al)
    };
    A(th, qh);
    th.prototype.g = function () {
        G(this.r.I());
        th.z.g.call(this)
    };
    th.prototype.al = function (a) {
        a = new oh(a.Ha);
        if (!a.Af) {
            a.Af = [];
            for (var b = 0; b < a.a[1].length; b++)a.Af[b] = new ph(a.a[1][b])
        }
        a = a.Af;
        for (b = 0; b < a.length; b++) {
            var c = a[b];
            sh(this, c.getName(), c.a[1])
        }
        this.Nh || (this.Nh = j, this.dispatchEvent("fa"))
    };
    var rh = function (a, b, c, d, e) {
        H.call(this, e || "ga");
        this.name = a;
        this.value = b;
        this.Nk = c;
        this.Mk = d
    };
    A(rh, H);
    var uh = function (a) {
        this.f = M("chat.model.ContactSet");
        this.Re = a;
        this.Ja = a.get("ja");
        this.ac = a.get("ka");
        this.ub = a.get("na");
        this.G = a.get("ga");
        this.pa = {};
        this.yb = new J;
        this.Ie = {};
        this.Qa = {};
        this.Dd = new J;
        this.ug = new J;
        this.Rb = new J;
        this.He = (new Date).getTime();
        this.Pa = {};
        this.Oc = 0;
        this.Qb = {};
        this.M = new yg(a);
        this.lf = new Ug;
        this.r = new R(this);
        Fb(this, this.r);
        a = a.get("ha");
        S(this.r, a, "otr", this.$j);
        S(this.r, a, "ru", this.bk);
        S(this.r, a, "rd", this.ak);
        S(this.r, a, "vc", this.ck);
        S(this.r, a, "cds", this.Yj);
        S(this.r,
            a, "cgu", this.Zj);
        this.Ja.tb.contains(Tf.Hd) || S(this.r, this.M, "G", this.ek);
        S(this.r, a, "cs", this.Xj);
        S(this.r, this.ub, "ga_rosterSize", this.fk);
        this.Ja.tb.contains(Tf.Hd) || S(this.r, this.ub, "ga_rosterSortOption", this.gk);
        this.ee = new de(1E3);
        Fb(this, this.ee);
        S(this.r, this.ee, "tick", this.dk, m, this);
        this.ee.start()
    };
    A(uh, T);
    var vh = function (a) {
        this.oj = a
    };
    vh.prototype.getName = function (a) {
        return"homepage" == a.gh ? this.xg || this.yg || this.di || this.oj : this.yg || this.di || this.xg || this.oj
    };
    uh.prototype.g = function () {
        uh.z.g.call(this);
        for (var a in this.pa)G(this.pa[a]);
        for (var b = this.yb.da(), c = 0; c < b.length; ++c)G(b[c]);
        this.yb.clear();
        for (a in this.Pa)G(this.Pa[a]);
        this.Qa = {};
        this.pa = {};
        this.Pa = {};
        this.Qb = {};
        G(this.M);
        this.M = k;
        this.lf.clear();
        this.Oc = 0
    };
    var wh = function (a, b) {
        H.call(this, a);
        this.Ne = b
    };
    A(wh, H);
    uh.prototype.Yj = function (a) {
        this.ac.send(xh(this, new Ig(a.Ha)))
    };
    var xh = function (a, b) {
        var c = new Jg;
        c.kh(b.jh());
        var d = [], e;
        for (e in a.pa) {
            var g = a.pa[e], h = new Kg;
            h.O(If(g.i()).toString());
            t(g.cf()) && h.Ve(g.cf());
            h.Rc(pg(g.Xb));
            h.Nd(g.Qc());
            d.push(h)
        }
        d = d || [];
        c.a[2] = [];
        for (e = 0; e < d.length; e++)c.a[2][e] = d[e].a;
        a.Ja.tb.contains(Tf.Cj) && (d = new Lg, e = a.ub.Jg, g = mh(e), g != k && (d.a[1] = pg(g)), d.a[0] = pg(e.Jd), c.a[3] = d ? d.a : d);
        return c
    }, X = function (a, b, c) {
        var b = b instanceof zf ? b.toString() : b, d = Hf(Ef(b)) ? b : Gf(b), c = c || a.Qb[b] || a.Qb[d], e = a.pa[b] || a.pa[d];
        if (!e) {
            var g;
            a.yb.W(b) ? g = b :
                a.yb.W(d) && (g = d);
            t(g) && (e = a.yb.get(g), a.yb.remove(g), a.pa[g] = e, a.Oc++, a.dispatchEvent(new wh("ia", e)))
        }
        !e && c && (e = a.Pa[b] || a.Pa[d], e || (e = new ng(a.Re, b), e.Ef(yh(a, "7b8ac4c3c5b468f9ceabd8eea8a9df61574ad997")), a.Ie[b] = new vh(b), a.Pa[b] = e, a.Oc++, a.dispatchEvent(new wh("ia", e))));
        return e
    }, zh = function (a) {
        var b = parseInt(a.ub.lc.get("rosterSize"), 10);
        return"number" == typeof b && !isNaN(b) ? b : a.G.Og
    };
    r = uh.prototype;
    r.fk = function (a) {
        if (a.Nk || a.Mk)0 <= this.G.Yb ? a = this.G.Yb : (a = zh(this), a = xe[a]), this.ac.Hf(["qs", a]);
        if (0 > this.G.Yb) {
            var a = this.M, b = xe[zh(this)], c = this.pa;
            if (!(a.rh || a.df == b)) {
                0 > b && f(Error("negative quick list size"));
                250 < b && (b = 250);
                a.$b = j;
                a.df = b;
                xg(a);
                var b = [], d;
                for (d in c)b.push(c[d]);
                a.Mi(b)
            }
        }
    };
    r.gk = function () {
        var a = this.M, b = this.ub.lc.get("rosterSortOption");
        if ("MOST_POPULAR" == b || "ALPHABETICAL" == b)a.bf = "MOST_POPULAR" == b ? a.yh : a.Dj, a.$b = j, a.Tb()
    };
    r.dk = function () {
        var a;
        if (t(this.Rb)) {
            a = Math.floor(((new Date).getTime() - this.He) / 1E3);
            for (var b = a - 3, c = this.Rb.Ma(), d = 0, e = 0; e < c.length; ++e)c[e] <= b ? this.Rb.remove(c[e]) : d += this.Rb.get(c[e], 0);
            b = this.Oc;
            this.f.info(a + " secs after creation, the ru/pr messages count in the past 3 secs is " + a + " for " + b + " contacts");
            d < 2 * b || 30 < a ? (this.f.info("Sending ROSTER_STABLE message at " + a), this.dispatchEvent("ra"), this.Rb = k, a = m) : a = j
        } else a = m;
        a || G(this.ee)
    };
    r.bk = function (a) {
        var a = new ig(a.Ha), b, c = m, d = "" + a.Ia(), e = a.i(), g = -1;
        X(this, e) && X(this, e).Ia() != d && (c = j, g = X(this, e).Ia(), this.f.q('previous entry for "' + e + '" under shortId ' + g));
        var h = this.Rb;
        if (t(h)) {
            var l = Math.floor(((new Date).getTime() - this.He) / 1E3), p = h.get(l, 0);
            h.set(l, p + 1)
        }
        if (d in this.Qa && (b = this.Qa[d], e != b.i().toString()))(this.f.q("contact mismatch for shortId " + d + ": " + e + " != " + b.i().toString()), delete this.Qa[d], delete this.pa[b.i().toString()], delete this.Qb[b.i().toString()], this.ug.remove(b.gb()),
            h = this.M, t(b) && (Eg(h, b), h.Tb()), G(b), b = X(this, e), u(b)) ? (this.Qa[d] = b, delete this.Qa[b.Ia()], b.Cc(d)) : this.f.info("handleRosterUpdate_: no contact for shortId or jid " + d + " " + e);
        if (!u(b) && (this.Qb[e] = j, b = X(this, e, j)))this.Pa[b.i().toString()] = k, this.pa[e] = b, this.Qa[d] = b, b.Cc(d), d = Gf(e), this.Dd.W(d) && (h = this.Dd.get(d), Ah(this, b, h, m), this.Dd.remove(d));
        c && delete this.Qa[g];
        c = this.Ie[e];
        c.yg = a.a[2];
        c.xg = a.a[16];
        c.ql = a.a[17];
        Hf(Ef(e));
        Fg(this.M, b);
        c = b.sb(c.getName(this.G));
        e = c |= b.Bc(a.Sb() || a.i());
        c = b;
        g = a.Ag();
        c.Cd != g ? (c.Cd = g, W(c, "F"), c = j) : c = m;
        c |= e;
        e = a.gb();
        u(e) && (c |= b.Hc(e), this.ug.set(e, b));
        c |= qg(b, a.Gd() || m);
        g = a.Cg() || [];
        e = [];
        for (d = 0; d < g.length; d++)h = g[d], l = new mg(h[0], h[1]), l.Gg = h[2], l.Aj = Boolean(h[3]), l.Me = Boolean(h[4]), l.wb = h[5], l.Le = h[6], e.push(l);
        g = b;
        h = g.Fd.length != e.length;
        for (d = 0; d < e.length && !h; d++)h = e[d], l = g.Fd[d], h = h.Gc == l.Gc ? !h.Ub(l) : j;
        h ? (g.Fd = e, W(g, "A"), e = j) : e = m;
        e |= c;
        c = b;
        g = a.zg();
        g = Boolean(g);
        c.Eg != g ? (c.Eg = g, W(c, "k"), c = j) : c = m;
        e |= c;
        c = b;
        g = a.Pe();
        d = g == k || c.Oe.length != g.length;
        for (h =
                 0; h < g.length && !d; h++)g[h] != c.Oe[h] && (d = j);
        d && (c.Oe = g, W(c, "p"));
        c = e | d;
        this.f.info("Contactset: the groups for " + b.Sb() + " are " + a.Pe());
        e = c;
        c = b;
        g = a.a[19];
        g = Boolean(g);
        c.Ke != g ? (c.Ke = g, W(c, "B"), c = j) : c = m;
        e = c = e | c | b.Fg(a.Dg() || "");
        c = b;
        g = a.Bg() || "";
        d = c.vg != g;
        c.vg = g;
        d && W(c, "x");
        c = e | d;
        e = "N";
        switch (a.rb()) {
            case 1:
                e = "B";
                break;
            case 2:
                e = "H";
                break;
            case 3:
                e = "N";
                break;
            case 4:
                e = "P"
        }
        g = "B" == b.rb();
        b.Ed(e) && (g && b.Qe(a.Dc()), c = j);
        c |= b.Je(a.a[20]);
        e = !u(b.Dc());
        b.Qe(a.Dc()) && (e || this.dispatchEvent({type:"AS", Ne:b}), c = j);
        Gg(this.M, b, j);
        c && this.dispatchEvent(new wh("la", b))
    };
    r.ak = function (a) {
        var b = new Rg(a.Ha);
        if (a = X(this, b.i()) || this.Pa[b.i()])delete this.Qa[a.Ia()], delete this.pa[a.i().toString()], delete this.Pa[a.i().toString()], delete this.Qb[a.i().toString()], this.yb.set(b.i(), a), this.Oc--, b = this.M, t(a) && (Eg(b, a), b.Tb()), this.dispatchEvent(new wh("ja", a))
    };
    r.ck = function (a) {
        var b = new Sg(a.Ha);
        if (a = X(this, b.i())) {
            var c = this.Ie[b.i()];
            c.di = b.getName();
            Fg(this.M, a);
            c = a.sb(c.getName(this.G));
            b = (b = b.hi()) ? yh(this, b) : yh(this, "7b8ac4c3c5b468f9ceabd8eea8a9df61574ad997");
            c |= a.Ef(b);
            Gg(this.M, a);
            c && this.dispatchEvent(new wh("la", a))
        } else this.f.q("missing contact object for " + b.i())
    };
    r.Zj = function (a) {
        var a = new Mg(a.Ha), b = X(this, a.i());
        if (b) {
            Fg(this.M, b);
            var c;
            c = m | b.Hc(a.gb());
            c |= qg(b, a.Gd());
            Gg(this.M, b);
            c && this.dispatchEvent(new wh("la", b))
        } else this.f.q("missing contact object for " + a.i())
    };
    var yh = function (a, b) {
        var c = new N(b);
        c.Y || (c = new N(a.G.Se.toString() + "image?h=" + b), c.U.add("authuser", He(a.G).Ui()));
        return c.toString()
    };
    uh.prototype.$j = function (a) {
        for (var a = new Og(a.Ha), b = 0; b < Qg(a).length; ++b) {
            var c = Qg(a)[b], d = c.i(), e = X(this, d);
            e ? Ah(this, e, c, j) : this.Dd.set(Gf(d), c)
        }
    };
    var Ah = function (a, b, c, d) {
        Fg(a.M, b);
        var c = c.ef(), e = b.Te != c;
        b.Te = c;
        e && W(b, "v");
        e && d && a.dispatchEvent(new wh("la", b));
        Gg(a.M, b)
    };
    uh.prototype.ek = function () {
        Bh(this, zg(this.M))
    };
    var Bh = function (a, b) {
        a.lf.clear();
        for (var c = 0; c < b.length; c++)a.lf.set(b[c], j);
        a.dispatchEvent("qa")
    };
    uh.prototype.Xj = function (a) {
        var a = new Hg(a.Ha), b;
        switch (a.af()) {
            case 1:
                b = "c";
                break;
            case 2:
                b = "p";
                break;
            case 3:
                b = "i";
                break;
            case 4:
                b = "g";
                break;
            default:
                b = "a"
        }
        var a = a.i(), c = X(this, a);
        t(c) ? c.We(b) : this.f.q("Received chat state for contact we don't have: " + a)
    };
    var Ch = function (a, b, c) {
        de.call(this, b, c);
        this.$k = a
    };
    A(Ch, de);
    var Dh = M("fava.core.Timer");
    Ch.prototype.fg = function () {
        K(Dh, "Tick for " + this.$k);
        Ch.z.fg.call(this)
    };
    var Fh = function (a, b, c, d) {
        ha(b) || b && "function" == typeof b.handleEvent || f(Error("Invalid listener argument"));
        a = y(Eh, k, a, b, d);
        return ce.setTimeout(a, c || 0)
    }, Eh = function (a, b, c) {
        K(Dh, "Callback for " + a);
        ha(b) ? b.call(c) : b && "function" == typeof b.handleEvent && b.handleEvent.call(b)
    };
    var Gh = function () {
    };
    A(Gh, Eb);
    Gh.prototype.f = M("fava.debug.ErrorReporter");
    Gh.prototype.ud = function () {
        this.sf = []
    };
    var Hh = new Gh;
    var Ih = function () {
        this.zj = z()
    };
    new Ih;
    Ih.prototype.set = function (a) {
        this.zj = a
    };
    Ih.prototype.reset = function () {
        this.set(z())
    };
    Ih.prototype.get = function () {
        return this.zj
    };
    var Y = function (a, b, c, d, e, g) {
        this.b = a;
        this.e = b;
        this.Ra = c;
        this.ta = d;
        this.Nc = e || 1;
        this.Tc = 45E3;
        this.r = new R(this);
        this.N = g || k;
        this.Td = new de;
        this.Td.setInterval(250)
    };
    r = Y.prototype;
    r.X = k;
    r.sa = m;
    r.qc = k;
    r.Of = k;
    r.Jc = k;
    r.Aa = k;
    r.ib = k;
    r.ga = k;
    r.hb = k;
    r.V = k;
    r.Xc = 0;
    r.Fa = k;
    r.dc = k;
    r.u = k;
    r.S = -1;
    r.Hh = j;
    r.hc = m;
    var Jh = function (a, b) {
        switch (a) {
            case 0:
                return"Non-200 return code (" + b + ")";
            case 1:
                return"XMLHTTP failure (no data)";
            case 2:
                return"HttpConnection timeout";
            default:
                return"Unknown error"
        }
    }, Kh = {}, Lh = {}, Mh = function () {
        return!C || ib(10)
    };
    Y.prototype.Ka = function (a) {
        this.X = a
    };
    Y.prototype.setTimeout = function (a) {
        this.Tc = a
    };
    var Oh = function (a, b, c) {
        a.Aa = 1;
        a.ib = pd(b.K());
        a.hb = c;
        a.lh = j;
        Nh(a, k)
    }, Ph = function (a, b, c, d, e) {
        a.Aa = 1;
        a.ib = pd(b.K());
        a.hb = k;
        a.lh = c;
        e && (a.Hh = m);
        Nh(a, d)
    }, Nh = function (a, b) {
        a.Jc = z();
        Qh(a);
        a.ga = a.ib.K();
        od(a.ga, "t", a.Nc);
        if (Rh(a)) {
            a.Xc = 0;
            a.V = a.b.uf(a.b.ae() ? b : k);
            S(a.r, a.V, "readystatechange", a.Uj, m, a);
            var c = a.X ? qb(a.X) : {};
            a.hb ? (a.dc = "POST", c["Content-Type"] = "application/x-www-form-urlencoded", a.V.send(a.ga, a.dc, a.hb, c)) : (a.dc = "GET", a.Hh && !E && (c.Connection = "close"), a.V.send(a.ga, a.dc, k, c));
            var d = a.hb;
            if (d)for (var c =
                "", d = d.split("&"), e = 0; e < d.length; e++) {
                var g = d[e].split("=");
                if (1 < g.length)var h = g[0], g = g[1], l = h.split("_"), c = 2 <= l.length && "type" == l[1] ? c + (h + "=" + g + "&") : c + (h + "=redacted&")
            } else c = k;
            a.e.info("XMLHTTP REQ (" + a.ta + ") [attempt " + a.Nc + "]: " + a.dc + "\n" + a.ga + "\n" + c)
        }
    };
    Y.prototype.Uj = function (a) {
        a = a.target;
        try {
            if (a == this.V)a:{
                var b = ff(this.V);
                if (!Mh() || E && !F("420+")) {
                    if (4 > b)break a
                } else if (3 > b || 3 == b && !Xa && !gf(this.V))break a;
                Sh(this);
                var c = this.V.fb();
                this.S = c;
                var d = gf(this.V);
                d || this.e.debug("No response text for uri " + this.ga + " status " + c);
                this.sa = 200 == c;
                this.e.info("XMLHTTP RESP (" + this.ta + ") [ attempt " + this.Nc + "]: " + this.dc + "\n" + this.ga + "\n" + b + " " + c);
                if (this.sa) {
                    if (4 == b && Th(this), this.lh ? (Uh(this, b, d), Xa && 3 == b && (S(this.r, this.Td, "tick", this.Rj), this.Td.start())) :
                        (Vh(this.e, this.ta, d, k), Wh(this, d)), this.sa && !this.hc)4 == b ? this.b.Wd(this) : (this.sa = m, Qh(this))
                } else this.u = 400 == c && 0 < d.indexOf("Unknown SID") ? 3 : 0, Z(), Vh(this.e, this.ta, d), Th(this), Xh(this)
            } else this.e.q("Called back with an unexpected xmlhttp")
        } catch (e) {
            this.e.debug("Failed call to OnXmlHttpReadyStateChanged_"), this.V && gf(this.V) ? Yh(this.e, e, "ResponseText: " + gf(this.V)) : Yh(this.e, e, "No response text")
        } finally {
        }
    };
    var Uh = function (a, b, c) {
        for (var d = j; !a.hc && a.Xc < c.length;) {
            var e = Zh(a, c);
            if (e == Lh) {
                4 == b && (a.u = 4, Z(), d = m);
                Vh(a.e, a.ta, k, "[Incomplete Response]");
                break
            } else if (e == Kh) {
                a.u = 4;
                Z();
                Vh(a.e, a.ta, c, "[Invalid Chunk]");
                d = m;
                break
            } else Vh(a.e, a.ta, e, k), Wh(a, e)
        }
        4 == b && 0 == c.length && (a.u = 1, Z(), d = m);
        a.sa = a.sa && d;
        d || (Vh(a.e, a.ta, c, "[Invalid Chunked Response]"), Th(a), Xh(a))
    };
    Y.prototype.Rj = function () {
        var a = ff(this.V), b = gf(this.V);
        this.Xc < b.length && (Sh(this), Uh(this, a, b), this.sa && 4 != a && Qh(this))
    };
    var Rh = function (a) {
        if (!a.N)return j;
        if ("u" != a.N.Xb && "?" != a.N.Xb)return S(a.r, a.N, "offline", a.Xi), j;
        a.Xi();
        return m
    };
    Y.prototype.Xi = function () {
        this.sa && this.e.w("Received browser offline event even though request completed successfully");
        this.e.info("BROWSER_OFFLINE: " + this.ga);
        Th(this);
        this.u = 6;
        Z();
        Xh(this)
    };
    var Zh = function (a, b) {
        var c = a.Xc, d = b.indexOf("\n", c);
        if (-1 == d)return Lh;
        c = Number(b.substring(c, d));
        if (isNaN(c))return Kh;
        d += 1;
        if (d + c > b.length)return Lh;
        var e = b.substr(d, c);
        a.Xc = d + c;
        return e
    }, $h = function (a, b) {
        a.Jc = z();
        Qh(a);
        var c = b ? window.location.hostname : "";
        a.ga = a.ib.K();
        O(a.ga, "DOMAIN", c);
        O(a.ga, "t", a.Nc);
        if (Rh(a)) {
            a.Fa = new ActiveXObject("htmlfile");
            var d = "<html><body>";
            b && (d += '<script>document.domain="' + c + '"<\/script>');
            d += "</body></html>";
            a.Fa.open();
            a.Fa.write(d);
            a.Fa.close();
            a.Fa.parentWindow.m =
                y(a.wk, a);
            a.Fa.parentWindow.d = y(a.xi, a, j);
            a.Fa.parentWindow.rpcClose = y(a.xi, a, m);
            c = a.Fa.createElement("div");
            a.Fa.parentWindow.document.body.appendChild(c);
            c.innerHTML = '<iframe src="' + a.ga + '"></iframe>';
            a.e.info("TRIDENT REQ (" + a.ta + ") [ attempt " + a.Nc + "]: GET\n" + a.ga)
        }
    };
    r = Y.prototype;
    r.wk = function (a) {
        ai(y(this.pl, this, a), 0)
    };
    r.pl = function (a) {
        if (!this.hc) {
            var b = this.e;
            b.info("TRIDENT TEXT (" + this.ta + "): " + bi(b, a));
            Sh(this);
            Wh(this, a);
            Qh(this)
        }
    };
    r.xi = function (a) {
        ai(y(this.ol, this, a), 0)
    };
    r.ol = function (a) {
        this.hc || (this.e.info("TRIDENT TEXT (" + this.ta + "): " + a ? "success" : "failure"), Th(this), this.sa = a, this.b.Wd(this))
    };
    r.cancel = function () {
        this.hc = j;
        Th(this)
    };
    var Qh = function (a) {
        a.Of = z() + a.Tc;
        ci(a, a.Tc)
    }, ci = function (a, b) {
        a.qc != k && f(Error("WatchDog timer not null"));
        a.qc = ai(y(a.hl, a), b)
    }, Sh = function (a) {
        a.qc && (s.clearTimeout(a.qc), a.qc = k)
    };
    Y.prototype.hl = function () {
        this.qc = k;
        var a = z();
        0 <= a - this.Of ? (this.sa && this.e.w("Received watchdog timeout even though request loaded successfully"), this.e.info("TIMEOUT: " + this.ga), Th(this), this.u = 2, Z(), Xh(this)) : (this.e.q("WatchDog timer called too early"), ci(this, this.Of - a))
    };
    var Xh = function (a) {
        !a.b.Li() && !a.hc && a.b.Wd(a)
    }, Th = function (a) {
        Sh(a);
        a.Td.stop();
        a.r.Wf();
        if (a.V) {
            var b = a.V;
            a.V = k;
            b.abort()
        }
        a.Fa && (a.Fa = k);
        a.N = k
    };
    Y.prototype.Qh = function () {
        return this.u
    };
    Y.prototype.ea = function () {
        return this.S
    };
    Y.prototype.de = function () {
        return this.Ra
    };
    var Wh = function (a, b) {
        try {
            a.b.si(a, b)
        } catch (c) {
            Yh(a.e, c, "Error in httprequest callback")
        }
    };
    var di = function () {
        this.f = M("goog.net.BrowserChannel")
    }, Vh = function (a, b, c, d) {
        a.info("XMLHTTP TEXT (" + b + "): " + bi(a, c) + (d ? " " + d : ""))
    };
    di.prototype.debug = function (a) {
        this.info(a)
    };
    var Yh = function (a, b, c) {
        a.w((c || "Exception") + b)
    };
    di.prototype.info = function (a) {
        this.f.info(a)
    };
    di.prototype.q = function (a) {
        this.f.q(a)
    };
    di.prototype.w = function (a) {
        this.f.w(a)
    };
    var bi = function (a, b) {
        if (!b || "y2f%" == b)return b;
        try {
            for (var c = bc(b), d = 0; d < c.length; d++)if (w(c[d])) {
                var e = c[d];
                if (!(2 > e.length)) {
                    var g = e[1];
                    if (w(g) && !(1 > g.length)) {
                        var h = g[0];
                        if ("noop" != h && "stop" != h)for (var l = 1; l < g.length; l++)g[l] = ""
                    }
                }
            }
            return ec(c)
        } catch (p) {
            return a.debug("Exception parsing expected JS array - probably was not JS"), b
        }
    };
    var fi = function (a, b, c, d, e) {
        (new di).debug("TestLoadImageWithRetries: " + e);
        if (0 == d)c(m); else {
            var g = e || 0;
            d--;
            ei(a, b, function (e) {
                e ? c(j) : s.setTimeout(function () {
                    fi(a, b, c, d, g)
                }, g)
            })
        }
    }, ei = function (a, b, c) {
        var d = new di;
        d.debug("TestLoadImage: loading " + a);
        var e = new Image;
        e.onload = function () {
            try {
                d.debug("TestLoadImage: loaded"), gi(e), c(j)
            } catch (a) {
                Yh(d, a)
            }
        };
        e.onerror = function () {
            try {
                d.debug("TestLoadImage: error"), gi(e), c(m)
            } catch (a) {
                Yh(d, a)
            }
        };
        e.onabort = function () {
            try {
                d.debug("TestLoadImage: abort"), gi(e),
                    c(m)
            } catch (a) {
                Yh(d, a)
            }
        };
        e.ontimeout = function () {
            try {
                d.debug("TestLoadImage: timeout"), gi(e), c(m)
            } catch (a) {
                Yh(d, a)
            }
        };
        s.setTimeout(function () {
            if (e.ontimeout)e.ontimeout()
        }, b);
        e.src = a
    }, gi = function (a) {
        a.onload = k;
        a.onerror = k;
        a.onabort = k;
        a.ontimeout = k
    };
    var hi = function (a, b) {
        this.b = a;
        this.e = b
    };
    r = hi.prototype;
    r.X = k;
    r.ma = k;
    r.ce = m;
    r.Mc = k;
    r.be = k;
    r.wf = k;
    r.H = k;
    r.h = k;
    r.S = -1;
    r.Sa = k;
    r.vf = k;
    r.Ka = function (a) {
        this.X = a
    };
    r.T = function (a) {
        this.H = a;
        a = ii(this.b, this.H);
        Z();
        od(a, "MODE", "init");
        this.ma = new Y(this, this.e, i, i, i, this.b.N);
        this.ma.Ka(this.X);
        Ph(this.ma, a, m, k, j);
        this.h = 0;
        this.Mc = z()
    };
    r.Wj = function (a) {
        a ? (this.h = 2, ji(this)) : (Z(), a = this.b, a.e.debug("Test Connection Blocked"), a.S = a.Eb.ea(), ki(a, 9))
    };
    var ji = function (a) {
        a.e.debug("TestConnection: starting stage 2");
        a.ma = new Y(a, a.e, i, i, i, a.b.N);
        a.ma.Ka(a.X);
        var b = li(a.b, a.Sa, a.H);
        Z();
        if (Mh())od(b, "TYPE", "xmlhttp"), Ph(a.ma, b, m, a.Sa, m); else {
            od(b, "TYPE", "html");
            var c = a.ma, a = Boolean(a.Sa);
            c.Aa = 3;
            c.ib = pd(b.K());
            $h(c, a)
        }
    };
    r = hi.prototype;
    r.uf = function (a) {
        return this.b.uf(a)
    };
    r.abort = function () {
        this.ma && (this.ma.cancel(), this.ma = k);
        this.S = -1
    };
    r.Li = function () {
        return m
    };
    r.si = function (a, b) {
        this.S = a.ea();
        if (0 == this.h)if (this.e.debug("TestConnection: Got data for stage 1"), b) {
            try {
                var c = bc(b)
            } catch (d) {
                Yh(this.e, d);
                mi(this.b, this);
                return
            }
            this.Sa = this.b.Ld(c[0]);
            this.vf = c[1]
        } else this.e.debug("TestConnection: Null responseText"), mi(this.b, this); else if (2 == this.h)if (this.ce)Z(), this.wf = z(); else if ("11111" == b) {
            if (Z(), this.ce = j, this.be = z(), c = this.be - this.Mc, Mh() || 500 > c)this.S = 200, this.ma.cancel(), this.e.debug("Test connection succeeded; using streaming connection"), Z(), ni(this.b,
                this, j)
        } else Z(), this.be = this.wf = z(), this.ce = m
    };
    r.Wd = function () {
        this.S = this.ma.ea();
        if (this.ma.sa)if (0 == this.h)if (this.e.debug("TestConnection: request complete for initial check"), this.vf) {
            this.h = 1;
            var a = oi(this.b, this.vf, "/mail/images/cleardot.gif");
            pd(a);
            fi(a.toString(), 5E3, y(this.Wj, this), 3, 2E3)
        } else this.h = 2, ji(this); else 2 == this.h && (this.e.debug("TestConnection: request complete for stage 2"), a = m, (a = Mh() ? this.ce : 200 > this.wf - this.be ? m : j) ? (this.e.debug("Test connection succeeded; using streaming connection"), Z(), ni(this.b, this, j)) : (this.e.debug("Test connection failed; not using streaming"),
            Z(), ni(this.b, this, m))); else this.e.debug("TestConnection: request failed, in state " + this.h), 0 == this.h ? Z() : 2 == this.h && Z(), mi(this.b, this)
    };
    r.ea = function () {
        return this.S
    };
    r.ae = function () {
        return this.b.ae()
    };
    r.Jb = function () {
        return this.b.Jb()
    };
    var $ = function (a) {
        this.Kc = a || k;
        this.h = 1;
        this.ha = [];
        this.La = [];
        this.e = new di
    }, pi = function (a, b, c) {
        this.dj = a;
        this.map = b;
        this.zf = c || k
    };
    r = $.prototype;
    r.X = k;
    r.md = k;
    r.fa = k;
    r.L = k;
    r.H = k;
    r.Rd = k;
    r.fh = k;
    r.Sa = k;
    r.N = k;
    r.Xk = j;
    r.$c = 0;
    r.uk = 0;
    r.Vj = m;
    r.n = k;
    r.lb = k;
    r.Ua = k;
    r.Fb = k;
    r.Eb = k;
    r.Xe = k;
    r.Ak = j;
    r.Fc = -1;
    r.Gh = -1;
    r.S = -1;
    r.Uc = 0;
    r.ec = 0;
    r.Zb = 8;
    var qi = new T, ri = function (a) {
        H.call(this, "statevent", a)
    };
    A(ri, H);
    var si = function (a, b) {
        H.call(this, "timingevent", a);
        this.size = b
    };
    A(si, H);
    $.prototype.oc = function (a) {
        t(a) && (this.e = a)
    };
    $.prototype.T = function (a, b, c, d, e) {
        this.e.debug("connect()");
        Z();
        this.H = b;
        this.md = c || {};
        d && u(e) && (this.md.OSID = d, this.md.OAID = e);
        this.e.debug("connectTest_()");
        ti(this) && (this.Eb = new hi(this, this.e), this.Eb.Ka(this.X), this.Eb.T(a))
    };
    $.prototype.Lf = function () {
        this.e.debug("disconnect()");
        ui(this);
        if (3 == this.h) {
            var a = this.$c++, b = this.Rd.K();
            O(b, "SID", this.Ra);
            O(b, "RID", a);
            O(b, "TYPE", "terminate");
            vi(this, b);
            a = new Y(this, this.e, this.Ra, a, i, this.N);
            a.Aa = 2;
            a.ib = pd(b.K());
            (new Image).src = a.ib;
            a.Jc = z();
            Qh(a);
            wi(this)
        }
    };
    $.prototype.de = function () {
        return this.Ra
    };
    var ui = function (a) {
        a.Eb && (a.Eb.abort(), a.Eb = k);
        a.L && (a.L.cancel(), a.L = k);
        a.Ua && (s.clearTimeout(a.Ua), a.Ua = k);
        xi(a);
        a.fa && (a.fa.cancel(), a.fa = k);
        a.lb && (s.clearTimeout(a.lb), a.lb = k)
    };
    r = $.prototype;
    r.Ka = function (a) {
        this.X = a
    };
    r.Xa = function (a, b) {
        0 == this.h && f(Error("Invalid operation: sending map when state is closed"));
        1E3 == this.ha.length && this.e.w("Already have 1000 queued maps upon queueing " + ec(a));
        this.ha.push(new pi(this.uk++, a, b));
        (2 == this.h || 3 == this.h) && yi(this)
    };
    r.Li = function () {
        return 0 == this.h
    };
    r.Ea = function () {
        return this.h
    };
    r.ea = function () {
        return this.S
    };
    var zi = function (a) {
        var b = 0;
        a.L && b++;
        a.fa && b++;
        return b
    }, yi = function (a) {
        !a.fa && !a.lb && (a.lb = ai(y(a.Oh, a), 0), a.Uc = 0)
    };
    $.prototype.Oh = function (a) {
        this.lb = k;
        this.e.debug("startForwardChannel_");
        if (ti(this))if (1 == this.h)if (a)this.e.w("Not supposed to retry the open"); else {
            this.e.debug("open_()");
            this.$c = Math.floor(1E5 * Math.random());
            var a = this.$c++, b = new Y(this, this.e, "", a, i, this.N);
            b.Ka(this.X);
            var c = Ai(this), d = this.Rd.K();
            O(d, "RID", a);
            this.Kc && O(d, "CVER", this.Kc);
            vi(this, d);
            Oh(b, d, c);
            this.fa = b;
            this.h = 2
        } else 3 == this.h && (a ? Bi(this, a) : 0 == this.ha.length ? this.e.debug("startForwardChannel_ returned: nothing to send") : this.fa ?
            this.e.w("startForwardChannel_ returned: connection already in progress") : (Bi(this), this.e.debug("startForwardChannel_ finished, sent request")))
    };
    var Bi = function (a, b) {
        var c, d;
        b ? 6 < a.Zb ? (a.ha = a.La.concat(a.ha), a.La.length = 0, c = a.$c - 1, d = Ai(a)) : (c = b.ta, d = b.hb) : (c = a.$c++, d = Ai(a));
        var e = a.Rd.K();
        O(e, "SID", a.Ra);
        O(e, "RID", c);
        O(e, "AID", a.Fc);
        vi(a, e);
        c = new Y(a, a.e, a.Ra, c, a.Uc + 1, a.N);
        c.Ka(a.X);
        c.setTimeout(Math.round(1E4) + Math.round(1E4 * Math.random()));
        a.fa = c;
        Oh(c, e, d)
    }, vi = function (a, b) {
        if (a.n) {
            var c = a.n.hj(a);
            c && pc(c, function (a, c) {
                O(b, c, a)
            })
        }
    }, Ai = function (a) {
        var b = Math.min(a.ha.length, 1E3), c = ["count=" + b], d;
        6 < a.Zb && 0 < b ? (d = a.ha[0].dj, c.push("ofs=" + d)) :
            d = 0;
        for (var e = 0; e < b; e++) {
            var g = a.ha[e].dj, h = a.ha[e].map, g = 6 >= a.Zb ? e : g - d;
            try {
                pc(h, function (a, b) {
                    c.push("req" + g + "_" + b + "=" + encodeURIComponent(a))
                })
            } catch (l) {
                c.push("req" + g + "_type=" + encodeURIComponent("_badmap"))
            }
        }
        a.La = a.La.concat(a.ha.splice(0, b));
        return c.join("&")
    }, Ci = function (a) {
        !a.L && !a.Ua && (a.Th = 1, a.Ua = ai(y(a.qi, a), 0), a.ec = 0)
    }, Ei = function (a) {
        if (a.L || a.Ua)return a.e.w("Request already in progress"), m;
        if (3 <= a.ec || !(a.N ? "u" != a.N.Xb && "?" != a.N.Xb : 1))return m;
        a.e.debug("Going to retry GET");
        a.Th++;
        a.Ua =
            ai(y(a.qi, a), Di(a, a.ec));
        a.ec++;
        return j
    };
    $.prototype.qi = function () {
        this.Ua = k;
        if (ti(this)) {
            this.e.debug("Creating new HttpRequest");
            this.L = new Y(this, this.e, this.Ra, "rpc", this.Th, this.N);
            this.L.Ka(this.X);
            var a = this.fh.K();
            O(a, "RID", "rpc");
            O(a, "SID", this.Ra);
            O(a, "CI", this.Xe ? "0" : "1");
            O(a, "AID", this.Fc);
            vi(this, a);
            if (Mh())O(a, "TYPE", "xmlhttp"), Ph(this.L, a, j, this.Sa, m); else {
                O(a, "TYPE", "html");
                var b = this.L, c = Boolean(this.Sa);
                b.Aa = 3;
                b.ib = pd(a.K());
                $h(b, c)
            }
            this.e.debug("New Request created")
        }
    };
    var ti = function (a) {
        if (a.n) {
            var b = a.n.Ki(a);
            if (0 != b)return a.e.debug("Handler returned error code from okToMakeRequest"), ki(a, b), m
        }
        return j
    };
    $.prototype.fd = function (a) {
        this.N = a
    };
    var ni = function (a, b, c) {
        a.e.debug("Test Connection Finished");
        a.Xe = a.Ak && c;
        a.S = b.ea();
        a.e.debug("connectChannel_()");
        a.Bk(1, 0);
        a.Rd = ii(a, a.H);
        yi(a)
    }, mi = function (a, b) {
        a.e.debug("Test Connection Failed");
        a.S = b.ea();
        ki(a, 2)
    };
    $.prototype.si = function (a, b) {
        if (!(0 == this.h || this.L != a && this.fa != a))if (this.S = a.ea(), this.fa == a && 3 == this.h)if (7 < this.Zb) {
            var c;
            try {
                c = bc(b)
            } catch (d) {
                c = k
            }
            if (w(c) && 3 == c.length) {
                var e = c;
                if (0 == e[0])a:if (this.e.debug("Server claims our backchannel is missing."), this.Ua)this.e.debug("But we are currently starting the request."); else {
                    if (this.L)if (this.L.Jc + 3E3 < this.fa.Jc)xi(this), this.L.cancel(), this.L = k; else break a; else this.e.q("We do not have a BackChannel established");
                    Ei(this);
                    Z()
                } else this.Gh = e[1],
                    c = this.Gh - this.Fc, 0 < c && (e = e[2], this.e.debug(e + " bytes (in " + c + " arrays) are outstanding on the BackChannel"), 37500 > e && this.Xe && 0 == this.ec && !this.Fb && (this.Fb = ai(y(this.Sj, this), 6E3)))
            } else this.e.debug("Bad POST response data returned"), ki(this, 11)
        } else"y2f%" != b && (this.e.debug("Bad data returned - missing/invald magic cookie"), ki(this, 11)); else if (this.L == a && xi(this), !/^[\s\xa0]*$/.test(b)) {
            c = bc(b);
            for (var e = this.n && this.n.Zd ? [] : k, g = 0; g < c.length; g++) {
                var h = c[g];
                this.Fc = h[0];
                h = h[1];
                2 == this.h ? "c" ==
                    h[0] ? (this.Ra = h[1], this.Sa = this.Ld(h[2]), h = h[3], this.Zb = t(h) ? h : 6, this.h = 3, this.n && this.n.Eh(this), this.fh = li(this, this.Sa, this.H), Ci(this)) : "stop" == h[0] && ki(this, 7) : 3 == this.h && ("stop" == h[0] ? (e && e.length && (this.n.Zd(this, e), e.length = 0), ki(this, 7)) : "noop" != h[0] && e && e.push(h), this.ec = 0)
            }
            e && e.length && this.n.Zd(this, e)
        }
    };
    $.prototype.Ld = function (a) {
        return this.Xk ? this.n ? this.n.Ld(a) : a : k
    };
    $.prototype.Sj = function () {
        t(this.Fb) && (this.Fb = k, this.L.cancel(), this.L = k, Ei(this), Z())
    };
    var xi = function (a) {
        t(a.Fb) && (s.clearTimeout(a.Fb), a.Fb = k)
    };
    $.prototype.Wd = function (a) {
        this.e.debug("Request complete");
        var b;
        if (this.L == a)xi(this), this.L = k, b = 2; else if (this.fa == a)this.fa = k, b = 1; else return;
        this.S = a.ea();
        if (0 != this.h)if (a.sa)1 == b ? (z(), qi.dispatchEvent(new si(qi, a.hb ? a.hb.length : 0)), yi(this), this.n && this.n.Ph(this, this.La), this.La.length = 0) : Ci(this); else {
            var c = a.Qh();
            if (3 == c || 0 == c && 0 < this.S)this.e.debug("Not retrying due to error type"); else {
                this.e.debug("Maybe retrying, last error: " + Jh(c, this.S));
                var d;
                if (d = 1 == b)this.fa || this.lb ? (this.e.w("Request already in progress"),
                    d = m) : 1 == this.h || this.Uc >= (this.Vj ? 0 : 2) ? d = m : (this.e.debug("Going to retry POST"), this.lb = ai(y(this.Oh, this, a), Di(this, this.Uc)), this.Uc++, d = j);
                if (d || 2 == b && Ei(this))return;
                this.e.debug("Exceeded max number of retries")
            }
            this.e.debug("Error: HTTP request failed");
            switch (c) {
                case 1:
                    ki(this, 5);
                    break;
                case 4:
                    ki(this, 10);
                    break;
                case 3:
                    ki(this, 6);
                    break;
                default:
                    ki(this, 2)
            }
        }
    };
    var Di = function (a, b) {
        var c = 5E3 + Math.floor(1E4 * Math.random());
        a.Jb() || (a.e.debug("Inactive channel"), c *= 2);
        return c * b
    };
    $.prototype.Bk = function (a) {
        0 <= Fa(arguments, this.h) || f(Error("Unexpected channel state: " + this.h))
    };
    var ki = function (a, b) {
        a.e.info("Error code " + b);
        if (2 == b || 9 == b) {
            var c = k;
            a.n && (c = a.n.Ji(a));
            var d = y(a.Jk, a);
            c || (c = new N("//www.google.com/images/cleardot.gif"), pd(c));
            ei(c.toString(), 1E4, d)
        } else Z();
        a.me(b)
    };
    $.prototype.Jk = function (a) {
        a ? (this.e.info("Successfully pinged google.com"), Z()) : (this.e.info("Failed to ping google.com"), Z(), this.me(8))
    };
    $.prototype.me = function (a) {
        this.e.debug("HttpChannel: error - " + a);
        this.h = 0;
        this.n && this.n.Ci(this, a);
        wi(this);
        ui(this)
    };
    var wi = function (a) {
        a.h = 0;
        a.S = -1;
        if (a.n)if (0 == a.La.length && 0 == a.ha.length)a.n.Nf(a); else {
            a.e.debug("Number of undelivered maps, pending: " + a.La.length + ", outgoing: " + a.ha.length);
            var b = Ma(a.La), c = Ma(a.ha);
            a.La.length = 0;
            a.ha.length = 0;
            a.n.Nf(a, b, c)
        }
    }, ii = function (a, b) {
        var c = oi(a, k, b);
        a.e.debug("GetForwardChannelUri: " + c);
        return c
    }, li = function (a, b, c) {
        b = oi(a, a.ae() ? b : k, c);
        a.e.debug("GetBackChannelUri: " + b);
        return b
    }, oi = function (a, b, c) {
        var d = qd(c);
        if ("" != d.Q())b && Xc(d, b + "." + d.Q()), Yc(d, d.Ca); else var e =
            window.location, d = hd(e.protocol, k, b ? b + "." + e.hostname : e.hostname, e.port, c);
        a.md && pc(a.md, function (a, b) {
            O(d, b, a)
        });
        O(d, "VER", a.Zb);
        vi(a, d);
        return d
    };
    $.prototype.uf = function (a) {
        a && f(Error("Can't create secondary domain capable XhrIo object."));
        return new Ye
    };
    $.prototype.Jb = function () {
        return!!this.n && this.n.Jb(this)
    };
    var ai = function (a, b) {
        ha(a) || f(Error("Fn must not be null and must be a function"));
        return s.setTimeout(function () {
            a()
        }, b)
    }, Z = function () {
        qi.dispatchEvent(new ri(qi))
    };
    $.prototype.ae = function () {
        return!Mh()
    };
    var Fi = function () {
    };
    r = Fi.prototype;
    r.Zd = k;
    r.Ki = function () {
        return 0
    };
    r.Eh = function () {
    };
    r.Ph = function () {
    };
    r.Ci = function () {
    };
    r.Nf = function () {
    };
    r.hj = function () {
        return{}
    };
    r.Ji = function () {
        return k
    };
    r.Jb = function () {
        return j
    };
    r.Ld = function (a) {
        return a
    };
    var Gi = function (a, b, c, d, e, g, h) {
        this.D = a;
        this.Ue = b;
        this.Dh = d || k;
        this.Ic = new Zg;
        this.Lc = new Zg;
        this.hh = e || k;
        this.e = k;
        this.Kc = c;
        this.h = -1;
        this.u = 0;
        this.b = this.Wb(this.Kc);
        this.Fj = !!g;
        this.X = h ? qb(h) : {};
        this.gf = new Ch("BrowserChannel heartbeat", 1E3);
        this.r = new R(this);
        S(this.r, this.gf, "tick", this.Qj);
        this.gf.start();
        this.kb = 5E3 + 2E4 * Math.random();
        this.b.Ka(this.X)
    };
    A(Gi, Fi);
    r = Gi.prototype;
    r.f = M("fava.net.BrowserChannelWrapper");
    r.Gf = k;
    r.Fe = 0;
    r.fe = m;
    r.se = [];
    r.I = function () {
        G(this.r);
        -1 != this.h && this.b.Lf();
        this.h = 1;
        this.Ic.clear();
        Hi(this);
        Ii(this);
        this.gf.I()
    };
    r.Ea = function () {
        return this.hd ? 4 : this.h
    };
    r.Sf = function () {
        return this.Gf
    };
    r.Zd = function (a, b) {
        if (a == this.b) {
            for (var c = 0; c < b.length; c++) {
                var d = this.Ic, e = b[c];
                d.Ya[d.nb++] = e
            }
            this.eh()
        }
    };
    r.eh = function () {
        Ii(this);
        for (var a = z(), b = []; !this.Ic.Va();) {
            var c = $g(this.Ic), d = z(), e = c, c = e[0], g;
            g = sf;
            var h = z(), l = rf(g), p = g.mb.P();
            if (g.ca.length + p > g.nf) {
                g.f.q("Giant thread trace. Clearing to avoid memory leak.");
                if (g.ca.length > g.nf / 2) {
                    for (var n = 0; n < g.ca.length; n++) {
                        var q = g.ca[n];
                        q.id && kf(g.fc, q.id);
                        kf(g.Db, q)
                    }
                    g.ca.length = 0
                }
                p > g.nf / 2 && g.mb.clear()
            }
            Lc("Start : BrowserChannelServices.handleArray_");
            q = jf(g.Db);
            q.bd = l;
            q.Yc = 0;
            q.id = Number(jf(g.fc));
            q.cd = "BrowserChannelServices.handleArray_";
            q.type = i;
            g.ca.push(q);
            g.mb.set("" + q.id, q);
            g.ff++;
            l = z();
            q.startTime = q.Xd = l;
            g.Vd += l - h;
            g = q.id;
            if (0 >= e.length)this.f.q("Got empty array"); else if (3 == this.h && "b" == e[0])Ji(this, 4); else if (4 == this.h)try {
                var v = this.D.Ec.get(e[0]);
                if (v && w(e[1])) {
                    var aa = e[1];
                    v.dispatchEvent(new Ki(aa));
                    c += "-" + aa[0]
                } else this.f.q("Unexpected response array: " + e)
            } catch (D) {
                e = D, Hh.vb ? Hh.f.info("reportException was called but ErrorReporter already disposed. Message: BC error handling array", e) : (Hh.f && (h = Hh.f.Lh(Fc, "BC error handling array", e), h.rl = j,
                    q = Hh.f, h.Kh().value >= Mc(q).value && Nc(q, h)), Hh.ic || Hh.sf && 10 > Hh.sf.length && Hh.sf.push(["BC error handling array", e]))
            }
            q = g;
            e = sf;
            g = z();
            p = i;
            p = e.xh;
            h = e.mb.get("" + q);
            if (h != k) {
                e.mb.remove("" + q);
                q = i;
                l = g - h.startTime;
                if (l < p)for (p = e.ca.length - 1; 0 <= p; p--) {
                    if (e.ca[p] == h) {
                        e.ca.splice(p, 1);
                        kf(e.fc, h.id);
                        kf(e.Db, h);
                        break
                    }
                } else q = jf(e.Db), q.Yc = 1, q.startTime = h.startTime, q.cd = h.cd, q.type = h.type, q.Tj = q.Xd = g, e.ca.push(q);
                p = h.type;
                n = k;
                p && (n = e.Cb.get(p), n || (n = jf(e.jf), n.type = p, e.Cb.set(p, n)), n.count++, n.time += l);
                q && (Lc("Stop : " +
                    q.cd), q.bd = rf(e), n && (n.Zc += q.bd - h.bd));
                e.Ud += z() - g
            }
            e = z();
            b.push([c + ":" + (e - d)]);
            if (500 < e - a) {
                this.f.q("Took too long handling arrays: " + Aa(b.join(",")));
                break
            }
        }
        this.Ic.Va() || (this.f.info("Delaying array handling"), this.pf = Fh("fava.net.BrowserChannelWrapper", this.eh, 0, this))
    };
    r.Ki = function () {
        return 0
    };
    r.Eh = function (a) {
        a == this.b && (this.kb = 5E3 + 2E4 * Math.random(), Ji(this, 3))
    };
    r.Ci = function (a, b) {
        a == this.b && (this.hd = m, 4 == b ? this.u = 1 : 2 == b ? this.u = 2 : 6 == b ? this.Fj ? (4 == this.h && !Li(this, j) && (this.hd = j), this.u = 0, this.fe = j) : this.u = 2 : 8 == b ? this.u = 3 : 7 == b ? this.u = 2 : 9 == b && (this.u = 4), Li(this, j), Ji(this, 5))
    };
    r.Ph = function (a, b) {
        a == this.b && this.D.dispatchEvent(new Mi(b))
    };
    r.Nf = function (a, b, c) {
        if (a == this.b && 5 != this.h && 6 != this.h && (this.hd = m, Ji(this, 1), b || c))this.D.dispatchEvent(new Ni(b || k, c || k))
    };
    r.hj = function (a) {
        if (a != this.b)return{};
        for (var b = {}, a = 0; a < this.se.length; a++)sb(b, this.se[a].bc);
        return b
    };
    r.Ji = function (a) {
        return a != this.b ? k : this.Dh ? (a = new N(this.Dh), pd(a), a) : k
    };
    r.Jb = function () {
        return j
    };
    var Oi = function (a) {
        Hi(a);
        a.Ue || f(Error("BrowserChannelServices.Channel: base path not set"));
        var b = a.Ue + "test", c = a.Ue + "bind";
        if (-1 != a.h) {
            (3 == a.b.Ea() || 0 != zi(a.b)) && a.f.w("BrowserChannelServices.Channel: unexpected reconnect state: " + a.b.Ea());
            var d = a.b.de(), e = a.b.Fc;
            a.b = a.Wb(a.Kc);
            a.b.Ka(a.X);
            a.b.T(b, c, {}, d, e)
        } else a.b.T(b, c, {});
        Ji(a, 2)
    };
    Gi.prototype.kc = function () {
        switch (this.Ea()) {
            case -1:
            case 2:
            case 3:
            case 4:
                return j;
            default:
                return t(this.qe)
        }
    };
    Gi.prototype.Xa = function (a, b, c) {
        this.kc() || f(Error("BrowserChannelServices: Trying to send a map while we are disconnected: " + a.type));
        b = b || 1;
        t(a) && (a = new Pi(a, c), c = this.Lc, c.Ya[c.nb++] = a, 5E3 <= this.Lc.P() && (this.f.q("Hit max queue size. Dropping BC message."), $g(this.Lc)));
        if (4 == this.h && 1 == b)for (; !this.Lc.Va();)a = $g(this.Lc), this.b.Xa(a.map, a.zf)
    };
    var Ji = function (a, b) {
        var c = a.h;
        if (5 == b)5 != c && Qi(a), a.h = b; else if (c != b)switch (a.h = b, b) {
            case 4:
                a.u = 0, a.hd = m, a.Xa(k), a.D.dispatchEvent("ua")
        } else return;
        a.hd || a.D.dispatchEvent(new Ri(c, b, a.u))
    };
    Gi.prototype.Wb = function (a) {
        a = this.hh ? this.hh(a) : new $(a);
        this.e && this.oc(this.e);
        this.N && a.fd(this.N);
        a.n = this;
        return a
    };
    var Hi = function (a) {
        t(a.qe) && (ce.clearTimeout(a.qe), a.qe = k)
    }, Ii = function (a) {
        t(a.pf) && (ce.clearTimeout(a.pf), a.pf = k)
    }, Qi = function (a) {
        1 != a.u && 4 != a.u && (!a.fe && 24E4 > 2 * a.kb && (a.kb *= 2), a.fe && (a.kb = 500), a.f.info("Retrying connection in " + a.kb + "ms"), a.Gf = z() + a.kb, Hi(a), a.qe = Fh("fava.net.BrowserChannelServices", a.Zh, a.kb, a))
    };
    Gi.prototype.Zh = function () {
        this.Gf = k;
        if (1 == this.h || 5 == this.h || 6 == this.h)this.fe = m, this.b && 0 == zi(this.b) ? Oi(this) : Qi(this)
    };
    Gi.prototype.Qj = function () {
        Li(this, m) || (this.Fe = z())
    };
    var Li = function (a, b) {
        var c = z(), d = 0 < a.Fe && 3E4 < c - a.Fe;
        b && (a.Fe = c);
        return d
    };
    Gi.prototype.hf = function (a) {
        var b = this.se;
        0 <= Fa(b, a) || b.push(a)
    };
    Gi.prototype.Vf = function (a) {
        Ka(this.se, a)
    };
    Gi.prototype.oc = function (a) {
        this.e = a;
        this.b && (a ? this.b.oc(a) : this.b.oc(new di))
    };
    Gi.prototype.fd = function (a) {
        this.N = a;
        this.b && this.b.fd(a);
        S(this.r, a, "online", this.Zh)
    };
    var Pi = function (a, b) {
        this.map = a;
        this.zf = b || k
    };
    var Si = function (a, b, c, d, e, g) {
        this.Ec = new J;
        this.na = new Gi(this, a, b, c, d, e, g)
    };
    A(Si, T);
    var Ti = function (a, b) {
        this.Aa = b;
        this.D = a
    };
    A(Ti, T);
    var Ki = function (a) {
        H.call(this, "sa");
        this.Gk = a
    };
    A(Ki, H);
    Ti.prototype.Ae = function () {
        return this.Aa
    };
    Ti.prototype.kc = function () {
        return this.D.kc()
    };
    Ti.prototype.Xa = function (a, b) {
        this.D.Xa(this, a, b)
    };
    var Ri = function (a, b, c) {
        H.call(this, "ta");
        this.zk = a;
        this.Bi = b;
        this.error = c
    };
    A(Ri, H);
    var Mi = function (a) {
        H.call(this, "va");
        this.rj = a
    };
    A(Mi, H);
    var Ni = function (a, b) {
        H.call(this, "wa");
        this.Ni = a;
        this.Oi = b
    };
    A(Ni, H);
    r = Si.prototype;
    r.f = M("fava.net.BrowserChannelServices");
    r.g = function () {
        Si.z.g.call(this);
        this.na.I();
        for (var a = this.Ec.da(), b = 0; b < a.length; b++)a[b].I();
        this.Ec.clear()
    };
    r.hf = function (a) {
        this.na.hf(a)
    };
    r.Vf = function (a) {
        this.na.Vf(a)
    };
    r.T = function () {
        -1 == this.Ea() && Oi(this.na)
    };
    r.Lf = function () {
        this.na.b.Lf()
    };
    r.de = function () {
        return this.na.b.de()
    };
    r.kc = function () {
        return this.na.kc()
    };
    r.Xa = function (a, b, c) {
        "_sc"in b && f(Error("sendMap called with reserved key: _sc"));
        b._sc = a.Ae();
        this.na.Xa(b, c)
    };
    r.Ea = function () {
        return this.na.Ea()
    };
    r.Sf = function () {
        return this.na.Sf()
    };
    r.oc = function (a) {
        this.na.oc(a)
    };
    r.ea = function () {
        return this.na.b.ea()
    };
    r.fd = function (a) {
        this.na.fd(a)
    };
    var ak = function (a) {
        this.a = a || ["ms"]
    };
    A(ak, U);
    V.ms = ak;
    ak.prototype.o = function () {
        return"ms"
    };
    ak.prototype.i = function () {
        return this.a[1]
    };
    ak.prototype.O = function (a) {
        this.a[1] = a
    };
    var bk = function (a) {
        this.a = a || ["nqr"];
        this.a[5] = this.a[5] || []
    };
    A(bk, U);
    V.nqr = bk;
    r = bk.prototype;
    r.o = function () {
        return"nqr"
    };
    r.$e = function () {
        return this.a[1]
    };
    r.Ac = function (a) {
        this.a[1] = a
    };
    r.$g = function () {
        return this.a[2]
    };
    r.setStart = function (a) {
        this.a[2] = a
    };
    r.Xg = function () {
        return this.a[3]
    };
    r.setEnd = function (a) {
        this.a[3] = a
    };
    r.Zg = function () {
        return this.a[4]
    };
    r.dh = function (a) {
        this.a[4] = a
    };
    r.Pd = function () {
        if (!this.Gb) {
            this.Gb = [];
            for (var a = 0; a < this.a[5].length; a++)this.Gb[a] = new ck(this.a[5][a])
        }
        return this.Gb
    };
    var ck = function (a) {
        this.a = a || [];
        this.a[1] = this.a[1] || []
    };
    A(ck, U);
    r = ck.prototype;
    r.i = function () {
        return this.a[0]
    };
    r.O = function (a) {
        this.a[0] = a
    };
    r.Yg = function () {
        return this.a[1]
    };
    r.ah = function (a) {
        a = a || [];
        this.a[1] = a
    };
    r.Ze = function () {
        !this.Bf && this.a[2] && (this.Bf = new hg(this.a[2]));
        return this.Bf
    };
    r.bh = function (a) {
        this.Bf = a;
        this.a[2] = a ? a.a : a
    };
    var dk = function (a) {
        !a.Vh && a.a[3] && (a.Vh = new ig(a.a[3]));
        return a.Vh
    }, ek = function (a) {
        !a.Yh && a.a[4] && (a.Yh = new Sg(a.a[4]));
        return a.Yh
    };
    var fk = function (a) {
        this.a = a || ["p"]
    };
    A(fk, U);
    V.p = fk;
    r = fk.prototype;
    r.o = function () {
        return"p"
    };
    r.i = function () {
        return this.a[1]
    };
    r.O = function (a) {
        this.a[1] = a
    };
    r.Sc = function () {
        return this.a[2]
    };
    r.Rc = function (a) {
        this.a[2] = a
    };
    r.fb = function () {
        return this.a[3]
    };
    r.qh = function (a) {
        this.a[3] = a
    };
    r.nh = function () {
        return this.a[4]
    };
    r.oh = function (a) {
        this.a[4] = a
    };
    r.Qc = function () {
        return this.a[5]
    };
    r.Nd = function (a) {
        this.a[5] = a
    };
    var gk = function (a) {
        this.a = a || ["m"]
    };
    A(gk, U);
    V.m = gk;
    r = gk.prototype;
    r.o = function () {
        return"m"
    };
    r.i = function () {
        return this.a[1]
    };
    r.O = function (a) {
        this.a[1] = a
    };
    r.sh = function () {
        return this.a[2]
    };
    r.Wa = function () {
        return this.a[3]
    };
    var hk = function (a) {
        this.a = a || ["ra"];
        this.a[18] = this.a[18] || []
    };
    A(hk, U);
    V.ra = hk;
    r = hk.prototype;
    r.o = function () {
        return"ra"
    };
    r.i = function () {
        return this.a[1]
    };
    r.O = function (a) {
        this.a[1] = a
    };
    r.getName = function () {
        return this.a[2]
    };
    r.sb = function (a) {
        this.a[2] = a
    };
    r.Sb = function () {
        return this.a[3]
    };
    r.Bc = function (a) {
        this.a[3] = a
    };
    r.hi = function () {
        return this.a[4]
    };
    r.Rg = function (a) {
        this.a[4] = a
    };
    r.Qg = function (a) {
        this.a[5] = a
    };
    r.Ag = function () {
        return this.a[6]
    };
    r.Tg = function (a) {
        this.a[6] = a
    };
    r.Sg = function (a) {
        this.a[7] = a
    };
    r.Ug = function (a) {
        this.a[8] = a
    };
    r.rb = function () {
        return this.a[9]
    };
    r.Ed = function (a) {
        this.a[9] = a
    };
    r.Wg = function (a) {
        this.a[12] = a
    };
    r.Bg = function () {
        return this.a[13]
    };
    r.Vg = function (a) {
        this.a[13] = a
    };
    r.Cg = function () {
        return this.a[14]
    };
    r.Ia = function () {
        return this.a[15]
    };
    r.Cc = function (a) {
        this.a[15] = a
    };
    r.zg = function () {
        return this.a[17]
    };
    r.Pe = function () {
        return this.a[18]
    };
    var ik = function (a) {
        this.a = a || ["qr"];
        this.a[5] = this.a[5] || []
    };
    A(ik, U);
    V.qr = ik;
    r = ik.prototype;
    r.o = function () {
        return"qr"
    };
    r.$e = function () {
        return this.a[1]
    };
    r.Ac = function (a) {
        this.a[1] = a
    };
    r.$g = function () {
        return this.a[2]
    };
    r.setStart = function (a) {
        this.a[2] = a
    };
    r.Xg = function () {
        return this.a[3]
    };
    r.setEnd = function (a) {
        this.a[3] = a
    };
    r.Zg = function () {
        return this.a[4]
    };
    r.dh = function (a) {
        this.a[4] = a
    };
    r.Pd = function () {
        if (!this.Gb) {
            this.Gb = [];
            for (var a = 0; a < this.a[5].length; a++)this.Gb[a] = new jk(this.a[5][a])
        }
        return this.Gb
    };
    var jk = function (a) {
        this.a = a || ["c"];
        this.a[1] = this.a[1] || []
    };
    A(jk, U);
    V.c = jk;
    r = jk.prototype;
    r.o = function () {
        return"c"
    };
    r.Yg = function () {
        return this.a[1]
    };
    r.ah = function (a) {
        a = a || [];
        this.a[1] = a
    };
    r.Ze = function () {
        return this.a[4]
    };
    r.bh = function (a) {
        this.a[4] = a
    };
    var kk = function (a) {
        R.call(this, this);
        this.Vb = a.get("ha");
        this.ub = a.get("na");
        this.Ja = a.get("ja");
        this.qb = a.get("oa");
        this.Z = [];
        S(this, a.get("oa"), "la", this.Sk);
        this.Ja.tb.contains(Tf.Hd) || S(this, a.get("oa"), "qa", this.Tk)
    };
    A(kk, R);
    kk.prototype.translate = function (a) {
        switch (a[0]) {
            case "nqr":
                var a = new bk(a), b = new ik;
                b.Ac(a.$e());
                b.setStart(a.$g());
                b.setEnd(a.Xg());
                b.dh(a.Zg());
                if (0 < a.Pd().length) {
                    for (var c = 0; c < a.Pd().length; ++c) {
                        var d = a.Pd()[c], e = new jk;
                        e.ah(d.Yg());
                        dk(d) && this.Vb.handle(ye(dk(d)));
                        ek(d) && this.Vb.handle(ye(ek(d)));
                        var g = d.i();
                        X(this.qb, g) && (lk(this, g), g = new hk(this.Z.pop()), e.a[2] = g ? g.a : g, e.a[3] = g.a[16], e.a[3] = g.a[16] ? 1 : 0, d.Ze() && (mk(this, d.Ze()), e.bh(this.Z.pop())), b.a[c + 5] = e.a)
                    }
                    this.Z.push(b.a)
                } else this.Z.push(b.a.slice(0,
                    5));
                break;
            case "p":
                mk(this, new hg(a));
                break;
            case "cs":
                this.Z.push(a);
                this.Z.push(ye(nk(a)));
                break;
            case "otr":
            case "ru":
            case "vc":
            case "cds":
                this.Vb.handle(a);
                break;
            case "sc":
            case "su":
                for (b = 1; b < a.length; ++b)sh(this.ub, a[b][0], a[b][1]);
                this.Z.push(a);
                break;
            case "cu":
                b = this.qb;
                d = a[1];
                c = X(b, a[2]);
                d /= 1E3;
                c && c.Id < d && (Fg(b.M, c), c.Je(d), Gg(b.M, c, j));
                this.Z.push(a);
                break;
            case "ql":
                this.Ja.tb.contains(Tf.Hd) && (a.shift(), Bh(this.qb, a), this.Z.push(a));
                break;
            default:
                this.Z.push(a)
        }
        a = this.Z;
        this.Z = [];
        return a
    };
    kk.prototype.Sk = function (a) {
        lk(this, a.Ne.i().toString())
    };
    kk.prototype.Tk = function () {
        this.Z.push(["ql"].concat(zg(this.qb.M)))
    };
    var lk = function (a, b) {
        var c = X(a.qb, b), d = new hk;
        d.O(c.i().toString());
        d.sb(c.Ba);
        d.Bc(c.Sb());
        var e = c.Pg();
        d.Rg("/image?h" + e.substring(e.indexOf("=")));
        d.Tg(c.Cd);
        d.Ed(c.rb());
        d.a[10] = c.Te || m;
        d.Vg(c.vg);
        d.Cc(parseInt(c.Ia(), 10));
        d.a[16] = "B" != c.rb() && (2 == c.Od || 4 == c.Od);
        d.Qg(m);
        d.Ug(1);
        d.Sg(1);
        d.a[11] = m;
        d.Wg("");
        d.a[14] = [];
        a.Z.push(d.a)
    }, mk = function (a, b) {
        var c = new fk, d = b.i(), e = d.indexOf("/");
        0 < e && (d = d.substring(0, e));
        c.O(d);
        switch (b.Sc()) {
            case 1:
                e = "u";
                break;
            case 2:
            case 3:
                e = "i";
                break;
            case 4:
                e = "b";
                break;
            default:
                e = "a"
        }
        c.Rc(e);
        c.qh(b.fb() || "");
        c.oh(b.nh());
        c.Nd(b.Qc());
        a.Z.push(c.a);
        c = X(a.qb, d);
        t(c) && t(b.a[7]) && c.Ve(b.a[7])
    }, nk = function (a) {
        var b = new Hg(a), a = new ak;
        a.O(b.i());
        switch (b.af()) {
            case 1:
                b = "c";
                break;
            case 2:
                b = "p";
                break;
            case 3:
                b = "i";
                break;
            case 4:
                b = "g";
                break;
            default:
                b = "a"
        }
        a.a[2] = b;
        return a
    };
    var pk = function (a, b, c, d, e, g, h, l, p, n, q, v, aa, D, ne) {
        R.call(this);
        if (!(b = Ne(h, "ha")))b = new tf, h.$("ha", b);
        this.Bj = b;
        !h.la.ka && !h.Sd.ka && (b = y(this.Ye, this), l = y(this.wg, this), b = new gg(b, l), h.$("ka", b));
        Ne(h, "na") || (b = new th(h), h.$("na", b));
        Ne(h, "ia") || (b = new Qf(h), h.$("ia", b));
        if (!(b = Ne(h, "oa")))b = new uh(h), h.$("oa", b);
        this.qb = b;
        this.Gj = new N(a);
        this.Hj = q || "channel";
        this.ih = c;
        this.jb = e;
        this.Ah = g;
        this.zh = v ? v : 0;
        this.Oj = p || "wcs";
        this.f = M("chat.WcsClient");
        this.bc = {};
        pb(this.bc, "clid", c);
        pb(this.bc, "gsessionid",
            d);
        pb(this.bc, "prop", this.Oj);
        0 != this.zh && pb(this.bc, "authuser", this.zh);
        aa && sb(this.bc, aa);
        C || be(this, window, "beforeunload", this.Ch, j);
        be(this, window, "unload", this.Ch, j);
        this.qa = m;
        this.Ij = {};
        for (a = 0; 256 > a; ++a)c = a.toString(16), 1 == c.length && (c = "0" + c), c = "%" + c, this.Ij[unescape(c)] = a;
        this.Bh = ok(this);
        this.Pc = new de(5E3);
        S(this, this.Pc, "tick", this.Kj, m, this);
        this.Pc.start();
        if (this.Ta = n && 0 < n ? new de(n) : k)S(this, this.Ta, "tick", this.Pj, m, this), this.Ta.start();
        if (!(n = Ne(h, "assign")))n = new kk(h), h.$("assign", n);
        this.Kg =
            n;
        if (fa(Ne(h, "e"))) {
            a = this.Gj;
            n = this.Hj + "/";
            a instanceof N || (a = qd(a));
            n instanceof N || (n = qd(n));
            d = a;
            a = d.K();
            (c = !!n.aa) ? Vc(a, n.aa) : c = !!n.Ab;
            c ? Wc(a, n.Ab) : c = !!n.Y;
            c ? Xc(a, n.Q()) : c = n.Ca != k;
            e = n.H;
            if (c)Yc(a, n.Ca); else if (c = !!n.H)if ("/" != e.charAt(0) && (d.Y && !d.H ? e = "/" + e : (d = a.H.lastIndexOf("/"), -1 != d && (e = a.H.substr(0, d + 1) + e))), ".." == e || "." == e)e = ""; else if (-1 != e.indexOf("./") || -1 != e.indexOf("/.")) {
                d = 0 == e.lastIndexOf("/", 0);
                e = e.split("/");
                g = [];
                for (p = 0; p < e.length;)q = e[p++], "." == q ? d && p == e.length && g.push("") : ".." ==
                    q ? ((1 < g.length || 1 == g.length && "" != g[0]) && g.pop(), d && p == e.length && g.push("")) : (g.push(q), d = j);
                e = g.join("/")
            }
            c ? Zc(a, e) : c = "" !== n.U.toString();
            c ? a.Ac(kd(n)) : c = !!n.zb;
            c && ad(a, n.zb);
            this.D = new Si(D ? a.toString() : a.H, "1", i, i, i, ne)
        } else this.D = h.get("e");
        this.D.hf(this);
        this.Re = h;
        h = this.D;
        D = h.Ec.get("c");
        D || (D = new Ti(h, "c"), h.Ec.set("c", D));
        this.Qd = D;
        S(this, this.D, "ta", this.Jj, j, this);
        S(this, this.D, "wa", this.Nj, j, this);
        S(this, this.D, "va", this.Lj, j, this);
        S(this, this.Qd, "sa", this.Mj, j, this)
    };
    A(pk, R);
    r = pk.prototype;
    r.jl = da;
    r.Ch = function () {
        this.kl || (this.kl = j, this.jl(), G(this))
    };
    r.g = function () {
        pk.z.g.call(this);
        G(this.Bj);
        G(this.Kg);
        this.D.Vf(this);
        this.D.I();
        this.Pc && (this.Pc.I(), this.Pc = k);
        this.Ta && (this.Ta.I(), this.Ta = k);
        for (; $e.length;)$e.pop().I();
        s.onerror = k
    };
    r.jd = function () {
    };
    r.uj = function (a, b) {
        b()
    };
    r.qj = function () {
        return this.qa
    };
    var ok = function (a, b) {
        var c;
        if (b)c = b + "="; else {
            if ("WCX" == a.Ah && (c = ok(a, "WCM"), c != k))return c;
            c = a.Ah + "="
        }
        for (var d = document.cookie.split(";"), e = 0; e < d.length; ++e) {
            var g = d[e], h = g.indexOf(c);
            if (-1 < h)return g.substring(h + c.length)
        }
        return k
    };
    pk.prototype.Kj = function () {
        var a = ok(this);
        this.Bh != a && (this.Bh = a)
    };
    pk.prototype.Pj = function () {
        4 == this.D.Ea() && this.Ye(["noop"])
    };
    var qk = function (a, b) {
        a.uj(b, y(a.il, a, b))
    };
    pk.prototype.il = function (a) {
        (window.parent === window.self || !rk(this, window.parent, a)) && rk(this, window.self, a)
    };
    var rk = function (a, b, c) {
        try {
            var d = "" + b.location;
            if (d) {
                var e = new N(d);
                O(e, "v", a.jb);
                c ? O(e, "clid", a.ih) : e.U.remove("clid");
                a.f.info("Reloading:  " + e.toString());
                b.location = e.toString();
                return j
            }
        } catch (g) {
            a.f.q("tryReload error: " + g.message)
        }
        return m
    };
    pk.prototype.wg = function (a, b, c) {
        var d;
        try {
            d = ec(a.a.slice(1))
        } catch (e) {
            return this.f.w("error serializing message: " + a.a), m
        }
        return sk(this, {t:a.o(), p:d}, b, c)
    };
    pk.prototype.Ye = function (a, b, c) {
        if (a == k || !w(a))return this.f.w("sending invalid array: " + a), m;
        var d;
        a:switch (a[0]) {
            case "m":
                d = new gk(a);
                break a;
            case "ms":
                var e = new ak(a);
                d = new Hg;
                d.O(e.i());
                switch (e.a[2]) {
                    case "c":
                        e = 1;
                        break;
                    case "p":
                        e = 2;
                        break;
                    case "i":
                        e = 3;
                        break;
                    case "g":
                        e = 4;
                        break;
                    default:
                        e = 0
                }
                d.We(e);
                break a;
            default:
                d = k
        }
        if (d)return this.wg(d, b, c);
        var g;
        try {
            g = ec(a)
        } catch (h) {
            return this.f.w("error serializing array: " + a), m
        }
        return sk(this, {m:g}, b, c)
    };
    var sk = function (a, b, c, d) {
        d = d || a.ih;
        b.c = d;
        if (a.Qd.kc())return c ? a.Qd.Xa(b, 2) : (a.Qd.Xa(b), a.Ta && (a.Ta.stop(), a.Ta.start())), j;
        a.f.q("Cannot send message: " + d + ": " + b);
        return m
    };
    r = pk.prototype;
    r.Wb = function () {
        this.f.info("Connecting browser channel.");
        this.D.T()
    };
    r.Jj = function (a) {
        this.f.info("Browser channel state change: " + a.zk + "->" + a.Bi);
        switch (a.Bi) {
            case 4:
                this.f.info("Browser channel opened");
                this.jd(["connect-state", "open"]);
                this.Ye(["connect-add-client"]);
                break;
            case 1:
                this.f.q("Browser channel closed");
                this.jd(["connect-state", "close"]);
                this.qa = m;
                break;
            case 5:
                var b = this.D.Sf();
                fa(b) || (this.f.info("Browser channel error state: " + a.error + ". Retry @ " + b), this.jd(["connect-state", "error", b]), this.qa = m, 401 == this.D.ea() && qk(this, m))
        }
    };
    r.Lj = function (a) {
        for (var b = 0; b < a.rj.length; b++)G(a.rj[b].zf)
    };
    r.Nj = function (a) {
        this.f.q("Browser channel closed with " + a.Ni.length + " pending maps and " + a.Oi.length + " undelivered maps");
        for (var b = 0; b < a.Ni.length; b++);
        for (b = 0; b < a.Oi.length; b++);
    };
    r.Mj = function (a) {
        a = a.Gk;
        if (2 != a.length)this.f.w("Array from server is not length 2: " + a); else if ("v" == a[1][0])this.f.info("Received version: " + a[1]), this.jb != a[1][1] && qk(this, j); else {
            "pu" == a[1][0] && (this.qa = j);
            for (var b = this.Kg.translate(a[1]), c = 0; c < b.length; ++c)try {
                this.jd(["connect-data", a[0], b[c]])
            } catch (d) {
            }
        }
    };
    var WcsDataClient = function (a, b, c, d, e, g, h, l, p, n, q) {
        var l = new Me, v = new Re;
        v.Se = a;
        v.Fm = d;
        v.Gm = b;
        v.ik = c;
        v.jk = g;
        v.hk = "dch";
        fa(Ne(l, "ga")) && l.$("ga", v);
        fa(Ne(l, "ga")) && l.$("ga", v);
        b = new Sf(l);
        l.$("ja", b);
        pk.call(this, a, 0, c, d, e, g, l, 0, "data", i, "dch", i, {token:h});
        this.D = q || this.D;
        this.od = [];
        this.kd = n || new te(h);
        this.ja = k;
        p ? this.ja = p : (a = new N(window.location.href), a = ac(a.U.get("xpc")), this.ja = new ke(a));
        this.ja.T(y(this.Uf, this));
        this.vb || this.Wb()
    };
    A(WcsDataClient, pk);
    WcsDataClient.prototype.Uf = function () {
        for (var a = 0; a < this.od.length; ++a)this.ja.send(this.od[a].message, this.od[a].data);
        this.od = []
    };
    WcsDataClient.prototype.jd = function (a) {
        if (a && a[0]) {
            var b;
            if ("connect-state" == a[0])"error" == a[1] ? 401 == this.D.ea() ? qk(this, m) : (b = {message:"onError", data:{}}, b.data.c = this.D.ea(), b.data.d = "") : "closed" == a[1] && (b = {message:"onClosed", data:{}}); else if ("connect-data" == a[0]) {
                if (!a[2] || !a[2][0])return;
                var a = a[2], c = a[0];
                "ae" == c ? (a = a[1], b = {message:"onMessage", data:{}}, b.data.m = a, b.data.s = we(this.kd, a), this.kd.qd++) : "me" == c ? b = {message:"opened", data:{}} : "m" == c && "e" == a[4] && (a = a[2], b = {message:"onError", data:{}}, b.data.c = 1,
                    b.data.d = a)
            }
            b && (this.ja.ua() ? this.ja.send(b.message, b.data) : this.od.push(b))
        }
    };
    WcsDataClient.prototype.uj = function (a, b) {
        b()
    };
    assign("chat.WcsDataClient", WcsDataClient);




})()