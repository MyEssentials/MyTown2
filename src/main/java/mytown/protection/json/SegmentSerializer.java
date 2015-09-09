package mytown.protection.json;

import com.forgeessentials.permissions.persistence.JsonProvider;
import com.google.common.base.Joiner;
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
import java.util.Map;

public class SegmentSerializer implements JsonSerializer<Segment>, JsonDeserializer<Segment>{

    @Override
    public JsonElement serialize(Segment segment, Type typeOfSrc, JsonSerializationContext context) {
        JsonObject json = new JsonObject();

        json.addProperty("class", segment.getCheckClass().getName());
        if(segment.getDenialValue().equals(Boolean.FALSE)) {
            json.addProperty("flag", segment.getFlag().toString());
        } else {
            JsonObject jsonFlag = new JsonObject();
            jsonFlag.addProperty("name", segment.getFlag().toString());
            jsonFlag.addProperty("denialValue", segment.getDenialValue().toString());
            json.add("flag", jsonFlag);
        }
        if(segment.getCondition() != null) {
            json.addProperty("condition", segment.getCondition().toString());
        }
        for(Getter getter : segment.getters) {
            json.add(getter.getName(), context.serialize(getter));
        }

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
        return json;
    }

    private void serializeBlock(SegmentBlock segment, JsonObject json, JsonSerializationContext context) {
        json.addProperty("blockType", segment.getType().toString());
        json.addProperty("meta", segment.getMeta());
        if(segment.hasClientUpdate()) {
            JsonObject jsonUpdate = new JsonObject();
            jsonUpdate.add("coords", context.serialize(segment.getClientUpdateCoords()));
            json.add("clientUpdate", jsonUpdate);
        }
    }

    private void serializeEntity(SegmentEntity segment, JsonObject json, JsonSerializationContext context) {
        json.addProperty("entityType", segment.getType().toString());
    }

    private void serializeItem(SegmentItem segment, JsonObject json, JsonSerializationContext context) {
        json.addProperty("itemType", segment.getType().toString());
        json.addProperty("isAdjacent", segment.isOnAdjacent());
        if(segment.hasClientUpdate()) {
            JsonObject jsonUpdate = new JsonObject();
            jsonUpdate.add("coords", context.serialize(segment.getClientUpdateCoords()));
            jsonUpdate.addProperty("directional", segment.isDirectionalClientUpdate());
            json.add("clientUpdate", jsonUpdate);
        }
    }

    private void serializeTileEntity(SegmentTileEntity segment, JsonObject json, JsonSerializationContext context) {
        json.addProperty("hasOwner", segment.hasOwner());
    }

    @Override
    public Segment deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
        if(!json.getAsJsonObject().has("class") || !json.getAsJsonObject().has("type") || !json.getAsJsonObject().has("flag")) {
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

        FlagType flag;
        Object denialValue = Boolean.FALSE;
        if(jsonObject.get("flag").isJsonObject()) {
            flag = FlagType.valueOf(jsonObject.get("flag").getAsJsonObject().get("name").getAsString());
            denialValue = getObjectFromPrimitive(jsonObject.get("denialValue").getAsJsonPrimitive());
        } else {
            flag = FlagType.valueOf(jsonObject.get("flag").getAsString());
        }
        jsonObject.remove("flag");

        String condition = null;
        if(jsonObject.has("condition")) {
            condition = jsonObject.get("condition").getAsString();
            jsonObject.remove("condition");
        }

        segment.setCheckClass(clazz);
        segment.setFlag(flag);
        segment.setDenialValue(denialValue);
        segment.setConditionString(condition);

        for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
            Getter getter = context.deserialize(entry.getValue(), Getter.class);
            getter.setName(entry.getKey());
            segment.getters.add(getter);
        }

        return segment;
    }

    private SegmentBlock deserializeBlock(JsonObject json, JsonDeserializationContext context) {
        if(!json.has("blockType")) {
            throw new ProtectionParseException("Missing blockType identifier");
        }

        BlockType type = BlockType.valueOf(json.get("blockType").getAsString());
        json.remove("blockType");

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

        return new SegmentBlock(type, meta, clientUpdateCoords);
    }

    private SegmentEntity deserializeEntity(JsonObject json, JsonDeserializationContext context) {
        if(!json.has("entityType")) {
            throw new ProtectionParseException("Missing entityType identifier");
        }

        EntityType type = EntityType.valueOf(json.get("entityType").getAsString());
        json.remove("entityType");
        return new SegmentEntity(type);
    }

    private SegmentItem deserializeItem(JsonObject json, JsonDeserializationContext context) {
        if(!json.has("itemType")) {
            throw new ProtectionParseException("Missing itemType identifier");
        }
        if(!json.has("isAdjacent")) {
            throw new ProtectionParseException("Missing isAdjacent identifier");
        }

        ItemType type = ItemType.valueOf(json.get("itemType").getAsString());
        json.remove("itemType");

        boolean isAdjacent = json.get("isAdjacent").getAsBoolean();
        json.remove("isAdjacent");

        Volume clientUpdate = null;
        boolean isDirectionalUpdate = false;
        if(json.has("clientUpdate")) {
            clientUpdate = context.deserialize(json.get("clientUpdate").getAsJsonObject().get("coords"), Volume.class);
            isDirectionalUpdate = json.get("clientUpdate").getAsJsonObject().get("directional").getAsBoolean();
            json.remove("clientUpdate");
        }

        return new SegmentItem(type, isDirectionalUpdate, clientUpdate, isDirectionalUpdate);
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
