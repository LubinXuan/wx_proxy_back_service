define("history/lazy.js", ["history/webp.js", "appmsg/cdn_img_lib.js"], function (t) {
    "use strict";
    function e() {
        return window.innerHeight || document.documentElement.clientHeight;
    }

    function i() {
        return document.documentElement.scrollTop || document.body.scrollTop;
    }

    function s(t, e, i, s) {
        t.style.setProperty ? t.style.setProperty(e, i, s) : t.style.cssText && (t.style.cssText += e + ":" + i + "!" + s + ";");
    }

    function r(t) {
        t = window.webp || t;
        for (var r = e(), n = r + 40, a = i() - 20, o = 0, c = images.length; c > o; o++) {
            for (var l = images[o], p = l.el; p && -1 == (p.getAttribute("class") || "").indexOf("js_msg_list");)
                p = p.parentNode;
            var d = p.offsetTop;
            if (!l.show && a < d + l.height && a + n > d) {
                var m = l.src;
                if (l.el.dataset && "300" == l.el.dataset.s && 0 == m.indexOf("http://mmbiz.qpic.cn")) {
                    var f = "/300?wxfrom=100" + (t ? "&tp=webp" : "");
                    m = m.replace(/\/0$/, f).replace(/\/0\?/, f + "&");
                }
                if (l.el.dataset && "640" == l.el.dataset.s && 0 == m.indexOf("http://mmbiz.qpic.cn") && l.el.dataset.t > +new Date("2014-06-01")) {
                    var f = "/640?wxfrom=100" + (t ? "&tp=webp" : "");
                    m = m.replace(/\/0$/, f).replace(/\/0\?/, f + "&");
                }
                l.el.setAttribute("src", m), l.el.removeAttribute("data-src"), l.show = !0, s(l.el, "visibility", "visible", "important");
            }
        }
    }

    function n() {
        for (var t = document.getElementsByTagName("img"), e = 0, i = t.length; i > e; e++) {
            var n = t.item(e);
            n.getAttribute("data-src") && "string" == typeof n.getAttribute("data-src") && (images.push({
                el: n,
                src: n.getAttribute("data-src").nogif(),
                height: 150 == n.parentNode.offsetHeight ? 150 : 50,
                show: !1
            }), s(n, "visibility", "hidden", "important"));
        }
        a.testWebp(r);
    }

    var a = t("history/webp.js");
    return t("appmsg/cdn_img_lib.js"), {
        lazyInit: n,
        detect: r
    };
});