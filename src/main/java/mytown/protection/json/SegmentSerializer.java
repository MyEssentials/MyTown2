package mytown.protection.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import myessentials.entities.Volume;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.*;
import mytown.protection.segment.enums.BlockType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.enums.ItemType;
import mytown.protection.segment.getter.Getter;
import mytown.util.exceptions.ProtectionParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SegmentSerializer implements JsonSerializer<Segment>, JsonDeserializer<Segment>{

    @Override
    public JsonElement serialize(Segment segment, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        json.addProperty("class", segment.getCheckClass().getName());

        if(segment instanceof SegmentBlock) {
            json.addProperty("type", "block");
            serializeBlock((SegmentBlock) segment, json, context);
        } else if(segment instanceof SegmentEntity) {
            json.addProperty("type", "entity");
            serializeEntity((SegmentEntity) segment, json, context);
        } else if(segment instanceof SegmentItem) {
            json.addProperty("type", "item");
            serializeItem((SegmentItem) segment, json, context);
        } else if(segment instanceof SegmentTileEntity) {
            json.addProperty("type", "tileEntity");
            serializeTileEntity((SegmentTileEntity) segment, json, context);
        }

        json.add("flags", serializeAsElementOrArray(segment.flags, context));

        if(segment.getCondition() != null) {
            json.addProperty("condition", segment.getCondition().toString());
        }
        for(Getter getter : segment.getters) {
            json.add(getter.getName(), context.serialize(getter, Getter.class));
        }

        return json;
    }

    private <T> JsonElement serializeAsElementOrArray(List<T> items, JsonSerializationContext context) {
        if(items.isEmpty()) {
            return null;
        }

        if(items.size() == 1) {
            return context.serialize(items.get(0));
        } else {
            return context.serialize(items);
        }
    }

    private void serializeBlock(SegmentBlock segment, JsonObject json, JsonSerializationContext context) {
        json.add("actions", serializeAsElementOrArray(segment.types, context));
        json.addProperty("meta", segment.getMeta());
        if(segment.clientUpdate != null) {
            JsonObject jsonUpdate = new JsonObject();
            jsonUpdate.add("coords", context.serialize(segment.clientUpdate.relativeCoords));
            json.add("clientUpdate", jsonUpdate);
        }
    }

    private void serializeEntity(SegmentEntity segment, JsonObject json, JsonSerializationContext context) {
        json.add("actions", serializeAsElementOrArray(segment.types, context));
    }

    private void serializeItem(SegmentItem segment, JsonObject json, JsonSerializationContext context) {
        json.add("actions", serializeAsElementOrArray(segment.types, context));
        json.addProperty("isAdjacent", segment.isOnAdjacent());
        if(segment.clientUpdate != null) {
            JsonObject jsonUpdate = new JsonObject();
            jsonUpdate.add("coords", context.serialize(segment.clientUpdate.relativeCoords));
            jsonUpdate.addProperty("directional", segment.directionalClientUpdate);
            json.add("clientUpdate", jsonUpdate);
        }
    }

    private void serializeTileEntity(SegmentTileEntity segment, JsonObject json, JsonSerializationContext context) {
        json.addProperty("hasOwner", segment.hasOwner());
    }

    @Override
    public Segment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(!json.getAsJsonObject().has("class") || !json.getAsJsonObject().has("type") || !json.getAsJsonObject().has("flags")) {
            throw new ProtectionParseException("One of the segments is invalid");
        }
        JsonObject jsonObject = json.getAsJsonObject();

        String type = jsonObject.get("type").getAsString();
        jsonObject.remove("type");

        Segment segment = null;
        if("block".equals(type)) {
            segment = deserializeBlock(jsonObject, context);
        } else if("entity".equals(type)) {
            segment = deserializeEntity(jsonObject, context);
        } else if("item".equals(type)) {
            segment = deserializeItem(jsonObject, context);
        } else if("tileEntity".equals(type)) {
            segment = deserializeTileEntity(jsonObject, context);
        }

        if(segment == null) {
            throw new ProtectionParseException("Identifier type is invalid");
        }

        Class<?> clazz;
        try {
            clazz = Class.forName(jsonObject.get("class").getAsString());
        } catch (ClassNotFoundException ex) {
            throw new ProtectionParseException("Class identifier is invalid");
        }
        jsonObject.remove("class");

        List<FlagType<Boolean>> flags = deserializeAsArray(jsonObject.get("flags"), context, new TypeToken<FlagType<Boolean>>() {}.getType(), new TypeToken<List<FlagType<Boolean>>>() {}.getType());        jsonObject.remove("flags");

        String condition = null;
        if(jsonObject.has("condition")) {
            condition = jsonObject.get("condition").getAsString();
            jsonObject.remove("condition");
        }

        segment.setCheckClass(clazz);
        segment.flags.addAll(flags);
        segment.setConditionString(condition);

        for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            Getter getter = context.deserialize(entry.getValue(), Getter.class);
            getter.setName(entry.getKey());
            segment.getters.add(getter);
        }

        return segment;
    }

    private <T> List<T> deserializeAsArray(JsonElement json, JsonDeserializationContext context, Type typeOfT, Type listOfT) {
        if(json.isJsonPrimitive()) {
            List<T> list = new ArrayList<T>();
            list.add((T) context.deserialize(json, typeOfT));
            return list;
        } else {
            return context.deserialize(json, listOfT);
        }
    }

    private SegmentBlock deserializeBlock(JsonObject json, JsonDeserializationContext context) {
        if(!json.has("actions")) {
            throw new ProtectionParseException("Missing actions identifier");
        }

        List<BlockType> types = deserializeAsArray(json.get("actions"), context, BlockType.class, new TypeToken<List<BlockType>>() {}.getType());
        json.remove("actions");

        int meta = -1;
        Volume clientUpdateCoords = null;

        if(json.has("meta")) {
            meta = json.get("meta").getAsInt();
            json.remove("meta");
        }

        if(json.has("clientUpdate")) {
            clientUpdateCoords = context.deserialize(json.get("clientUpdate").getAsJsonObject().get("coords"), Volume.class);
            json.remove("clientUpdate");
        }
        SegmentBlock block = new SegmentBlock(meta, clientUpdateCoords);
        block.types.addAll(types);
        return block;
    }

    private SegmentEntity deserializeEntity(JsonObject json, JsonDeserializationContext context) {
        if(!json.has("actions")) {
            throw new ProtectionParseException("Missing actions identifier");
        }

        List<EntityType> types = deserializeAsArray(json.get("actions"), context, EntityType.class, new TypeToken<List<EntityType>>() {}.getType());
        json.remove("actions");
        SegmentEntity segment = new SegmentEntity();
        segment.types.addAll(types);
        return segment;
    }

    private SegmentItem deserializeItem(JsonObject json, JsonDeserializationContext context) {
        if(!json.has("actions")) {
            throw new ProtectionParseException("Missing actions identifier");
        }
        if(!json.has("isAdjacent")) {
            throw new ProtectionParseException("Missing isAdjacent identifier");
        }

        List<ItemType> types = deserializeAsArray(json.get("actions"), context, ItemType.class, new TypeToken<List<ItemType>>() {}.getType());
        json.remove("actions");

        boolean isAdjacent = json.get("isAdjacent").getAsBoolean();
        json.remove("isAdjacent");

        Volume clientUpdate = null;
        boolean isDirectionalUpdate = false;
        if(json.has("clientUpdate")) {
            clientUpdate = context.deserialize(json.get("clientUpdate").getAsJsonObject().get("coords"), Volume.class);
            isDirectionalUpdate = json.get("clientUpdate").getAsJsonObject().get("directional").getAsBoolean();
            json.remove("clientUpdate");
        }

        SegmentItem segment = new SegmentItem(isDirectionalUpdate, clientUpdate, isDirectionalUpdate);
        segment.types.addAll(types);
        return segment;
    }

    private SegmentTileEntity deserializeTileEntity(JsonObject json, JsonDeserializationContext context) {
        if(!json.has("owner")) {
            throw new ProtectionParseException("Missing hasOwner identifier");
        }

        boolean hasOwner = json.getAsJsonObject().get("hasOwner").getAsBoolean();
        json.remove("hasOwner");
        return new SegmentTileEntity(hasOwner);
    }

    private Object getObjectFromPrimitive(JsonPrimitive json) {
        if(json.isBoolean()) {
            return json.getAsBoolean();
        } else if(json.isString()) {
            return json.getAsString();
        } else if(json.isNumber()) {
            return json.getAsNumber();
        }
        return null;
    }
}
