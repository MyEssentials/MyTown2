package mytown.protection.json.segment;

import com.google.gson.*;
import mytown.protection.segment.SegmentTileEntity;

import java.lang.reflect.Type;

public class SegmentTileEntitySerializer implements JsonSerializer<SegmentTileEntity>, JsonDeserializer<SegmentTileEntity> {

    @Override
    public JsonElement serialize(SegmentTileEntity src, Type typeOfSrc, JsonSerializationContext context) {
        return null;
    }

    @Override
    public SegmentTileEntity deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return null;
    }
}
