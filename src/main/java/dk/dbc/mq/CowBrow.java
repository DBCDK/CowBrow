package dk.dbc.mq;

import com.sun.messaging.ConnectionFactory;
import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.jmq.ClientConstants;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.util.admin.DestinationInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueBrowser;
import javax.jms.Session;
import javax.jms.TemporaryQueue;
import javax.jms.TextMessage;
import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Enumeration;
import java.util.Vector;

public class CowBrow
{
    private static Logger LOGGER = LoggerFactory.getLogger(CowBrow.class);

    // magiske værdier fundet i com.sun.messaging.jmq.util.admin.MessageType
    private static String JMQADMINDEST = "__JMQAdmin";
    private static String JMQMESSAGETYPE = "JMQMessageType";
    private static int GET_DESTINATIONS = 20;
    private static int CREATE_DESTINATION = 10;
    private static int DESTROY_DESTINATION = 12;

    private Connection connection;

    public Session connect(String host, String port, String user,
            String password) {
        try {
            ConnectionFactory cf = new ConnectionFactory();
            // uden CONNECTIONTYPE_ADMIN får man autorisations-fejl
            cf.setConnectionType(ClientConstants.CONNECTIONTYPE_ADMIN);
            cf.setProperty(ConnectionConfiguration.imqBrokerHostName, host);
            cf.setProperty(ConnectionConfiguration.imqBrokerHostPort, port);
            //cf.setProperty(ConnectionConfiguration.imqConnectionType, "SSL");
            connection = cf.createConnection(user, password);
            connection.start();
            return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch(JMSException e) {
            LOGGER.error("error connecting to host {} port {}: {}", host,
                port, e.toString());
        }
        return null;
    }

    public void disconnect() {
        try {
            if(connection != null)
                connection.close();
        } catch(JMSException e) {
            LOGGER.error("error trying to disconnect: {}", e.toString());
        }
    }

    private Message sendBrokerCmd(Session session, int messageType) throws JMSException {
        CmdProperty[] prop = {new CmdProperty(JMQMESSAGETYPE, messageType)};
        return sendBrokerCmd(session, null, prop);
    }

    private Message sendBrokerCmd(Session session, int messageType,
            Serializable object) throws JMSException {
        CmdProperty[] prop = {new CmdProperty(JMQMESSAGETYPE, messageType)};
        return sendBrokerCmd(session, object, prop);
    }

    private Message sendBrokerCmd(Session session, Serializable obj,
            CmdProperty[] properties) throws JMSException {
        return sendObjectMessageTo(session, JMQADMINDEST, obj, properties);
    }

    private Message sendObjectMessageTo(Session session, String queueName,
            Serializable obj, CmdProperty[] properties)
            throws JMSException {
        ObjectMessage msg = session.createObjectMessage();
        if(obj != null)
            msg.setObject(obj);
        return sendMessageForReply(session, queueName, msg, properties);
    }

    public void sendTextMessageTo(Session session, String queueName,
            String body, CmdProperty[] properties) {
        try {
            TextMessage message = session.createTextMessage(body);
            sendMessageTo(session, queueName, message, properties, null);
        } catch(JMSException e) {
            LOGGER.error("error sending textmessage: {}", e.toString());
        }
    }

    private Message sendMessageForReply(Session session, String queueName,
            Message msg, CmdProperty[] properties) throws JMSException {
        TemporaryQueue replyQueue = session.createTemporaryQueue();
        sendMessageTo(session, queueName, msg, properties, replyQueue);
        MessageConsumer consumer = session.createConsumer(replyQueue);
        ObjectMessage receivedMessage = (ObjectMessage) consumer.receive();
        receivedMessage.acknowledge();
        return receivedMessage;
    }

    private void sendMessageTo(Session session, String queueName,
            Message msg, CmdProperty[] properties, TemporaryQueue replyQueue)
            throws JMSException {
        Queue requestQueue = session.createQueue(queueName);
        if(replyQueue != null)
            msg.setJMSReplyTo(replyQueue);
        for(CmdProperty prop : properties) {
            Object value = prop.getValue();
            if(value instanceof Integer)
                msg.setIntProperty(prop.getKey(), (Integer) value);
            else if(value instanceof Double)
                msg.setDoubleProperty(prop.getKey(), (Double) value);
            else if(value instanceof Float)
                msg.setFloatProperty(prop.getKey(), (Float) value);
            else if(value instanceof String)
                msg.setStringProperty(prop.getKey(), (String) value);
            else
                LOGGER.warn("property value not understood: {}", prop.getKey());
        }
        MessageProducer producer = session.createProducer(requestQueue);
        producer.send(msg);
    }

    public void listQueues(Session session) {
        try {
            ObjectMessage receivedMessage = (ObjectMessage) sendBrokerCmd(
                session, GET_DESTINATIONS);
            if(!checkMessageStatus(receivedMessage))
                return;
            Vector destinations = (Vector) receivedMessage.getObject();
            Enumeration elements = destinations.elements();
            while (elements.hasMoreElements()) {
                DestinationInfo info = (DestinationInfo) elements.nextElement();
                if (info.name.equals(JMQADMINDEST) ||
                        DestType.isInternal(info.fulltype) ||
                        DestType.isTemporary(info.type))
                    continue;
                System.out.println(info);
            }
        }
        catch(JMSException e) {
            LOGGER.error("error getting queue list: {}", e.toString());
        }
    }

    public void createQueue(Session session, String name) {
        try {
            DestinationInfo dest = new DestinationInfo();
            dest.setName(name);
            Message receivedMessage = sendBrokerCmd(session,
                CREATE_DESTINATION, dest);
            checkMessageStatus(receivedMessage);
        } catch (JMSException e) {
            LOGGER.error("error creating queue {}: {}", name, e.toString());
        }
    }

    public void destroyQueue(Session session, String name) {
        try {
            CmdProperty[] properties = {
                new CmdProperty(JMQMESSAGETYPE, DESTROY_DESTINATION),
                new CmdProperty("JMQDestination", name),
                new CmdProperty("JMQDestType", DestType.DEST_TYPE_QUEUE),
            };
            Message receivedMessage = sendBrokerCmd(session, null, properties);
            checkMessageStatus(receivedMessage);
        } catch(JMSException e){
            LOGGER.error("error destroying queue {}: {}", name, e.toString());
        }
    }

    private String messageToString(Message message) throws JMSException {
        String body, type;
        body = type = "";
        if(message instanceof TextMessage) {
            body = ((TextMessage) message).getText();
            type = "TextMessage";
        } else if(message instanceof ObjectMessage) {
            body = ((ObjectMessage) message).getObject().toString();
            type = "ObjectMessage";
        }
        Date date = new Date(message.getJMSTimestamp());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd - HH:mm:ss");
        String dateFormated = sdf.format(date);
        return String.format("%s\t%s\t%s\n", dateFormated, type, body);
    }

    public void listMessages(Session session, String queueName) {
        try {
            Queue queue = session.createQueue(queueName);
            QueueBrowser browser = session.createBrowser(queue);
            Enumeration messages = browser.getEnumeration();
            StringBuilder sb = new StringBuilder();
            while(messages.hasMoreElements()) {
                Message message = (Message) messages.nextElement();
                String out = messageToString(message);
                sb.append(out);
            }
            System.out.println("Timestamp\tType\tBody\n");
            System.out.println(sb.toString());
        } catch(JMSException e) {
            LOGGER.error("error listing messages {}", e.toString());
        }
    }

    private boolean checkMessageStatus(Message message) throws JMSException {
        int status = message.getIntProperty("JMQStatus");
        if(status != 200) {
            String errorMsg = message.getStringProperty("JMQErrorString");
            LOGGER.error("got error: {}", errorMsg);
            return false;
        }
        return true;
    }

    public CmdProperty makeProperty(String key, Object value) {
        return new CmdProperty(key, value);
    }

    protected class CmdProperty {
        private String key;
        private Object value;
        public CmdProperty(String key, Object value) {
            this.key = key;
            this.value = value;
        }
        public String getKey() {
            return key;
        }
        public Object getValue() {
            return value;
        }
    }
}