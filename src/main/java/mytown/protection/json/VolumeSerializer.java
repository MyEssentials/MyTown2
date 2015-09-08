package mytown.protection.json;

import com.google.gson.*;
import myessentials.entities.Volume;

import java.lang.reflect.Type;

public class VolumeSerializer implements JsonSerializer<Volume>, JsonDeserializer<Volume> {

    @Override
    public JsonElement serialize(Volume volume, Type typeOfSrc, JsonSerializationContext context) {
        JsonArray json = new JsonArray();
        json.add(new JsonPrimitive(volume.getMinX()));
        json.add(new JsonPrimitive(volume.getMinY()));
        json.add(new JsonPrimitive(volume.getMinZ()));
        json.add(new JsonPrimitive(volume.getMaxX()));
        json.add(new JsonPrimitive(volume.getMaxY()));
        json.add(new JsonPrimitive(volume.getMaxZ()));
        return json;
    }

    @Override
    public Volume deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        JsonArray jsonArray = json.getAsJsonArray();
        return new Volume(jsonArray.get(0).getAsInt(), jsonArray.get(1).getAsInt(), jsonArray.get(2).getAsInt(),
                jsonArray.get(3).getAsInt(), jsonArray.get(4).getAsInt(), jsonArray.get(5).getAsInt());
    }
}
