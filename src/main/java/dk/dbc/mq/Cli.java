package dk.dbc.mq;

import javax.jms.Session;
import java.util.NoSuchElementException;
import java.util.Scanner;

public class Cli {
    private CowBrow context;

    public Cli(CowBrow context) {
        this.context = context;
    }

    public void consume(Session session) {
        Scanner scanner = new Scanner(System.in);
        ArgsHandler.printAvailableActions();
        System.out.println("? or help for help, -h for command line " +
                "parsing help\nq or exit to quit");
        while(true) {
            try {
                String response = scanner.nextLine();
                if (response.equals("q") || response.equals("exit")) {
                    context.disconnect();
                    break;
                } else if(response.equals("?") || response.equals("help")) {
                    ArgsHandler.printAvailableActions();
                    continue;
                }
                ArgsHandler.runAction(response, context, session);
            } catch(NoSuchElementException e) {
                // ^D
                break;
            }
        }
    }
}
