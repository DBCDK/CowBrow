package dk.dbc.mq;

import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.action.AppendArgumentAction;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.ArgumentParserException;
import net.sourceforge.argparse4j.inf.Namespace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Session;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ArgsHandler {
    private static Logger LOGGER = LoggerFactory.getLogger(ArgsHandler.class);

    private static String[] actionList = {"list", "create", "destroy",
        "sendtext", "listmessages"};
    private static String availableActions = Stream.of(actionList).collect(
        Collectors.joining(", "));

    public static void runAction(String args, CowBrow context, Session session) {
        runAction(args.split(" "), context, session);
    }

    public static void runAction(String[] argStrings, CowBrow context, Session session) {
        Args args = handleArgs(argStrings);
        runAction(args, context, session);
    }

    public static void runAction(Args args, CowBrow context, Session session) {
        List<String> actions = args.getList("action");
        String action = actions.get(0);
        CowBrow.CmdProperty[] properties = handleProperties(
            args.getList("prop"), context);
        String response = "";
        if(Arrays.stream(actionList).noneMatch(action::equals)) {
            LOGGER.warn("action {} not recognized", action);
        } else if(action.equals("list")) {
            response = context.listQueues(session);
        } else if(action.equals("create")) {
            response = context.createQueue(session, args.getString("queuename"));
        } else if(action.equals("destroy")) {
            response = context.destroyQueue(session, args.getString("queuename"));
        } else if(action.equals("sendtext")) {
            response = context.sendTextMessageTo(session, args.getString("queuename"),
                actions.get(1), properties);
        } else if(action.equals("listmessages")) {
            response = context.listMessages(session, args.getString("queuename"));
        }
        System.out.println(response);
    }

    private static CowBrow.CmdProperty[] handleProperties(List<ArrayList<String>> propertyValuesList, CowBrow context) {
        List<CowBrow.CmdProperty> properties = propertyValuesList.stream()
            .map(pair -> context.makeProperty(pair.get(0), pair.get(1)))
            .collect(Collectors.toList());
        return properties.toArray(new CowBrow.CmdProperty[0]);
    }

    public static Args handleArgs(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("CowBrow");
        parser.addArgument("action").nargs("+").help(String.format(
            "%s - [cli to start cli interface]", availableActions));
        parser.addArgument("-H", "--host").setDefault("");
        parser.addArgument("-p", "--port").setDefault("7676");
        parser.addArgument("-q", "--queuename");
        parser.addArgument("--prop")
            .nargs(2)
            .action(new AppendArgumentAction())
            .setDefault(new ArrayList<String>());
        parser.addArgument("--user").setDefault("admin");
        parser.addArgument("--password").setDefault("admin");
        Namespace ns = null;
        try {
            ns = parser.parseArgs(args);
        } catch(ArgumentParserException e) {
            parser.handleError(e);
            System.exit(1);
        }
        return Args.newArgs(ns.getAttrs());
    }

    public static void printAvailableActions() {
        System.out.println(String.format("Available actions:\n%s",
            availableActions));
    }

    // wrapper over argparse4j Namespace to avoid leaking argument
    // handling details
    protected static class Args extends Namespace {
        public Args(Map<String, Object> attrs) {
            super(attrs);
        }
        public static Args newArgs(Map<String, Object> attrs) {
            return new Args(attrs);
        }
    }
}
