package ru.own.www.smack;

import org.jivesoftware.smack.*;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.ChatManagerListener;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.filter.PacketFilter;
import org.jivesoftware.smack.filter.PacketTypeFilter;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.packet.Stanza;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smack.roster.Roster;
import org.jivesoftware.smack.roster.RosterEntry;
import org.jivesoftware.smack.roster.RosterListener;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.offline.OfflineMessageManager;
import org.jivesoftware.smackx.receipts.DeliveryReceipt;
import org.jivesoftware.smackx.receipts.DeliveryReceiptManager;
import org.jivesoftware.smackx.receipts.DeliveryReceiptRequest;
import org.jivesoftware.smackx.receipts.ReceiptReceivedListener;

import java.io.IOException;
import java.util.Collection;
import java.util.Date;

import static java.lang.Thread.sleep;

public class XMPPTCPOffLIne {

    private AbstractXMPPConnection conn;

    public void connect() {

        try {

            // Create a connection to the jabber.org server on a specific port.
            XMPPTCPConnectionConfiguration config = XMPPTCPConnectionConfiguration.builder()
//                .setUsernameAndPassword("demo", "qazwsx")
                    .setServiceName("127.0.0.1")
                    .setHost("127.0.0.1")
                    .setPort(5222)
                    .setConnectTimeout(20000)//设置连接超时时间
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled).setCompressionEnabled(false)//设置是否启用安全连接
                    .setSendPresence(false) //设置离线状态

                    .build();

            //需要经过同意才可以添加好友，如果打开，需要有一个监听器
//            Roster.setDefaultSubscriptionMode(Roster.SubscriptionMode.manual);

            conn = new XMPPTCPConnection(config);//根据配置生成一个连接
//            ((XMPPTCPConnection)conn).setUseStreamManagementResumption(true); //这个是流管理器，还不知具体怎么样用

            conn.addConnectionListener(new ConnectionListener() {
                @Override
                public void connected(XMPPConnection connection) {
                    //已连接上服务器
                    System.out.println("已经连接上服务器!恭喜！");
                }

                @Override
                public void authenticated(XMPPConnection connection, boolean resumed) {
                    //已认证
                    System.out.println("认证成功！");
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

            //自动添加回执，在createFilter函数中，有手动添加回执的代码, 自动添加有错误，这里就用手动的
//            ProviderManager pm = new ProviderManager();
//            // add delivery receipts
//            pm.addExtensionProvider(DeliveryReceipt.ELEMENT, DeliveryReceipt.NAMESPACE,
//                    new DeliveryReceipt.Provider());
//            pm.addExtensionProvider(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE,
//                    new DeliveryReceiptRequest.Provider());



            //原文：https://blog.csdn.net/t8500071/article/details/13094933


            conn.connect(); // 登录，上边的username和password分别为用户名和密码
        } catch (Exception e) {
            e.printStackTrace();
        }

        createFilter(); // 在这类好用，不知道放在登录之后是否好用，过滤器暂时不用，作用是对接收到的包进行过滤等操作
        RosterListener();  //必须要登录之前设置
        login("admin", "qazwsx");
        getAllFriends();

        readOfflineMsg();  //读取离线消息
        setStatus();  //设置状态
        sendMessage("demo@jql-pc", "i am from java,哈哈，你上当了"); //发送消息
        ReceiveMessage(); //接受消息

    }

    private void RosterListener() {
        Roster roster = Roster.getInstanceFor(conn);
        roster.addRosterListener(new RosterListener() {
            public void entriesAdded(Collection<String> addresses) {
                System.out.println("entries Added");
            }

            public void entriesDeleted(Collection<String> addresses) {
                System.out.println("entries Deleted");
            }
            public void entriesUpdated(Collection<String> addresses) {
                System.out.println("entries Updated");
            }
            public void presenceChanged(Presence presence) {
                System.out.println("Presence changed: " + presence.getFrom() + " " + presence);
            }
        });
    }

    // 还不能得到好友？？？？？？？？？？？？
    private void getAllFriends() {
        System.out.println("我的好友列表：=======================" );

        //        String user=conn.getUser();
        //        System.out.println("本次登录用户: " +user);

        Roster roster=Roster.getInstanceFor(conn);

        Presence tempPres = roster.getPresence("demo@127.0.0.1");
        System.out.println("demo用户状态: " +tempPres.getStatus());

        Collection<RosterEntry> rosters = roster.getEntries();
        for (RosterEntry rosterEntry : rosters){
           System.out.print("name: " +rosterEntry.getName()+ ",jid: " +rosterEntry.getUser()); //此处可获取用户JID
           System.out.println("" );
        }
        System.out.println("我的好友列表：=======================" );
    }

    // 只对接收起作用，比如可以只针对某个用户的包
    private void createFilter() {
        // Create a packet filter to listen for new messages from a particular
        // user. We use an AndFilter to combine two other filters._
        PacketFilter packetfilter=new PacketFilter() {
            public boolean accept(Stanza packet) {
				return true;  //对所有的包都接收
             //因为下面有了一些强制类型的转换，所有会一起一些错误
             //java.lang.ClassCastException: org.jivesoftware.smack.packet.Bind cannot be cast to org.jivesoftware.smack.packet.Message
			}
		};
        // 包的过滤器
        PacketTypeFilter filterMessage =  new PacketTypeFilter(Message.class);




        //创建多个过滤器
//        StanzaFilter filter = new AndFilter(new StanzaTypeFilter(Message.class),
//                new PacketTypeFilter(Presence.class),packetfilter);
        // Assume we've created an XMPPConnection name "connection".
// smack_4_1_4/releasedocs/documentation/processing.html
        // First, register a packet collector using the filter we created.
//        PacketCollector myCollector = conn.createPacketCollector(filter);
        // Normally, you'd do something with the collector, like wait for new packets.

//        // Next, create a packet listener. We use an anonymous inner class for brevity.
//        PacketListener myListener = new PacketListener() {
//            @Override
//            public void processPacket(Stanza packet) {
//                // Do something with the incoming packet here._
//                System.out.println("在过滤器里面执行");
//                Presence presence = (Presence)packet;
//                if (presence.isAvailable()) {//判断用户是否在线
//                    //发送信息代码
//                    System.out.println("用户在线");
//                }else{
//                    System.out.println("用户不在线");
//                }
////                list.remove(bareJID);//删除记录
//            }
//
//        };
        // 创建包的监听器
        PacketListener myListener = new PacketListener() {
            public void processPacket(Stanza packet) {
                // 以XML格式输出接收到的消息
                System.out.println("在过滤器里面执行");

                // 监听消息，在检查到对方要求回执时，客户端手动发送回执给对方
                if(packet instanceof Message){
                    System.out.println("Body: " + packet.getFrom());
                    Message message = (Message)packet;
                    DeliveryReceiptRequest receipt = message.getExtension(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE);
                    if(receipt != null){
                        Message receiptMessage = new Message();
                        receiptMessage.setTo(message.getFrom());
                        receiptMessage.setFrom(message.getTo());
                        receiptMessage.addExtension(new DeliveryReceipt(message.getPacketID()));
                        try {
                            System.out.println("回执 To："+message.getFrom());
                            System.out.println("回执 from："+message.getTo());
                            conn.sendPacket(receiptMessage);
//                            conn.sendStanza(receiptMessage);
                        } catch (SmackException.NotConnectedException e) {
                            e.printStackTrace();
                        }
                    }
                }

            }
        };
        // 给连接注册一个包的监听器
        conn.addPacketListener(myListener, packetfilter);

    }

    private void readOfflineMsg() {
        OfflineMessageManager offlineManager = new OfflineMessageManager(conn);
        java.util.Iterator<Message> it = null;
        try {
            it = offlineManager.getMessages().iterator();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        while (it.hasNext()) {
            org.jivesoftware.smack.packet.Message message = it.next();
            System.out.println("收到离线消息, Received from 【" + message.getFrom() + "】 message: " + message.getBody());
        }
        //删除离线消息
        try {
            offlineManager.deleteMessages();
        } catch (SmackException.NoResponseException e) {
            e.printStackTrace();
        } catch (XMPPException.XMPPErrorException e) {
            e.printStackTrace();
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
        //将状态设置成在线
        Presence presence;
        presence = new Presence(Presence.Type.available);
        try {
            conn.sendPacket(presence);
        } catch (SmackException.NotConnectedException e) {
            e.printStackTrace();
        }
    }

    private void sendMessage(String to, String message) {

        Message messa;
        messa = new Message(to, message);

//        addResponse(messa);
//        要求回执的代码，已经抽取为了函数，就是addResponse函数，但是由于是针对消息的，所以我还是放到这里了，没有测试addResponse
        //在发消息之前通过DeliveryReceiptManager订阅回执
        DeliveryReceiptManager ss=DeliveryReceiptManager.getInstanceFor(conn);
        ss.addReceiptReceivedListener(new ReceiptReceivedListener() {
            public void onReceiptReceived(String fromJid, String toJid, String receiptId, Stanza receipt) {
                // If the receiving entity does not support delivery receipts,
                // then the receipt received listener may not get invoked.
                System.out.println("接收到回执了");
                System.err.println((new Date()).toString()+ " - drm:" + receipt.toXML());
            }
        });
//        ss.autoAddDeliveryReceiptRequests(); //这个好像是自动添加要求回执的要求，如果这个打开，那么下面这个addTo貌似就不用了，我没有测试
        //将消息放到DeliveryReceiptRequest中，这样就可以在发送Message后发送回执请求
        String deliveryReceiptId=DeliveryReceiptRequest.addTo(messa);
        System.out.println("sendMessage: deliveryReceiptId for this message is: " + deliveryReceiptId);

        if (null != to && !"@".equals(to)) {
            to = to + "@" + conn.getServiceName();
        }
        // 发送消息
        boolean flag = conn.isConnected();  //代表是否连接
        while (!flag)
            login("admin", "qazwsx");
        flag = conn.isConnected();  //代表是否连接
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        Chat newChat=null;
        while (true) {
            try {
                // 发送消息
                // Assume we've created an XMPPConnection name "connection"._
                ChatManager chatmanager = ChatManager.getInstanceFor(conn);

                newChat = chatmanager.createChat(to, new ChatMessageListener() {
                    @Override
                    public void processMessage(Chat chat, Message message) {
                        System.out.println("in function sendMessage: Received message: " + message);
                    }
                });
                //原文：https://blog.csdn.net/frankcheng5143/article/details/48622649
                // 下面是另一种方式，当发送消息的时候，可以接受对方的消息，官方说的方法，但是测试不成功
        //        Chat newChat = chatmanager.createChat("admin@127.0.0.1", new ChatMessageListener() {
        //            @Override
        //            public void processMessage(Chat chat, Message message) {
        //                System.out.println("Received message: " + message);
        //            }
        //
        //        });
//                newChat.sendMessage(messa); // 不知道为什么，这个不起作用
                conn.sendStanza(messa); // 这是官方要求回执的方法，目前测试的是不起作用
//                conn.sendStanza(messa);
                System.out.println("正确发送了消息:"+message);
                break; //发送完毕之后，就不在循环发送了
            } catch (SmackException.NotConnectedException e) { //没有连接，则连接上再次发送
                System.out.println("发送之前重新连接");
                login("admin", "qazwsx");
                try {
                    sleep(2000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                //e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
//            GetXMPPConnection.closeConnection(conn);
            }
        }
    }

    private void addResponse(Message message) {


        //在发消息之前通过DeliveryReceiptManager订阅回执
        DeliveryReceiptManager ss=DeliveryReceiptManager.getInstanceFor(conn);
        ss.addReceiptReceivedListener(new ReceiptReceivedListener() {
            public void onReceiptReceived(String fromJid, String toJid, String receiptId, Stanza receipt) {
                // If the receiving entity does not support delivery receipts,
                // then the receipt received listener may not get invoked.
                System.out.println("接收到回执了");
                System.err.println((new Date()).toString()+ " - drm:" + receipt.toXML());
            }
        });
//        ss.autoAddDeliveryReceiptRequests(); //这个好像是自动添加要求回执的要求，如果这个打开，那么下面这个addTo貌似就不用了，我没有测试
        //将消息放到DeliveryReceiptRequest中，这样就可以在发送Message后发送回执请求
        // 来为你的packet添加<request xmlns='urn:xmpp:receipts'/>节点。
        String deliveryReceiptId=DeliveryReceiptRequest.addTo(message);
        System.out.println("sendMessage: deliveryReceiptId for this message is: " + deliveryReceiptId);
        // 添加回执请求,上边那个addTo已经起作用了
//        ss.addDeliveryReceiptRequest(message);

        //也可以这样添加回执请求，我没有测试
        //DeliveryReceiptRequest deliveryReceiptRequest = new DeliveryReceiptRequest();
        //message.addExtension(new DeliveryReceiptRequest());
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
                                System.out.println((msg.getFrom() + "-->" + msg.getTo() + "\n" + msg.getBody()));

                            } else {
                                System.out.println("接收到新消息：内容为空");
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
    }

    //注册账户
//    public void registerUser(String username, String password) {
//        HashMap<String, String> attributes = new HashMap<String, String>();//附加信息
//        AccountManager.sensitiveOperationOverInsecureConnectionDefault(true);
//        AccountManager.getInstance(conn).createAccount(username, password, attributes);//创建一个账户，username和password分别为用户名和密码
//
//    }
}