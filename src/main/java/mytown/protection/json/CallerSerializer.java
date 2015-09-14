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
        json.addProperty("type", getTypeFromCaller(caller));
        if(caller.getValueType() != null) {
            json.addProperty("valueType", caller.getValueType().getName());
        }
        return json;
    }

    @Override
    public Caller deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        Caller caller = null;
        JsonObject jsonObj = json.getAsJsonObject();
        if(!jsonObj.has("element") || !jsonObj.has("type")) {
            throw new ProtectionParseException("Caller has null element or type field!");
        }

        String type = jsonObj.get("type").getAsString();
        String name = jsonObj.get("element").getAsString();
        Class<?> valueType = null;
        if(jsonObj.has("valueType")) {
            try {
                valueType = Class.forName(jsonObj.get("valueType").getAsString());
            } catch (ClassNotFoundException ex) {
                throw new ProtectionParseException("Invalid valueType in caller " + name);
            }
        }

        return createCaller(type, name, valueType);
    }

    private Caller createCaller(String type, String name, Class<?> valueType) {
        if("METHOD".equals(type)) {
            return new CallerMethod(name, valueType);
        } else if("FIELD".equals(type)) {
            return new CallerField(name, valueType);
        } else if("FORMULA".equals(type)) {
            return new CallerFormula(name, valueType);
        } else if("NBT".equals(type)) {
            return new CallerNBT(name, valueType);
        }
        return null;
    }

    private String getTypeFromCaller(Caller caller) {
        return caller.getClass().getSimpleName().substring(6).toUpperCase();
    }
}
