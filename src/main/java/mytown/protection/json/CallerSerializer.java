package mytown.protection.json;

import com.google.gson.*;
import mytown.protection.segment.caller.*;
import mytown.util.exceptions.ProtectionParseException;

import java.lang.reflect.Type;

public class CallerSerializer implements JsonSerializer<Caller>, JsonDeserializer<Caller> {

    @Override
    public JsonElement serialize(Caller caller, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();
        json.addProperty("name", caller.getName());
        json.addProperty("type", caller.getCallerTypeString());
        if(caller.getValueType() != null) {
            json.addProperty("valueType", caller.getValueType().getName());
        }
        return json;
    }

    @Override
    public Caller deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Caller caller = null;
        JsonObject jsonObj = json.getAsJsonObject();
        if(!jsonObj.has("name") || !jsonObj.has("type")) {
            throw new ProtectionParseException("Caller has null element or type field!");
        }

        String type = jsonObj.get("type").getAsString();
        String name = jsonObj.get("name").getAsString();
        Class<?> valueType = null;
        if(jsonObj.has("valueType")) {
            try {
                valueType = Class.forName(jsonObj.get("valueType").getAsString());
            } catch (ClassNotFoundException ex) {
                throw new ProtectionParseException("Invalid valueType in caller " + name);
            }
        }

        if("METHOD".equals(type)) {
            caller = new CallerMethod(name, valueType);
        } else if("FIELD".equals(type)) {
            caller = new CallerField(name, valueType);
        } else if("FORMULA".equals(type)) {
            caller = new CallerFormula(name, valueType);
        } else if("NBT".equals(type)) {
            caller = new CallerNBT(name, valueType);
        }

        return caller;
    }
}
