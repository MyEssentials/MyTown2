package mytown.protection.json.segment;

import com.google.gson.*;
import mytown.protection.segment.SegmentItem;

import java.lang.reflect.Type;

public class SegmentItemSerializer implements JsonSerializer<SegmentItem>, JsonDeserializer<SegmentItem> {

    @Override
    public JsonElement serialize(SegmentItem src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }

    @Override
    public SegmentItem deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}
