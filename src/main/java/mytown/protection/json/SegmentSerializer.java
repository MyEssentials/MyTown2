package mytown.protection.json;

import com.google.gson.*;
import mytown.protection.segment.Segment;

import java.lang.reflect.Type;

public class SegmentSerializer implements JsonSerializer<Segment>, JsonDeserializer<Segment>{

    @Override
    public JsonElement serialize(Segment segment, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("class", segment.getCheckClass().getName());
        json.addProperty("type", );

    }

    @Override
    public Segment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}
