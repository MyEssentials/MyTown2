package mytown.protection.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import myessentials.json.SerializerTemplate;
import mytown.protection.segment.*;
import mytown.util.exceptions.ProtectionParseException;

import java.lang.reflect.Type;
import java.util.List;

/**
 * This object is used only for loading the protection.
 */
public class Protection {

    public final String modid;
    public final String version;

    public final Segment.Container<Segment> segments = new Segment.Container<Segment>();

    public Protection(String modid) {
        this(modid, "");
    }

    public Protection(String modid, String version) {
        this.modid = modid;
        this.version = version;
    }

    public static class Serializer extends SerializerTemplate<Protection> {

        @Override
        public void register(GsonBuilder builder) {

        }

        @Override
        public JsonElement serialize(Protection protection, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            json.addProperty("modid", protection.modid);
            if(!protection.version.equals("")) {
                json.addProperty("version", protection.version);
            }

            json.add("segments", context.serialize(protection.segments, new TypeToken<List<Segment>>() {}.getType()));

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
            Protection protection = new Protection(modid, version);

            if(jsonObject.has("segments")) {
                protection.segments.addAll((List<Segment>) context.deserialize(jsonObject.get("segments"), new TypeToken<List<Segment>>() {
                }.getType()));
            }

            return protection;
        }
    }
}