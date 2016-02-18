package mytown.protection.segment.getter;

import com.google.gson.*;
import com.google.gson.reflect.TypeToken;
import myessentials.json.api.SerializerTemplate;
import mytown.protection.segment.caller.Caller;
import mytown.util.exceptions.GetterException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public abstract class Getter {

    protected String name;

    public void setName(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    public void setClass(Class<?> clazz) {
        // Defaults to not used as it is only needed in Dynamic
    }

    public abstract Object invoke(Class<?> returnType, Object instance, Object... parameters) throws GetterException;

    public static class Serializer extends SerializerTemplate<Getter> {

        @Override
        public void register(GsonBuilder builder) {
            builder.registerTypeAdapter(Getter.class, this);
            new Caller.Serializer().register(builder);
        }

        @Override
        public JsonElement serialize(Getter src, Type typeOfSrc, JsonSerializationContext context) {

            if (src instanceof GetterConstant) {
                return context.serialize(((GetterConstant) src).constant);
            } else if (src instanceof GetterDynamic) {
                return context.serialize(((GetterDynamic) src).callers, new TypeToken<List<Caller>>() {}.getType());
            }
            return null;
        }

        @Override
        public Getter deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            Getter getter = null;

            if (json.isJsonPrimitive()) {
                JsonPrimitive primitive = (JsonPrimitive) json;
                if (primitive.isBoolean()) {
                    getter = new GetterConstant(primitive.getAsBoolean());
                } else if (primitive.isNumber()) {
                    getter = new GetterConstant(primitive.getAsNumber());
                } else if (primitive.isString()) {
                    getter = new GetterConstant(primitive.getAsString());
                }
            } else {
                getter = new GetterDynamic((List<Caller>) context.deserialize(json, new TypeToken<List<Caller>>() {}.getType()));
            }

            return getter;
        }
    }

    public static class Container extends ArrayList<Getter> {

        public Getter get(String getterName) {
            for(Getter getter : this) {
                if(getter.getName().equals(getterName)) {
                    return getter;
                }
            }
            return null;
        }

        public boolean remove(String getterName) {
            for(Iterator<Getter> it = iterator(); it.hasNext(); ) {
                Getter getter = it.next();
                if(getter.getName().equals(getterName)) {
                    it.remove();
                    return true;
                }
            }
            return false;
        }

        public boolean contains(String getterName) {
            for(Getter getter : this) {
                if(getter.getName().equals(getterName)) {
                    return true;
                }
            }
            return false;
        }

    }
}
