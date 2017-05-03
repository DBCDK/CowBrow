package dk.dbc.mq.json;

import java.util.ArrayList;
import java.util.List;

public class MessageJSON<T> {
    public String type;
    public T payload;
    public MessageHeaders headers;
    public List<Property> properties;

    public static String TYPE_TEXTMESSAGE = "TextMessage";
    public static String TYPE_OBJECTMESSAGE = "ObjectMessage";

    public MessageJSON() {
        properties = new ArrayList<>();
    }

    public MessageJSON withPayload(T payload) {
        this.payload = payload;
        return this;
    }

    public MessageJSON withHeaders(MessageHeaders headers) {
        this.headers = headers;
        return this;
    }

    public MessageJSON withType(String type) {
        this.type = type;
        return this;
    }

    public void addProperty(Property property) {
        properties.add(property);
    }

    public static class MessageHeaders {
        public int JMSDeliveryMode;
        public String JMSPriority;
        public String JMSMessageID;
        public long JMSTimestamp;
        public String JMSCorrelationID;
        public String JMSType;
    }

    public static class Property {
        public String key;
        public Object value;
    }
}
