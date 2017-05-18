package dk.dbc.mq.json;

public class StatusJson {
    public String message;
    private static String OK = "OK";

    public StatusJson withMessage(String message) {
        this.message = message;
        return this;
    }

    public static StatusJson genericOk() {
        return new StatusJson().withMessage(OK);
    }
}
