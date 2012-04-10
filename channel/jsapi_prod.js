/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

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
    }, bind = function(a, b, c) {
        bind = Function.prototype.bind && -1 != Function.prototype.bind.toString().indexOf("native code") ? ia : ja;
        return bind.apply(null, arguments)
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
    }, extend = function(subclass, superclass) {
        function c() {
        }

        c.prototype = superclass.prototype;
        subclass.D = superclass.prototype;
        subclass.prototype = new c;
        subclass.prototype.constructor = subclass
    };
    Function.prototype.bind = Function.prototype.bind || function(a, b) {
        if (1 < arguments.length) {
            var c = Array.prototype.slice.call(arguments, 1);
            c.unshift(this, a);
            return bind.apply(null, c)
        }
        return bind(this, a)
    };



    var AbstractSomething = function() {
    };
    AbstractSomething.prototype.connected = false;
    AbstractSomething.prototype.V = function() {
        this.connected || (this.connected = true, this.close())
    };
    AbstractSomething.prototype.close = function() {
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
    extend(pa, Error);
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
    extend(Aa, pa);
    Aa.prototype.name = "AssertionError";
    var Ba = function(a, b, c) {
        if (!a) {
            var d = Array.prototype.slice.call(arguments, 2), f = "Assertion failed";
            if (b)var f = f + (": " + b), e = d;
            throwException(new Aa("" + f, e || []))
        }
    }, error = function(a, b) {
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
    var isOpera1, isIE1, isSafari, isMozilla, getUserAgent = function() {
        return q.navigator ? q.navigator.userAgent : null
    };
    isMozilla = isSafari = isIE1 = isOpera1 = false;
    var userAgent;
    if (userAgent = getUserAgent()) {
        isOpera1 = 0 == userAgent.indexOf("Opera");
        isIE1 = !isOpera1 && -1 != userAgent.indexOf("MSIE");
        isSafari = !isOpera1 && -1 != userAgent.indexOf("WebKit");
        isMozilla = !isOpera1 && !isSafari && "Gecko" == q.navigator.product
    }
    var opera = isOpera1, isIE = isIE1, mozilla = isMozilla, safari = isSafari, Xa;
    a:{
        var Ya = "", Za;
        if (opera && q.opera)var $a = q.opera.version, Ya = "function" == typeof $a ? $a() : $a; else if (mozilla ? Za = /rv\:([^\);]+)(\)|;)/ : isIE ? Za = /MSIE\s+([^\);]+)(\)|;)/ : safari && (Za = /WebKit\/(\S+)/), Za)var ab = Za.exec(getUserAgent()), Ya = ab ? ab[1] : "";
        if (isIE) {
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
        return fb[9] || (fb[9] = isIE && document.documentMode && 9 <= document.documentMode)
    };
    var hb, ib = !isIE || gb();
    !mozilla && !isIE || isIE && gb() || mozilla && D("1.9.1");
    isIE && D("9");
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
        return a.querySelectorAll && a.querySelector && (!safari || "CSS1Compat" == document.compatMode || D("528")) && c ? a.querySelectorAll(c + "") : a.getElementsByTagName(c || "*")
    }, nb = function(a, b) {
        Ka(b, function(b, d) {
            "style" == d ? a.style.cssText = b : "class" == d ? a.className = b : "for" == d ? a.htmlFor = b : d in HtmlTagAttributes ? a.setAttribute(HtmlTagAttributes[d], b) : 0 == d.lastIndexOf("aria-", 0) ? a.setAttribute(d, b) : a[d] = b
        })
    }, HtmlTagAttributes = {cellpadding:"cellPadding",cellspacing:"cellSpacing",colspan:"colSpan",rowspan:"rowSpan",valign:"vAlign",height:"height",width:"width",usemap:"useMap",frameborder:"frameBorder",maxlength:"maxLength",type:"type"}, pb = function(a, b, c) {
        function d(c) {
            c && b.appendChild(v(c) ? a.createTextNode(c) : c)
        }

        for (var f = 2; f < c.length; f++) {
            var e = c[f];
            s(e) && !(ea(e) && 0 < e.nodeType) ? Ea(ob(e) ? Ha(e) : e, d) : d(e)
        }
    }, removeElement = function(a) {
        return a && a.parentNode ? a.parentNode.removeChild(a) : null
    }, ob = function(a) {
        if (a && "number" == typeof a.length) {
            if (ea(a))return"function" ==
                typeof a.item || "string" == typeof a.item;
            if (da(a))return"function" == typeof a.item
        }
        return false
    }, kb = function(a) {
        this.document = a || q.document || document
    };
    p = kb.prototype;
    p.qb = function(a, b, c) {
        var d = this.document, f = arguments, e = f[0], g = f[1];
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
        return this.document.createElement(a)
    };
    p.createTextNode = function(a) {
        return this.document.createTextNode(a)
    };
    p.getWindow = function() {
        return this.document.parentWindow || this.document.defaultView
    };
    p.appendChild = function(a, b) {
        a.appendChild(b)
    };
    p.removeNode = removeElement;
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
    !isIE || gb();
    var tb = !isIE || gb();
    isIE && D("8");
    !safari || D("528");
    mozilla && D("1.9b") || isIE && D("8") || opera && D("9.5") || safari && D("528");
    !mozilla || D("8");
    var ub = function(a, b) {
        this.type = a;
        this.currentTarget = this.target = b
    };
    extend(ub, AbstractSomething);
    ub.prototype.close = function() {
        delete this.type;
        delete this.target;
        delete this.currentTarget
    };
    ub.prototype.Qa = false;
    ub.prototype.ec = true;
    var vb = function(a, b) {
        a && this.qa(a, b)
    };
    extend(vb, ub);
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
        d ? mozilla && (sb(d, "nodeName") || (d = null)) : "mouseover" == c ? d = a.fromElement : "mouseout" == c && (d = a.toElement);
        this.relatedTarget = d;
        this.offsetX = safari || a.offsetX !== j ? a.offsetX : a.layerX;
        this.offsetY = safari || a.offsetY !== j ? a.offsetY : a.layerY;
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
    p.close = function() {
        vb.D.close.call(this);
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
    var Logger = function(a) {
        this.name = a
    };
    Logger.prototype.parent = null;
    Logger.prototype.ka = null;
    Logger.prototype.Ua = null;
    Logger.prototype.Ib = null;

    var KeyValuePair = function(a, b) {
        this.name = a;
        this.value = b
    };
    KeyValuePair.prototype.toString = function() {
        return this.name
    };

    Logger.prototype.getName = function() {
        return this.name
    };
    Logger.prototype.getParent = function() {
        return this.parent
    };
    Logger.prototype.Gb = function(a) {
        this.ka = a
    };
    var hc = function(a) {
        if (a.ka)return a.ka;
        if (a.parent)return hc(a.parent);
        error("Root logger has no level set.");
        return null
    };
    Logger.prototype.log = function(a, b, c) {
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
    Logger.prototype.gc = function(a, b, c) {
        var d = new $b(a, "" + b, this.name);
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
    var severe = function(a, b) {
        LOG.log(new KeyValuePair("SEVERE", 1E3), a, b)
    }, warn = function(a, b) {
        a.log(new KeyValuePair("WARNING", 900), b, j)
    };
    Logger.prototype.info = function(a, b) {
        this.log(new KeyValuePair("INFO", 800), a, b)
    };
    var fine = function(a) {
        LOG.log(new KeyValuePair("FINE", 500), a, j)
    }, finest = function(a) {
        LOG.log(new KeyValuePair("FINEST", 300), a, j)
    }, ic = {}, jc = null, kc = function(a) {
        jc || (jc = new Logger(""), ic[""] = jc, jc.Gb(new KeyValuePair("CONFIG", 700)));
        var b;
        if (!(b = ic[a])) {
            b = new Logger(a);
            var c = a.lastIndexOf("."), d = a.substr(c + 1), c = kc(a.substr(0, c));
            c.Ua || (c.Ua = {});
            c.Ua[d] = b;
            b.parent = c;
            ic[a] = b
        }
        return b
    };


    var AbstractChannel = function() {
        this.handlerMethods = {}
    };
    extend(AbstractChannel, AbstractSomething);
    AbstractChannel.prototype.na = kc("goog.messaging.AbstractChannel");
    AbstractChannel.prototype.connect = function(a) {
        a && a()
    };
    AbstractChannel.prototype.isClosing = function() {
        return true
    };
    var registerHandlerMethod = function(channel, methodName, func) {
        channel.handlerMethods[methodName] = {eb:func,fb:false}
    };
    AbstractChannel.prototype.close = function() {
        AbstractChannel.D.close.call(this);
        oa(this.na);
        delete this.na;
        delete this.handlerMethods;
        delete this.Ya
    };
    var nc = RegExp("^(?:([^:/?#.]+):)?(?://(?:([^/?#]*)@)?([\\w\\d\\-\\u0100-\\uffff.%]*)(?::([0-9]+))?)?([^?#]+)?(?:\\?([^#]*))?(?:#(.*))?$");
    var oc = function(a) {
        var b = a.match(nc), a = b[1], c = b[2], d = b[3], b = b[4], f = [];
        a && f.push(a, ":");
        d && (f.push("//"), c && f.push(c, "@"), f.push(d), b && f.push(":", b));
        return f.join("")
    };
    var Url = function(a, b) {
        var c;
        a instanceof Url ? (this.U(b == null ? a.w : b), Q(this, a.protocol), pc(this, a.ia), qc(this, a.F), rc(this, a.G), sc(this, a.J), tc(this, a.parameters.fa()), uc(this, a.ha)) : a && (c = ("" + a).match(nc)) ? (this.U(!!b), Q(this, c[1] || "", true), pc(this, c[2] || "", true), qc(this, c[3] || "", true), rc(this, c[4]), sc(this, c[5] || "", true), tc(this, c[6] || "", true), uc(this, c[7] || "", true)) : (this.U(!!b), this.parameters = new vc(null, this, this.w))
    };
    p = Url.prototype;
    Url.prototype.protocol = "";
    Url.prototype.ia = "";
    Url.prototype.F = "";
    Url.prototype.G = null;
    Url.prototype.J = "";
    Url.prototype.ha = "";
    Url.prototype.jc = false;
    Url.prototype.w = false;
    Url.prototype.toString = function() {
        if (this.p)return this.p;
        var a = [];
        this.protocol && a.push(wc(this.protocol, xc), ":");
        this.F && (a.push("//"), this.ia && a.push(wc(this.ia, xc), "@"), a.push(v(this.F) ? encodeURIComponent(this.F) : null), this.G != null && a.push(":", "" + this.G));
        this.J && (this.F && "/" != this.J.charAt(0) && a.push("/"), a.push(wc(this.J, "/" == this.J.charAt(0) ? yc : zc)));
        var b = "" + this.parameters;
        b && a.push("?", b);
        this.ha && a.push("#", wc(this.ha, Ac));
        return this.p = a.join("")
    };
    Url.prototype.fa = function() {
        var a = this.protocol, b = this.ia, c = this.F, d = this.G, f = this.J, e = this.parameters.fa(), g = this.ha, i = new Url(null, this.w);
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
        a.protocol = c ? b ? decodeURIComponent(b) : "" : b;
        a.protocol && (a.protocol = a.protocol.replace(/:$/, ""))
    }, pc = function(a, b, c) {
        R(a);
        delete a.p;
        a.ia = c ? b ? decodeURIComponent(b) : "" : b
    }, qc = function(a, b, c) {
        R(a);
        delete a.p;
        a.F = c ? b ? decodeURIComponent(b) : "" : b
    }, rc = function(a, port) {
        R(a);
        delete a.p;
        port ? (port = Number(port), (isNaN(port) || 0 > port) && throwException(Error("Bad port number " + port)), a.G = port) : a.G = null
    }, sc = function(a, b, c) {
        R(a);
        delete a.p;
        a.J = c ? b ? decodeURIComponent(b) : "" : b
    }, tc = function(a, b, c) {
        R(a);
        delete a.p;
        b instanceof vc ? (a.parameters = b, a.parameters.Oa = a, a.parameters.U(a.w)) :
            (c || (b = wc(b, Bc)), a.parameters = new vc(b, a, a.w))
    }, uc = function(a, b, c) {
        R(a);
        delete a.p;
        a.ha = c ? b ? decodeURIComponent(b) : "" : b
    }, R = function(a) {
        a.jc && throwException(Error("Tried to modify a read-only Uri"))
    };
    Url.prototype.U = function(a) {
        this.w = a;
        this.parameters && this.parameters.U(a);
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
    var TransportMethodNames = {1:"NativeMessagingTransport",2:"FrameElementMethodTransport",3:"IframeRelayTransport",4:"IframePollingTransport",5:"FlashTransport",6:"NixTransport"};
    var Hc = ["url","lru","pru","lpu","ppu"];
    var channels = {};

    var createRandomString = function(length) {
        var d = ""
        var b = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        for (var c = b.length; 0 < length--;) {
            d += b.charAt(Math.floor(Math.random() * c));
        }
        return d
    };
    LOG = kc("goog.net.xpc");


    var AbstractTransport = function(a) {
        this.l = a || E()
    };
    extend(AbstractTransport, AbstractSomething);

    AbstractTransport.prototype.transportMethod = 0;
    AbstractTransport.prototype.getWindow = function() {
        return this.l.getWindow()
    };
    AbstractTransport.prototype.getName = function() {
        return TransportMethodNames[this.transportMethod] || ""
    };





    var FrameElementMethodTransport = function(crossPageChannel, b) {
        this.l = b || E();
        this.crossPageChannel = crossPageChannel;
        this.ga = [];
        this.Nb = bind(this.Vb, this)
    };
    extend(FrameElementMethodTransport, AbstractTransport);
    FrameElementMethodTransport.prototype.transportMethod = 2;
    FrameElementMethodTransport.prototype.Ka = false;
    FrameElementMethodTransport.prototype.connect = function() {
        0 == Lc(this.crossPageChannel) ? (this.B = this.crossPageChannel.P, this.B.XPC_toOuter = bind(this.hb, this)) : this.setup()
    };
    FrameElementMethodTransport.prototype.setup = function() {
        var a = true;
        try {
            this.B || (this.B = this.getWindow().frameElement), this.B && this.B.XPC_toOuter && (this.Ga = this.B.XPC_toOuter, this.B.XPC_toOuter.XPC_toInner = bind(this.hb, this), a = false, this.send("tp", "SETUP_ACK"), V(this.crossPageChannel))
        } catch(b) {
            severe("exception caught while attempting setup: " + b)
        }
        a && (this.mb || (this.mb = bind(this.setup, this)), this.getWindow().setTimeout(this.mb, 100))
    };
    FrameElementMethodTransport.prototype.handleSetupMessage = function(a) {
        0 == Lc(this.crossPageChannel) && !this.crossPageChannel.isClosing() && "SETUP_ACK" == a ? (this.Ga = this.B.XPC_toOuter.XPC_toInner, V(this.crossPageChannel)) : throwException(Error("Got unexpected transport message."))
    };
    FrameElementMethodTransport.prototype.hb = function(a, b) {
        !this.Ka && 0 == this.ga.length ? this.crossPageChannel.deliver(a, b) : (this.ga.push({Pb:a,Ha:b}), 1 == this.ga.length && this.getWindow().setTimeout(this.Nb, 1))
    };
    FrameElementMethodTransport.prototype.Vb = function() {
        for (; this.ga.length;) {
            var a = this.ga.shift();
            this.crossPageChannel.deliver(a.Pb, a.Ha)
        }
    };
    FrameElementMethodTransport.prototype.send = function(a, b) {
        this.Ka = true;
        this.Ga(a, b);
        this.Ka = false
    };
    FrameElementMethodTransport.prototype.close = function() {
        FrameElementMethodTransport.D.close.call(this);
        this.B = this.Ga = null
    };




    var IFramePollingTransport = function(crossPageChannel, b) {
        this.l = b || E();
        this.crossPageChannel = crossPageChannel;
        this.url = this.crossPageChannel.b.ppu;
        this.Ob = this.crossPageChannel.b.lpu;
        this.events = []
    }, Mc, Nc;
    extend(IFramePollingTransport, AbstractTransport);
    IFramePollingTransport.prototype.transportMethod = 4;
    IFramePollingTransport.prototype.pa = 0;
    IFramePollingTransport.prototype.S = false;
    IFramePollingTransport.prototype.initialized = false;

    var getMsgFrameName = function(transport) {
        return"googlexpc_" + transport.crossPageChannel.name + "_msg"
    };

    var getAckFrameName = function(transport) {
        return"googlexpc_" + transport.crossPageChannel.name + "_ack"
    };

    IFramePollingTransport.prototype.connect = function() {
        if (!this.connected) {
            fine("transport connect called");
            if (!this.initialized) {
                fine("initializing...");
                var name = getMsgFrameName(this);
                this.msgSenderFrame = createSenderFrame(this, name);
                this.msgReceiverFrame = this.getWindow().frames[name];

                name = getAckFrameName(this);
                this.ackSenderFrame = createSenderFrame(this, name);
                this.ackReceiverFrame = this.getWindow().frames[name];
                this.initialized = true
            }
            if (!frameExists(this, getMsgFrameName(this)) || !frameExists(this, getAckFrameName(this))) {
                finest("foreign frames not (yet) present");
                if (1 == Lc(this.crossPageChannel) && !this.Lb)finest("innerPeerReconnect called"), this.crossPageChannel.name = createRandomString(10), finest("switching channels: " + this.crossPageChannel.name), deconstructSenderFrames(this), this.initialized = false, this.Lb = createSenderFrame(this, "googlexpc_reconnect_" + this.crossPageChannel.name); else if (0 ==
                    Lc(this.crossPageChannel)) {
                    finest("outerPeerReconnect called");
                    for (var a = this.crossPageChannel.window.frames, b = a.length, c = 0; c < b; c++) {
                        var d;
                        try {
                            a[c] && a[c].name && (d = a[c].name)
                        } catch(f) {
                        }
                        if (d) {
                            var e = d.split("_");
                            if (3 == e.length && "googlexpc" == e[0] && "reconnect" == e[1]) {
                                this.crossPageChannel.name = e[2];
                                deconstructSenderFrames(this);
                                this.initialized = false;
                                break
                            }
                        }
                    }
                }
                this.getWindow().setTimeout(bind(this.connect, this), 100)
            } else fine("foreign frames present"), this.Wa = new Tc(this, this.crossPageChannel.window.frames[getMsgFrameName(this)], bind(this.msgReceived, this)), this.Va = new Tc(this, this.crossPageChannel.window.frames[getAckFrameName(this)], bind(this.ackReceived, this)), this.setup()
        }
    };
    var createSenderFrame = function(transport, name) {
        finest("constructing sender frame: " + name);
        var iframe = document.createElement("iframe");
        iframe.style.position = "absolute";
        iframe.style.top = "-10px";
        iframe.style.left = "10px";
        iframe.style.width = "1px";
        iframe.style.height = "1px";
        iframe.id = iframe.name = name;
        iframe.src = transport.url + "#INITIAL";
        transport.getWindow().document.body.appendChild(iframe);
        return iframe
    };

    var deconstructSenderFrames = function(transport) {
        finest("deconstructSenderFrames called");
        transport.msgSenderFrame && (transport.msgSenderFrame.parentNode.removeChild(transport.msgSenderFrame), transport.msgSenderFrame = null, transport.msgReceiverFrame = null);
        transport.ackSenderFrame && (transport.ackSenderFrame.parentNode.removeChild(transport.ackSenderFrame), transport.ackSenderFrame = null, transport.ackReceiverFrame = null)
    };

    var frameExists = function(a, b) {
        finest("checking for receive frame: " + b);
        try {
            var c = a.crossPageChannel.window.frames[b];
            if (!c || 0 != c.location.href.indexOf(a.Ob)) return false
        } catch(d) {
            return false
        }
        return true
    };
    IFramePollingTransport.prototype.setup = function() {
        var a = this.crossPageChannel.window.frames;
        !a[getAckFrameName(this)] || !a[getMsgFrameName(this)] ? (this.bb || (this.bb = bind(this.setup, this)), this.getWindow().setTimeout(this.bb, 100), fine("local frames not (yet) present")) : (this.$a = new Sender(this.url, this.msgReceiverFrame), this.ma = new Sender(this.url, this.ackReceiverFrame), fine("local frames ready"), this.getWindow().setTimeout(bind(function() {
            this.$a.send("SETUP");
            this.S = true;
            fine("SETUP sent")
        }, this), 100))
    };
    var deliverQueuedMessages = function(a) {
        if (a.La && a.jb) {
            if (V(a.crossPageChannel), a.W) {
                fine("delivering queued messages (" + a.W.length + ")");
                for (var b = 0, c; b < a.W.length; b++)c = a.W[b], a.crossPageChannel.deliver(c.Sb, c.Ha);
                delete a.W
            }
        } else finest("checking if connected: ack sent:" + a.La + ", ack rcvd: " + a.jb)
    };
    IFramePollingTransport.prototype.msgReceived = function(a) {
        finest("msg received: " + a);
        if ("SETUP" == a)this.ma && (this.ma.send("SETUP_ACK"), finest("SETUP_ACK sent"), this.La = true, deliverQueuedMessages(this)); else if (this.crossPageChannel.isClosing() || this.La) {
            var b = a.indexOf("|"), c = a.substring(0, b), a = a.substring(b + 1), b = c.indexOf(",");
            if (-1 == b) {
                var d;
                this.ma.send("ACK:" + c);
                Wc(this, a)
            } else d = c.substring(0, b), this.ma.send("ACK:" + d), c = c.substring(b + 1).split("/"), b = parseInt(c[0], 10), c = parseInt(c[1], 10), 1 == b && (this.Na = []), this.Na.push(a), b == c && (Wc(this, this.Na.join("")), delete this.Na)
        } else warn(LOG, "received msg, but channel is not connected")
    };
    IFramePollingTransport.prototype.ackReceived = function(msg) {
        finest("ack received: " + msg);
        "SETUP_ACK" == msg ? (this.S = false, this.jb = true, deliverQueuedMessages(this)) : this.crossPageChannel.isClosing() ? this.S ? parseInt(msg.split(":")[1], 10) == this.pa ? (this.S = false, Xc(this)) : warn(LOG, "got ack with wrong sequence") : warn(LOG, "got unexpected ack") : warn(LOG, "received ack, but channel not connected")
    };
    var Xc = function(transport) {
        if (!transport.S && transport.events.length) {
            var b = transport.events.shift();
            ++transport.pa;
            transport.$a.send(transport.pa + b);
            finest("msg sent: " + transport.pa + b);
            transport.S = true
        }
    }, Wc = function(a, b) {
        var c = b.indexOf(":"), d = b.substr(0, c), c = b.substring(c + 1);
        a.crossPageChannel.isClosing() ? a.crossPageChannel.deliver(d, c) : ((a.W || (a.W = [])).push({Sb:d,Ha:c}), finest("queued delivery"))
    };
    IFramePollingTransport.prototype.Ba = 3800;
    IFramePollingTransport.prototype.send = function(a, b) {
        var c = a + ":" + b;
        if (!isIE || b.length <= this.Ba) {
            this.events.push("|" + c);
        } else for (var d = b.length, f = Math.ceil(d / this.Ba), e = 0, g = 1; e < d;)this.events.push("," + g + "/" + f + "|" + c.substr(e, this.Ba)), g++, e += this.Ba;
        Xc(this)
    };
    IFramePollingTransport.prototype.close = function() {
        IFramePollingTransport.D.close.call(this);
        var a = Yc;
        Fa(a, this.Wa);
        Fa(a, this.Va);
        this.Wa = this.Va = null;
        removeElement(this.msgSenderFrame);
        removeElement(this.ackSenderFrame);
        this.msgReceiverFrame = this.ackReceiverFrame = this.msgSenderFrame = this.ackSenderFrame = null
    };
    var Yc = [], Zc = bind(function() {
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
            if (LOG.info("receive_() failed: " + i), b = Yc[b].transporter.crossPageChannel, LOG.info("Transport Error"), b.close(), !Yc.length)return
        }
        b = la();
        a && (Mc = b);
        Nc = window.setTimeout(Zc, 1E3 > b - Mc ? 10 : 100)
    }, IFramePollingTransport), $c = function() {
        fine("starting receive-timer");
        Mc = la();
        Nc && window.clearTimeout(Nc);
        Nc = window.setTimeout(Zc, 10)
    };

    var Sender = function(url, document) {
        this.url = url;
        this.document = document;
        this.oneOrTwo = 0
    };
    Sender.prototype.send = function(url) {
        this.oneOrTwo = ++this.oneOrTwo % 2;
        url = this.url + "#" + this.oneOrTwo + encodeURIComponent(url);
        try {
            safari ? this.document.location.href = url : this.document.location.replace(url)
        } catch(b) {
            severe("sending failed", b)
        }
        $c()
    };
    var Tc = function(transporter, b, c) {
        this.transporter = transporter;
        this.pb = b;
        this.Ub = c;
        this.ob = this.pb.location.href.split("#")[0] + "#INITIAL";
        Yc.push(this);
        $c()
    };





    var IFrameRelayTransporter = function(crossPageChannel, b) {
        this.l = b || E();
        this.crossPageChannel = crossPageChannel;
        this.Mb = this.crossPageChannel.b.pru;
        this.ab = this.crossPageChannel.b.iframeId;
        safari && ad()
    };
    extend(IFrameRelayTransporter, AbstractTransport);
    if (safari)var bd = [], cd = 0, ad = function() {
        cd || (cd = window.setTimeout(function() {
            safariRemove()
        }, 1E3))
    }, safariRemove = function(a) {
        for (var b = la(), a = a || 3E3; bd.length && b - bd[0].timestamp >= a;) {
            var c = bd.shift().Tb;
            removeElement(c);
            finest("iframe removed")
        }
        cd = window.setTimeout(ed, 1E3)
    }, ed = function() {
        safariRemove()
    };
    var fd = {};
    IFrameRelayTransporter.prototype.transportMethod = 3;
    IFrameRelayTransporter.prototype.connect = function() {
        this.getWindow().xpcRelay || (this.getWindow().xpcRelay = gd);
        this.send("tp", "SETUP")
    };
    var gd = function(a, b) {
        var c = b.indexOf(":"), d = b.substr(0, c), f = b.substr(c + 1);
        if (!isIE || -1 == (c = d.indexOf("|")))var e = d; else {
            var e = d.substr(0, c), d = d.substr(c + 1), c = d.indexOf("+"), g = d.substr(0, c), c = parseInt(d.substr(c + 1), 10), i = fd[g];
            i || (i = fd[g] = {Db:[],Eb:0,Cb:0});
            -1 != d.indexOf("++") && (i.Cb = c + 1);
            i.Db[c] = f;
            i.Eb++;
            if (i.Eb != i.Cb)return;
            f = i.Db.join("");
            delete fd[g]
        }
        channels[a].deliver(e, decodeURIComponent(f))
    };
    IFrameRelayTransporter.prototype.handleSetupMessage = function(msg) {
        "SETUP" == msg ? (this.send("tp", "SETUP_ACK"), V(this.crossPageChannel)) : "SETUP_ACK" == msg && V(this.crossPageChannel)
    };
    IFrameRelayTransporter.prototype.send = function(a, b) {
        var c = encodeURIComponent(b), d = c.length;
        if (isIE && 1800 < d)for (var f = Math.floor(2147483648 * Math.random()).toString(36) + Math.abs(Math.floor(2147483648 * Math.random()) ^ la()).toString(36), e = 0, g = 0; e < d; g++) {
            var i = c.substr(e, 1800), e = e + 1800;
            createIFrame(this, a, i, f + (e >= d ? "++" : "+") + g)
        } else createIFrame(this, a, c)
    };
    var createIFrame = function(ifrmaeRelayTransporter, b, c, d) {
        if (isIE) {
            var div = ifrmaeRelayTransporter.getWindow().document.createElement("div");
            div.innerHTML = '<iframe onload="this.xpcOnload()"></iframe>';
            iframe = div.childNodes[0];
            iframe.xpcOnload = iframeOnLoad
        } else {
            iframe = ifrmaeRelayTransporter.getWindow().document.createElement("iframe");
            safari ? bd.push({timestamp:la(),Tb:div}) : Bb(div, "load", iframeOnLoad);
        }
        iframe.style.visibility = "hidden";
        iframe.style.width = iframe.style.height = "0px";
        iframe.style.position = "absolute";

        var src = ifrmaeRelayTransporter.Mb;
        src += "#" + ifrmaeRelayTransporter.crossPageChannel.name;
        ifrmaeRelayTransporter.ab && (src += "," + ifrmaeRelayTransporter.ab);
        src += "|" + b;
        d && (src += "|" + d);
        src += ":" + c;
        iframe.src = src;

        ifrmaeRelayTransporter.getWindow().document.body.appendChild(div);
        finest("msg sent: " + src)
    }, iframeOnLoad = function() {
        finest("iframe-load");
        removeElement(this)
    };
    IFrameRelayTransporter.prototype.close = function() {
        IFrameRelayTransporter.D.close.call(this);
        safari && safariRemove(0)
    };





    var NativeMessagingTransporter = function(crossPageChannel, b, c) {
        this.l = c || E();
        this.crossPageChannel = crossPageChannel;
        this.hostname = b || "*"
    };
    extend(NativeMessagingTransporter, AbstractTransport);
    NativeMessagingTransporter.prototype.initialized = false;
    NativeMessagingTransporter.prototype.transportMethod = 1;

    var jd = {};
    var kd = function(a) {
        var b = a.ra.data;
        if (!v(b))return false;
        var pipeIndex = b.indexOf("|"), colonIndex = b.indexOf(":");
        if (-1 == pipeIndex || -1 == colonIndex)return false;
        var channelName = b.substring(0, pipeIndex), service = b.substring(pipeIndex + 1, colonIndex), payload = b.substring(colonIndex + 1);
        fine("messageReceived: channel=" + channelName + ", service=" + service + ", payload=" + payload);
        if (something = channels[channelName])return something.deliver(service, payload, a.ra.origin), (true);
        for (var e in channels)if (a = channels[e], 1 == Lc(a) && !a.isClosing() && "tp" == service && "SETUP" == payload)return fine("changing channel name to " + channelName), a.name = channelName, delete channels[e], channels[channelName] = a, a.deliver(service, payload), (true);
        LOG.info('channel name mismatch; message ignored"');
        return false
    };
    NativeMessagingTransporter.prototype.handleSetupMessage = function(msg) {
        switch (msg) {
            case "SETUP":
                this.send("tp", "SETUP_ACK");
                break;
            case "SETUP_ACK":
                V(this.crossPageChannel)
        }
    };
    NativeMessagingTransporter.prototype.connect = function() {
        var a = this.getWindow(), b = ha(a), c = jd[b];
        "number" == typeof c || (c = 0);
        0 == c && Bb(a.postMessage ? a : a.document, "message", kd, false, NativeMessagingTransporter);
        jd[b] = c + 1;
        this.initialized = true;
        this.connectInternal()
    };
    NativeMessagingTransporter.prototype.connectInternal = function() {
        if (!this.crossPageChannel.isClosing() && !this.connected) {
            this.send("tp", "SETUP");
            this.getWindow().setTimeout(bind(this.connectInternal, this), 100);
        }
    };
    NativeMessagingTransporter.prototype.send = function(a, payload) {
        var window = this.crossPageChannel.window;
        if (window) {
            var worker = window.postMessage ? window : window.document;
            this.send = function(a, payload) {
                fine("send(): payload=" + payload + " to hostname=" + this.hostname);
                worker.postMessage(this.crossPageChannel.name + "|" + a + ":" + payload, this.hostname)
            };
            this.send(a, payload)
        } else fine("send(): window not ready")
    };
    NativeMessagingTransporter.prototype.close = function() {
        NativeMessagingTransporter.D.close.call(this);
        if (this.initialized) {
            var a = this.getWindow(), b = ha(a), c = jd[b];
            jd[b] = c - 1;
            1 == c && Eb(a.postMessage ? a : a.document, "message", kd, false, NativeMessagingTransporter)
        }
        delete this.send
    };


    var NixTransporter = function(crossPageChannel, b) {
        this.l = b || E();
        this.crossPageChannel = crossPageChannel;
        this.Za = crossPageChannel.at || "";
        this.cb = crossPageChannel.rat || "";
        var window = this.getWindow();
        if (!window.nix_setup_complete)try {
            window.execScript("Class GCXPC____NIXVBS_wrapper\n Private m_Transport\nPrivate m_Auth\nPublic Sub SetTransport(transport)\nIf isEmpty(m_Transport) Then\nSet m_Transport = transport\nEnd If\nEnd Sub\nPublic Sub SetAuth(auth)\nIf isEmpty(m_Auth) Then\nm_Auth = auth\nEnd If\nEnd Sub\nPublic Function GetAuthToken()\n GetAuthToken = m_Auth\nEnd Function\nPublic Sub SendMessage(service, payload)\n Call m_Transport.GCXPC____NIXJS_handle_message(service, payload)\nEnd Sub\nPublic Sub CreateChannel(channel)\n Call m_Transport.GCXPC____NIXJS_create_channel(channel)\nEnd Sub\nPublic Sub GCXPC____NIXVBS_container()\n End Sub\nEnd Class\n Function GCXPC____NIXVBS_get_wrapper(transport, auth)\nDim wrap\nSet wrap = New GCXPC____NIXVBS_wrapper\nwrap.SetTransport transport\nwrap.SetAuth auth\nSet GCXPC____NIXVBS_get_wrapper = wrap\nEnd Function", "vbscript");
            window.nix_setup_complete = true
        } catch(d) {
            severe("exception caught while attempting global setup: " + d)
        }
        this.GCXPC____NIXJS_handle_message = this.handleMessage;
        this.GCXPC____NIXJS_create_channel = this.createChannel
    };
    extend(NixTransporter, AbstractTransport);
    NixTransporter.prototype.transportMethod = 6;
    NixTransporter.prototype.T = false;
    NixTransporter.prototype.opener = null;
    NixTransporter.prototype.connect = function() {
        0 == Lc(this.crossPageChannel) ? this.lb() : this.ib()
    };
    NixTransporter.prototype.lb = function() {
        if (!this.T) {
            var a = this.crossPageChannel.P;
            try {
                a.contentWindow.opener = this.getWindow().GCXPC____NIXVBS_get_wrapper(this, this.Za), this.T = true
            } catch(b) {
                severe("exception caught while attempting setup: " + b)
            }
            this.T || this.getWindow().setTimeout(bind(this.lb, this), 100)
        }
    };
    NixTransporter.prototype.ib = function() {
        if (!this.T) {
            try {
                var opener = this.getWindow().opener;
                if (opener && "GCXPC____NIXVBS_container"in opener) {
                    this.opener = opener;
                    if (this.opener.GetAuthToken() != this.cb) {
                        severe("Invalid auth token from other party");
                        return
                    }
                    this.opener.CreateChannel(this.getWindow().GCXPC____NIXVBS_get_wrapper(this, this.Za));
                    this.T = true;
                    V(this.crossPageChannel)
                }
            } catch(b) {
                severe("exception caught while attempting setup: " + b);
                return
            }
            this.T || this.getWindow().setTimeout(bind(this.ib, this), 100)
        }
    };
    NixTransporter.prototype.createChannel = function(a) {
        ("unknown" != typeof a || !("GCXPC____NIXVBS_container"in a)) && severe("Invalid NIX channel given to createChannel_");
        this.opener = a;
        this.opener.GetAuthToken() != this.cb ? severe("Invalid auth token from other party") : V(this.crossPageChannel)
    };
    NixTransporter.prototype.handleMessage = function(service, payload) {
        this.getWindow().setTimeout(bind(function() {
            this.crossPageChannel.deliver(service, payload)
        }, this), 1)
    };
    NixTransporter.prototype.send = function(a, b) {
        "unknown" !== typeof this.opener && severe("NIX channel not connected");
        this.opener.SendMessage(a, b)
    };
    NixTransporter.prototype.close = function() {
        NixTransporter.D.close.call(this);
        this.opener = null
    };



    var CrossPageChannel = function(a, b) {
        this.handlerMethods = {};
        for (var c = 0, d; d = Hc[c]; c++)d in a && !/^https?:\/\//.test(a[d]) && throwException(Error("URI " + a[d] + " is invalid for field " + d));
        this.b = a;
        this.name = this.b.cn || createRandomString(10);
        this.l = b || E();
        this.ca = [];
        a.lpu = a.lpu || oc(this.l.getWindow().location.href) + "/robots.txt";
        a.ppu = a.ppu || oc(a.url || "") + "/robots.txt";
        channels[this.name] = this;
        Bb(window, "unload", md);
        LOG.info("CrossPageChannel created: " + this.name)
    };
    extend(CrossPageChannel, AbstractChannel);
    var someRegexp = /^%*tp$/, someOtherRegexp = /^%+tp$/;

    CrossPageChannel.prototype.transporter = null;
    CrossPageChannel.prototype.status = 1;
    CrossPageChannel.prototype.isClosing = function() {
        return 2 == this.status
    };
    CrossPageChannel.prototype.window = null;
    CrossPageChannel.prototype.P = null;

    var createIFrame = function(a) {
        iframeId = a.b.iframeId;
        iframeId || (iframeId = a.b.iframeId = "xpcpeer" + createRandomString(4));

        var iframe = document.createElement("IFRAME");
        iframe.id = iframeId;
        iframe.name = iframeId;
        iframe.style.width = "100%";
        iframe.style.height = "100%";
        var iframeSrcUrl = getUrl(a);
        if (mozilla || safari) {
            a.Ia = true;
            window.setTimeout(bind(function() {
                this.Ia = false;
                iframe.src = iframeSrcUrl.toString();
                document.body.appendChild(iframe);
                LOG.info("peer iframe created (" + iframeId + ")");
                this.da && this.connect(this.Fa)
            }, a), 1);
        } else {
            iframe.src = iframeSrcUrl.toString();
            document.body.appendChild(iframe);
            LOG.info("peer iframe created (" + iframeId + ")");
        }
    };

    var getUrl = function(a) {
        var b = a.b.url;
        v(b) && (b = a.b.url = new Url(b));
        var xpc = {};
        xpc.cn = a.name;
        xpc.tp = a.b.tp;
        a.b.lru && (xpc.pru = a.b.lru);
        a.b.lpu && (xpc.ppu = a.b.lpu);
        a.b.ppu && (xpc.lpu = a.b.ppu);
        a = b;
        xpc = Mb(xpc);
        R(a);
        delete a.p;
        a.parameters.set("xpc", xpc);
        return b
    };
    CrossPageChannel.prototype.Ia = false;
    CrossPageChannel.prototype.da = false;
    CrossPageChannel.prototype.connect = function(a) {
        this.Fa = a || ba;
        if (this.Ia)LOG.info("connect() deferred"), this.da = true; else {
            this.da = false;
            LOG.info("connect()");
            this.b.iframeId && (this.P = v(this.b.iframeId) ? this.l.document.getElementById(this.b.iframeId) : this.b.iframeId);
            this.P && ((a = this.P.contentWindow) || (a = window.frames[this.b.iframeId]), this.window = a);
            this.window || (window == top && throwException(Error("CrossPageChannel: Can't connect, peer window-object not set.")), this.window = window.parent);
            if (!this.transporter) {
                if (!this.b.tp) {
                    var a = this.b, b;
                    if (da(document.postMessage) || da(window.postMessage) ||
                        isIE && window.postMessage)b = 1; else if (mozilla)b = 2; else if (isIE && this.b.pru)b = 3; else {
                        var c;
                        if (c = isIE) {
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
                        this.transporter = new NativeMessagingTransporter(this, this.b.ph, this.l);
                        break;
                    case 6:
                        this.transporter = new NixTransporter(this, this.l);
                        break;
                    case 2:
                        this.transporter = new FrameElementMethodTransport(this, this.l);
                        break;
                    case 3:
                        this.transporter = new IFrameRelayTransporter(this, this.l);
                        break;
                    case 4:
                        this.transporter = new IFramePollingTransport(this, this.l)
                }
                this.transporter ? LOG.info("Transport created: " + this.transporter.getName()) : throwException(Error("CrossPageChannel: No suitable transport found!"))
            }
            for (this.transporter.connect(); 0 <
                this.ca.length;)this.ca.shift()()
        }
    };
    CrossPageChannel.prototype.close = function() {
        this.isClosing() && (this.status = 3, this.transporter.V(), this.Fa = this.transporter = null, this.da = false, this.ca.length = 0, LOG.info('Channel "' + this.name + '" closed'))
    };
    var V = function(a) {
        a.isClosing() || (a.status = 2, LOG.info('Channel "' + a.name + '" connected'), a.Fa())
    };
    CrossPageChannel.prototype.send = function(a, b) {
        this.isClosing() ? Boolean(this.window.closed) ? (severe("Peer has disappeared."), this.close()) : (ea(b) && (b = Mb(b)), this.transporter.send(rd(a), b)) : severe("Can't send. Channel not connected.")
    };
    CrossPageChannel.prototype.deliver = function(serviceName, payload, origin) {
        if (this.da)this.ca.push(bind(this.deliver, this, serviceName, payload, origin)); else {
            var d = this.b.ph;
            if (/^[\s\xa0]*$/.test(origin == null ? "" : "" + origin) || /^[\s\xa0]*$/.test(d == null ? "" : "" + d) || origin == this.b.ph)if (this.connected)warn(LOG, "CrossPageChannel::deliver_(): Disposed."); else if (!serviceName || "tp" == serviceName)this.transporter.handleSetupMessage(payload); else if (this.isClosing()) {
                if (serviceName = serviceName.replace(/%[0-9a-f]{2}/gi, decodeURIComponent), serviceName = someOtherRegexp.test(serviceName) ? serviceName.substring(1) : serviceName, origin = this.handlerMethods[serviceName], origin || (this.Ya ? (origin = ka(this.Ya, serviceName), d = ea(payload), origin = {eb:origin,fb:d}) : (warn(this.na, 'Unknown service name "' + serviceName + '"'), origin = null)), origin) {
                    var f;
                    a:{
                        if ((d = origin.fb) && v(payload))try {
                            f = Jb(payload);
                            break a
                        } catch(e) {
                            warn(this.na, "Expected JSON payload for " + serviceName + ', was "' + payload + '"');
                            f = null;
                            break a
                        } else if (!d && !v(payload)) {
                            f = Mb(payload);
                            break a
                        }
                        f = payload
                    }
                    f != null && origin.eb(f)
                }
            } else LOG.info("CrossPageChannel::deliver_(): Not connected."); else warn(LOG, 'Message received from unapproved origin "' + origin + '" - rejected.')
        }
    };
    var rd = function(a) {
        someRegexp.test(a) && (a = "%" + a);
        return a.replace(/[%:|]/g, encodeURIComponent)
    }, Lc = function(a) {
        return window.parent == a.window ? 1 : 0
    };
    CrossPageChannel.prototype.close = function() {
        CrossPageChannel.D.close.call(this);
        this.close();
        this.P = this.window = null;
        delete channels[this.name];
        this.ca.length = 0
    };
    var md = function() {
        for (var a in channels) {
            var b = channels[a];
            b && b.V()
        }
    };
    var sd = function(element, content) {
        isIE ? element.cssText = content : element[safari ? "innerText" : "innerHTML"] = content
    };
    var WcsCrossPageChannel = function(token, b, iframeId, href, f, e) {
        var url = new Url(href || window.location.href);
        var url2 = new Url;
        var host = f ? f : Math.floor(1E3 * Math.random()) + ".talkgadget.google.com";
        qc(url2, host);
        sc(url2, "/talkgadget/d");
        R(url2);
        delete url2.p;
        url2.parameters.set("token", token);
        e && rc(url2, e);
        var a = iframeId || "wcs-iframe";
        var c = "#" + a + " { display: none; }";
        var i = E(j);
        var styleSheet = null;
        if (isIE) {
            styleSheet = i.document.createStyleSheet();
            sd(styleSheet, c);
        } else {
            var n = lb(i.document, "head")[0];
            n || (styleSheet = lb(i.document, "body")[0], n = i.qb("head"), styleSheet.parentNode.insertBefore(n, styleSheet));
            styleSheet = i.qb("style");
            sd(styleSheet, c);
            i.appendChild(n, styleSheet)
        }
        c = {};

        i = new Url;
        qc(i, host);
        e && rc(i, e);
        sc(i, "/talkgadget/xpc_blank");
        "http" == url.protocol || "https" == url.protocol ? (Q(url2, url.protocol), Q(i, url.protocol), e = new Url, Q(e, url.protocol), qc(e, url.F), 80 != url.G && rc(e, url.G), sc(e, b)) : (Q(url2, "http"), Q(i, "http"), e = new Url("http://www.google.com/xpc_blank"));
        c.lpu = e.toString();
        c.ppu = i.toString();
        c.iframeId = a;
        c.url = url2.toString();
        CrossPageChannel.call(this, c)
    };
    extend(WcsCrossPageChannel, CrossPageChannel);
    assign("chat.WcsCrossPageChannel", WcsCrossPageChannel);
    var ud = null, vd = null, wd = null;


    var MySocket = function(token, handler, channel, elementId, f) {
        this.readyState = 0;
        this.events = [];
        this.onopen = handler.onopen;
        this.onmessage = handler.onmessage;
        this.onerror = handler.onerror;
        this.onclose = handler.onclose;
        this.channel = channel || new WcsCrossPageChannel(token, "/_ah/channel/xpc_blank");
        this.taElementId = channel ? elementId : "wcs-iframe";
        this.parsedToken = f || new ParsedToken(token);
        document.body || throwException("document.body is not defined -- do not create socket from script in <head>.");
        createIFrame(this.channel);
        registerHandlerMethod(this.channel, "opened", bind(this.opened, this));
        registerHandlerMethod(this.channel, "onMessage", bind(this.onMessage, this));
        registerHandlerMethod(this.channel, "onError", bind(this.onError, this));
        registerHandlerMethod(this.channel, "onClosed", bind(this.close, this));
        this.channel.connect(bind(function() {
        }, this))
    };
    MySocket.prototype.send = function() {
        return false
    };
    MySocket.prototype.close = function() {
        this.close()
    };
    MySocket.prototype.handleEvent = function() {
        for (var i = 0, event; event = this.events[i]; i++)switch (event.type) {
            case 0:
                this.onopen(event.body);
                break;
            case 1:
                this.onmessage(event.body);
                break;
            case 2:
                this.onerror(event.body);
                break;
            case 3:
                this.onclose(event.body)
        }
        this.events = []
    };
    var pushEvent = function(socket, eventType, eventBody) {
        socket.events.push({type:eventType,body:eventBody});
        window.setTimeout(bind(socket.handleEvent, socket), 1)
    };
    MySocket.prototype.onMessage = function(a) {
        for (var a = Jb(a), messageBody = a.m, a = a.s, c = this.parsedToken, d = [], f = 0, e = 0; e < messageBody.length; e++) {
            for (var g = messageBody.charCodeAt(e); 255 < g;)d[f++] = g & 255, g >>= 8;
            d[f++] = g
        }
        d.push(c.wa);

        var ghmac = c.ghmac;
        ghmac.reset();
        ghmac.update(d);
        a:if (d = ghmac.Z(), !s(d) || !s(a) || d.length != a.length)a = false; else {
            for (f = 0; f < d.length; f++)if (d[f] !== a[f]) {
                a = false;
                break a
            }
            a = true
        }
        a && pushEvent(this, 1, {data:messageBody});
        this.parsedToken.wa++
    };
    MySocket.prototype.onError = function(a) {
        a = Jb(a);
        pushEvent(this, 2, {description:a.d,code:a.c})
    };
    MySocket.prototype.opened = function() {
        this.readyState = 1;
        pushEvent(this, 0, {})
    };
    MySocket.prototype.close = function() {
        this.channel.close();
        this.readyState = 3;
        pushEvent(this, 3, {});
        if (this.taElementId) {
            var a = new kb, b = v(this.taElementId) ? a.document.getElementById(this.taElementId) : this.taElementId;
            b && a.removeNode(b)
        }
    };
    var ParsedToken = function(token) {
        for (; 0 != token.length % 4;)token += ".";
        this.wa = 0;
        try {
            if (!ud) {
                ud = {};
                vd = {};
                wd = {};
                for (var b = 0; 65 > b; b++) ud[b] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789+/=".charAt(b), vd[b] = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789-_.".charAt(b), wd[vd[b]] = b
            }
            for (var b = wd, c = [], d = 0; d < token.length;) {
                var f = b[token.charAt(d++)], e = d < token.length ? b[token.charAt(d)] : 0;
                ++d;
                var g = d < token.length ? b[token.charAt(d)] : 0;
                ++d;
                var i = d < token.length ? b[token.charAt(d)] : 0;
                ++d;
                (f == null || e == null || g == null || i == null) && throwException(Error());
                c.push(f << 2 | e >> 4);
                64 != g && (c.push(e << 4 & 240 | g >> 2), 64 != i && c.push(g << 6 & 192 | i))
            }
            this.Bb = c
        } catch(m) {
            m.message && throwException(Error("The provided token is invalid (" + m.name + ": " + m.message + ")"));
            throwException(Error("The provided token is invalid."))
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
    }, someChecksumFunction = function(a, b) {
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
        if (0 == this.O)for (; c + 64 < b;)someChecksumFunction(this, a.slice(c, c + 64)), c += 64, this.$ += 64;
        for (; c < b;)if (this.ua[this.O++] = a[c++], ++this.$, 64 == this.O) {
            this.O = 0;
            for (someChecksumFunction(this, this.ua); c + 64 < b;)someChecksumFunction(this, a.slice(c, c + 64)), c += 64, this.$ += 64
        }
    };
    SHA1.prototype.Z = function() {
        var a = Array(20), b = 8 * this.$;
        56 > this.O ? this.update(this.xa, 56 - this.O) : this.update(this.xa, 64 - (this.O - 56));
        for (var c = 63; 56 <= c; --c)this.ua[c] = b & 255, b >>>= 8;
        someChecksumFunction(this, this.ua);
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