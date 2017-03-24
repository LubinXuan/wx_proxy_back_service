//访问新页面
setTimeout(function () {
    window.location.href = "http://mp.weixin.qq.com/mp/getmasssendmsg?__biz={biz}#wechat_webview_type=1&wechat_redirect";
}, 1500);


//页面滚动
setTimeout(function () {scrollTo(0,document.body.scrollHeight);}, 1500);

setTimeout(function () {document.title="暂时没有公众号需要爬取，3秒后重试"; window.location.reload();},3000);