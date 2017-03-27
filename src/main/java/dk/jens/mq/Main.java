package dk.jens.mq;

import javax.jms.Session;
import java.util.List;

public class Main {
    public static void main(String[] args)
    {
        ArgsHandler.Args parsedArgs = ArgsHandler.handleArgs(args);

        CowBrow cowBrow = new CowBrow();
        Session session = cowBrow.connect(parsedArgs.getString("host"),
            parsedArgs.getString("port"));
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
