package mytown.protection.json;

import com.google.gson.*;
import mytown.entities.flag.FlagType;

import java.lang.reflect.Type;

public class FlagTypeSerializer implements JsonSerializer<FlagType>, JsonDeserializer<FlagType> {

    @Override
    public JsonElement serialize(FlagType flagType, Type typeOfSrc, JsonSerializationContext context) {
        return new JsonPrimitive(flagType.name);
    }

    @Override
    public FlagType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        return FlagType.valueOf(json.getAsString());
    }
}
