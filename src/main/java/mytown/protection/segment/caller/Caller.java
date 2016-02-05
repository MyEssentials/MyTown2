package mytown.protection.segment.caller;

import com.google.gson.*;
import myessentials.json.api.SerializerTemplate;
import mytown.protection.segment.caller.reflectasm.ASMCallerField;
import mytown.protection.segment.caller.reflectasm.ASMCallerMethod;
import mytown.util.exceptions.ProtectionParseException;

import java.lang.reflect.Type;

/**
 * A caller is the information needed to get an object/value
 */
public abstract class Caller {
    protected String name;
    protected Class<?> valueType;
    protected Class<?> checkClass;

    public abstract Object invoke(Object instance, Object... parameters) throws Exception;

    public Class<?> getValueType() {
        return valueType;
    }

    public String getName() {
        return name;
    }

    public void setClass(Class<?> clazz) {
        checkClass = clazz;
    }

    public Class<?> nextClass() throws Exception {
        return null;
    }

    public static class Serializer extends SerializerTemplate<Caller> {

        @Override
        public void register(GsonBuilder builder) {
            builder.registerTypeAdapter(Caller.class, this);
        }

        @Override
        public JsonElement serialize(Caller caller, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("element", caller.name);
            json.addProperty("type", getTypeFromCaller(caller));
            if(caller.getValueType() != null) {
                json.addProperty("valueType", caller.valueType.getName());
            }
            return json;
        }

        @Override
        public Caller deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {

            JsonObject jsonObj = json.getAsJsonObject();
            if(!jsonObj.has("element") || !jsonObj.has("type")) {
                throw new ProtectionParseException("Caller has null element or type field!");
            }

            Caller caller = createCaller(jsonObj.get("type").getAsString());
            if(caller == null) {
                throw new ProtectionParseException("Caller has an invalid type!");
            }

            caller.name = jsonObj.get("element").getAsString();

            if(jsonObj.has("valueType")) {
                try {
                    caller.valueType = Class.forName(jsonObj.get("valueType").getAsString());
                } catch (ClassNotFoundException ex) {
                    throw new ProtectionParseException("Invalid valueType in caller " + caller.name);
                }
            }

            return caller;
        }

        private Caller createCaller(String type) {
            if("METHOD".equals(type)) {
                return new ASMCallerMethod();
            } else if("FIELD".equals(type)) {
                return new ASMCallerField();
            } else if("FORMULA".equals(type)) {
                return new CallerFormula();
            } else if("NBT".equals(type)) {
                return new CallerNBT();
            }
            return null;
        }

        private String getTypeFromCaller(Caller caller) {
            return caller.getClass().getSimpleName().substring(6).toUpperCase();
        }
    }
}