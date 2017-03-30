package dk.jens.mq;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Session;
import java.util.List;

public class Main {
    private static Logger LOGGER = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args)
    {
        ArgsHandler.Args parsedArgs = ArgsHandler.handleArgs(args);

        CowBrow cowBrow = new CowBrow();
        Session session = cowBrow.connect(parsedArgs.getString("host"),
            parsedArgs.getString("port"), parsedArgs.getString("user"),
            parsedArgs.getString("password"));
        if(session == null) {
            LOGGER.error("starting jms session failed");
            System.exit(1);
        }
        List<String> actions = parsedArgs.getList("action");
        if(actions.get(0).equals("cli")) {
            Cli cli = new Cli(cowBrow);
            cli.consume(session);
        } else {
            ArgsHandler.runAction(parsedArgs, cowBrow, session);
        }
        cowBrow.disconnect();
    }
}
