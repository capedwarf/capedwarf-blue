(function() {
    function throwException(ex) {
        throw ex;
    }

    var j = void 0, p, q = this, aa = function(a) {
        for (var a = a.split("."), b = q, c; c = a.shift();)if (b[c] != null)b = b[c]; else return null;
        return b
    }, ba = function() {
    }, ca = function(a) {
        var b = typeof a;
        if ("object" == b)if (a) {
            if (a instanceof Array)return"array";
            if (a instanceof Object)return b;
            var c = Object.prototype.toString.call(a);
            if ("[object Window]" == c)return"object";
            if ("[object Array]" == c || "number" == typeof a.length && "undefined" != typeof a.splice && "undefined" != typeof a.propertyIsEnumerable && !a.propertyIsEnumerable("splice"))return"array";
            if ("[object Function]" ==
                c || "undefined" != typeof a.call && "undefined" != typeof a.propertyIsEnumerable && !a.propertyIsEnumerable("call"))return"function"
        } else return"null"; else if ("function" == b && "undefined" == typeof a.call)return"object";
        return b
    }, r = function(a) {
        return"array" == ca(a)
    }, s = function(a) {
        var b = ca(a);
        return"array" == b || "object" == b && "number" == typeof a.length
    }, v = function(a) {
        return"string" == typeof a
    }, da = function(a) {
        return"function" == ca(a)
    }, ea = function(a) {
        a = ca(a);
        return"object" == a || "array" == a || "function" == a
    }, ha = function(a) {
        return a[fa] ||
            (a[fa] = ++ga)
    }, fa = "closure_uid_" + Math.floor(2147483648 * Math.random()).toString(36), ga = 0, ia = function(a, b, c) {
        return a.call.apply(a.bind, arguments)
    }, ja = function(a, b, c) {
        a || throwException(Error());
        if (2 < arguments.length) {
            var d = Array.prototype.slice.call(arguments, 2);
            return function() {
                var c = Array.prototype.slice.call(arguments);
                Array.prototype.unshift.apply(c, d);
                return a.apply(b, c)
            }
        }
        return function() {
            return a.apply(b, arguments)
        }
    }, w = function(a, b, c) {
        w = Function.prototype.bind && -1 != Function.prototype.bind.toString().indexOf("native code") ? ia : ja;
        return w.apply(null, arguments)
    }, ka = function(a, b) {
        var c = Array.prototype.slice.call(arguments, 1);
        return function() {
            var b = Array.prototype.slice.call(arguments);
            b.unshift.apply(b, c);
            return a.apply(this, b)
        }
    }, la = Date.now || function() {
        return+new Date
    }, assign = function(name, value) {
        var c = name.split("."), d = q;
        !(c[0]in d) && d.execScript && d.execScript("var " + c[0]);
        for (var f; c.length && (f = c.shift());) !c.length && value !== j ? d[f] = value : d = d[f] ? d[f] : d[f] = {}
    }, y = function(a, b) {
        function c() {
        }

        c.prototype = b.prototype;
        a.D = b.prototype;
        a.prototype = new c;
        a.prototype.constructor = a
    };
    Function.prototype.bind = Function.prototype.bind || function(a, b) {
        if (1 < arguments.length) {
            var c = Array.prototype.slice.call(arguments, 1);
            c.unshift(this, a);
            return w.apply(null, c)
        }
        return w(this, a)
    };
    var ma = function() {
    };
    ma.prototype.ba = false;
    ma.prototype.V = function() {
        this.ba || (this.ba = true, this.i())
    };
    ma.prototype.i = function() {
        this.ic && na.apply(null, this.ic)
    };
    var oa = function(a) {
        a && "function" == typeof a.V && a.V()
    }, na = function(a) {
        for (var b = 0, c = arguments.length; b < c; ++b) {
            var d = arguments[b];
            s(d) ? na.apply(null, d) : oa(d)
        }
    };
    var pa = function(a) {
        this.stack = Error().stack || "";
        a && (this.message = "" + a)
    };
    y(pa, Error);
    pa.prototype.name = "CustomError";
    var qa = function(a, b) {
        for (var c = 1; c < arguments.length; c++)var d = ("" + arguments[c]).replace(/\$/g, "$$$$"), a = a.replace(/\%s/, d);
        return a
    }, ra = /^[a-zA-Z0-9\-_.!~*'()]*$/, sa = function(a) {
        a = "" + a;
        return!ra.test(a) ? encodeURIComponent(a) : a
    }, za = function(a) {
        if (!ta.test(a))return a;
        -1 != a.indexOf("&") && (a = a.replace(ua, "&amp;"));
        -1 != a.indexOf("<") && (a = a.replace(va, "&lt;"));
        -1 != a.indexOf(">") && (a = a.replace(wa, "&gt;"));
        -1 != a.indexOf('"') && (a = a.replace(ya, "&quot;"));
        return a
    }, ua = /&/g, va = /</g, wa = />/g, ya = /\"/g, ta = /[&<>\"]/;
    var Aa = function(a, b) {
        b.unshift(a);
        pa.call(this, qa.apply(null, b));
        b.shift()
    };
    y(Aa, pa);
    Aa.prototype.name = "AssertionError";
    var Ba = function(a, b, c) {
        if (!a) {
            var d = Array.prototype.slice.call(arguments, 2), f = "Assertion failed";
            if (b)var f = f + (": " + b), e = d;
            throwException(new Aa("" + f, e || []))
        }
    }, Ca = function(a, b) {
        throwException(new Aa("Failure" + (a ? ": " + a : ""), Array.prototype.slice.call(arguments, 1)))
    };
    var z = Array.prototype, Da = z.indexOf ? function(a, b, c) {
        Ba(a.length != null);
        return z.indexOf.call(a, b, c)
    } : function(a, b, c) {
        c = c == null ? 0 : 0 > c ? Math.max(0, a.length + c) : c;
        if (v(a))return!v(b) || 1 != b.length ? -1 : a.indexOf(b, c);
        for (; c < a.length; c++)if (c in a && a[c] === b)return c;
        return-1
    }, Ea = z.forEach ? function(a, b, c) {
        Ba(a.length != null);
        z.forEach.call(a, b, c)
    } : function(a, b, c) {
        for (var d = a.length, f = v(a) ? a.split("") : a, e = 0; e < d; e++)e in f && b.call(c, f[e], e, a)
    }, Fa = function(a, b) {
        var c = Da(a, b);
        0 <= c && (Ba(a.length != null), z.splice.call(a, c, 1))
    }, Ga = function(a) {
        return z.concat.apply(z, arguments)
    }, Ha = function(a) {
        if (r(a))return Ga(a);
        for (var b = [], c = 0, d = a.length; c < d; c++)b[c] = a[c];
        return b
    }, Ia = function(a, b) {
        for (var c = 1; c < arguments.length; c++) {
            var d = arguments[c], f;
            if (r(d) || (f = s(d)) && d.hasOwnProperty("callee"))a.push.apply(a, d); else if (f)for (var e = a.length, g = d.length, i = 0; i < g; i++)a[e + i] = d[i]; else a.push(d)
        }
    }, Ja = function(a, b, c) {
        Ba(a.length != null);
        return 2 >= arguments.length ? z.slice.call(a, b) : z.slice.call(a, b, c)
    };
    var Ka = function(a, b) {
        for (var c in a)b.call(j, a[c], c, a)
    }, La = function(a) {
        var b = [], c = 0, d;
        for (d in a)b[c++] = a[d];
        return b
    }, Ma = function(a) {
        var b = [], c = 0, d;
        for (d in a)b[c++] = d;
        return b
    }, Na = "constructor,hasOwnProperty,isPrototypeOf,propertyIsEnumerable,toLocaleString,toString,valueOf".split(","), Oa = function(a, b) {
        for (var c, d, f = 1; f < arguments.length; f++) {
            d = arguments[f];
            for (c in d)a[c] = d[c];
            for (var e = 0; e < Na.length; e++)c = Na[e], Object.prototype.hasOwnProperty.call(d, c) && (a[c] = d[c])
        }
    };
    var Pa, Qa, Ra, Sa, Ta = function() {
        return q.navigator ? q.navigator.userAgent : null
    };
    Sa = Ra = Qa = Pa = false;
    var Ua;
    if (Ua = Ta()) {
        var Va = q.navigator;
        Pa = 0 == Ua.indexOf("Opera");
        Qa = !Pa && -1 != Ua.indexOf("MSIE");
        Ra = !Pa && -1 != Ua.indexOf("WebKit");
        Sa = !Pa && !Ra && "Gecko" == Va.product
    }
    var Wa = Pa, A = Qa, B = Sa, C = Ra, Xa;
    a:{
        var Ya = "", Za;
        if (Wa && q.opera)var $a = q.opera.version, Ya = "function" == typeof $a ? $a() : $a; else if (B ? Za = /rv\:([^\);]+)(\)|;)/ : A ? Za = /MSIE\s+([^\);]+)(\)|;)/ : C && (Za = /WebKit\/(\S+)/), Za)var ab = Za.exec(Ta()), Ya = ab ? ab[1] : "";
        if (A) {
            var bb, cb = q.document;
            bb = cb ? cb.documentMode : j;
            if (bb > parseFloat(Ya)) {
                Xa = "" + bb;
                break a
            }
        }
        Xa = Ya
    }
    var db = Xa, eb = {}, D = function(a) {
        var b;
        if (!(b = eb[a])) {
            b = 0;
            for (var c = ("" + db).replace(/^[\s\xa0]+|[\s\xa0]+$/g, "").split("."), d = ("" + a).replace(/^[\s\xa0]+|[\s\xa0]+$/g, "").split("."), f = Math.max(c.length, d.length), e = 0; 0 == b && e < f; e++) {
                var g = c[e] || "", i = d[e] || "", m = RegExp("(\\d*)(\\D*)", "g"), n = RegExp("(\\d*)(\\D*)", "g");
                do{
                    var t = m.exec(g) || ["","",""], u = n.exec(i) || ["","",""];
                    if (0 == t[0].length && 0 == u[0].length)break;
                    b = ((0 == t[1].length ? 0 : parseInt(t[1], 10)) < (0 == u[1].length ? 0 : parseInt(u[1], 10)) ? -1 : (0 == t[1].length ?
                        0 : parseInt(t[1], 10)) > (0 == u[1].length ? 0 : parseInt(u[1], 10)) ? 1 : 0) || ((0 == t[2].length) < (0 == u[2].length) ? -1 : (0 == t[2].length) > (0 == u[2].length) ? 1 : 0) || (t[2] < u[2] ? -1 : t[2] > u[2] ? 1 : 0)
                } while (0 == b)
            }
            b = eb[a] = 0 <= b
        }
        return b
    }, fb = {}, gb = function() {
        return fb[9] || (fb[9] = A && document.documentMode && 9 <= document.documentMode)
    };
    var hb, ib = !A || gb();
    !B && !A || A && gb() || B && D("1.9.1");
    A && D("9");
    var jb = function(a, b) {
        var c;
        c = (c = a.className) && "function" == typeof c.split ? c.split(/\s+/) : [];
        var d = Ja(arguments, 1), f;
        f = c;
        for (var e = 0, g = 0; g < d.length; g++)0 <= Da(f, d[g]) || (f.push(d[g]), e++);
        f = e == d.length;
        a.className = c.join(" ");
        return f
    };
    var E = function(a) {
        return a ? new kb(9 == a.nodeType ? a : a.ownerDocument || a.document) : hb || (hb = new kb)
    }, lb = function(a, b) {
        var c = b && "*" != b ? b.toUpperCase() : "";
        return a.querySelectorAll && a.querySelector && (!C || "CSS1Compat" == document.compatMode || D("528")) && c ? a.querySelectorAll(c + "") : a.getElementsByTagName(c || "*")
    }, nb = function(a, b) {
        Ka(b, function(b, d) {
            "style" == d ? a.style.cssText = b : "class" == d ? a.className = b : "for" == d ? a.htmlFor = b : d in mb ? a.setAttribute(mb[d], b) : 0 == d.lastIndexOf("aria-", 0) ? a.setAttribute(d, b) : a[d] = b
        })
    }, mb = {cellpadding:"cellPadding",cellspacing:"cellSpacing",colspan:"colSpan",rowspan:"rowSpan",valign:"vAlign",height:"height",width:"width",usemap:"useMap",frameborder:"frameBorder",maxlength:"maxLength",type:"type"}, pb = function(a, b, c) {
        function d(c) {
            c && b.appendChild(v(c) ? a.createTextNode(c) : c)
        }

        for (var f = 2; f < c.length; f++) {
            var e = c[f];
            s(e) && !(ea(e) && 0 < e.nodeType) ? Ea(ob(e) ? Ha(e) : e, d) : d(e)
        }
    }, qb = function(a) {
        return a && a.parentNode ? a.parentNode.removeChild(a) : null
    }, ob = function(a) {
        if (a && "number" == typeof a.length) {
            if (ea(a))return"function" ==
                typeof a.item || "string" == typeof a.item;
            if (da(a))return"function" == typeof a.item
        }
        return false
    }, kb = function(a) {
        this.v = a || q.document || document
    };
    p = kb.prototype;
    p.qb = function(a, b, c) {
        var d = this.v, f = arguments, e = f[0], g = f[1];
        if (!ib && g && (g.name || g.type)) {
            e = ["<",e];
            g.name && e.push(' name="', za(g.name), '"');
            if (g.type) {
                e.push(' type="', za(g.type), '"');
                var i = {};
                Oa(i, g);
                g = i;
                delete g.type
            }
            e.push(">");
            e = e.join("")
        }
        e = d.createElement(e);
        g && (v(g) ? e.className = g : r(g) ? jb.apply(null, [e].concat(g)) : nb(e, g));
        2 < f.length && pb(d, e, f);
        return e
    };
    p.createElement = function(a) {
        return this.v.createElement(a)
    };
    p.createTextNode = function(a) {
        return this.v.createTextNode(a)
    };
    p.e = function() {
        return this.v.parentWindow || this.v.defaultView
    };
    p.appendChild = function(a, b) {
        a.appendChild(b)
    };
    p.removeNode = qb;
    var rb = function(a) {
        rb[" "](a);
        return a
    };
    rb[" "] = ba;
    var sb = function(a, b) {
        try {
            return rb(a[b]), (true)
        } catch(c) {
        }
        return false
    };
    !A || gb();
    var tb = !A || gb();
    A && D("8");
    !C || D("528");
    B && D("1.9b") || A && D("8") || Wa && D("9.5") || C && D("528");
    !B || D("8");
    var ub = function(a, b) {
        this.type = a;
        this.currentTarget = this.target = b
    };
    y(ub, ma);
    ub.prototype.i = function() {
        delete this.type;
        delete this.target;
        delete this.currentTarget
    };
    ub.prototype.Qa = false;
    ub.prototype.ec = true;
    var vb = function(a, b) {
        a && this.qa(a, b)
    };
    y(vb, ub);
    p = vb.prototype;
    p.target = null;
    p.relatedTarget = null;
    p.offsetX = 0;
    p.offsetY = 0;
    p.clientX = 0;
    p.clientY = 0;
    p.screenX = 0;
    p.screenY = 0;
    p.button = 0;
    p.keyCode = 0;
    p.charCode = 0;
    p.ctrlKey = false;
    p.altKey = false;
    p.shiftKey = false;
    p.metaKey = false;
    p.ra = null;
    p.qa = function(a, b) {
        var c = this.type = a.type;
        ub.call(this, c);
        this.target = a.target || a.srcElement;
        this.currentTarget = b;
        var d = a.relatedTarget;
        d ? B && (sb(d, "nodeName") || (d = null)) : "mouseover" == c ? d = a.fromElement : "mouseout" == c && (d = a.toElement);
        this.relatedTarget = d;
        this.offsetX = C || a.offsetX !== j ? a.offsetX : a.layerX;
        this.offsetY = C || a.offsetY !== j ? a.offsetY : a.layerY;
        this.clientX = a.clientX !== j ? a.clientX : a.pageX;
        this.clientY = a.clientY !== j ? a.clientY : a.pageY;
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
        this.ra = a;
        delete this.ec;
        delete this.Qa
    };
    p.i = function() {
        vb.D.i.call(this);
        this.relatedTarget = this.currentTarget = this.target = this.ra = null
    };
    var wb = function() {
    }, xb = 0;
    p = wb.prototype;
    p.key = 0;
    p.X = false;
    p.yb = false;
    p.qa = function(a, b, c, d, f, e) {
        da(a) ? this.xb = true : a && a.handleEvent && da(a.handleEvent) ? this.xb = false : throwException(Error("Invalid listener argument"));
        this.ja = a;
        this.rb = b;
        this.src = c;
        this.type = d;
        this.capture = !!f;
        this.Ma = e;
        this.yb = false;
        this.key = ++xb;
        this.X = false
    };
    p.handleEvent = function(a) {
        return this.xb ? this.ja.call(this.Ma || this.src, a) : this.ja.handleEvent.call(this.ja, a)
    };
    var yb = {}, F = {}, zb = {}, Ab = {}, Bb = function(a, b, c, d, f) {
        if (b)if (r(b))for (var e = 0; e < b.length; e++)Bb(a, b[e], c, d, f); else {
            var d = !!d, g = F;
            b in g || (g[b] = {f:0,K:0});
            g = g[b];
            d in g || (g[d] = {f:0,K:0}, g.f++);
            var g = g[d], i = ha(a), m;
            g.K++;
            if (g[i]) {
                m = g[i];
                for (e = 0; e < m.length; e++)if (g = m[e], g.ja == c && g.Ma == f) {
                    if (g.X)break;
                    return
                }
            } else m = g[i] = [], g.f++;
            e = Cb();
            e.src = a;
            g = new wb;
            g.qa(c, e, a, b, d, f);
            c = g.key;
            e.key = c;
            m.push(g);
            yb[c] = g;
            zb[i] || (zb[i] = []);
            zb[i].push(g);
            a.addEventListener ? (a == q || !a.Wb) && a.addEventListener(b, e, d) : a.attachEvent(b in
                Ab ? Ab[b] : Ab[b] = "on" + b, e)
        } else throwException(Error("Invalid event type"))
    }, Cb = function() {
        var a = Db, b = tb ? function(c) {
            return a.call(b.src, b.key, c)
        } : function(c) {
            c = a.call(b.src, b.key, c);
            if (!c)return c
        };
        return b
    }, Eb = function(a, b, c, d, f) {
        if (r(b))for (var e = 0; e < b.length; e++)Eb(a, b[e], c, d, f); else {
            d = !!d;
            a:{
                e = F;
                if (b in e && (e = e[b], d in e && (e = e[d], a = ha(a), e[a]))) {
                    a = e[a];
                    break a
                }
                a = null
            }
            if (a)for (e = 0; e < a.length; e++)if (a[e].ja == c && a[e].capture == d && a[e].Ma == f) {
                Fb(a[e].key);
                break
            }
        }
    }, Fb = function(a) {
        if (yb[a]) {
            var b = yb[a];
            if (!b.X) {
                var c =
                    b.src, d = b.type, f = b.rb, e = b.capture;
                c.removeEventListener ? (c == q || !c.Wb) && c.removeEventListener(d, f, e) : c.detachEvent && c.detachEvent(d in Ab ? Ab[d] : Ab[d] = "on" + d, f);
                c = ha(c);
                f = F[d][e][c];
                if (zb[c]) {
                    var g = zb[c];
                    Fa(g, b);
                    0 == g.length && delete zb[c]
                }
                b.X = true;
                f.tb = true;
                Gb(d, e, c, f);
                delete yb[a]
            }
        }
    }, Gb = function(a, b, c, d) {
        if (!d.va && d.tb) {
            for (var f = 0, e = 0; f < d.length; f++)d[f].X ? d[f].rb.src = null : (f != e && (d[e] = d[f]), e++);
            d.length = e;
            d.tb = false;
            0 == e && (delete F[a][b][c], F[a][b].f--, 0 == F[a][b].f && (delete F[a][b], F[a].f--), 0 == F[a].f && delete F[a])
        }
    }, Ib = function(a, b, c, d, f) {
        var e = 1, b = ha(b);
        if (a[b]) {
            a.K--;
            a = a[b];
            a.va ? a.va++ : a.va = 1;
            try {
                for (var g = a.length, i = 0; i < g; i++) {
                    var m = a[i];
                    m && !m.X && (e &= Hb(m, f) !== false)
                }
            } finally {
                a.va--, Gb(c, d, b, a)
            }
        }
        return Boolean(e)
    }, Hb = function(a, b) {
        var c = a.handleEvent(b);
        a.yb && Fb(a.key);
        return c
    }, Db = function(a, b) {
        if (!yb[a])return true;
        var c = yb[a], d = c.type, f = F;
        if (!(d in f))return true;
        var f = f[d], e, g;
        if (!tb) {
            e = b || aa("window.event");
            var i = true in f, m = false in f;
            if (i) {
                if (0 > e.keyCode || e.returnValue != j)return true;
                a:{
                    var n = false;
                    if (0 == e.keyCode)try {
                        e.keyCode =
                            -1;
                        break a
                    } catch(t) {
                        n = true
                    }
                    if (n || e.returnValue == j)e.returnValue = true
                }
            }
            n = new vb;
            n.qa(e, this);
            e = true;
            try {
                if (i) {
                    for (var u = [], xa = n.currentTarget; xa; xa = xa.parentNode)u.push(xa);
                    g = f[true];
                    g.K = g.f;
                    for (var H = u.length - 1; !n.Qa && 0 <= H && g.K; H--)n.currentTarget = u[H], e &= Ib(g, u[H], d, true, n);
                    if (m) {
                        g = f[false];
                        g.K = g.f;
                        for (H = 0; !n.Qa && H < u.length && g.K; H++)n.currentTarget = u[H], e &= Ib(g, u[H], d, false, n)
                    }
                } else e = Hb(c, n)
            } finally {
                u && (u.length = 0), n.V()
            }
            return e
        }
        d = new vb(b, this);
        try {
            e = Hb(c, d)
        } finally {
            d.V()
        }
        return e
    };
    var Jb = function(a) {
        a = "" + a;
        if (/^\s*$/.test(a) ? 0 : /^[\],:{}\s\u2028\u2029]*$/.test(a.replace(/\\["\\\/bfnrtu]/g, "@").replace(/"[^"\\\n\r\u2028\u2029\x00-\x08\x10-\x1f\x80-\x9f]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g, "]").replace(/(?:^|:|,)(?:[\s\u2028\u2029]*\[)+/g, "")))try {
            return eval("(" + a + ")")
        } catch(b) {
        }
        throwException(Error("Invalid JSON string: " + a))
    }, Kb = function() {
        this.Ca = j
    }, Mb = function(a) {
        var b = [];
        Lb(new Kb, a, b);
        return b.join("")
    }, Lb = function(a, b, c) {
        switch (typeof b) {
            case "string":
                Nb(b, c);
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
                if (b == null) {
                    c.push("null");
                    break
                }
                if (r(b)) {
                    var d = b.length;
                    c.push("[");
                    for (var f = "", e = 0; e < d; e++)c.push(f), f = b[e], Lb(a, a.Ca ? a.Ca.call(b, "" + e, f) : f, c), f = ",";
                    c.push("]");
                    break
                }
                c.push("{");
                d = "";
                for (e in b)Object.prototype.hasOwnProperty.call(b, e) && (f = b[e], "function" != typeof f && (c.push(d), Nb(e, c), c.push(":"), Lb(a, a.Ca ? a.Ca.call(b, e, f) : f, c), d = ","));
                c.push("}");
                break;
            case "function":
                break;
            default:
                throwException(Error("Unknown type: " + typeof b))
        }
    }, Ob = {'"':'\\"',"\\":"\\\\","/":"\\/","\u0008":"\\b","\u000c":"\\f","\n":"\\n","\r":"\\r","\t":"\\t","\x0B":"\\u000b"}, Pb = /\uffff/.test("\uffff") ? /[\\\"\x00-\x1f\x7f-\uffff]/g : /[\\\"\x00-\x1f\x7f-\xff]/g, Nb = function(a, b) {
        b.push('"', a.replace(Pb, function(a) {
            if (a in Ob)return Ob[a];
            var b = a.charCodeAt(0), f = "\\u";
            16 > b ? f += "000" : 256 > b ? f += "00" : 4096 > b && (f += "0");
            return Ob[a] = f + b.toString(16)
        }), '"')
    };
    var Qb = "StopIteration"in q ? q.StopIteration : Error("StopIteration"), Rb = function() {
    };
    Rb.prototype.next = function() {
        throwException(Qb)
    };
    Rb.prototype.hc = function() {
        return this
    };
    var Sb = function(a) {
        if ("function" == typeof a.L)return a.L();
        if (v(a))return a.split("");
        if (s(a)) {
            for (var b = [], c = a.length, d = 0; d < c; d++)b.push(a[d]);
            return b
        }
        return La(a)
    }, Tb = function(a, b, c) {
        if ("function" == typeof a.forEach)a.forEach(b, c); else if (s(a) || v(a))Ea(a, b, c); else {
            var d;
            if ("function" == typeof a.Y)d = a.Y(); else if ("function" != typeof a.L)if (s(a) || v(a)) {
                d = [];
                for (var f = a.length, e = 0; e < f; e++)d.push(e)
            } else d = Ma(a); else d = j;
            for (var f = Sb(a), e = f.length, g = 0; g < e; g++)b.call(c, f[g], d && d[g], a)
        }
    };
    var G = function(a, b) {
        this.z = {};
        this.j = [];
        var c = arguments.length;
        if (1 < c) {
            c % 2 && throwException(Error("Uneven number of arguments"));
            for (var d = 0; d < c; d += 2)this.set(arguments[d], arguments[d + 1])
        } else if (a) {
            a instanceof G ? (c = a.Y(), d = a.L()) : (c = Ma(a), d = La(a));
            for (var f = 0; f < c.length; f++)this.set(c[f], d[f])
        }
    };
    p = G.prototype;
    p.f = 0;
    p.sa = 0;
    p.L = function() {
        Ub(this);
        for (var a = [], b = 0; b < this.j.length; b++)a.push(this.z[this.j[b]]);
        return a
    };
    p.Y = function() {
        Ub(this);
        return this.j.concat()
    };
    p.M = function(a) {
        return Vb(this.z, a)
    };
    p.remove = function(a) {
        return Vb(this.z, a) ? (delete this.z[a], this.f--, this.sa++, this.j.length > 2 * this.f && Ub(this), (true)) : false
    };
    var Ub = function(a) {
        if (a.f != a.j.length) {
            for (var b = 0, c = 0; b < a.j.length;) {
                var d = a.j[b];
                Vb(a.z, d) && (a.j[c++] = d);
                b++
            }
            a.j.length = c
        }
        if (a.f != a.j.length) {
            for (var f = {}, c = b = 0; b < a.j.length;)d = a.j[b], Vb(f, d) || (a.j[c++] = d, f[d] = 1), b++;
            a.j.length = c
        }
    };
    G.prototype.get = function(a, b) {
        return Vb(this.z, a) ? this.z[a] : b
    };
    G.prototype.set = function(a, b) {
        Vb(this.z, a) || (this.f++, this.j.push(a), this.sa++);
        this.z[a] = b
    };
    G.prototype.fa = function() {
        return new G(this)
    };
    G.prototype.hc = function(a) {
        Ub(this);
        var b = 0, c = this.j, d = this.z, f = this.sa, e = this, g = new Rb;
        g.next = function() {
            for (; ;) {
                f != e.sa && throwException(Error("The map has changed since the iterator was created"));
                b >= c.length && throwException(Qb);
                var g = c[b++];
                return a ? g : d[g]
            }
        };
        return g
    };
    var Vb = function(a, b) {
        return Object.prototype.hasOwnProperty.call(a, b)
    };
    var Xb = function(a) {
        return Wb(a || arguments.callee.caller, [])
    }, Wb = function(a, b) {
        var c = [];
        if (0 <= Da(b, a))c.push("[...circular reference...]"); else if (a && 50 > b.length) {
            c.push(Yb(a) + "(");
            for (var d = a.arguments, f = 0; f < d.length; f++) {
                0 < f && c.push(", ");
                var e;
                e = d[f];
                switch (typeof e) {
                    case "object":
                        e = e ? "object" : "null";
                        break;
                    case "string":
                        break;
                    case "number":
                        e = "" + e;
                        break;
                    case "boolean":
                        e = e ? "true" : "false";
                        break;
                    case "function":
                        e = (e = Yb(e)) ? e : "[fn]";
                        break;
                    default:
                        e = typeof e
                }
                40 < e.length && (e = e.substr(0, 40) + "...");
                c.push(e)
            }
            b.push(a);
            c.push(")\n");
            try {
                c.push(Wb(a.caller, b))
            } catch(g) {
                c.push("[exception trying to get caller]\n")
            }
        } else a ? c.push("[...long stack...]") : c.push("[end]");
        return c.join("")
    }, Yb = function(a) {
        if (Zb[a])return Zb[a];
        a = "" + a;
        if (!Zb[a]) {
            var b = /function ([^\(]+)/.exec(a);
            Zb[a] = b ? b[1] : "[Anonymous]"
        }
        return Zb[a]
    }, Zb = {};
    var $b = function(a, b, c, d, f) {
        this.reset(a, b, c, d, f)
    };
    $b.prototype.wa = 0;
    $b.prototype.Ab = null;
    $b.prototype.zb = null;
    var ac = 0;
    $b.prototype.reset = function(a, b, c, d, f) {
        this.wa = "number" == typeof f ? f : ac++;
        d || la();
        this.ka = a;
        this.dc = b;
        delete this.Ab;
        delete this.zb
    };
    $b.prototype.Gb = function(a) {
        this.ka = a
    };
    var I = function(a) {
        this.Hb = a
    };
    I.prototype.za = null;
    I.prototype.ka = null;
    I.prototype.Ua = null;
    I.prototype.Ib = null;
    var J = function(a, b) {
        this.name = a;
        this.value = b
    };
    J.prototype.toString = function() {
        return this.name
    };
    var bc = new J("SEVERE", 1E3), cc = new J("WARNING", 900), dc = new J("INFO", 800), ec = new J("CONFIG", 700), fc = new J("FINE", 500), gc = new J("FINEST", 300);
    I.prototype.getName = function() {
        return this.Hb
    };
    I.prototype.getParent = function() {
        return this.za
    };
    I.prototype.Gb = function(a) {
        this.ka = a
    };
    var hc = function(a) {
        if (a.ka)return a.ka;
        if (a.za)return hc(a.za);
        Ca("Root logger has no level set.");
        return null
    };
    I.prototype.log = function(a, b, c) {
        if (a.value >= hc(this).value) {
            a = this.gc(a, b, c);
            b = "log:" + a.dc;
            q.console && (q.console.timeStamp ? q.console.timeStamp(b) : q.console.markTimeline && q.console.markTimeline(b));
            q.msWriteProfilerMark && q.msWriteProfilerMark(b);
            for (b = this; b;) {
                var c = b, d = a;
                if (c.Ib) for (var f = 0, e = j; e = c.Ib[f]; f++)e(d);
                b = b.getParent()
            }
        }
    };
    I.prototype.gc = function(a, b, c) {
        var d = new $b(a, "" + b, this.Hb);
        if (c) {
            d.Ab = c;
            var f;
            var e = arguments.callee.caller;
            try {
                var g;
                var i = aa("window.location.href");
                if (v(c))g = {message:c,name:"Unknown error",lineNumber:"Not available",fileName:i,stack:"Not available"}; else {
                    var m, n, t = false;
                    try {
                        m = c.lineNumber || c.lc || "Not available"
                    } catch(u) {
                        m = "Not available", t = true
                    }
                    try {
                        n = c.fileName || c.filename || c.sourceURL || i
                    } catch(xa) {
                        n = "Not available", t = true
                    }
                    g = t || !c.lineNumber || !c.fileName || !c.stack ? {message:c.message,name:c.name,lineNumber:m,
                        fileName:n,stack:c.stack || "Not available"} : c
                }
                f = "Message: " + za(g.message) + '\nUrl: <a href="view-source:' + g.fileName + '" target="_new">' + g.fileName + "</a>\nLine: " + g.lineNumber + "\n\nBrowser stack:\n" + za(g.stack + "-> ") + "[end]\n\nJS stack traversal:\n" + za(Xb(e) + "-> ")
            } catch(H) {
                f = "Exception trying to expose exception! You win, we lose. " + H
            }
            d.zb = f
        }
        return d
    };
    var L = function(a, b) {
        K.log(bc, a, b)
    }, M = function(a, b) {
        a.log(cc, b, j)
    };
    I.prototype.info = function(a, b) {
        this.log(dc, a, b)
    };
    var N = function(a) {
        K.log(fc, a, j)
    }, O = function(a) {
        K.log(gc, a, j)
    }, ic = {}, jc = null, kc = function(a) {
        jc || (jc = new I(""), ic[""] = jc, jc.Gb(ec));
        var b;
        if (!(b = ic[a])) {
            b = new I(a);
            var c = a.lastIndexOf("."), d = a.substr(c + 1), c = kc(a.substr(0, c));
            c.Ua || (c.Ua = {});
            c.Ua[d] = b;
            b.za = c;
            ic[a] = b
        }
        return b
    };
    var lc = function() {
        this.la = {}
    };
    y(lc, ma);
    lc.prototype.na = kc("goog.messaging.AbstractChannel");
    lc.prototype.u = function(a) {
        a && a()
    };
    lc.prototype.r = function() {
        return true
    };
    var mc = function(a, b, c) {
        a.la[b] = {eb:c,fb:false}
    };
    lc.prototype.i = function() {
        lc.D.i.call(this);
        oa(this.na);
        delete this.na;
        delete this.la;
        delete this.Ya
    };
    var nc = RegExp("^(?:([^:/?#.]+):)?(?://(?:([^/?#]*)@)?([\\w\\d\\-\\u0100-\\uffff.%]*)(?::([0-9]+))?)?([^?#]+)?(?:\\?([^#]*))?(?:#(.*))?$"), oc = function(a) {
        var b = a.match(nc), a = b[1], c = b[2], d = b[3], b = b[4], f = [];
        a && f.push(a, ":");
        d && (f.push("//"), c && f.push(c, "@"), f.push(d), b && f.push(":", b));
        return f.join("")
    };
    var P = function(a, b) {
        var c;
        a instanceof P ? (this.U(b == null ? a.w : b), Q(this, a.n), pc(this, a.ia), qc(this, a.F), rc(this, a.G), sc(this, a.J), tc(this, a.t.fa()), uc(this, a.ha)) : a && (c = ("" + a).match(nc)) ? (this.U(!!b), Q(this, c[1] || "", true), pc(this, c[2] || "", true), qc(this, c[3] || "", true), rc(this, c[4]), sc(this, c[5] || "", true), tc(this, c[6] || "", true), uc(this, c[7] || "", true)) : (this.U(!!b), this.t = new vc(null, this, this.w))
    };
    p = P.prototype;
    p.n = "";
    p.ia = "";
    p.F = "";
    p.G = null;
    p.J = "";
    p.ha = "";
    p.jc = false;
    p.w = false;
    p.toString = function() {
        if (this.p)return this.p;
        var a = [];
        this.n && a.push(wc(this.n, xc), ":");
        this.F && (a.push("//"), this.ia && a.push(wc(this.ia, xc), "@"), a.push(v(this.F) ? encodeURIComponent(this.F) : null), this.G != null && a.push(":", "" + this.G));
        this.J && (this.F && "/" != this.J.charAt(0) && a.push("/"), a.push(wc(this.J, "/" == this.J.charAt(0) ? yc : zc)));
        var b = "" + this.t;
        b && a.push("?", b);
        this.ha && a.push("#", wc(this.ha, Ac));
        return this.p = a.join("")
    };
    p.fa = function() {
        var a = this.n, b = this.ia, c = this.F, d = this.G, f = this.J, e = this.t.fa(), g = this.ha, i = new P(null, this.w);
        a && Q(i, a);
        b && pc(i, b);
        c && qc(i, c);
        d && rc(i, d);
        f && sc(i, f);
        e && tc(i, e);
        g && uc(i, g);
        return i
    };
    var Q = function(a, b, c) {
        R(a);
        delete a.p;
        a.n = c ? b ? decodeURIComponent(b) : "" : b;
        a.n && (a.n = a.n.replace(/:$/, ""))
    }, pc = function(a, b, c) {
        R(a);
        delete a.p;
        a.ia = c ? b ? decodeURIComponent(b) : "" : b
    }, qc = function(a, b, c) {
        R(a);
        delete a.p;
        a.F = c ? b ? decodeURIComponent(b) : "" : b
    }, rc = function(a, b) {
        R(a);
        delete a.p;
        b ? (b = Number(b), (isNaN(b) || 0 > b) && throwException(Error("Bad port number " + b)), a.G = b) : a.G = null
    }, sc = function(a, b, c) {
        R(a);
        delete a.p;
        a.J = c ? b ? decodeURIComponent(b) : "" : b
    }, tc = function(a, b, c) {
        R(a);
        delete a.p;
        b instanceof vc ? (a.t = b, a.t.Oa = a, a.t.U(a.w)) :
            (c || (b = wc(b, Bc)), a.t = new vc(b, a, a.w))
    }, uc = function(a, b, c) {
        R(a);
        delete a.p;
        a.ha = c ? b ? decodeURIComponent(b) : "" : b
    }, R = function(a) {
        a.jc && throwException(Error("Tried to modify a read-only Uri"))
    };
    P.prototype.U = function(a) {
        this.w = a;
        this.t && this.t.U(a);
        return this
    };
    var Cc = /^[a-zA-Z0-9\-_.!~*'():\/;?]*$/, wc = function(a, b) {
        var c = null;
        v(a) && (c = a, Cc.test(c) || (c = encodeURI(a)), 0 <= c.search(b) && (c = c.replace(b, Dc)));
        return c
    }, Dc = function(a) {
        a = a.charCodeAt(0);
        return"%" + (a >> 4 & 15).toString(16) + (a & 15).toString(16)
    }, xc = /[#\/\?@]/g, zc = /[\#\?:]/g, yc = /[\#\?]/g, Bc = /[\#\?@]/g, Ac = /#/g, vc = function(a, b, c) {
        this.C = a || null;
        this.Oa = b || null;
        this.w = !!c
    }, S = function(a) {
        if (!a.g && (a.g = new G, a.f = 0, a.C))for (var b = a.C.split("&"), c = 0; c < b.length; c++) {
            var d = b[c].indexOf("="), f = null, e = null;
            0 <= d ? (f = b[c].substring(0,
                d), e = b[c].substring(d + 1)) : f = b[c];
            f = decodeURIComponent(f.replace(/\+/g, " "));
            f = Ec(a, f);
            a.add(f, e ? decodeURIComponent(e.replace(/\+/g, " ")) : "")
        }
    };
    p = vc.prototype;
    p.g = null;
    p.f = null;
    p.add = function(a, b) {
        S(this);
        Fc(this);
        a = Ec(this, a);
        if (this.M(a)) {
            var c = this.g.get(a);
            r(c) ? c.push(b) : this.g.set(a, [c,b])
        } else this.g.set(a, b);
        this.f++;
        return this
    };
    p.remove = function(a) {
        S(this);
        a = Ec(this, a);
        if (this.g.M(a)) {
            Fc(this);
            var b = this.g.get(a);
            r(b) ? this.f -= b.length : this.f--;
            return this.g.remove(a)
        }
        return false
    };
    p.M = function(a) {
        S(this);
        a = Ec(this, a);
        return this.g.M(a)
    };
    p.Y = function() {
        S(this);
        for (var a = this.g.L(), b = this.g.Y(), c = [], d = 0; d < b.length; d++) {
            var f = a[d];
            if (r(f))for (var e = 0; e < f.length; e++)c.push(b[d]); else c.push(b[d])
        }
        return c
    };
    p.L = function(a) {
        S(this);
        if (a)if (a = Ec(this, a), this.M(a)) {
            var b = this.g.get(a);
            if (r(b))return b;
            a = [];
            a.push(b)
        } else a = []; else for (var b = this.g.L(), a = [], c = 0; c < b.length; c++) {
            var d = b[c];
            r(d) ? Ia(a, d) : a.push(d)
        }
        return a
    };
    p.set = function(a, b) {
        S(this);
        Fc(this);
        a = Ec(this, a);
        if (this.M(a)) {
            var c = this.g.get(a);
            r(c) ? this.f -= c.length : this.f--
        }
        this.g.set(a, b);
        this.f++;
        return this
    };
    p.get = function(a, b) {
        S(this);
        a = Ec(this, a);
        if (this.M(a)) {
            var c = this.g.get(a);
            return r(c) ? c[0] : c
        }
        return b
    };
    p.toString = function() {
        if (this.C)return this.C;
        if (!this.g)return"";
        for (var a = [], b = 0, c = this.g.Y(), d = 0; d < c.length; d++) {
            var f = c[d], e = sa(f), f = this.g.get(f);
            if (r(f))for (var g = 0; g < f.length; g++)0 < b && a.push("&"), a.push(e), "" !== f[g] && a.push("=", sa(f[g])), b++; else 0 < b && a.push("&"), a.push(e), "" !== f && a.push("=", sa(f)), b++
        }
        return this.C = a.join("")
    };
    var Fc = function(a) {
        delete a.Pa;
        delete a.C;
        a.Oa && delete a.Oa.p
    };
    vc.prototype.fa = function() {
        var a = new vc;
        this.Pa && (a.Pa = this.Pa);
        this.C && (a.C = this.C);
        this.g && (a.g = this.g.fa());
        return a
    };
    var Ec = function(a, b) {
        var c = "" + b;
        a.w && (c = c.toLowerCase());
        return c
    };
    vc.prototype.U = function(a) {
        a && !this.w && (S(this), Fc(this), Tb(this.g, function(a, c) {
            var d = c.toLowerCase();
            c != d && (this.remove(c), this.add(d, a))
        }, this));
        this.w = a
    };
    var Gc = {1:"NativeMessagingTransport",2:"FrameElementMethodTransport",3:"IframeRelayTransport",4:"IframePollingTransport",5:"FlashTransport",6:"NixTransport"}, Hc = ["pu","lru","pru","lpu","ppu"], T = {}, Jc = function(a) {
        for (var b = Ic, c = b.length, d = ""; 0 < a--;)d += b.charAt(Math.floor(Math.random() * c));
        return d
    }, Ic = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789", K = kc("goog.net.xpc");
    var U = function(a) {
        this.l = a || E()
    };
    y(U, ma);
    U.prototype.aa = 0;
    U.prototype.e = function() {
        return this.l.e()
    };
    U.prototype.getName = function() {
        return Gc[this.aa] || ""
    };
    var Kc = function(a, b) {
        this.l = b || E();
        this.a = a;
        this.ga = [];
        this.Nb = w(this.Vb, this)
    };
    y(Kc, U);
    p = Kc.prototype;
    p.aa = 2;
    p.Ka = false;
    p.u = function() {
        0 == Lc(this.a) ? (this.B = this.a.P, this.B.XPC_toOuter = w(this.hb, this)) : this.gb()
    };
    p.gb = function() {
        var a = true;
        try {
            this.B || (this.B = this.e().frameElement), this.B && this.B.XPC_toOuter && (this.Ga = this.B.XPC_toOuter, this.B.XPC_toOuter.XPC_toInner = w(this.hb, this), a = false, this.send("tp", "SETUP_ACK"), V(this.a))
        } catch(b) {
            L("exception caught while attempting setup: " + b)
        }
        a && (this.mb || (this.mb = w(this.gb, this)), this.e().setTimeout(this.mb, 100))
    };
    p.Ja = function(a) {
        0 == Lc(this.a) && !this.a.r() && "SETUP_ACK" == a ? (this.Ga = this.B.XPC_toOuter.XPC_toInner, V(this.a)) : throwException(Error("Got unexpected transport message."))
    };
    p.hb = function(a, b) {
        !this.Ka && 0 == this.ga.length ? this.a.A(a, b) : (this.ga.push({Pb:a,Ha:b}), 1 == this.ga.length && this.e().setTimeout(this.Nb, 1))
    };
    p.Vb = function() {
        for (; this.ga.length;) {
            var a = this.ga.shift();
            this.a.A(a.Pb, a.Ha)
        }
    };
    p.send = function(a, b) {
        this.Ka = true;
        this.Ga(a, b);
        this.Ka = false
    };
    p.i = function() {
        Kc.D.i.call(this);
        this.B = this.Ga = null
    };
    var W = function(a, b) {
        this.l = b || E();
        this.a = a;
        this.ea = this.a.b.ppu;
        this.Ob = this.a.b.lpu;
        this.oa = []
    }, Mc, Nc;
    y(W, U);
    W.prototype.aa = 4;
    W.prototype.pa = 0;
    W.prototype.S = false;
    W.prototype.I = false;
    var Oc = function(a) {
        return"googlexpc_" + a.a.name + "_msg"
    }, Pc = function(a) {
        return"googlexpc_" + a.a.name + "_ack"
    };
    W.prototype.u = function() {
        if (!this.ba) {
            N("transport connect called");
            if (!this.I) {
                N("initializing...");
                var a = Oc(this);
                this.R = Qc(this, a);
                this.Ea = this.e().frames[a];
                a = Pc(this);
                this.Q = Qc(this, a);
                this.Da = this.e().frames[a];
                this.I = true
            }
            if (!Rc(this, Oc(this)) || !Rc(this, Pc(this))) {
                O("foreign frames not (yet) present");
                if (1 == Lc(this.a) && !this.Lb)O("innerPeerReconnect called"), this.a.name = Jc(10), O("switching channels: " + this.a.name), Sc(this), this.I = false, this.Lb = Qc(this, "googlexpc_reconnect_" + this.a.name); else if (0 ==
                    Lc(this.a)) {
                    O("outerPeerReconnect called");
                    for (var a = this.a.o.frames, b = a.length, c = 0; c < b; c++) {
                        var d;
                        try {
                            a[c] && a[c].name && (d = a[c].name)
                        } catch(f) {
                        }
                        if (d) {
                            var e = d.split("_");
                            if (3 == e.length && "googlexpc" == e[0] && "reconnect" == e[1]) {
                                this.a.name = e[2];
                                Sc(this);
                                this.I = false;
                                break
                            }
                        }
                    }
                }
                this.e().setTimeout(w(this.u, this), 100)
            } else N("foreign frames present"), this.Wa = new Tc(this, this.a.o.frames[Oc(this)], w(this.Kb, this)), this.Va = new Tc(this, this.a.o.frames[Pc(this)], w(this.Jb, this)), this.Xa()
        }
    };
    var Qc = function(a, b) {
        O("constructing sender frame: " + b);
        var c = document.createElement("iframe"), d = c.style;
        d.position = "absolute";
        d.top = "-10px";
        d.left = "10px";
        d.width = "1px";
        d.height = "1px";
        c.id = c.name = b;
        c.src = a.ea + "#INITIAL";
        a.e().document.body.appendChild(c);
        return c
    }, Sc = function(a) {
        O("deconstructSenderFrames called");
        a.R && (a.R.parentNode.removeChild(a.R), a.R = null, a.Ea = null);
        a.Q && (a.Q.parentNode.removeChild(a.Q), a.Q = null, a.Da = null)
    }, Rc = function(a, b) {
        O("checking for receive frame: " + b);
        try {
            var c = a.a.o.frames[b];
            if (!c ||
                0 != c.location.href.indexOf(a.Ob))return false
        } catch(d) {
            return false
        }
        return true
    };
    W.prototype.Xa = function() {
        var a = this.a.o.frames;
        !a[Pc(this)] || !a[Oc(this)] ? (this.bb || (this.bb = w(this.Xa, this)), this.e().setTimeout(this.bb, 100), N("local frames not (yet) present")) : (this.$a = new Uc(this.ea, this.Ea), this.ma = new Uc(this.ea, this.Da), N("local frames ready"), this.e().setTimeout(w(function() {
            this.$a.send("SETUP");
            this.S = true;
            N("SETUP sent")
        }, this), 100))
    };
    var Vc = function(a) {
        if (a.La && a.jb) {
            if (V(a.a), a.W) {
                N("delivering queued messages (" + a.W.length + ")");
                for (var b = 0, c; b < a.W.length; b++)c = a.W[b], a.a.A(c.Sb, c.Ha);
                delete a.W
            }
        } else O("checking if connected: ack sent:" + a.La + ", ack rcvd: " + a.jb)
    };
    W.prototype.Kb = function(a) {
        O("msg received: " + a);
        if ("SETUP" == a)this.ma && (this.ma.send("SETUP_ACK"), O("SETUP_ACK sent"), this.La = true, Vc(this)); else if (this.a.r() || this.La) {
            var b = a.indexOf("|"), c = a.substring(0, b), a = a.substring(b + 1), b = c.indexOf(",");
            if (-1 == b) {
                var d;
                this.ma.send("ACK:" + c);
                Wc(this, a)
            } else d = c.substring(0, b), this.ma.send("ACK:" + d), c = c.substring(b + 1).split("/"), b = parseInt(c[0], 10), c = parseInt(c[1], 10), 1 == b && (this.Na = []), this.Na.push(a), b == c && (Wc(this, this.Na.join("")), delete this.Na)
        } else M(K,
            "received msg, but channel is not connected")
    };
    W.prototype.Jb = function(a) {
        O("ack received: " + a);
        "SETUP_ACK" == a ? (this.S = false, this.jb = true, Vc(this)) : this.a.r() ? this.S ? parseInt(a.split(":")[1], 10) == this.pa ? (this.S = false, Xc(this)) : M(K, "got ack with wrong sequence") : M(K, "got unexpected ack") : M(K, "received ack, but channel not connected")
    };
    var Xc = function(a) {
        if (!a.S && a.oa.length) {
            var b = a.oa.shift();
            ++a.pa;
            a.$a.send(a.pa + b);
            O("msg sent: " + a.pa + b);
            a.S = true
        }
    }, Wc = function(a, b) {
        var c = b.indexOf(":"), d = b.substr(0, c), c = b.substring(c + 1);
        a.a.r() ? a.a.A(d, c) : ((a.W || (a.W = [])).push({Sb:d,Ha:c}), O("queued delivery"))
    };
    W.prototype.Ba = 3800;
    W.prototype.send = function(a, b) {
        var c = a + ":" + b;
        if (!A || b.length <= this.Ba)this.oa.push("|" + c); else for (var d = b.length, f = Math.ceil(d / this.Ba), e = 0, g = 1; e < d;)this.oa.push("," + g + "/" + f + "|" + c.substr(e, this.Ba)), g++, e += this.Ba;
        Xc(this)
    };
    W.prototype.i = function() {
        W.D.i.call(this);
        var a = Yc;
        Fa(a, this.Wa);
        Fa(a, this.Va);
        this.Wa = this.Va = null;
        qb(this.R);
        qb(this.Q);
        this.Ea = this.Da = this.R = this.Q = null
    };
    var Yc = [], Zc = w(function() {
        var a = false;
        try {
            for (var b = 0, c = Yc.length; b < c; b++) {
                var d;
                if (!(d = a)) {
                    var f = Yc[b], e = f.pb.location.href;
                    if (e != f.ob) {
                        f.ob = e;
                        var g = e.split("#")[1];
                        g && (g = g.substr(1), f.Ub(decodeURIComponent(g)));
                        d = true
                    } else d = false
                }
                a = d
            }
        } catch(i) {
            if (K.info("receive_() failed: " + i), b = Yc[b].k.a, K.info("Transport Error"), b.close(), !Yc.length)return
        }
        b = la();
        a && (Mc = b);
        Nc = window.setTimeout(Zc, 1E3 > b - Mc ? 10 : 100)
    }, W), $c = function() {
        N("starting receive-timer");
        Mc = la();
        Nc && window.clearTimeout(Nc);
        Nc = window.setTimeout(Zc,
            10)
    }, Uc = function(a, b) {
        this.ea = a;
        this.Fb = b;
        this.Ta = 0
    };
    Uc.prototype.send = function(a) {
        this.Ta = ++this.Ta % 2;
        a = this.ea + "#" + this.Ta + encodeURIComponent(a);
        try {
            C ? this.Fb.location.href = a : this.Fb.location.replace(a)
        } catch(b) {
            L("sending failed", b)
        }
        $c()
    };
    var Tc = function(a, b, c) {
        this.k = a;
        this.pb = b;
        this.Ub = c;
        this.ob = this.pb.location.href.split("#")[0] + "#INITIAL";
        Yc.push(this);
        $c()
    };
    var X = function(a, b) {
        this.l = b || E();
        this.a = a;
        this.Mb = this.a.b.pru;
        this.ab = this.a.b.ifrid;
        C && ad()
    };
    y(X, U);
    if (C)var bd = [], cd = 0, ad = function() {
        cd || (cd = window.setTimeout(function() {
            dd()
        }, 1E3))
    }, dd = function(a) {
        for (var b = la(), a = a || 3E3; bd.length && b - bd[0].timestamp >= a;) {
            var c = bd.shift().Tb;
            qb(c);
            O("iframe removed")
        }
        cd = window.setTimeout(ed, 1E3)
    }, ed = function() {
        dd()
    };
    var fd = {};
    X.prototype.aa = 3;
    X.prototype.u = function() {
        this.e().xpcRelay || (this.e().xpcRelay = gd);
        this.send("tp", "SETUP")
    };
    var gd = function(a, b) {
        var c = b.indexOf(":"), d = b.substr(0, c), f = b.substr(c + 1);
        if (!A || -1 == (c = d.indexOf("|")))var e = d; else {
            var e = d.substr(0, c), d = d.substr(c + 1), c = d.indexOf("+"), g = d.substr(0, c), c = parseInt(d.substr(c + 1), 10), i = fd[g];
            i || (i = fd[g] = {Db:[],Eb:0,Cb:0});
            -1 != d.indexOf("++") && (i.Cb = c + 1);
            i.Db[c] = f;
            i.Eb++;
            if (i.Eb != i.Cb)return;
            f = i.Db.join("");
            delete fd[g]
        }
        T[a].A(e, decodeURIComponent(f))
    };
    X.prototype.Ja = function(a) {
        "SETUP" == a ? (this.send("tp", "SETUP_ACK"), V(this.a)) : "SETUP_ACK" == a && V(this.a)
    };
    X.prototype.send = function(a, b) {
        var c = encodeURIComponent(b), d = c.length;
        if (A && 1800 < d)for (var f = Math.floor(2147483648 * Math.random()).toString(36) + Math.abs(Math.floor(2147483648 * Math.random()) ^ la()).toString(36), e = 0, g = 0; e < d; g++) {
            var i = c.substr(e, 1800), e = e + 1800;
            hd(this, a, i, f + (e >= d ? "++" : "+") + g)
        } else hd(this, a, c)
    };
    var hd = function(a, b, c, d) {
        if (A) {
            var f = a.e().document.createElement("div");
            f.innerHTML = '<iframe onload="this.xpcOnload()"></iframe>';
            f = f.childNodes[0];
            f.xpcOnload = id
        } else f = a.e().document.createElement("iframe"), C ? bd.push({timestamp:la(),Tb:f}) : Bb(f, "load", id);
        var e = f.style;
        e.visibility = "hidden";
        e.width = f.style.height = "0px";
        e.position = "absolute";
        e = a.Mb;
        e += "#" + a.a.name;
        a.ab && (e += "," + a.ab);
        e += "|" + b;
        d && (e += "|" + d);
        e += ":" + c;
        f.src = e;
        a.e().document.body.appendChild(f);
        O("msg sent: " + e)
    }, id = function() {
        O("iframe-load");
        qb(this)
    };
    X.prototype.i = function() {
        X.D.i.call(this);
        C && dd(0)
    };
    var Y = function(a, b, c) {
        this.l = c || E();
        this.a = a;
        this.nb = b || "*"
    };
    y(Y, U);
    Y.prototype.I = false;
    Y.prototype.aa = 1;
    var jd = {}, kd = function(a) {
        var b = a.ra.data;
        if (!v(b))return false;
        var c = b.indexOf("|"), d = b.indexOf(":");
        if (-1 == c || -1 == d)return false;
        var f = b.substring(0, c), c = b.substring(c + 1, d), b = b.substring(d + 1);
        N("messageReceived: channel=" + f + ", service=" + c + ", payload=" + b);
        if (d = T[f])return d.A(c, b, a.ra.origin), (true);
        for (var e in T)if (a = T[e], 1 == Lc(a) && !a.r() && "tp" == c && "SETUP" == b)return N("changing channel name to " + f), a.name = f, delete T[e], T[f] = a, a.A(c, b), (true);
        K.info('channel name mismatch; message ignored"');
        return false
    };
    p = Y.prototype;
    p.Ja = function(a) {
        switch (a) {
            case "SETUP":
                this.send("tp", "SETUP_ACK");
                break;
            case "SETUP_ACK":
                V(this.a)
        }
    };
    p.u = function() {
        var a = this.e(), b = ha(a), c = jd[b];
        "number" == typeof c || (c = 0);
        0 == c && Bb(a.postMessage ? a : a.document, "message", kd, false, Y);
        jd[b] = c + 1;
        this.I = true;
        this.kb()
    };
    p.kb = function() {
        !this.a.r() && !this.ba && (this.send("tp", "SETUP"), this.e().setTimeout(w(this.kb, this), 100))
    };
    p.send = function(a, b) {
        var c = this.a.o;
        if (c) {
            var d = c.postMessage ? c : c.document;
            this.send = function(a, b) {
                N("send(): payload=" + b + " to hostname=" + this.nb);
                d.postMessage(this.a.name + "|" + a + ":" + b, this.nb)
            };
            this.send(a, b)
        } else N("send(): window not ready")
    };
    p.i = function() {
        Y.D.i.call(this);
        if (this.I) {
            var a = this.e(), b = ha(a), c = jd[b];
            jd[b] = c - 1;
            1 == c && Eb(a.postMessage ? a : a.document, "message", kd, false, Y)
        }
        delete this.send
    };
    var ld = function(a, b) {
        this.l = b || E();
        this.a = a;
        this.Za = a.at || "";
        this.cb = a.rat || "";
        var c = this.e();
        if (!c.nix_setup_complete)try {
            c.execScript("Class GCXPC____NIXVBS_wrapper\n Private m_Transport\nPrivate m_Auth\nPublic Sub SetTransport(transport)\nIf isEmpty(m_Transport) Then\nSet m_Transport = transport\nEnd If\nEnd Sub\nPublic Sub SetAuth(auth)\nIf isEmpty(m_Auth) Then\nm_Auth = auth\nEnd If\nEnd Sub\nPublic Function GetAuthToken()\n GetAuthToken = m_Auth\nEnd Function\nPublic Sub SendMessage(service, payload)\n Call m_Transport.GCXPC____NIXJS_handle_message(service, payload)\nEnd Sub\nPublic Sub CreateChannel(channel)\n Call m_Transport.GCXPC____NIXJS_create_channel(channel)\nEnd Sub\nPublic Sub GCXPC____NIXVBS_container()\n End Sub\nEnd Class\n Function GCXPC____NIXVBS_get_wrapper(transport, auth)\nDim wrap\nSet wrap = New GCXPC____NIXVBS_wrapper\nwrap.SetTransport transport\nwrap.SetAuth auth\nSet GCXPC____NIXVBS_get_wrapper = wrap\nEnd Function",
                "vbscript"), c.nix_setup_complete = true
        } catch(d) {
            L("exception caught while attempting global setup: " + d)
        }
        this.GCXPC____NIXJS_handle_message = this.Rb;
        this.GCXPC____NIXJS_create_channel = this.Qb
    };
    y(ld, U);
    p = ld.prototype;
    p.aa = 6;
    p.T = false;
    p.H = null;
    p.u = function() {
        0 == Lc(this.a) ? this.lb() : this.ib()
    };
    p.lb = function() {
        if (!this.T) {
            var a = this.a.P;
            try {
                a.contentWindow.opener = this.e().GCXPC____NIXVBS_get_wrapper(this, this.Za), this.T = true
            } catch(b) {
                L("exception caught while attempting setup: " + b)
            }
            this.T || this.e().setTimeout(w(this.lb, this), 100)
        }
    };
    p.ib = function() {
        if (!this.T) {
            try {
                var a = this.e().opener;
                if (a && "GCXPC____NIXVBS_container"in a) {
                    this.H = a;
                    if (this.H.GetAuthToken() != this.cb) {
                        L("Invalid auth token from other party");
                        return
                    }
                    this.H.CreateChannel(this.e().GCXPC____NIXVBS_get_wrapper(this, this.Za));
                    this.T = true;
                    V(this.a)
                }
            } catch(b) {
                L("exception caught while attempting setup: " + b);
                return
            }
            this.T || this.e().setTimeout(w(this.ib, this), 100)
        }
    };
    p.Qb = function(a) {
        ("unknown" != typeof a || !("GCXPC____NIXVBS_container"in a)) && L("Invalid NIX channel given to createChannel_");
        this.H = a;
        this.H.GetAuthToken() != this.cb ? L("Invalid auth token from other party") : V(this.a)
    };
    p.Rb = function(a, b) {
        this.e().setTimeout(w(function() {
            this.a.A(a, b)
        }, this), 1)
    };
    p.send = function(a, b) {
        "unknown" !== typeof this.H && L("NIX channel not connected");
        this.H.SendMessage(a, b)
    };
    p.i = function() {
        ld.D.i.call(this);
        this.H = null
    };
    var Z = function(a, b) {
        this.la = {};
        for (var c = 0, d; d = Hc[c]; c++)d in a && !/^https?:\/\//.test(a[d]) && throwException(Error("URI " + a[d] + " is invalid for field " + d));
        this.b = a;
        this.name = this.b.cn || Jc(10);
        this.l = b || E();
        this.ca = [];
        a.lpu = a.lpu || oc(this.l.e().location.href) + "/robots.txt";
        a.ppu = a.ppu || oc(a.pu || "") + "/robots.txt";
        T[this.name] = this;
        Bb(window, "unload", md);
        K.info("CrossPageChannel created: " + this.name)
    };
    y(Z, lc);
    var nd = /^%*tp$/, od = /^%+tp$/;
    p = Z.prototype;
    p.k = null;
    p.Ra = 1;
    p.r = function() {
        return 2 == this.Ra
    };
    p.o = null;
    p.P = null;
    var qd = function(a) {
        var b = document.body, c = a.b.ifrid;
        c || (c = a.b.ifrid = "xpcpeer" + Jc(4));
        var d = document.createElement("IFRAME");
        d.id = d.name = c;
        d.style.width = d.style.height = "100%";
        var f = pd(a);
        B || C ? (a.Ia = true, window.setTimeout(w(function() {
            this.Ia = false;
            b.appendChild(d);
            d.src = f.toString();
            K.info("peer iframe created (" + c + ")");
            this.da && this.u(this.Fa)
        }, a), 1)) : (d.src = f.toString(), b.appendChild(d), K.info("peer iframe created (" + c + ")"))
    }, pd = function(a) {
        var b = a.b.pu;
        v(b) && (b = a.b.pu = new P(b));
        var c = {};
        c.cn = a.name;
        c.tp =
            a.b.tp;
        a.b.lru && (c.pru = a.b.lru);
        a.b.lpu && (c.ppu = a.b.lpu);
        a.b.ppu && (c.lpu = a.b.ppu);
        a = b;
        c = Mb(c);
        R(a);
        delete a.p;
        a.t.set("xpc", c);
        return b
    };
    Z.prototype.Ia = false;
    Z.prototype.da = false;
    Z.prototype.u = function(a) {
        this.Fa = a || ba;
        if (this.Ia)K.info("connect() deferred"), this.da = true; else {
            this.da = false;
            K.info("connect()");
            this.b.ifrid && (this.P = v(this.b.ifrid) ? this.l.v.getElementById(this.b.ifrid) : this.b.ifrid);
            this.P && ((a = this.P.contentWindow) || (a = window.frames[this.b.ifrid]), this.o = a);
            this.o || (window == top && throwException(Error("CrossPageChannel: Can't connect, peer window-object not set.")), this.o = window.parent);
            if (!this.k) {
                if (!this.b.tp) {
                    var a = this.b, b;
                    if (da(document.postMessage) || da(window.postMessage) ||
                        A && window.postMessage)b = 1; else if (B)b = 2; else if (A && this.b.pru)b = 3; else {
                        var c;
                        if (c = A) {
                            c = false;
                            try {
                                b = window.opener, window.opener = {}, c = sb(window, "opener"), window.opener = b
                            } catch(d) {
                            }
                        }
                        b = c ? 6 : 4
                    }
                    a.tp = b
                }
                switch (this.b.tp) {
                    case 1:
                        this.k = new Y(this, this.b.ph, this.l);
                        break;
                    case 6:
                        this.k = new ld(this, this.l);
                        break;
                    case 2:
                        this.k = new Kc(this, this.l);
                        break;
                    case 3:
                        this.k = new X(this, this.l);
                        break;
                    case 4:
                        this.k = new W(this, this.l)
                }
                this.k ? K.info("Transport created: " + this.k.getName()) : throwException(Error("CrossPageChannel: No suitable transport found!"))
            }
            for (this.k.u(); 0 <
                this.ca.length;)this.ca.shift()()
        }
    };
    Z.prototype.close = function() {
        this.r() && (this.Ra = 3, this.k.V(), this.Fa = this.k = null, this.da = false, this.ca.length = 0, K.info('Channel "' + this.name + '" closed'))
    };
    var V = function(a) {
        a.r() || (a.Ra = 2, K.info('Channel "' + a.name + '" connected'), a.Fa())
    };
    Z.prototype.send = function(a, b) {
        this.r() ? Boolean(this.o.closed) ? (L("Peer has disappeared."), this.close()) : (ea(b) && (b = Mb(b)), this.k.send(rd(a), b)) : L("Can't send. Channel not connected.")
    };
    Z.prototype.A = function(a, b, c) {
        if (this.da)this.ca.push(w(this.A, this, a, b, c)); else {
            var d = this.b.ph;
            if (/^[\s\xa0]*$/.test(c == null ? "" : "" + c) || /^[\s\xa0]*$/.test(d == null ? "" : "" + d) || c == this.b.ph)if (this.ba)M(K, "CrossPageChannel::deliver_(): Disposed."); else if (!a || "tp" == a)this.k.Ja(b); else if (this.r()) {
                if (a = a.replace(/%[0-9a-f]{2}/gi, decodeURIComponent), a = od.test(a) ? a.substring(1) : a, c = this.la[a], c || (this.Ya ? (c = ka(this.Ya, a), d = ea(b), c = {eb:c,fb:d}) : (M(this.na, 'Unknown service name "' + a + '"'), c = null)), c) {
                    var f;
                    a:{
                        if ((d =
                            c.fb) && v(b))try {
                            f = Jb(b);
                            break a
                        } catch(e) {
                            M(this.na, "Expected JSON payload for " + a + ', was "' + b + '"');
                            f = null;
                            break a
                        } else if (!d && !v(b)) {
                            f = Mb(b);
                            break a
                        }
                        f = b
                    }
                    f != null && c.eb(f)
                }
            } else K.info("CrossPageChannel::deliver_(): Not connected."); else M(K, 'Message received from unapproved origin "' + c + '" - rejected.')
        }
    };
    var rd = function(a) {
        nd.test(a) && (a = "%" + a);
        return a.replace(/[%:|]/g, encodeURIComponent)
    }, Lc = function(a) {
        return window.parent == a.o ? 1 : 0
    };
    Z.prototype.i = function() {
        Z.D.i.call(this);
        this.close();
        this.P = this.o = null;
        delete T[this.name];
        this.ca.length = 0
    };
    var md = function() {
        for (var a in T) {
            var b = T[a];
            b && b.V()
        }
    };
    var sd = function(a, b) {
        A ? a.cssText = b : a[C ? "innerText" : "innerHTML"] = b
    };
    var td = function(token, b, c, d, f, e) {
        var d = new P(d || window.location.href), g = new P, f = f ? f : Math.floor(1E3 * Math.random()) + ".talkgadget.google.com";
        qc(g, f);
        sc(g, "/talkgadget/d");
        R(g);
        delete g.p;
        g.t.set("token", token);
        e && rc(g, e);
        var a = c || "wcs-iframe", c = "#" + a + " { display: none; }", i = E(j), m = null;
        if (A)m = i.v.createStyleSheet(), sd(m, c); else {
            var n = lb(i.v, "head")[0];
            n || (m = lb(i.v, "body")[0], n = i.qb("head"), m.parentNode.insertBefore(n, m));
            m = i.qb("style");
            sd(m, c);
            i.appendChild(n, m)
        }
        c = {};
        i = new P;
        qc(i, f);
        e && rc(i, e);
        sc(i, "/talkgadget/xpc_blank");
        "http" == d.n || "https" == d.n ? (Q(g, d.n), Q(i, d.n), e = new P, Q(e, d.n), qc(e, d.F), 80 != d.G && rc(e, d.G), sc(e, b)) : (Q(g, "http"), Q(i, "http"), e = new P("http://www.google.com/xpc_blank"));
        c.lpu = e.toString();
        c.ppu = i.toString();
        c.ifrid = a;
        c.pu = g.toString();
        Z.call(this, c)
    };
    y(td, Z);
    assign("chat.WcsCrossPageChannel", td);
    var ud = null, vd = null, wd = null;


    var MySocket = function(token, handler, c, d, f) {
        this.readyState = 0;
        this.someArray = [];
        this.onopen = handler.onopen;
        this.onmessage = handler.onmessage;
        this.onerror = handler.onerror;
        this.onclose = handler.onclose;
        this.N = c || new td(token, "/_ah/channel/xpc_blank");
        this.taElementId = c ? d : "wcs-iframe";
        this.sb = f || new xd(token);
        document.body || throwException("document.body is not defined -- do not create socket from script in <head>.");
        qd(this.N);
        mc(this.N, "opened", w(this.Zb, this));
        mc(this.N, "onMessage", w(this.Yb, this));
        mc(this.N, "onError", w(this.Xb, this));
        mc(this.N, "onClosed", w(this.close, this));
        this.N.u(w(function() {
        }, this))
    };
    MySocket.prototype.send = function() {
        return false
    };
    MySocket.prototype.close = function() {
        this.close()
    };
    MySocket.prototype.fc = function() {
        for (var a = 0, b; b = this.someArray[a]; a++)switch (b.type) {
            case 0:
                this.onopen(b.Aa);
                break;
            case 1:
                this.onmessage(b.Aa);
                break;
            case 2:
                this.onerror(b.Aa);
                break;
            case 3:
                this.onclose(b.Aa)
        }
        this.someArray = []
    };
    var yd = function(a, b, c) {
        a.someArray.push({type:b,Aa:c});
        window.setTimeout(w(a.fc, a), 1)
    };
    MySocket.prototype.Yb = function(a) {
        for (var a = Jb(a), b = a.m, a = a.s, c = this.sb, d = [], f = 0, e = 0; e < b.length; e++) {
            for (var g = b.charCodeAt(e); 255 < g;)d[f++] = g & 255, g >>= 8;
            d[f++] = g
        }
        d.push(c.wa);
        c = c.ghmac;
        c.reset();
        c.update(d);
        a:if (d = c.Z(), !s(d) || !s(a) || d.length != a.length)a = false; else {
            c = d.length;
            for (f = 0; f < c; f++)if (d[f] !== a[f]) {
                a = false;
                break a
            }
            a = true
        }
        a && yd(this, 1, {data:b});
        this.sb.wa++
    };
    MySocket.prototype.Xb = function(a) {
        a = Jb(a);
        yd(this, 2, {description:a.d,code:a.c})
    };
    MySocket.prototype.Zb = function() {
        this.readyState = 1;
        yd(this, 0, {})
    };
    MySocket.prototype.close = function() {
        this.N.close();
        this.readyState = 3;
        yd(this, 3, {});
        if (this.taElementId) {
            var a = new kb, b = v(this.taElementId) ? a.v.getElementById(this.taElementId) : this.taElementId;
            b && a.removeNode(b)
        }
    };
    var xd = function(a) {
        for (; 0 != a.length % 4;)a += ".";
        this.wa = 0;
        try {
            if (!ud) {
                ud = {};
                vd = {};
                wd = {};
                for (var b = 0; 65 > b; b++) ud[b] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".charAt(b), vd[b] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.".charAt(b), wd[vd[b]] = b
            }
            for (var b = wd, c = [], d = 0; d < a.length;) {
                var f = b[a.charAt(d++)], e = d < a.length ? b[a.charAt(d)] : 0;
                ++d;
                var g = d < a.length ? b[a.charAt(d)] : 0;
                ++d;
                var i = d < a.length ? b[a.charAt(d)] : 0;
                ++d;
                (f == null || e == null || g == null || i == null) && throwException(Error());
                c.push(f <<
                    2 | e >> 4);
                64 != g && (c.push(e << 4 & 240 | g >> 2), 64 != i && c.push(g << 6 & 192 | i))
            }
            this.Bb = c
        } catch(m) {
            m.message && throwException(Error("The provided token is invalid (" + m.name + ": " + m.message + ")")), throwException(Error("The provided token is invalid."))
        }
        this.sha1 = new SHA1;
        this.ghmac = new G_HMAC(this.sha1, this.Bb, this.Bb.length)
    };
    assign("goog.appengine.Socket", MySocket);
    assign("goog.appengine.Socket.ReadyState", {CONNECTING:0,OPEN:1,CLOSING:2,CLOSED:3});
    assign("goog.appengine.Socket.ReadyState.CONNECTING", 0);
    assign("goog.appengine.Socket.ReadyState.OPEN", 1);
    assign("goog.appengine.Socket.ReadyState.CLOSING", 2);
    assign("goog.appengine.Socket.ReadyState.CLOSED", 3);
    assign("goog.appengine.Socket.prototype.send", MySocket.prototype.send);
    assign("goog.appengine.Socket.prototype.close", MySocket.prototype.close);

    var MyChannel = function(token) {
        this.token = token
    };
    var MyHandler = {
        onopen:function() {
        },
        onclose:function() {
        },
        onerror:function() {
        },
        onmessage:function() {
        }
    };

    MyChannel.prototype.open = function(handler) {
        handler = handler || MyHandler;
        return new MySocket(this.token, handler)
    };

    assign("goog.appengine.Channel", MyChannel);
    assign("goog.appengine.Channel.prototype.open", MyChannel.prototype.open);


    SHA1 = function() {
        this.h = Array(5);
        this.ua = Array(64);
        this.cc = Array(80);
        this.xa = Array(64);
        this.xa[0] = 128;
        for (var a = 1; 64 > a; ++a)this.xa[a] = 0;
        this.reset()
    };
    SHA1.prototype.reset = function() {
        this.h[0] = 1732584193;
        this.h[1] = 4023233417;
        this.h[2] = 2562383102;
        this.h[3] = 271733878;
        this.h[4] = 3285377520;
        this.$ = this.O = 0
    };
    var Bd = function(a, b) {
        return(a << b | a >>> 32 - b) & 4294967295
    }, Cd = function(a, b) {
        for (var c = a.cc, d = 0; 64 > d; d += 4)c[d / 4] = b[d] << 24 | b[d + 1] << 16 | b[d + 2] << 8 | b[d + 3];
        for (d = 16; 80 > d; ++d)c[d] = Bd(c[d - 3] ^ c[d - 8] ^ c[d - 14] ^ c[d - 16], 1);
        for (var f = a.h[0], e = a.h[1], g = a.h[2], i = a.h[3], m = a.h[4], n, t, d = 0; 80 > d; ++d)40 > d ? 20 > d ? (n = i ^ e & (g ^ i), t = 1518500249) : (n = e ^ g ^ i, t = 1859775393) : 60 > d ? (n = e & g | i & (e | g), t = 2400959708) : (n = e ^ g ^ i, t = 3395469782), n = Bd(f, 5) + n + m + t + c[d] & 4294967295, m = i, i = g, g = Bd(e, 30), e = f, f = n;
        a.h[0] = a.h[0] + f & 4294967295;
        a.h[1] = a.h[1] + e & 4294967295;
        a.h[2] = a.h[2] + g & 4294967295;
        a.h[3] = a.h[3] + i & 4294967295;
        a.h[4] = a.h[4] + m & 4294967295
    };
    SHA1.prototype.update = function(a, b) {
        b || (b = a.length);
        var c = 0;
        if (0 == this.O)for (; c + 64 < b;)Cd(this, a.slice(c, c + 64)), c += 64, this.$ += 64;
        for (; c < b;)if (this.ua[this.O++] = a[c++], ++this.$, 64 == this.O) {
            this.O = 0;
            for (Cd(this, this.ua); c + 64 < b;)Cd(this, a.slice(c, c + 64)), c += 64, this.$ += 64
        }
    };
    SHA1.prototype.Z = function() {
        var a = Array(20), b = 8 * this.$;
        56 > this.O ? this.update(this.xa, 56 - this.O) : this.update(this.xa, 64 - (this.O - 56));
        for (var c = 63; 56 <= c; --c)this.ua[c] = b & 255, b >>>= 8;
        Cd(this, this.ua);
        for (c = b = 0; 5 > c; ++c)for (var d = 24; 0 <= d; d -= 8)a[b++] = this.h[c] >> d & 255;
        return a
    };
    G_HMAC = function(a, b, c) {
        (!a || "object" != typeof a || !a.reset || !a.update || !a.Z) && throwException(Error("Invalid hasher object. Hasher unspecified or does not implement expected interface."));
        b.constructor != Array && throwException(Error("Invalid key."));
        c && "number" != typeof c && throwException(Error("Invalid block size."));
        this.sha1 = a;
        this.ya = c || 16;
        this.vb = Array(this.ya);
        this.wb = Array(this.ya);
        b.length > this.ya && (this.sha1.update(b), b = this.sha1.Z());
        for (c = 0; c < this.ya; c++)a = c < b.length ? b[c] : 0, this.vb[c] = a ^ G_HMAC.ac, this.wb[c] = a ^ G_HMAC.$b
    };
    G_HMAC.ac = 92;
    G_HMAC.$b = 54;
    G_HMAC.prototype.reset = function() {
        this.sha1.reset();
        this.sha1.update(this.wb)
    };
    G_HMAC.prototype.update = function(a) {
        a.constructor != Array && throwException(Error("Invalid data. Data must be an array."));
        this.sha1.update(a)
    };
    G_HMAC.prototype.Z = function() {
        var a = this.sha1.Z();
        this.sha1.reset();
        this.sha1.update(this.vb);
        this.sha1.update(a);
        return this.sha1.Z()
    };
})()