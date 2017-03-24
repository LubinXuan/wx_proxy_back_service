/**
 * Created by xuanlubin on 2017/2/21.
 */
(function () {


    window.onerror = function () {
        if (arguments) {
            var i;
            for (i in arguments) {
                alert(arguments[i]);
            }
        }
        if (-1 != arguments[0].indexOf('WeixinJSBridge')) {
            location.reload();
        }
    };

    var loadInterval = 2000;

    document.title = "[jsInterceptor]" + document.title;

    var expertMinId = 0xffffffff;

    var pageScrollTask;

    var noMore = document.querySelector("div.more_wrapper.no_more");

    var page = 1;

    var loadNextGzh = function () {
        clearInterval(pageScrollTask);
        location.href = "http://10.2.2.92:8080/wx_proxy/init?page=" + page + "&biz=" + encodeURIComponent(window['biz']);
    };

    var getMinId = function () {
        var msgList = document.querySelectorAll("div.msg_list"), l = msgList.length;
        var images = window['images'];
        if (images && images.length > 0) {
            images.splice(0, images.length)
        }
        if (l > 10) {
            for (var i = 0; i < 10; i++) {
                try {
                    msgList[i].parentNode.removeChild(msgList[i]);
                } catch (e) {
                    alert(e)
                }
            }
        }
        var e = document.querySelectorAll("div[msgid]"), t = e.length;
        return t > 0 ? parseInt(e[t - 1].getAttribute("msgid")) : 0xffffffff;
    };

    var loadNextPage = function () {
        if (window['isFriend'] == '0' || window['isContinue'] == '0' || noMore.style.display != 'none') {
            loadNextGzh();
        } else {
            var curMinId = getMinId();
            if (expertMinId > curMinId) {
                page++;
                document.title = "p:" + page + " " + noMore.style.display + " " + expertMinId + " " + curMinId;
                expertMinId = curMinId;
            }
            //alert(loadWrapper.style.display)
            scrollTo(0, 0);
            setTimeout(function () {
                scrollTo(0, document.body.scrollHeight);
            }, 500);
        }
    };

    pageScrollTask = setInterval(loadNextPage, loadInterval, loadInterval)
})();
