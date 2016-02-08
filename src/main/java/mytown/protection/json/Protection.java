package mytown.protection.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import myessentials.json.api.SerializerTemplate;
import mytown.protection.segment.*;
import mytown.util.exceptions.ProtectionParseException;

import java.lang.reflect.Type;
import java.util.List;

/**
 * This object is used only for loading the protection.
 */
public class Protection {

    public final String modid;
    public final String name;
    public final String version;

    public final Segment.Container<Segment> segments = new Segment.Container<Segment>();

    public Protection(String modid) {
        this(modid, "", "");
    }

    public Protection(String modid, String name, String version) {
        this.modid = modid;
        this.name = name;
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
            if(!protection.name.equals("")) {
                json.addProperty("name", protection.name);
            }
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
            String name = "";
            String version = "";

            if(jsonObject.has("name")) {
                name = jsonObject.get("name").getAsString();
            }
            if(jsonObject.has("version")) {
                version = jsonObject.get("version").getAsString();
            }
            
            if (!isModLoaded(modid, name, version)) {
            	return null;
            }

            Protection protection = new Protection(modid, name, version);

            if(jsonObject.has("segments")) {
                protection.segments.addAll((List<Segment>) context.deserialize(jsonObject.get("segments"), new TypeToken<List<Segment>>() {
                }.getType()));
            }

            return protection;
        }
        
        private static boolean isModLoaded(String modid, String name, String version) {
        	if ("Minecraft".equals(modid)) {
        		return true;
        	}
            for(ModContainer mod : Loader.instance().getModList()) {
                if(mod.getModId().equals(modid) && (name.length() == 0 || mod.getName().equals(name)) && mod.getVersion().startsWith(version)) {
                    return true;
                }
            }
            return false;
        }
    }
}