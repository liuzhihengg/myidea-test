package javabase.portal;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class JsonUtil {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private JsonUtil() {
    }

    public static JsonNode readTree(String json) throws IOException {
        return MAPPER.readTree(json);
    }

    public static String toJson(Object value) throws IOException {
        return MAPPER.writeValueAsString(value);
    }

    public static com.fasterxml.jackson.databind.node.ObjectNode objectNode() {
        return MAPPER.createObjectNode();
    }

    public static List<JsonNode> toList(JsonNode arrayNode) {
        List<JsonNode> list = new ArrayList<>();
        arrayNode.forEach(list::add);
        return list;
    }
}
