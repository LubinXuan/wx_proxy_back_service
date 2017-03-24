var utils      = require("./util"),
    bodyParser = require("body-parser"),
    path       = require("path"),
    fs         = require("fs"),
    Promise    = require("promise");

var isRootCAFileExists = require("./certMgr.js").isRootCAFileExists(),
    interceptFlag      = false;

var back_service = "10.2.2.92";

//e.g. [ { keyword: 'aaa', local: '/Users/Stella/061739.pdf' } ]
var mapConfig = [], configFile = "mapConfig.json";
function saveMapConfig(content,cb){
    new Promise(function(resolve,reject){
        var anyproxyHome = utils.getAnyProxyHome(),
            mapCfgPath   = path.join(anyproxyHome,configFile);

        if(typeof content == "object"){
            content = JSON.stringify(content);
        }
        resolve({
            path    :mapCfgPath,
            content :content
        });
    })
    .then(function(config){
        return new Promise(function(resolve,reject){
            fs.writeFile(config.path, config.content, function(e){
                if(e){
                    reject(e);
                }else{
                    resolve();
                }
            });
        });
    })
    .catch(function(e){
        cb && cb(e);
    })
    .done(function(){
        cb && cb();
    });
}
function getMapConfig(cb){
    var read = Promise.denodeify(fs.readFile);

    new Promise(function(resolve,reject){
        var anyproxyHome = utils.getAnyProxyHome(),
            mapCfgPath   = path.join(anyproxyHome,configFile);

        resolve(mapCfgPath);
    })
    .then(read)
    .then(function(content){
        return JSON.parse(content);
    })
    .catch(function(e){
        cb && cb(e);
    })
    .done(function(obj){
        cb && cb(null,obj);
    });
}

setTimeout(function(){
    //load saved config file
    getMapConfig(function(err,result){
        if(result){
            mapConfig = result;
        }
    });
},1000);


module.exports = {
    token: Date.now(),
    summary:function(){
        var tip = "the default rule for AnyProxy.";
        if(!isRootCAFileExists){
            tip += "\nRoot CA does not exist, will not intercept any https requests.";
        }
        return tip;
    },

    shouldUseLocalResponse : function(req,reqBody){
        //intercept all options request
        var simpleUrl = (req.headers.host || "") + (req.url || "");
        mapConfig.map(function(item){
            var key = item.keyword;
            if(simpleUrl.indexOf(key) >= 0){
                req.anyproxy_map_local = item.local;
                return false;
            }
        });

        if(req.url.indexOf("mp/getmasssendmsg")>-1){
            if(req.url.indexOf("key=")<0){
                return false;
            }

            if(!req.headers['cookie']){
                return false;
            }
        }

        return !!req.anyproxy_map_local;
    },

    dealLocalResponse : function(req,reqBody,callback){
        if(req.anyproxy_map_local){
            fs.readFile(req.anyproxy_map_local,function(err,buffer){
                if(err){
                    callback(200, {}, "[AnyProxy failed to load local file] " + err);
                }else{
                    var header = {
                        'Content-Type': utils.contentType(req.anyproxy_map_local)
                    };
                    callback(200, header, buffer);
                }
            });
        }
        if(req.url.indexOf("mp/getmasssendmsg")>-1){
            HttpPost(req.clientIp,"/wx_proxy/report", {
                "url":req.url,
                "headers":JSON.stringify(req.headers)
            },false);
        }
    },

    replaceRequestProtocol:function(req,protocol){
    },

    replaceRequestOption : function(req,option){
        option.headers['client'] = req.clientIp;
        return option
    },

    replaceRequestData: function(req,data){
    },

    replaceResponseStatusCode: function(req,res,statusCode){
    },

    replaceResponseHeader: function(req,res,header){
    },

    // Deprecated
    // replaceServerResData: function(req,res,serverResData){
    //     return serverResData;
    // },

    //replaceServerResDataAsync: replaceServerResDataAsyncHandle,

    pauseBeforeSendingResponse: function(req,res){
    },

    shouldInterceptHttpsReq:function(req){
        return interceptFlag;
    },

    //[beta]
    //fetch entire traffic data
    fetchTrafficData: function(id,info){},

    setInterceptFlag: function(flag){
        interceptFlag = flag && isRootCAFileExists;
    },

    _plugIntoWebinterface: function(app,cb){

        app.get("/filetree",function(req,res){
            try{
                var root = req.query.root || utils.getUserHome() || "/";
                utils.filewalker(root,function(err, info){
                    res.json(info);
                });
            }catch(e){
                res.end(e);
            }
        });

        app.use(bodyParser.json());
        app.get("/getMapConfig",function(req,res){
            res.json(mapConfig);
        });
        app.post("/setMapConfig",function(req,res){
            mapConfig = req.body;
            res.json(mapConfig);

            saveMapConfig(mapConfig);
        });

        cb();
    },

    _getCustomMenu : function(){
        return [
            // {
            //     name:"test",
            //     icon:"uk-icon-lemon-o",
            //     url :"http://anyproxy.io"
            // }
        ];
    }
};


function replaceServerResDataAsyncHandle(req, res, serverResData, callback) {
    callback(serverResData);
}

var querystring = require('querystring');
var http = require('http');

function HttpPost(client, path, data, json, callback) {
    var options = {
        method: "POST",
        host: back_service,
        port: 8080,
        path: path,
        headers: {
            'client':client,
            'Content-Type': json?'application/json':'application/x-www-form-urlencoded'
        }
    };

    
    var data_callback = undefined;

    if(data){
        var data_write;
        if(json){
            data_write = JSON.stringify(data);
        }else{
            data_write = querystring.stringify(data);
        }
        data_callback = function(req){
            req.write(data_write);
        }
    }

    sendWithRetry(options, data_callback, callback, 10);
}


function sendWithRetry(options,data_callback, callback, retry) {
    var req = http.request(options, function (res) {
        res.setEncoding('utf8');
        var data_chunk = '';
        res.on('data', function (chunk) {
            data_chunk+=chunk;
        }).on('end', function () {
            callback && callback(data_chunk);
        });
    });

    req.on('error', function (e) {
        if(retry>0){
            console.log('retry problem with request: ' + e.message);
            //5s 后重试
            setTimeout(function(){
                sendWithRetry(options,data_callback, callback, retry-1);
            },5000)
        }else{
            console.log('problem with request: ' + e.message);
            callback && callback('problem with request: ' + e.message)    
        }
    });
    data_callback && data_callback(req);
    req.end();
}