<%@ page language="java" import="java.util.*" pageEncoding="utf-8"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
%>

<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.01 Transitional//EN">
<html>
  <head>
    <base href="<%=basePath%>">
    
    <title>My JSP 'index.jsp' starting page</title>
	<meta http-equiv="pragma" content="no-cache">
	<meta http-equiv="cache-control" content="no-cache">
	<meta http-equiv="expires" content="0">    
	<meta http-equiv="keywords" content="keyword1,keyword2,keyword3">
	<meta http-equiv="description" content="This is my page">
	<!--
	<link rel="stylesheet" type="text/css" href="styles.css">
	-->
	<script type="text/javascript" src="jslib/jquery-1.9.1.js"></script>
    <script type="text/javascript" src="jslib/jsjac.js"></script>
    <script type="text/javascript" src="jslib/shared.js"></script>
    <script language="JavaScript">
    /************************************************************************
     *                       ******  global variable  *******
     ************************************************************************
     */
    var con, Debug, srcW;
    var JABBERSERVER="127.0.0.1";
    var BACKEND_TYPE="binding";
    var HTTPBASE="/jwChat/JHB/";
	var name="";
    var pass="123456";

    var jid="admin@127.0.0.1";
    var register=false;
    
    /************************************************************************
     *                       ******  global variable  *******
     ************************************************************************
     */
     
     
    
			$(function()
    		{
				
    			$("#but").click(
    				function()
    				{
    					connectServer();	
    				}
    			);
    		
    		
    		});//end of ready
    		
			function connectServer() 
			{
				
    			 name=$("#name").val();
    			 
    			 if(name=="")
    			 {
    				 alert("对不起，还没有登录");
    			 }
    			 
    			 jid=name+"@"+JABBERSERVER;
				
			  /* initialise debugger */
			  if (!Debug || typeof(Debug) == 'undefined' || !Debug.start) 
			  {
			    if (typeof(Debugger) != 'undefined')
			      Debug = new Debugger(DEBUG_LVL,'JWChat ' + cutResource(jid));
			    else 
			    {
			      Debug = new Object();
			      Debug.log = function() {};
			      Debug.start = function() {};
			    }
			  }
			  if (Debug)
			    Debug.start();
			  
			 /*   Debug.log("jid: "+jid+"\npass: "+pass,2);*/
			 console.log("jid: "+jid+"\npass: "+pass,2);

			  /* get some refs to static elements */
			  statusLed = frames["jwc_main"].document.getElementById('statusLed');
			  statusMsg = frames["jwc_main"].document.getElementById('statusMsg');
			  fmd = frames["jwc_main"].iRoster.document;
			  
			  /* set title */
			  document.title = "JWChat - " + nick;

			  /* set nick */
			  frames["jwc_main"].document.getElementById('myNickname').innerHTML = nick;  

			  /* init empty roster */
			  roster = new Roster();
			  
			  /* ***
			   * create new connection
			   */
			  var oArg = {oDbg: Debug, httpbase: HTTPBASE, timerval: timerval};
			  
			  if (BACKEND_TYPE == 'binding')
			    con = new JSJaCHttpBindingConnection(oArg);
			  else
			    con = new JSJaCHttpPollingConnection(oArg);
			  
			  /* register handlers */
			  con.registerHandler('iq',handleIQSet);
			  con.registerHandler('presence',handlePresence);
			  con.registerHandler('message',handleMessage);
			  con.registerHandler('message',handleMessageError);
			  con.registerHandler('ondisconnect',handleDisconnect);
			  con.registerHandler('onconnect',handleConnected);
			  con.registerHandler('onerror',handleConError);
			  
			  /* connect to remote */
			  oArg = {domain:JABBERSERVER,username:jid.substring(0,jid.indexOf('@')),resource:jid.substring(jid.indexOf('/')+1),pass:pass,register:register}
			  
			  if (BACKEND_TYPE == 'binding') {
			    if (opener.connect_port && !isNaN(opener.connect_port))
			      oArg.port = opener.connect_port;
			    if (opener.connect_host && opener.connect_host != '')
			      oArg.host = opener.connect_host;
			    if (opener && opener.connect_secure)
			      oArg.secure = true;
			  }
			  con.connect(oArg);
			}
    
    
    </script>
    
  </head>
  
  <body>
    <div>
    	用户名:<input type="text" id="name" value="admin" style="width: 130px;height: 30px;"></input>
    	密码:<input type="password" id="pass" value="123456" style="width: 130px;height: 30px;"></input>
    </div>
    <div>
    	<button id="but" type="button" style="width: 70px;height: 70px;">点击对话</button>
    </div>
     <br>
  </body>
</html>
