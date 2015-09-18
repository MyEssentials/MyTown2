package mytown.protection.segment.getter;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import mytown.protection.segment.caller.Caller;
import mytown.util.exceptions.GetterException;

import java.lang.reflect.Type;
import java.util.List;

public abstract class Getter {

    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public abstract Object invoke(Class<?> returnType, Object instance, Object... parameters) throws GetterException;

    public static class Serializer implements JsonSerializer<Getter>, JsonDeserializer<Getter> {

        @Override
        public JsonElement serialize(Getter src, Type typeOfSrc, JsonSerializationContext context) {

            if(src instanceof GetterConstant) {
                return context.serialize(((GetterConstant) src).constant);
            } else if(src instanceof GetterDynamic) {
                return context.serialize(((GetterDynamic) src).callers, new TypeToken<List<Caller>>() {}.getType());
            }
            return null;
        }

        @Override
        public Getter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Getter getter = null;

            if(json.isJsonPrimitive()) {
                JsonPrimitive primitive = (JsonPrimitive) json;
                if(primitive.isBoolean()) {
                    getter = new GetterConstant(primitive.getAsBoolean());
                } else if(primitive.isNumber()) {
                    getter = new GetterConstant(primitive.getAsNumber());
                } else if(primitive.isString()) {
                    getter = new GetterConstant(primitive.getAsString());
                }
            } else {
                getter = new GetterDynamic((List<Caller>) context.deserialize(json, new TypeToken<List<Caller>>() {}.getType()));
            }

            return getter;
        }
    }
}
