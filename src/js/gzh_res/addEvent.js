define("history/addEvent.js", ["biz_common/dom/event.js", "biz_common/dom/class.js", "biz_wap/utils/ajax.js", "history/location.js", "history/render.js", "history/lazy.js", "pages/report.js"], function (require, exports, module, alert) {
    "use strict";
    function isScrollEnd() {
        return getScrollTop() + getWindowHeight() > getDocumentHeight() - 1.5 * clipHeight;
    }

    function hasData() {
        return 1 == parseInt(isContinue) ? !0 : !1;
    }

    function getMinMsgId() {
        var e = document.querySelectorAll("div[msgid]"), t = e.length;
        return t > 0 ? parseInt(e[t - 1].getAttribute("msgid")) : 4294967295;
    }

    function getScrollTop() {
        var e = top ? top.document : document;
        return e.documentElement.scrollTop || e.body.scrollTop;
    }

    function successHandler(data) {
        var data = eval("(" + data + ")");
        if (loading.style.display = "none", !data || 0 != data.ret)return alert("系统繁忙，请稍后再试"), void(isLoading = !1);
        isContinue = data.is_continue;
        var tmpList = JSON.parse(data.general_msg_list);
        save.saveLocation("info", tmpList.list), render.renderList(tmpList.list), isLoading = !1,
            0 == isContinue ? document.querySelector(".no_more").style.display = "" : loading.style.display = "",
            lazy.lazyInit();
    }

    function getWindowHeight() {
        var e = top ? top.document : document, t = top ? top.window : window;
        return t.innerHeight || e.documentElement.clientHeight;
    }

    function getDocumentHeight() {
        var e = top ? top.document : document;
        return e.body.scrollHeight;
    }

    function showImg(e) {
        function t(e) {
            var t, o, i = e.height, n = e.width, r = document.documentElement.clientHeight, s = document.documentElement.clientWidth;
            return (i > r || n > s) && (i / r > n / s ? (n = r / i * n, i = r) : (i = s / n * i, n = s)), t = (r - i) / 2, o = (s - n) / 2, {
                img_top: t,
                img_left: o
            };
        }

        e = e.replace("small", "normal");
        var o = new Image;
        o.src = e, o.onload = function () {
            var i = t(o), n = "";
            n = document.querySelector("#show_normal_img") ? document.querySelector("#show_normal_img") : document.createElement("div"),
                n.id = "show_normal_img", n.innerHTML = '<img id="normal_img" style="top:' + i.img_top + "px;left:" + i.img_left + 'px; position: absolute;" src="' + e + '" />',
                document.body.appendChild(n), document.getElementById("show_normal_img").onclick = function () {
                document.querySelector("#show_normal_img").style.display = "none";
            }, document.querySelector("#show_normal_img").style.display = "";
        }, DomEvent.on(window, "resize", function () {
            var e = t(o);
            document.querySelector("#normal_img").style.top = e.img_top + "px", document.querySelector("#normal_img").style.left = e.img_left + "px";
        });
    }

    function voicePlay(e) {
        var t = e.querySelector("audio"), o = t.getAttribute("fileid");
        if (Class.hasClass(e, "cur"))try {
            Report.report({
                type: 3,
                comment_id: "",
                voiceid: o,
                action: 18
            }), Class.removeClass(e, "cur");
            var i = e.querySelector("audio");
            i.pause();
        } catch (n) {
        } else {
            var r = document.querySelector(".cur");
            if (r)try {
                Class.removeClass(r, "cur");
                var i = r.querySelector("audio");
                i.pause();
            } catch (n) {
            }
            Class.addClass(e, "cur"), t.play(), Report.report({
                type: 3,
                comment_id: "",
                voiceid: o,
                action: 17
            });
        }
        DomEvent.on(t, "ended", function () {
            Class.removeClass(document.querySelector(".cur"), "cur");
        });
    }

    function scrollProcess() {
        if (lazy.detect(), isLoading)return !1;
        if (parseInt(isFriend) && hasData() && isScrollEnd()) {
            loading.style.display = "", isLoading = !0;
            var e = ["__biz=" + biz, "uin=" + uin, "key=" + key, "f=json", "frommsgid=" + getMinMsgId(), "count=10"].join("&");
            Ajax({
                url: "/mp/getmasssendmsg?" + e,
                type: "get",
                success: function (e) {
                    successHandler(e);
                }
            });
        }
    }

    var DomEvent = require("biz_common/dom/event.js"), Class = require("biz_common/dom/class.js"), Ajax = require("biz_wap/utils/ajax.js"), save = require("history/location.js"), render = require("history/render.js"), lazy = require("history/lazy.js"), Report = require("pages/report.js"), page_dom = document.querySelector("#msg_page");
    DomEvent.on(page_dom, "click", function (e) {
        var t = e.target || e.srcElement, o = t.getAttribute("hrefs"), i = "img" == t.tagName.toLowerCase() || "imgp" == t.getAttribute("data-type"), n = Class.hasClass(t, "voice");
        if (o)return save.saveLocation("scroll"), top ? top.location.href = o : location.href = o,
            !1;
        if (n)return voicePlay(t), !1;
        if (i) {
            var r = t.getAttribute("data-msgid");
            if (r) {
                var s = "/mp/getmediadata?__biz=" + biz + "&type=img&mode=small&msgid=" + r + "&uin=" + uin + "&key=" + key;
                showImg(s);
            }
            return !1;
        }
    }, !0);
    var win = top ? top.window : window;
    DomEvent.on(win, "scroll", scrollProcess), scrollProcess();
});