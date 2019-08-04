<%@ page language="java" pageEncoding="UTF-8" %>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>
 
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>WebIM Chat</title>
    <meta http-equiv="pragma" content="no-cache">
    <meta http-equiv="cache-control" content="no-cache">
    <meta http-equiv="expires" content="0">    
    <meta http-equiv="author" content="hoojo">
    <meta http-equiv="email" content="hoojo_@126.com">
    <meta http-equiv="blog" content="http://blog.csdn.net/IBM_hoojo">
    <meta http-equiv="blog" content="http://hoojo.cnblogs.com">
    <link rel="stylesheet" type="text/css" href="css/chat-2.0.css" />
    <script type="text/javascript">
        window.contextPath = "<%=path%>";
        // console.log(window.contextPath)
        window["serverDomin"] = "127.0.0.1";  //#服务器的地址
        // console.log(window["serverDomin"])
    </script>
    <script type="text/javascript" src="jslib/strophe.js"></script>
    <script type="text/javascript" src="jslib/jquery-1.9.1.js"></script>
    <script type="text/javascript" src="jslib/jsjac.js"></script>
    <!-- script type="text/javascript" src="debugger/Debugger.js"></script-->
    <script type="text/javascript" src="jslib/send.message.editor-1.0.js"></script>
    <script type="text/javascript" src="jslib/jquery.easydrag.js"></script>
    <script type="text/javascript" src="jslib/remote.jsjac.chat-2.0.js"></script>
    <script type="text/javascript" src="jslib/local.chat-2.0.js"></script>
    <script type="text/javascript" src="jslib/jquery.xmpp.js"></script>
    <script type="text/javascript">
    var BOSH_SERVICE = 'http://127.0.0.1:7070/http-bind/';

    // XMPP连接
    var connection = null;

    // 当前状态是否连接
    var connected = false;

    // 当前登录的JID
    var jid = "";

    // 连接状态改变的事件
    function onConnect(status) {
        console.log(status)
        if (status == Strophe.Status.CONNFAIL) {
            alert("连接失败！");
        } else if (status == Strophe.Status.AUTHFAIL) {
            alert("登录失败！");
        } else if (status == Strophe.Status.DISCONNECTED) {
            alert("连接断开！");
            connected = false;
        } else if (status == Strophe.Status.CONNECTED) {
            alert("连接成功，可以开始聊天了！");
            connected = true;

            // 当接收到<message>节，调用onMessage回调函数
            connection.addHandler(onMessage, null, 'message', null, null, null);

            // 首先要发送一个<presence>给服务器（initial presence）
            connection.send($pres().tree());
        }
    }

    // 接收到<message>
    function onMessage(msg) {

        // 解析出<message>的from、type属性，以及body子元素
        var from = msg.getAttribute('from');
        var type = msg.getAttribute('type');
        var elems = msg.getElementsByTagName('body');

        if (type == "chat" && elems.length > 0) {
            var body = elems[0];
            $("#msg").append(from + ":<br>" + Strophe.getText(body) + "<br>")
        }
        return true;
    }
    

    
    // function notifyUser(msg)
    // {
    //
    //         var elems = msg.getElementsByTagName('body');
    //         var body = elems[0];
    //         $('#notifications').append(Strophe.getText(body));
    //
    //     return true;
    // }
    //
    // function onConnect(status)
    // {
    //     $('#notifications').html('<p class="welcome">Hello! Any new posts will appear below.</p>');
    //     connection.addHandler(notifyUser, null, 'message', null, null,  null);
    //     connection.send($pres().tree());
    // }
    
        $(function () {
        	
        	// $("#conb").click(
        	// 	function()
        	// 	{
        	// 	    connection = new Strophe.Connection(BOSH_SERVICE); // in strophe.js
        	// 	    connection.connect("demo@127.0.0.1","qazwsx",onConnect);
             //        jid = "demo@127.0.0.1";
        	// 	}
        	// );

            // 通过BOSH连接XMPP服务器
            $('#btn-login').click(function() {
                if(!connected) {
                    connection = new Strophe.Connection(BOSH_SERVICE);
                    connection.connect($("#input-jid").val(), $("#input-pwd").val(), onConnect);
                    jid = $("#input-jid").val();
                }
            });

            // 发送消息
            $("#btn-send").click(function() {
                if(connected) {
                    if($("#input-contacts").val() == '') {
                        alert("请输入联系人！");
                        return;
                    }

                    // 创建一个<message>元素并发送
                    var msg = $msg({
                        to: $("#input-contacts").val(),
                        from: jid,
                        type: 'chat'
                    }).c("body", null, $("#input-msg").val());
                    connection.send(msg.tree());

                    $("#msg").append(jid + ":<br>" + $("#input-msg").val() + "<br>");
                    $("#input-msg").val('');
                } else {
                    alert("请先登录！");
                }
            });
        	
        	$("#connectBut").click(
        		function()
        		{
        			$.xmpp.connect({jid:"demo@127.0.0.1", password:"qazwsx", url:"http://127.0.0.1:7070/http-bind/"
              		   ,onDisconnect:function(){
              		      alert("Disconnected");
              		   },onConnect: function(){
                            $.xmpp.setPresence(null);
              		        console.log("Connected");
              		   },
              		   onIq: function(iq){
              		       console.log(iq);
              		   },onMessage: function(message){
              		        console.log("New message of " + message.from + ": "+message.body);
              		   },onPresence: function(presence){
              		        console.log("New presence of " + presence.from + " is "+presence.show);
              		   },onError: function(error){
              		        console.log("Error: "+error);
              		   }
              		   });
        		}
        	);

            $("#sendBut").click(
                function()
                {
                    //简单形式，参考：https://github.com/maxpowel/jQuery-XMPP-plugin
                   $.xmpp.sendMessage({body: "Hey dude! i am from demo", to:"adminA@127.0.0.1"});
                   //复杂形式
                   //  $.xmpp.sendMessage({body: "Hey dude! 我是来自demo用户", to:"admin@127.0.0.1", resource:"MyChat", otherAttr:"value"},
                   //      "<error>My custom error</error>",function(){ alert("Message sent!"); });
                }
            );

            $("#admin").click(
                function()
                {
                    $.xmpp.connect({jid:"adminA@127.0.0.1", password:"qazwsx", url:"http://127.0.0.1:7070/http-bind/"
                        ,onDisconnect:function(){
                            alert("Disconnected");
                        },onConnect: function(){
                            $.xmpp.setPresence(null);
                            console.log("Connected");
                        },
                        onIq: function(iq){
                            console.log(iq);
                        },onMessage: function(message){
                            console.log("New message of " + message.from + ": "+message.body);
                        },onPresence: function(presence){
                            console.log("New presence of " + presence.from + " is "+presence.show);
                        },onError: function(error){
                            console.log("Error: "+error);
                        }
                    });
                }
            );




            $("#login").click(function () {
                var userName = $(":text[name='userName']").val();
                var receiver = $("*[name='to']").val();
                // 建立一个聊天窗口应用，并设置发送者和消息接收者
                $.WebIM({
                    sender: userName,
                    receiver: receiver
                });
                // 登陆到openfire服务器,采用这种方式实在是调不好，只能采用下面xmpp的方式了
                // remote.jsjac.chat.login(document.userForm);  // in remote.jsjac.chat-2.0.js  line-number: 120


                $.xmpp.connect({resource:"MyChat", jid:"admin@127.0.0.1", password:"qazwsx", url:"http://127.0.0.1:7070/http-bind/"
                    ,onDisconnect:function(){
                        alert("Disconnected");
                    },onConnect: function(){
                        $.xmpp.setPresence(null);
                        console.log("Connected");
                    },
                    onIq: function(iq){
                        console.log(iq);
                    },onMessage: function(message){
                        console.log("New message of " + message.from + ": "+message.body);
                    },onPresence: function(presence){
                        console.log("New presence of " + presence.from + " is "+presence.show);
                    },onError: function(error){
                        console.log("Error: "+error);
                    }
                });



                 $("label").text(userName);
                 $("form").hide();
                 $("#newConn").show();


            });
            
            $("#logout").click(function () {
                 // 退出openfire登陆，断开链接
                 remote.jsjac.chat.logout();
                 $("form").show();
                 $("#newConn").hide();
                 $("#chat").hide(800);
            });
            
            $("#newSession").click(function () {
                var receiver = $("#sendTo").val();
                // 建立一个新聊天窗口，并设置消息接收者（发送给谁？）
                $.WebIM.newWebIM({
                    receiver: receiver
                });
            });
        });
    </script>
  </head>
  
  <body>
  JID：<input type="text" id="input-jid">
  <br>
  密码：<input type="password" id="input-pwd">
  <br>
  <button id="btn-login">登录</button>
  <div id="msg" style="height: 400px; width: 400px; overflow: scroll;"></div>
  联系人JID：
  <input type="text" id="input-contacts">
  <br>
  消息：
  <br>
  <textarea id="input-msg" cols="30" rows="4"></textarea>
  <br>
  <button id="btn-send">发送</button>
  <h1>=====================上边是通过============================================</h1>
  
      <h1>Latest content:</h1>
    <div id="notifications"></div>
    
    <div>
  		<button id="conb">Connect use Strophe</button>
  	</div>
  
    <div>
  		<button id="connectBut">Connect</button>
        <button id="sendBut">sendMessageTest</button>
  	</div>

      <div>
          <button id="admin">AdminConnect</button>
      </div>
    <!-- 登陆表单 -->
    <form name="userForm" style="background-color: #fcfcfc; width: 100%;">
        userName：<input type="text" name="userName" value="admin"/>
        password：<input type="password" name="password" value="12345"/>
        
        register: <input type="checkbox" name="register"/>
        sendTo： <input type="text" id="to" name="to" value="admin1" width="10"/>
        <input type="button" value="Login" id="login"/> 
    </form>
    <!-- 新窗口聊天 -->
    <div id="newConn" style="display: none; background-color: #fcfcfc; width: 100%;">
           User：<label></label>
           sendTo： <input type="text" id="sendTo" value="hoojo" width="10"/>
           <input type="button" value="new Chat" id="newSession"/> 
           <input type="button" value="Logout" id="logout"/>
    </div>
    <!-- 日志信息 -->
    <div id="error" style="display: ; background-color: red;"></div>
    <div id="info" style="display: ; background-color: #999999;"></div>
    <!-- 聊天来消息提示 -->
    <div class="chat-message">
        <img src="images/write_icon.png" class="no-msg"/>
        <img src="images/write_icon.gif" class="have-msg" style="display: none;"/>
    </div>
  </body>
</html>