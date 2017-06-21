package dk.dbc.mq;

import com.sun.messaging.ConnectionFactory;
import com.sun.messaging.ConnectionConfiguration;
import com.sun.messaging.jmq.ClientConstants;
import com.sun.messaging.jmq.util.DestType;
import com.sun.messaging.jmq.util.admin.DestinationInfo;
import dk.dbc.mq.json.DestinationJSON;
import dk.dbc.mq.json.JsonHandler;
import dk.dbc.mq.json.MessageJSON;
import dk.dbc.mq.json.ResultJSON;
import dk.dbc.mq.json.StatusJson;
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
import java.util.Enumeration;
import java.util.Vector;

public class CowBrow
{
    private static Logger LOGGER = LoggerFactory.getLogger(CowBrow.class);

    // magiske værdier fundet i com.sun.messaging.jmq.util.admin.MessageType
    private static String JMQADMINDEST = "__JMQAdmin";
    private static String JMQMESSAGETYPE = "JMQMessageType";
    private static String JMQDESTINATION = "JMQDestination";
    private static String JMQDESTTYPE = "JMQDestType";
    private static String JMQPAUSETARGET = "JMQPauseTarget";
    private static int GET_DESTINATIONS = 20;
    private static int CREATE_DESTINATION = 10;
    private static int DESTROY_DESTINATION = 12;
    private static int PAUSE_DESTINATION = 30;
    private static int RESUME_DESTINATION = 36;

    public static final String DEFAULT_PORT = "7676";
    public static final String DEFAULT_USER = "admin";
    public static final String DEFAULT_PASSWORD = "admin";
    public static final String DEFAULT_PAYLOAD_CUTOFF = "-1";
    public static final String DEFAULT_MESSAGES_TO_SHOW = "-1";

    private Connection connection;

    private enum QueueStateChange { PAUSE, RESUME };

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
            System.out.println(ResultJSON.writeErrorJson(500,
                String.format("error connecting to host %s port %s: %s", host,
                port, e.toString())));
        }
        return null;
    }

    public String disconnect() {
        try {
            if(connection != null)
                connection.close();
            ResultJSON<StatusJson> result = new ResultJSON<>();
            result.withResponseCode(200)
                .withResponseType(ResultJSON.TYPE_STATUS);
            result.addResponse(StatusJson.genericOk());
            return JsonHandler.toJson(result);
        } catch(JMSException e) {
            LOGGER.error("error trying to disconnect: {}", e.toString());
            return ResultJSON.writeErrorJson(500,
                String.format("error trying to disconnect: %s", e.toString()));
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

    public String sendTextMessageTo(Session session, String queueName,
            String body, CmdProperty[] properties) {
        try {
            TextMessage message = session.createTextMessage(body);
            return sendMessageTo(session, queueName, message, properties, null);
        } catch(JMSException e) {
            LOGGER.error("error sending textmessage: {}", e.toString());
            return ResultJSON.writeErrorJson(500,
                String.format("error sending textmessage: %s", e.toString()));
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

    private String sendMessageTo(Session session, String queueName,
            Message msg, CmdProperty[] properties, TemporaryQueue replyQueue)
            throws JMSException {
        Queue requestQueue = session.createQueue(queueName);
        if(replyQueue != null)
            msg.setJMSReplyTo(replyQueue);
        for(CmdProperty prop : properties) {
            Object value = prop.getValue();
            if(value instanceof Integer) {
                msg.setIntProperty(prop.getKey(), (Integer) value);
            } else if(value instanceof Double) {
                msg.setDoubleProperty(prop.getKey(), (Double) value);
            } else if(value instanceof Float) {
                msg.setFloatProperty(prop.getKey(), (Float) value);
            } else if(value instanceof String) {
                msg.setStringProperty(prop.getKey(), (String) value);
            } else {
                LOGGER.warn("property value not understood: {}", prop.getKey());
                ResultJSON<StatusJson> result = new ResultJSON<>();
                result.withResponseCode(500)
                    .withResponseType(ResultJSON.TYPE_STATUS);
                result.addResponse(new StatusJson().withMessage(String.format(
                    "property value not understood: %s", prop.getKey())));
                return JsonHandler.toJson(result);
            }
        }
        MessageProducer producer = session.createProducer(requestQueue);
        producer.send(msg);
        ResultJSON<StatusJson> result = new ResultJSON<>();
        result.withResponseCode(200)
            .withResponseType(ResultJSON.TYPE_STATUS);
        result.addResponse(StatusJson.genericOk());
        return JsonHandler.toJson(result);
    }

    public String listQueues(Session session) {
        try {
            ResultJSON<DestinationJSON> result = new ResultJSON<>();
            ObjectMessage receivedMessage = (ObjectMessage) sendBrokerCmd(
                session, GET_DESTINATIONS);
            if(receivedMessage.getIntProperty("JMQStatus") != 200)
                return JsonHandler.toJson(checkMessageStatus(receivedMessage));
            result.withResponseCode(200)
                .withResponseType(ResultJSON.TYPE_DESTINATIONS);
            Vector destinations = (Vector) receivedMessage.getObject();
            Enumeration elements = destinations.elements();
            while (elements.hasMoreElements()) {
                DestinationInfo info = (DestinationInfo) elements.nextElement();
                if (info.name.equals(JMQADMINDEST) ||
                        DestType.isInternal(info.fulltype) ||
                        DestType.isTemporary(info.type))
                    continue;
                DestinationJSON jsonObject = new DestinationJSON();
                jsonObject.consumers = info.nConsumers;
                jsonObject.maxMessages = info.maxMessages;
                jsonObject.maxMessageBytes = info.maxMessageBytes;
                jsonObject.name = info.name;
                jsonObject.type = DestType.toString(info.type);
                jsonObject.numMessages = info.nMessages;
                jsonObject.numMessageBytes = info.nMessageBytes;
                jsonObject.producers = info.nProducers;
                result.addResponse(jsonObject);
            }
            return JsonHandler.toJson(result);
        }
        catch(JMSException e) {
            LOGGER.error("error getting queue list: {}", e.toString());
            return ResultJSON.writeErrorJson(500,
                String.format("error getting queue list: %s", e.toString()));
        }
    }

    public String createQueue(Session session, String name) {
        try {
            DestinationInfo dest = new DestinationInfo();
            dest.setName(name);
            Message receivedMessage = sendBrokerCmd(session,
                CREATE_DESTINATION, dest);
            return JsonHandler.toJson(checkMessageStatus(receivedMessage));
        } catch (JMSException e) {
            LOGGER.error("error creating queue {}: {}", name, e.toString());
            return ResultJSON.writeErrorJson(500,
                String.format("error creating queue %s: %s", name, e.toString()));
        }
    }

    public String destroyQueue(Session session, String name) {
        try {
            CmdProperty[] properties = {
                new CmdProperty(JMQMESSAGETYPE, DESTROY_DESTINATION),
                new CmdProperty(JMQDESTINATION, name),
                new CmdProperty(JMQDESTTYPE, DestType.DEST_TYPE_QUEUE),
            };
            Message receivedMessage = sendBrokerCmd(session, null, properties);
            return JsonHandler.toJson(checkMessageStatus(receivedMessage));
        } catch(JMSException e){
            LOGGER.error("error destroying queue {}: {}", name, e.toString());
            return ResultJSON.writeErrorJson(500,
                String.format("error destroying queue %s: %s", name, e.toString()));
        }
    }

    public String pauseQueue(Session session, String name) {
        return sendPauseResumeMessage(session, name, QueueStateChange.PAUSE);
    }

    public String resumeQueue(Session session, String name) {
        return sendPauseResumeMessage(session, name, QueueStateChange.RESUME);
    }

    private String sendPauseResumeMessage(Session session, String name, QueueStateChange change) {
        int stateProperty = -1;
        if(change == QueueStateChange.PAUSE) {
            stateProperty = PAUSE_DESTINATION;
        } else if(change == QueueStateChange.RESUME) {
            stateProperty = RESUME_DESTINATION;
        } else {
            return ResultJSON.writeErrorJson(500,
                String.format("state change request not understood: %s", change));
        }
        try {
            CmdProperty[] properties = {
                new CmdProperty(JMQMESSAGETYPE, stateProperty),
                new CmdProperty(JMQPAUSETARGET, JMQDESTINATION),
                new CmdProperty(JMQDESTINATION, name),
                new CmdProperty(JMQDESTTYPE, DestType.DEST_TYPE_QUEUE),
            };
            Message receivedMessage = sendBrokerCmd(session, null, properties);
            return JsonHandler.toJson(checkMessageStatus(receivedMessage));
        } catch(JMSException e) {
            LOGGER.error("error pausing queue {}: {}", name, e.toString());
            return ResultJSON.writeErrorJson(500,
                String.format("error pausing queue %s: %s", name, e.toString()));
        }
    }

    private MessageJSON messageToJson(Message message) throws JMSException {
        String payload = null;
        String type = null;
        if(message instanceof TextMessage) {
            payload = ((TextMessage) message).getText();
            type = MessageJSON.TYPE_TEXTMESSAGE;
        } else if(message instanceof ObjectMessage) {
            payload = ((ObjectMessage) message).getObject().toString();
            type = MessageJSON.TYPE_OBJECTMESSAGE;
        }

        MessageJSON.MessageHeaders headers = new MessageJSON.MessageHeaders();
        headers.JMSCorrelationID = message.getJMSCorrelationID();
        headers.JMSDeliveryMode = message.getJMSDeliveryMode();
        headers.JMSMessageID = message.getJMSMessageID();
        headers.JMSPriority = message.getJMSType();
        headers.JMSTimestamp = message.getJMSTimestamp();
        headers.JMSType = message.getJMSType();

        MessageJSON<String> jsonObject = new MessageJSON<>();
        jsonObject.withPayload(payload)
            .withHeaders(headers)
            .withType(type);

        Enumeration enumeration = message.getPropertyNames();
        while(enumeration.hasMoreElements()) {
            String name = (String) enumeration.nextElement();
            Object value = message.getObjectProperty(name);

            MessageJSON.Property property = new MessageJSON.Property();
            property.key = name;
            property.value = value;
            jsonObject.addProperty(property);
        }

        return jsonObject;
    }

    public String listMessages(Session session, String queueName, int payloadCutoff,
            int messagesToShow) {
        try {
            Queue queue = session.createQueue(queueName);
            QueueBrowser browser = session.createBrowser(queue);

            ResultJSON<MessageJSON> result = new ResultJSON<>();
            result.withResponseType(ResultJSON.TYPE_MESSAGES)
                .withResponseCode(200);

            int messagesShown = 0;
            Enumeration messages = browser.getEnumeration();
            while(messages.hasMoreElements()) {
                if(messagesToShow >= 0 && messagesShown++ >= messagesToShow)
                    break;
                Message message = (Message) messages.nextElement();
                MessageJSON response = messageToJson(message);
                if(payloadCutoff >= 0 && response.payload instanceof String) {
                    String payload = "";
                    int plc=((String) response.payload).length();
                    plc=plc>payloadCutoff?payloadCutoff:plc;
                    if(payloadCutoff > 0) {
                        payload = ((String) response.payload).substring(0,
                            plc) + " [...]";
                    }
                    response.withPayload(payload);
                }
                result.addResponse(response);
            }
            return JsonHandler.toJson(result);
        } catch(JMSException e) {
            LOGGER.error("error listing messages {}", e.toString());
            return ResultJSON.writeErrorJson(500,
                String.format("error listing messages %s", e.toString()));
        }
    }

    private ResultJSON<StatusJson> checkMessageStatus(Message message) throws JMSException {
        int status = message.getIntProperty("JMQStatus");
        ResultJSON<StatusJson> result = new ResultJSON<>();
        result.withResponseCode(status)
            .withResponseType(ResultJSON.TYPE_STATUS);
        if(status != 200) {
            String errorMsg = message.getStringProperty("JMQErrorString");
            result.addResponse(new StatusJson().withMessage(errorMsg));
            LOGGER.error("got error: {}", errorMsg);
        } else {
            result.addResponse(StatusJson.genericOk());
        }
        return result;
    }

    public CmdProperty makeProperty(String key, Object value) {
        return new CmdProperty(key, value);
    }

    public class CmdProperty {
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
