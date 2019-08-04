package ru.own.www.smack;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.io.IOException;

import static java.lang.Thread.sleep;

public class XMPPTCP {

    AbstractXMPPConnection conn;

    public void connect() {

        try {
            // Create a connection to the jabber.org server on a specific port.
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
//                .setUsernameAndPassword("demo", "qazwsx")
                    .setServiceName("127.0.0.1")
                    .setHost("127.0.0.1")
                    .setPort(5222)
                    .setConnectTimeout(20000)//设置连接超时时间
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)//设置是否启用安全连接
                    .build();
            conn = new XMPPTCPConnection(config);//根据配置生成一个连接
            conn.addConnectionListener(new ConnectionListener() {
                @Override
                public void connected(XMPPConnection connection) {
                    //已连接上服务器
                    System.out.println("已经连接上服务器!恭喜！");
                }

                @Override
                public void authenticated(XMPPConnection connection, boolean resumed) {
                    //已认证
                    System.out.println("已经连接上服务器!恭喜！");
                }

                @Override
                public void connectionClosed() {
                    //连接已关闭
                    System.out.println("连接已关闭");
                }

                public void connectionClosedOnError(Exception e) {
                    //关闭连接发生错误
                    System.out.println("关闭连接发生错误");
                }

                public void reconnectionSuccessful() {
                    //重连成功
                    System.out.println("重连成功");
                }


                public void reconnectingIn(int seconds) {
                    //重连中
                    System.out.println("重连中");
                }


                public void reconnectionFailed(Exception e) {
                    //重连失败
                    System.out.println("重连失败");
                }
            });


            conn.connect(); // 登录，上边的username和password分别为用户名和密码
        } catch (Exception e) {
            e.printStackTrace();
        }
        login("demo", "qazwsx");
        setStatus();
        sendMessage("admina@127.0.0.1", "我是来自java");
        ReceiveMessage();
    }

    private void sendMessage(String to, String message) {
        // 发送消息
        try {
            // 发送消息
            // Assume we've created an XMPPConnection name "connection"._
            ChatManager chatmanager = ChatManager.getInstanceFor(conn);
            Chat newChat = chatmanager.createChat(to);
            newChat.sendMessage(message);

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
//            GetXMPPConnection.closeConnection(conn);;

        }
//        原文：https://blog.csdn.net/frankcheng5143/article/details/48622649

        // 下面是另一种方式，当发送消息的时候，可以接受对方的消息，官方说的方法，但是测试不成功
        // Assume we've created an XMPPConnection name "connection"._
//        ChatManager chatmanager = ChatManager.getInstanceFor(conn);
//        Chat newChat = chatmanager.createChat("admin@127.0.0.1", new ChatMessageListener() {
//            @Override
//            public void processMessage(Chat chat, Message message) {
//                System.out.println("Received message: " + message);
//            }
//
//        });
//
//        try {
//            newChat.sendMessage("Howdy!");
//        }
//        catch (Exception e) {
//            e.printStackTrace();
//        } finally {
////            GetXMPPConnection.closeConnection(conn);;
//
//        }
//        while (true);

    }

    public void ReceiveMessage() {
        try {

            ChatManager chatmanager = ChatManager.getInstanceFor(conn);
            System.out.println("等待接受消息...");

            chatmanager.addChatListener(new ChatManagerListener() {
                @Override
                public void chatCreated(Chat chat, boolean createdLocally) {
                    if (!createdLocally) {
                        System.out.println("新消息不是本地的");
                    }
                    chat.addMessageListener(new ChatMessageListener() {
                        @Override
                        public void processMessage(Chat chat, Message msg) {
                            // TODO Auto-generated method stub
                            if (null != msg.getBody()) {
                                System.out.println(msg.getFrom() + ":" + "接收到新消息：" + msg.getBody());

                            }
                        }
                    });
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        while (true) {
            try {
                sleep(3000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void setStatus() {

        //设置成在线，这里如果改成unavailable则会显示用户不在线
        Presence presence = new Presence(Presence.Type.available);
        presence.setStatus("在线");

        try {
            conn.sendStanza(presence);//发送Presence包
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void login(String username, String password) {
        try {
            if (conn != null) {
                conn.login(username, password);
                System.out.println("user " + username + " login successfully.");
            }
        } catch (XMPPException e) {
            e.printStackTrace();
        } catch (SmackException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }

//        ---------------------
//                作者：luffy5459
//        来源：CSDN
//        原文：https://blog.csdn.net/feinifi/article/details/80816433
//        版权声明：本文为博主原创文章，转载请附上博文链接！

    }

    //注册账户
//    public void registerUser(String username, String password) {
//        HashMap<String, String> attributes = new HashMap<String, String>();//附加信息
//        AccountManager.sensitiveOperationOverInsecureConnectionDefault(true);
//        AccountManager.getInstance(conn).createAccount(username, password, attributes);//创建一个账户，username和password分别为用户名和密码
//
//    }
}