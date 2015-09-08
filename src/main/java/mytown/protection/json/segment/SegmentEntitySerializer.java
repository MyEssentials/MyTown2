package mytown.protection.json.segment;

import com.google.gson.*;
import mytown.protection.segment.SegmentEntity;

import java.lang.reflect.Type;

public class SegmentEntitySerializer implements JsonSerializer<SegmentEntity>, JsonDeserializer<SegmentEntity> {

    @Override
    public JsonElement serialize(SegmentEntity src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }

    @Override
    public SegmentEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}