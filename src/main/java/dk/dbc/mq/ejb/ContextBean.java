package dk.dbc.mq.ejb;

import dk.dbc.mq.CowBrow;
import dk.dbc.mq.json.JsonHandler;
import dk.dbc.mq.json.ResultJSON;

import javax.enterprise.context.SessionScoped;
import javax.jms.Session;
import java.io.IOException;
import java.io.Serializable;

@SessionScoped
public class ContextBean implements Serializable {
    private CowBrow context;
    private Session session;

    public boolean login(String host, String port, String user, String password) {
        context = new CowBrow();
        session = context.connect(host, port, user, password);
        return session != null;
    }

    public String getQueues() {
        return context.listQueues(session);
    }

    public String getMessages(String queue) {
        return context.listMessages(session, queue);
    }

    public String createQueue(String queue) {
        return context.createQueue(session, queue);
    }

    public String destroyQueue(String queue) {
        return context.destroyQueue(session, queue);
    }

    public String sendText(String queue, String text, String propertiesJson) {
        try {
            CowBrow.CmdProperty[] properties = JsonHandler.handleProperties(
                propertiesJson, context);
            return context.sendTextMessageTo(session, queue, text, properties);
        } catch(IOException e) {
            return ResultJSON.writeErrorJson(500, e.toString());
        }
    }
}
