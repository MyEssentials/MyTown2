package mytown.protection.json.segment;

import com.google.gson.*;
import mytown.protection.segment.SegmentBlock;

import java.lang.reflect.Type;

public class SegmentBlockSerializer implements JsonSerializer<SegmentBlock>, JsonDeserializer<SegmentBlock> {

    @Override
    public JsonElement serialize(SegmentBlock src, Type typeOfSrc, JsonSerializationContext context) {
        

        return null;
    }

    @Override
    public SegmentBlock deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}