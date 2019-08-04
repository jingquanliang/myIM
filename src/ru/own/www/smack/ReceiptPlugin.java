package ru.own.www.smack;

import java.io.File;
import org.dom4j.Element;
import org.jivesoftware.openfire.session.Session;
import org.jivesoftware.openfire.XMPPServer;
import org.jivesoftware.openfire.container.Plugin;
import org.jivesoftware.openfire.container.PluginManager;
import org.jivesoftware.openfire.interceptor.InterceptorManager;
import org.jivesoftware.openfire.interceptor.PacketInterceptor;
import org.jivesoftware.openfire.interceptor.PacketRejectedException;
import org.xmpp.packet.Message;
import org.xmpp.packet.Packet;

public class ReceiptPlugin implements Plugin, PacketInterceptor{
    private XMPPServer server;
    private String domain;
    private InterceptorManager interceptorManager;

    public void initializePlugin(PluginManager manager, File pluginDirectory) {
        server = XMPPServer.getInstance();
        domain = XMPPServer.getInstance().getServerInfo().getXMPPDomain();
        interceptorManager = InterceptorManager.getInstance();
        interceptorManager.addInterceptor(this);
    }

    public void interceptPacket(Packet packet, Session session,
                                boolean incoming, boolean processed) throws PacketRejectedException {
        if(packet instanceof Message && incoming == true && processed == false){
            Message message = (Message)packet;
            String to = message.getTo().getNode();
            //注意插件中Message类来自tinder.jar包， DeliveryReceipt来自smackx.jar包
//			PacketExtension receipt = message.getExtension(DeliveryReceiptRequest.ELEMENT, DeliveryReceipt.NAMESPACE);
            Element receipt = message.getChildElement("request", "urn:xmpp:receipts");
            if(receipt != null){
                Message receiptMessage = new Message();
                receiptMessage.setTo(message.getFrom());
                receiptMessage.setFrom(message.getTo());
//				Element received = receiptMessage.addChildElement(DeliveryReceipt.ELEMEN, DeliveryReceipt.NAMESPACE);
                Element received = receiptMessage.addChildElement("received", "urn:xmpp:receipts");
                received.setAttributeValue("id", message.getID());
                try{
                    server.getPacketDeliverer().deliver(receiptMessage);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        }
    }

    public void destroyPlugin() {
        interceptorManager.removeInterceptor(this);
    }
}
