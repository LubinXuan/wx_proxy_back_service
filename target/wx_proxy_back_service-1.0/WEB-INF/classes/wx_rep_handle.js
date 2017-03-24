function replaceServerResDataAsyncHandle(req, res, serverResData, callback) {
    var call = true;
    if (/mp\/getmasssendmsg/i.test(req.url)) {
        var rspStr = serverResData.toString();
        if (rspStr !== "") {
            try {//防止报错退出程序
                var data = {url: req.url};
                if (req.url.indexOf('&frommsgid=') > -1) {
                    data.data = serverResData;
                } else {
                    var reg = /msgList = (.*?);\r\n/;//定义历史消息正则匹配规则
                    var ret = reg.exec(rspStr);//转换变量为string
                    var isFriend = /isFriend = "(.)",\r\n/.exec(rspStr)
                    data.data = ret[1];
                    data.friend = isFriend == '1';
                }
                call = false;
                HttpPost("/wx_proxy/MsgListRecServlet", data, function (rspStr) {
                    callback(serverResData + rspStr)
                });
            } catch (e) {

            }
        }
    }
    call && callback(serverResData);
}
function HttpPost(path, data, callback) {
    var http = require('http');
    var options = {
        method: "POST",
        host: "10.2.2.92",//注意没有http://，这是服务器的域名。
        port: 8080,
        path: path,//接收程序的路径和文件名
        headers: {
            'Content-Type': 'application/json; charset=UTF-8'
        }
    };
    var req = http.request(options, function (res) {
        res.setEncoding('utf8');
        res.on('data', function (chunk) {
            console.log('BODY: ' + chunk);
            callback && callback(chunk);
        });
    });
    req.on('error', function (e) {
        console.log('problem with request: ' + e.message);
        callback && callback("")
    });
    req.write(JSON.stringify(data));
    req.end();
}