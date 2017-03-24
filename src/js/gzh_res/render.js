define("history/render.js",["history/template_helper.js","biz_common/utils/string/emoji.js","biz_common/utils/string/html.js"],function(e){
    "use strict";
    function t(e){
        for(var t=e?e.length:0,n=0;t>n;n++){
            if(e[n].comm_msg_info&&e[n].comm_msg_info.content){
                for(var i=e[n].comm_msg_info.content.replace(/\\n/g,"<br>"),o=i.split("&lt;/a&gt;"),r=[],_=0,m=o.length;m>_;_++)r.push(o[_].replace(/&lt;a(.*?)href=&quot;([^"]*)&quot;(.*?)&gt;/g,'<a href="$2">'));
                e[n].comm_msg_info.content=r.join("</a>");
            }
            if(e[n].app_msg_ext_info&&e[n].app_msg_ext_info.digest,e[n].comm_msg_info&&49==+e[n].comm_msg_info.type&&1==+e[n].is_card){
                e[n].comm_msg_info.type=17;
                var s=e[n].card.card_type?e[n].card.card_type.toLowerCase():"";
                e[n].carddata=s?e[n].card[s].base_info:{};
            }
            if(e[n].comm_msg_info&&49==+e[n].comm_msg_info.type){
                3==window.ascene?e[n].app_msg_ext_info.content_url=e[n].app_msg_ext_info.content_url.replace("scene=4","scene=37"):1==+window.__from&&(e[n].app_msg_ext_info.content_url=e[n].app_msg_ext_info.content_url.replace("scene=4","scene=35"));
                var a=e[n].app_msg_ext_info.multi_app_msg_item_list.length;
                if(a)for(var _=0;a>_;_++)3==window.ascene?e[n].app_msg_ext_info.multi_app_msg_item_list[_].content_url=e[n].app_msg_ext_info.multi_app_msg_item_list[_].content_url.replace("scene=4","scene=37"):1==+window.__from&&(e[n].app_msg_ext_info.multi_app_msg_item_list[_].content_url=e[n].app_msg_ext_info.multi_app_msg_item_list[_].content_url.replace("scene=4","scene=35"));
            }
        }
        return e;
    }
    function n(){
        msgList&&c&&(c.innerHTML+=r.render("list",{
            list:t(msgList.list),
            biz:biz,
            uin:uin,
            key:key
        })),o();
    }
    function i(e){
        if(r){
            var n=r.render("list",{
                list:t(e),
                biz:biz,
                uin:uin,
                key:key
            }),i=document.createElement("div");
            i.innerHTML=n,c.appendChild(i),o();
        }
    }
    function o(){
        var e=s(".msg_text");
        if(e)for(var t=0,n=e.length;n>t;t++)"false"==e[t].getAttribute("data-flag")&&(e[t].innerHTML=_.replaceEmoji(e[t].innerHTML),
            e[t].setAttribute("data-flag","true"));
        var i=s(".msg_item.voice");
        if(i)for(var t=0,n=i.length;n>t;t++)if("false"==i[t].getAttribute("data-flag")){
            var o=parseInt(i[t].getAttribute("length")),r=Math.ceil(o/1e3),m=r/60*150,a="10px "+(m>150?150:m).toString()+"px 10px 8px",c=r+'"';
            s(".msg_item.voice .msg_desc")[t].innerHTML=c,i[t].style.padding=a,i[t].setAttribute("data-flag","true");
        }
    }
    var r=e("history/template_helper.js"),_=e("biz_common/utils/string/emoji.js"),m=(e("biz_common/utils/string/html.js"),
    document.head||document.getElementsByTagName("head")[0],document.body,function(e){
        return document.querySelector(e);
    }),s=function(e){
        return document.querySelectorAll(e);
    },a=0,c=m(".msg_page");
    if(top){
        var l=function(){
            var e=document.body.scrollHeight;
            a!=e&&top.handlerIframe&&top.handlerIframe(document.body.scrollHeight),setTimeout(l,500);
        };
        setTimeout(l,500);
    }
    return{
        render:n,
        renderList:i
    };
});