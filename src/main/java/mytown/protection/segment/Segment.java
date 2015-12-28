package mytown.protection.segment;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import com.google.gson.internal.LazilyParsedNumber;

import myessentials.entities.Volume;
import myessentials.json.SerializerTemplate;
import mytown.MyTown;
import mytown.new_datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.FlagType;
import mytown.protection.ProtectionManager;
import mytown.protection.segment.enums.BlockType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.enums.ItemType;
import mytown.protection.segment.enums.Priority;
import mytown.protection.segment.getter.Getter;
import mytown.util.exceptions.ConditionException;
import mytown.util.exceptions.GetterException;
import mytown.util.exceptions.ProtectionParseException;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.common.util.FakePlayer;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Type;
import java.util.*;

/**
 * A part of the protection that protects against a specific thing.
 */
public abstract class Segment {
    protected boolean isDisabled = false;
    protected Priority priority = Priority.NORMAL;
    protected Class<?> checkClass;
    protected Condition condition;
    protected final List<FlagType<Boolean>> flags = new ArrayList<FlagType<Boolean>>();
    protected final Getter.Container getters = new Getter.Container();

    public boolean isDisabled() {
        return isDisabled;
    }

    public boolean shouldCheckType(Class<?> clazz) {
        return checkClass.isAssignableFrom(clazz);
    }

    public Class<?> getCheckClass() {
        return this.checkClass;
    }

    public Priority getPriority() {
        return this.priority;
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
                if (username.length() == 36 && (username.split("-", -1).length - 1) == 4) {
                    UUID uuid = UUID.fromString(username);
                    return MyTownUniverse.instance.getOrMakeResident(uuid);
                }
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

    protected boolean hasPermissionAtLocation(Resident res, int dim, int x, int y, int z) {
        if (res != null && res.getFakePlayer()) {
            if(!ProtectionManager.hasPermission(res, FlagType.FAKERS, dim, x, y, z)) {
                return false;
            }
        } else {
            for(FlagType<Boolean> flagType : flags) {
                if(!ProtectionManager.hasPermission(res, flagType, dim, x, y, z)) {
                    return false;
                }
            }
        }
        return true;
    }

    protected boolean hasPermissionAtLocation(Resident res, int dim, Volume volume) {
        if (res != null && res.getFakePlayer()) {
            if(!ProtectionManager.hasPermission(res, FlagType.FAKERS, dim, volume)) {
                return false;
            }
        } else {
            for (FlagType<Boolean> flagType : flags) {
                if(!ProtectionManager.hasPermission(res, flagType, dim, volume)) {
                    return false;
                }
            }
        }
        return true;
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

    protected int getRange(Object object) {
        try {
            return getters.contains("range") ? ((LazilyParsedNumber) getters.get("range").invoke(LazilyParsedNumber.class, object, object)).intValue() : 0;
        } catch (GetterException ex) {
            return 0;
        }
    }

    protected void disable() {
        MyTown.instance.LOG.error("Disabling segment for {}", checkClass.getName());
        MyTown.instance.LOG.info("Reload protections to enable it again.");
        this.isDisabled = true;
    }

    public static class Serializer extends SerializerTemplate<Segment> {

        @Override
        public void register(GsonBuilder builder) {
            builder.registerTypeAdapter(Segment.class, this);
            new Getter.Serializer().register(builder);
            new Volume.Serializer().register(builder);
            new FlagType.Serializer().register(builder);
        }

        @Override
        public JsonElement serialize(Segment segment, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();
            json.addProperty("class", segment.checkClass.getName());

            if (segment instanceof SegmentSpecialBlock) {
                json.addProperty("type", "specialBlock");
                serializeSpecialBlock((SegmentSpecialBlock) segment, json, context);
            } else {
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
                if(segment.priority != Priority.NORMAL) {
                    json.addProperty("priority", segment.priority.toString());
                }
                for(Getter getter : segment.getters) {
                    json.add(getter.getName(), context.serialize(getter, Getter.class));
                }
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

        private void serializeSpecialBlock(SegmentSpecialBlock segment, JsonObject json, JsonSerializationContext context) {
            json.addProperty("meta", segment.getMeta());
            json.addProperty("isAlwaysBreakable", segment.isAlwaysBreakable);
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
            if(!json.getAsJsonObject().has("class")) {
                throw new ProtectionParseException("One of the segments is missing a class identifier");
            }

            JsonObject jsonObject = json.getAsJsonObject();
            String classString = jsonObject.get("class").getAsString();

            if(!json.getAsJsonObject().has("type")) {
                throw new ProtectionParseException("Segment for " + classString + " is missing a type");
            }
            String type = jsonObject.get("type").getAsString();
            jsonObject.remove("type");

            Segment segment = null;
            if ("specialBlock".equals(type)) {
                segment = deserializeSpecialBlock(jsonObject, context);
            } else if("block".equals(type)) {
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

            try {
                segment.checkClass = Class.forName(classString);
            } catch (ClassNotFoundException ex) {
                //throw new ProtectionParseException("Invalid class identifier: " + classString);
            	MyTown.instance.LOG.error("Invalid class identifier {" + classString + "}: >>> Segment Rejected <<<");
            	return null;
            }
            jsonObject.remove("class");

            if (!(segment instanceof SegmentSpecialBlock)) {
                if(!json.getAsJsonObject().has("flags")) {
                    throw new ProtectionParseException("Segment for " + classString + " is missing flags");
                }
                segment.flags.addAll(deserializeAsArray(jsonObject.get("flags"), context, new TypeToken<FlagType<Boolean>>() {}, new TypeToken<List<FlagType<Boolean>>>() {}.getType()));
                jsonObject.remove("flags");

                if(jsonObject.has("condition")) {
                    segment.condition = new Condition(jsonObject.get("condition").getAsString());
                    jsonObject.remove("condition");
                }

                if(jsonObject.has("priority")) {
                    segment.priority = Priority.valueOf(jsonObject.get("priority").getAsString());
                    jsonObject.remove("priority");
                }

                for(Map.Entry<String, JsonElement> entry : jsonObject.entrySet()) {
                    Getter getter = context.deserialize(entry.getValue(), Getter.class);
                    getter.setName(entry.getKey());
                    segment.getters.add(getter);
                }
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
                segment.clientUpdate = new ClientBlockUpdate((Volume) context.deserialize(json.get("clientUpdate").getAsJsonObject().get("coords"), Volume.class));
                json.remove("clientUpdate");
            }

            return segment;
        }

        private SegmentSpecialBlock deserializeSpecialBlock(JsonObject json, JsonDeserializationContext context) {
            SegmentSpecialBlock segment = new SegmentSpecialBlock();

            if(json.has("meta")) {
                segment.meta = json.get("meta").getAsInt();
                json.remove("meta");
            }

            if(json.has("isAlwaysBreakable")) {
                segment.isAlwaysBreakable = json.get("isAlwaysBreakable").getAsBoolean();
                json.remove("isAlwaysBreakable");
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
                segment.clientUpdate = new ClientBlockUpdate((Volume) context.deserialize(jsonClientUpdate.get("coords"), Volume.class));
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

    public static class Container<T extends Segment> extends ArrayList<T> {

        public List<T> get(Class<?> clazz) {
            List<T> usableSegments = new ArrayList<T>();
            for(Segment segment : this) {
                if(!segment.isDisabled() && segment.shouldCheckType(clazz)) {
                    usableSegments.add((T)segment);
                }
            }
            if(usableSegments.size() > 1) {
                Priority highestPriority = Priority.LOWEST;
                for(Segment segment : usableSegments) {
                    if(highestPriority.ordinal() < segment.getPriority().ordinal()) {
                        highestPriority = segment.getPriority();
                    }
                }

                for(Iterator<T> it = usableSegments.iterator(); it.hasNext();) {
                    Segment segment = it.next();
                    if(segment.getPriority() != highestPriority) {
                        it.remove();
                    }
                }
            }
            return usableSegments;
        }
    }
}
