package mytown.protection.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;

import mytown.protection.Protection;
import mytown.protection.segment.Segment;
import mytown.util.exceptions.ProtectionParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ProtectionSerializer implements JsonSerializer<Protection>, JsonDeserializer<Protection> {

    @Override
    public JsonElement serialize(Protection protection, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        json.addProperty("modid", protection.modid);
        if(!protection.version.equals("")) {
            json.addProperty("version", protection.version);
        }
        List<Segment> segments = new ArrayList<Segment>();
        segments.addAll(protection.segmentsBlocks);
        segments.addAll(protection.segmentsEntities);
        segments.addAll(protection.segmentsItems);
        segments.addAll(protection.segmentsTiles);

        json.add("segments", context.serialize(segments));

        return json;
    }

    @Override
    public Protection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonObject jsonObject = json.getAsJsonObject();
        if(!jsonObject.has("modid")) {
            throw new ProtectionParseException("Missing modid identifier");
        }

        String modid = jsonObject.get("modid").getAsString();
        String version = "";
        if(jsonObject.has("version")) {
            version = jsonObject.get("version").getAsString();
        }
        List<Segment> segments = new ArrayList<Segment>();
        if(jsonObject.has("segments")) {
            segments.addAll((List<Segment>) context.deserialize(jsonObject.get("segments"), new TypeToken<List<Segment>>() {}.getType()));
        }

        return new Protection(modid, version, segments);
    }
}
