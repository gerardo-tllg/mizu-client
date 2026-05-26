package meteordevelopment.meteorclient.utils.other;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import java.lang.reflect.Type;
import java.time.Instant;
import java.util.Date;

/* JADX INFO: loaded from: meteor-client-1.21.5-local.jar:meteordevelopment/meteorclient/utils/other/JsonDateDeserializer.class */
public class JsonDateDeserializer implements JsonDeserializer<Date> {
    /* JADX INFO: renamed from: deserialize, reason: merged with bridge method [inline-methods] */
    public Date m524deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        try {
            return Date.from(Instant.parse(jsonElement.getAsString()));
        } catch (Exception e) {
            return null;
        }
    }
}
