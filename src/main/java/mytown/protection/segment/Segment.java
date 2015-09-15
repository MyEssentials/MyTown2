package mytown.protection.segment;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;
import myessentials.entities.Volume;
import mytown.MyTown;
import mytown.api.container.GettersContainer;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.FlagType;
import mytown.protection.ProtectionUtils;
import mytown.protection.segment.enums.BlockType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.enums.ItemType;
import mytown.protection.segment.getter.Getter;
import mytown.util.exceptions.ConditionException;
import mytown.util.exceptions.GetterException;
import mytown.util.exceptions.ProtectionParseException;
import net.minecraft.entity.player.EntityPlayer;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A part of the protection that protects against a specific thing.
 */
public abstract class Segment {
    protected boolean isDisabled = false;
    protected Class<?> checkClass;
    protected Condition condition;
    protected final List<FlagType<Boolean>> flags = new ArrayList<FlagType<Boolean>>();
    protected final GettersContainer getters = new GettersContainer();

    protected boolean hasPermissionAtLocation(Resident res, int dim, int x, int y, int z) {
        for(FlagType<Boolean> flagType : flags) {
            if(!ProtectionUtils.hasPermission(res, flagType, dim, x, y, z)) {
                return false;
            }
        }
        return true;
    }

    protected boolean hasPermissionAtLocation(Resident res, int dim, Volume volume) {
        for (FlagType<Boolean> flagType : flags) {
            if(!ProtectionUtils.hasPermission(res, flagType, dim, volume)) {
                return false;
            }
        }
        return true;
    }

    public Resident getOwner(Object object) {
        try {
            EntityPlayer player = getters.contains("owner") ? (EntityPlayer) getters.get("owner").invoke(EntityPlayer.class, object, object) : null;
            if(player == null)
                return null;
            return MyTownUniverse.instance.getOrMakeResident(player);
        } catch (GetterException ex) {
            try {
                String username = getters.contains("owner") ? (String) getters.get("owner").invoke(String.class, object, object) : null;
                if (username == null)
                    return null;
                return MyTownUniverse.instance.getOrMakeResident(username);
            } catch (GetterException ex2) {
                try {
                    UUID uuid = getters.contains("owner") ? (UUID) getters.get("owner").invoke(UUID.class, object, object) : null;
                    if (uuid == null)
                        return null;
                    return MyTownUniverse.instance.getOrMakeResident(uuid);
                } catch (GetterException ex3) {
                    return null;
                }
            }
        }
    }

    protected boolean shouldCheck(Object object) {
        try {
            if (condition != null && !condition.execute(object, getters)) {
                return false;
            }
        } catch (GetterException ex) {
            MyTown.instance.LOG.error("Encountered error when checking condition for {}", checkClass.getSimpleName());
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
            disable();
        } catch (ConditionException ex) {
            MyTown.instance.LOG.error("Encountered error when checking condition for {}", checkClass.getSimpleName());
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
            disable();
        }
        return true;
    }

    public boolean shouldCheck(Class<?> clazz) {
        return checkClass.isAssignableFrom(clazz);
    }

    public int getRange(Object object) {
        try {
            return getters.contains("range") ? ((LazilyParsedNumber) getters.get("range").invoke(LazilyParsedNumber.class, object, object)).intValue() : 0;
        } catch (GetterException ex) {
            return 0;
        }
    }

    public boolean isDisabled() {
        return isDisabled;
    }

    public void disable() {
        MyTown.instance.LOG.error("Disabling segment for {}", checkClass.getName());
        MyTown.instance.LOG.info("Reload protections to enable it again.");
        this.isDisabled = true;
    }

    public static class Serializer implements JsonSerializer<Segment>, JsonDeserializer<Segment> {
        @Override
        public JsonElement serialize(Segment segment, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("class", segment.checkClass.getName());

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

            if(segment.condition != null) {
                json.addProperty("condition", segment.condition.toString());
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
            json.addProperty("isAdjacent", segment.isAdjacent);
            if(segment.clientUpdate != null) {
                JsonObject jsonUpdate = new JsonObject();
                jsonUpdate.add("coords", context.serialize(segment.clientUpdate.relativeCoords));
                jsonUpdate.addProperty("directional", segment.directionalClientUpdate);
                json.add("clientUpdate", jsonUpdate);
            }
        }

        private void serializeTileEntity(SegmentTileEntity segment, JsonObject json, JsonSerializationContext context) {
            json.addProperty("retainsOwner", segment.retainsOwner);
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

            List<FlagType<Boolean>> flags = deserializeAsArray(jsonObject.get("flags"), context, new TypeToken<FlagType<Boolean>>() {}, new TypeToken<List<FlagType<Boolean>>>() {}.getType());
            jsonObject.remove("flags");

            String condition = null;
            if(jsonObject.has("condition")) {
                condition = jsonObject.get("condition").getAsString();
                jsonObject.remove("condition");
            }

            segment.checkClass = clazz;
            segment.flags.addAll(flags);
            if(condition != null) {
                segment.condition = new Condition(condition);
            }

            for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                Getter getter = context.deserialize(entry.getValue(), Getter.class);
                getter.setName(entry.getKey());
                segment.getters.add(getter);
            }

            return segment;
        }

        private <T> List<T> deserializeAsArray(JsonElement json, JsonDeserializationContext context, TypeToken<T> typeToken, Type listOfT) {
            if(json.isJsonPrimitive()) {
                List<T> list = new ArrayList<T>();
                list.add((T) context.deserialize(json, typeToken.getType()));
                return list;
            } else {
                return context.deserialize(json, listOfT);
            }
        }

        private SegmentBlock deserializeBlock(JsonObject json, JsonDeserializationContext context) {
            if(!json.has("actions")) {
                throw new ProtectionParseException("Missing actions identifier");
            }
            SegmentBlock segment = new SegmentBlock();
            segment.types.addAll(deserializeAsArray(json.get("actions"), context, new TypeToken<BlockType>() {}, new TypeToken<List<BlockType>>() {}.getType()));
            json.remove("actions");


            if(json.has("meta")) {
                segment.meta = json.get("meta").getAsInt();
                json.remove("meta");
            }

            if(json.has("clientUpdate")) {
                segment.clientUpdate = context.deserialize(json.get("clientUpdate").getAsJsonObject().get("coords"), Volume.class);
                json.remove("clientUpdate");
            }

            return segment;
        }

        private SegmentEntity deserializeEntity(JsonObject json, JsonDeserializationContext context) {
            if(!json.has("actions")) {
                throw new ProtectionParseException("Missing actions identifier");
            }

            SegmentEntity segment = new SegmentEntity();

            segment.types.addAll(deserializeAsArray(json.get("actions"), context, new TypeToken<EntityType>() {}, new TypeToken<List<EntityType>>() {}.getType()));
            json.remove("actions");

            return segment;
        }

        private SegmentItem deserializeItem(JsonObject json, JsonDeserializationContext context) {
            if(!json.has("actions")) {
                throw new ProtectionParseException("Missing actions identifier");
            }

            SegmentItem segment = new SegmentItem();

            segment.types.addAll(deserializeAsArray(json.get("actions"), context, new TypeToken<ItemType>() {}, new TypeToken<List<ItemType>>() {}.getType()));
            json.remove("actions");

            if(json.has("isAdjacent")) {
                segment.isAdjacent = json.get("isAdjacent").getAsBoolean();
                json.remove("isAdjacent");
            }

            if(json.has("clientUpdate")) {
                JsonObject jsonClientUpdate = json.get("clientUpdate").getAsJsonObject();
                segment.clientUpdate = context.deserialize(jsonClientUpdate.get("coords"), Volume.class);
                if(jsonClientUpdate.has("directional")) {
                    segment.directionalClientUpdate = jsonClientUpdate.get("directional").getAsBoolean();
                }
                json.remove("clientUpdate");
            }

            return segment;
        }

        private SegmentTileEntity deserializeTileEntity(JsonObject json, JsonDeserializationContext context) {
            SegmentTileEntity segment = new SegmentTileEntity();

            segment.retainsOwner = json.getAsJsonObject().get("retainsOwner").getAsBoolean();
            json.remove("retainsOwner");

            return segment;
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
}
