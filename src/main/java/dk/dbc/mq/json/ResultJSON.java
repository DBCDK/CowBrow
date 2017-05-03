package dk.dbc.mq.json;

import java.util.ArrayList;
import java.util.List;

public class ResultJSON<T> {
    public int statusCode;
    public String responseType;
    public List<T> responses;

    public static String TYPE_MESSAGE = "message";
    public static String TYPE_QUEUE = "queue";

    public ResultJSON() {
        responses = new ArrayList<>();
    }

    public ResultJSON withResponseCode(int statusCode) {
        this.statusCode = statusCode;
        return this;
    }

    public void addResponse(T response) {
        responses.add(response);
    }

    public ResultJSON withResponseType(String responseType) {
        this.responseType = responseType;
        return this;
    }

    public static String writeErrorJson(int statusCode, String errorMsg) {
        // hand-written json since the error could be that machine json writing failed
        return "{\"statusCode\": " + statusCode +
            ", \"responseType\": \"error\", \"response\": \"" + errorMsg + "\"}";
    }
}
