package dk.dbc.mq.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import dk.dbc.mq.CowBrow;

import java.io.IOException;
import java.util.ArrayList;

public class JsonHandler {
    private static final ObjectMapper mapper = new ObjectMapper();
    private static final JsonFactory jsonFactory = new JsonFactory();

    public static final String JSON_KEY_PROPERTIES = "properties";
    public static final String JSON_KEY_KEY = "key";
    public static final String JSON_KEY_VALUE = "value";

    public static String toJson(ResultJSON result) {
        try {
            return mapper.writeValueAsString(result);
        } catch(JsonProcessingException e) {
            return ResultJSON.writeErrorJson(500, e.toString());
        }
    }

    public static CowBrow.CmdProperty[] handleProperties(String json, CowBrow context) throws IOException {
        ArrayList<CowBrow.CmdProperty> propertyList = new ArrayList<>();
        JsonParser parser = jsonFactory.createParser(json);
        while(!parser.isClosed()) {
            JsonToken token = parser.nextToken();
            if(JsonToken.FIELD_NAME.equals(token)) {
                String fieldName = parser.getCurrentName();
                parser.nextToken();
                if(fieldName.equals(JSON_KEY_PROPERTIES)) {
                    ArrayNode tree = mapper.readTree(parser);
                    for(JsonNode node : tree) {
                        if(!node.has(JSON_KEY_KEY) || !node.has(JSON_KEY_VALUE))
                            continue;
                        String key = node.get(JSON_KEY_KEY).asText();
                        String value = node.get(JSON_KEY_VALUE).asText();
                        propertyList.add(context.makeProperty(key, value));
                    }
                }

            }
        }
        return propertyList.toArray(new CowBrow.CmdProperty[0]);
    }
}
