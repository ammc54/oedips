package oedips.challenge.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.core.JsonProcessingException;

/**
 * Json Utilitary class
 */
public class JsonUtils {

    private JsonUtils() {
    }

    /**
     * Prints the object as a json.
     * 
     * @param object
     * @return json string
     */
    public static String asJsonString(Object object) {
        try {
            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
            return ow.writeValueAsString(object);
        } catch (JsonProcessingException e) {
            return "{\"error\":{\"error processing json\"}}";
        }
    }

}
