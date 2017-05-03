package dk.dbc.mq.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class JsonHandler {
    private static final ObjectMapper mapper = new ObjectMapper();

    public static String toJson(ResultJSON result) {
        try {
            return mapper.writeValueAsString(result);
        } catch(JsonProcessingException e) {
            return ResultJSON.writeErrorJson(500, e.toString());
        }
    }
}
