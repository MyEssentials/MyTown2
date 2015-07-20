package mytown.protection.json;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import mytown.MyTown;
import myessentials.entities.Volume;
import mytown.entities.flag.FlagType;
import mytown.protection.Protection;
import mytown.protection.segment.*;
import mytown.protection.segment.enums.BlockType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.enums.ItemType;
import mytown.protection.segment.getter.Caller;
import mytown.protection.segment.getter.Getters;
import mytown.util.exceptions.SegmentException;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.util.*;

/**
 * Serializes a protection
 */
public class ProtectionTypeAdapter extends TypeAdapter<Protection>{

    @Override
    public void write(JsonWriter out, Protection value) throws IOException {
        if(out == null) {
            return;
        }
        out.beginObject();
        out.name("modid").value(value.modid);
        out.name("segments").beginArray();
        for(SegmentTileEntity segment : value.segmentsTiles) {
            out.beginObject();
            out.name("class").value(segment.getCheckClass().getName());
            out.name("type").value("tileEntity");
            out.name("condition").value(StringUtils.join(segment.getConditionString(), " "));
            out.name("flag");
            if(segment.getFlag().getType() == Boolean.class && segment.getDenialValue() == Boolean.FALSE) {
                out.value(segment.getFlag().toString());
            } else {
                out.beginObject();
                out.name("name").value(segment.getFlag().toString());
                out.name("denialValue");
                if(segment.getDenialValue() instanceof Boolean) {
                    out.value((Boolean)segment.getDenialValue());
                } else {
                    out.value(segment.getDenialValue().toString());
                }
                out.endObject();
            }
            for (Map.Entry<String, List<Caller>> entry : segment.getGetters().getCallersMap().entrySet()) {
                out.name(entry.getKey()).beginArray();
                for(Caller caller : entry.getValue()) {
                    out.beginObject();
                    out.name("element").value(caller.getElement());
                    out.name("type").value(caller.getCallerType().toString());
                    out.endObject();
                }
                out.endArray();
            }
            for(Map.Entry<String, Object> entry : segment.getGetters().getConstantsMap().entrySet()) {
                out.name(entry.getKey()).value((Integer)entry.getValue());
            }
            out.endObject();
        }
        for(SegmentBlock segment : value.segmentsBlocks) {
            out.beginObject();
            out.name("class").value(segment.getCheckClass().getName());
            out.name("type").value("block");
            out.name("blockType").value(segment.getType().toString());
            out.name("meta").value(segment.getMeta());
            out.name("flag");
            if(segment.getFlag().getType() == Boolean.class && segment.getDenialValue() == Boolean.FALSE) {
                out.value(segment.getFlag().toString());
            } else {
                out.beginObject();
                out.name("name").value(segment.getFlag().toString());
                out.name("denialValue");
                if(segment.getDenialValue() instanceof Boolean) {
                    out.value((Boolean)segment.getDenialValue());
                } else {
                    out.value(segment.getDenialValue().toString());
                }
                out.endObject();
            }
            if(segment.getConditionString() != null)
                out.name("condition").value(StringUtils.join(segment.getConditionString(), " "));
            if(segment.hasClientUpdate()) {
                out.name("clientUpdate");
                out.beginObject();
                out.name("coords");
                out.beginArray();
                out.value(segment.getClientUpdateCoords().getMinX());
                out.value(segment.getClientUpdateCoords().getMinY());
                out.value(segment.getClientUpdateCoords().getMinZ());
                out.value(segment.getClientUpdateCoords().getMaxX());
                out.value(segment.getClientUpdateCoords().getMaxY());
                out.value(segment.getClientUpdateCoords().getMaxZ());
                out.endArray();
                out.endObject();
            }
            for (Map.Entry<String, List<Caller>> entry : segment.getGetters().getCallersMap().entrySet()) {
                out.name(entry.getKey()).beginArray();
                for(Caller caller : entry.getValue()) {
                    out.beginObject();
                    out.name("element").value(caller.getElement());
                    out.name("type").value(caller.getCallerType().toString());
                    out.endObject();
                }
                out.endArray();
            }
            for(Map.Entry<String, Object> entry : segment.getGetters().getConstantsMap().entrySet()) {
                out.name(entry.getKey()).value((Integer)entry.getValue());
            }
            out.endObject();
        }
        for(SegmentEntity segment : value.segmentsEntities) {
            out.beginObject();
            out.name("class").value(segment.getCheckClass().getName());
            out.name("type").value("entity");
            out.name("entityType").value(segment.getType().toString());
            out.name("flag");
            if(segment.getFlag().getType() == Boolean.class && segment.getDenialValue() == Boolean.FALSE) {
                out.value(segment.getFlag().toString());
            } else {
                out.beginObject();
                out.name("name").value(segment.getFlag().toString());
                out.name("denialValue");
                if(segment.getDenialValue() instanceof Boolean) {
                    out.value((Boolean)segment.getDenialValue());
                } else {
                    out.value(segment.getDenialValue().toString());
                }
                out.endObject();
            }
            if(segment.getConditionString() != null)
                out.name("condition").value(StringUtils.join(segment.getConditionString(), " "));
            for (Map.Entry<String, List<Caller>> entry : segment.getGetters().getCallersMap().entrySet()) {
                out.name(entry.getKey()).beginArray();
                for(Caller caller : entry.getValue()) {
                    out.beginObject();
                    out.name("element").value(caller.getElement());
                    out.name("type").value(caller.getCallerType().toString());
                    out.endObject();
                }
                out.endArray();
            }
            for(Map.Entry<String, Object> entry : segment.getGetters().getConstantsMap().entrySet()) {
                out.name(entry.getKey()).value((Integer)entry.getValue());
            }
            out.endObject();
        }
        for(SegmentItem segment : value.segmentsItems) {
            out.beginObject();
            out.name("class").value(segment.getCheckClass().getName());
            out.name("type").value("item");
            out.name("itemType").value(segment.getType().toString());
            out.name("onAdjacent").value(segment.isOnAdjacent());
            if(segment.getConditionString() != null)
                out.name("condition").value(StringUtils.join(segment.getConditionString(), " "));
            if(segment.hasClientUpdate()) {
                out.name("clientUpdate");
                out.beginObject();
                out.name("coords");
                out.beginArray();
                out.value(segment.getClientUpdateCoords().getMinX());
                out.value(segment.getClientUpdateCoords().getMinY());
                out.value(segment.getClientUpdateCoords().getMinZ());
                out.value(segment.getClientUpdateCoords().getMaxX());
                out.value(segment.getClientUpdateCoords().getMaxY());
                out.value(segment.getClientUpdateCoords().getMaxZ());
                out.endArray();
                out.name("isDirectional").value(segment.isDirectionalClientUpdate());
                out.endObject();
            }
            out.name("flag");
            if(segment.getFlag().getType() == Boolean.class && segment.getDenialValue() == Boolean.FALSE) {
                out.value(segment.getFlag().toString());
            } else {
                out.beginObject();
                out.name("name").value(segment.getFlag().toString());
                out.name("denialValue");
                if(segment.getDenialValue() instanceof Boolean) {
                    out.value((Boolean)segment.getDenialValue());
                } else {
                    out.value(segment.getDenialValue().toString());
                }
                out.endObject();
            }
            for (Map.Entry<String, List<Caller>> entry : segment.getGetters().getCallersMap().entrySet()) {
                out.name(entry.getKey()).beginArray();
                for(Caller caller : entry.getValue()) {
                    out.beginObject();
                    out.name("element").value(caller.getElement());
                    out.name("type").value(caller.getCallerType().toString());
                    out.endObject();
                }
                out.endArray();
            }
            for(Map.Entry<String, Object> entry : segment.getGetters().getConstantsMap().entrySet()) {
                out.name(entry.getKey()).value((Integer)entry.getValue());
            }
            out.endObject();
        }
        out.endArray();
        out.endObject();
    }

    @SuppressWarnings("unchecked")
    @Override
    public Protection read(JsonReader in) throws IOException {

        String modid = null;
        String version = null;
        List<Segment> segments = new ArrayList<Segment>();

        String nextName;
        in.beginObject();
        while(!in.peek().equals(JsonToken.END_OBJECT)) {

            nextName = in.nextName();
            if ("modid".equals(nextName)) {
                modid = in.nextString();
                if (modid == null)
                    throw new IOException("Missing modid for a protection!");
                if(!Loader.isModLoaded(modid) && !"Minecraft".equals(modid)) {
                    MyTown.instance.LOG.info("   Skipped protection because the mod {} wasn't loaded.", modid);
                    return null;
                } else if(version != null && !verifyVersion(modid, version)) {
                    MyTown.instance.LOG.info("   Skipped protection because it doesn't support the version loaded of mod: {} ({})", modid, version);
                    return null;
                }
            } else if("version".equals(nextName)) {
                version = in.nextString();
                if(modid != null && !"Vanilla".equals(modid) && !verifyVersion(modid, version)) {
                    MyTown.instance.LOG.info("   Skipped protection because it doesn't support the version loaded of mod: {} ({})", modid, version);
                    return null;
                }
            } else if ("segments".equals(nextName)) {
                in.beginArray();
                MyTown.instance.LOG.info("   ------------------------------------------------------------");
                while (in.hasNext()) {
                    Segment segment = null;
                    String type = null;
                    Class clazz = null;

                    EntityType entityType = null;
                    ItemType itemType = null;
                    BlockType blockType = null;

                    String condition = null;
                    FlagType flag = null;
                    Object denialValue = null;

                    Volume clientUpdateCoords = null;
                    boolean directionalClientUpdate = false;

                    boolean isAdjacent = false;
                    boolean hasOwner = false;
                    int meta = -1;
                    Getters getters = new Getters();

                    in.beginObject();
                    try {
                        while (!in.peek().equals(JsonToken.END_OBJECT)) {
                            nextName = in.nextName();
                            if ("class".equals(nextName)) {
                                try {
                                    clazz = Class.forName(in.nextString());
                                } catch (ClassNotFoundException ex) {
                                    throw new SegmentException("[Segment: " + clazz + "] Class " + clazz + " is invalid!", ex);
                                }
                                getters.setName(clazz.getName());
                                continue;
                            }
                            if ("type".equals(nextName)) {
                                type = in.nextString();
                                if (type == null)
                                    throw new SegmentException("[Segment: " + clazz + "] Segment is missing type!");
                                continue;
                            }
                            if ("condition".equals(nextName)) {
                                condition = in.nextString();
                                continue;
                            }
                            if ("flag".equals(nextName)) {
                                if(in.peek() == JsonToken.BEGIN_OBJECT) {
                                    in.beginObject();
                                    if("name".equals(in.nextName()))
                                        flag = FlagType.valueOf(in.nextString().toUpperCase());
                                    if("denialValue".equals(in.nextName()))
                                        denialValue = parseConstant(in, clazz.getName(), "denialValue");
                                    in.endObject();
                                } else {
                                    flag = FlagType.valueOf(in.nextString());
                                    denialValue = Boolean.FALSE;
                                    if(flag != null && flag.getType() != Boolean.class)
                                       throw new SegmentException("[Segment: " + clazz + "] Flag is not a boolean type. Non-boolean types need to have a denialValue specified.");
                                }
                                if (flag == null)
                                    throw new SegmentException("[Segment: " + clazz + "] The segment does not have a valid flag!");
                                if(flag.getType() != denialValue.getClass() || !flag.isValueAllowed(denialValue))
                                    throw new SegmentException("[Segment: " + clazz + "] The segment does not have a valid flag denial value!");
                                continue;
                            }
                            // Checking if clazz and type is not null before anything else.
                            if (clazz == null)
                                throw new SegmentException("[Protection: " + modid + "] Class is not being specified in one of the segments.");
                            if (type == null)
                                throw new SegmentException("[Segment: " + clazz + "] Type is specified after the type-specific data.");

                            if ("entity".equals(type)) {
                                if ("entityType".equals(nextName)) {
                                	entityType = EntityType.valueOf(in.nextString());
                                    if (entityType == null)
                                        throw new SegmentException("[Segment: " + clazz + "] Invalid entity type.");
                                    continue;
                                }
                            }
                            if ("item".equals(type)) {
                                if ("itemType".equals(nextName)) {
                                	itemType = ItemType.valueOf(in.nextString());
                                    if (itemType == null)
                                        throw new SegmentException("[Segment: " + clazz + "] Invalid item type.");
                                    continue;
                                }
                                if ("isAdjacent".equals(nextName)) {
                                    isAdjacent = in.nextBoolean();
                                    continue;
                                }
                                if("clientUpdate".equals(nextName)) {
                                    in.beginObject();
                                    if("coords".equals(in.nextName())) {
                                        in.beginArray();
                                        clientUpdateCoords = new Volume(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
                                        in.endArray();
                                    }
                                    if(in.peek() == JsonToken.NAME && "directional".equals(in.nextName())) {
                                        directionalClientUpdate = in.nextBoolean();
                                    }
                                    in.endObject();
                                    continue;
                                }

                            }
                            if ("block".equals(type)) {
                                if ("meta".equals(nextName)) {
                                    meta = in.nextInt();
                                    continue;
                                }
                                if("blockType".equals(nextName)) {
                                	blockType = BlockType.valueOf(in.nextString());
                                    if(blockType == null)
                                        throw new SegmentException("[Segment: " + clazz + "] Invalid block type.");
                                    continue;
                                }
                                if("clientUpdate".equals(nextName)) {
                                    in.beginObject();
                                    if("coords".equals(in.nextName())) {
                                        in.beginArray();
                                        clientUpdateCoords = new Volume(in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt(), in.nextInt());
                                        in.endArray();
                                    }
                                    in.endObject();
                                    continue;
                                }
                            }
                            if("tileEntity".equals(type)) {
                                if("hasOwner".equals(nextName)) {
                                    hasOwner = in.nextBoolean();
                                    continue;
                                }
                            }

                            // If it gets here it means that it should be some extra data that will be used in checking something
                            if(in.peek() == JsonToken.BOOLEAN || in.peek() == JsonToken.NUMBER || in.peek() == JsonToken.STRING)
                                getters.addConstant(nextName, parseConstant(in, clazz.getName(), nextName));
                            else
                                getters.addCallers(nextName, parseCallers(in, clazz.getName(), nextName));

                        }

                        in.endObject();

                        try {
                            if (type != null) {
                                if (flag == null)
                                    throw new SegmentException("[Segment: " + clazz + "] The segment does not have a valid flag!");
                                if ("tileEntity".equals(type)) {
                                    // Log if the segment is using default protection
                                    if (getters.getCallersMap().get("xMin") == null || getters.getCallersMap().get("xMax") == null || getters.getCallersMap().get("zMin") == null || getters.getCallersMap().get("zMax") == null) {
                                        MyTown.instance.LOG.info("   [Segment: {}] Could not find one of the getters (xMin, xMax, zMin, zMax). Using default protection size.", clazz);

                                        // Removing all of them since it will only create problems if left there
                                        getters.removeGetter("xMin");
                                        getters.removeGetter("xMax");
                                        getters.removeGetter("zMin");
                                        getters.removeGetter("zMax");
                                    }
                                    segment = new SegmentTileEntity(clazz, getters, flag, denialValue, condition, hasOwner);
                                } else if ("entity".equals(type)) {
                                    if (entityType == null) {
                                        MyTown.instance.LOG.info("   entityType is not specified, defaulting to 'TRACKED'.");
                                        entityType = EntityType.TRACKED;
                                    }
                                    segment = new SegmentEntity(clazz, getters, flag, denialValue, condition, entityType);
                                } else if ("item".equals(type)) {
                                    if(itemType == null) {
                                        MyTown.instance.LOG.info("   itemType is not specified, defaulting to 'RIGHT_CLICK_BLOCK'.");
                                        itemType = ItemType.RIGHT_CLICK_BLOCK;
                                    }
                                    segment = new SegmentItem(clazz, getters, flag, denialValue, condition, itemType, isAdjacent, clientUpdateCoords, directionalClientUpdate);
                                } else if ("block".equals(type)) {
                                    if(blockType == null) {
                                        MyTown.instance.LOG.info("   blockType is not specified, defaulting to 'RIGHT_CLICK'.");
                                        blockType = BlockType.RIGHT_CLICK;
                                    }
                                    segment = new SegmentBlock(clazz, getters, flag, denialValue, condition, blockType, meta, clientUpdateCoords);
                                }
                            }
                        } catch (SegmentException ex) {
                            // This catch is for missing elements or other runtime verifiable  conditions.
                            MyTown.instance.LOG.error("  " + ExceptionUtils.getStackTrace(ex));
                            MyTown.instance.LOG.error("  Segment will NOT be added, reload configs to try again.");
                        }
                    } catch (SegmentException ex) {
                        // This catch is for parsing issues when reading the segment.

                        MyTown.instance.LOG.error("  " + ExceptionUtils.getStackTrace(ex));
                        MyTown.instance.LOG.error("  Segment will NOT be added, reload configs to try again.");
                        // Skipping everything in the segment if it errors
                        while(!in.peek().equals(JsonToken.END_OBJECT))
                            in.skipValue();
                        in.endObject();
                    }
                    if (segment == null)
                        MyTown.instance.LOG.error("  [Segment: {}] Segment was not properly initialized!", clazz);
                    else {
                        // Precheck for configurations
                        //if(segment instanceof SegmentEntity && ((SegmentEntity) segment).type == EntityType.explosive && Config.useExtraEvents)
                        //    MyTown.instance.log.info("  [Segment:" + segment.getCheckClass().getName() + "] Omitting segment because use of extra events is enabled.");
                        //else {
                            MyTown.instance.LOG.info("   Added segment for class: {}", segment.getCheckClass().getName());
                            segments.add(segment);
                        //}
                    }
                    MyTown.instance.LOG.info("   ------------------------------------------------------------");
                }
                in.endArray();
            }
        }
        in.endObject();

        Protection protection;
        if(version == null)
            protection = new Protection(modid, segments);
        else
            protection = new Protection(modid, version, segments);
        return protection;
    }

    /**
     * Returns the list of callers that are next in the JSON file stream.
     */
    private List<Caller> parseCallers(JsonReader in, String clazz, String getterName) throws IOException {
        in.beginArray();
        List<Caller> callers = new ArrayList<Caller>();
        String nextName;
        while(in.hasNext()) {
            in.beginObject();
            String element = null;
            Caller.CallerType callerType = null;
            Class<?> valueType = null;

            nextName = in.nextName();
            if("element".equals(nextName))
                element = in.nextString();
            nextName = in.nextName();
            if("type".equals(nextName))
                callerType = Caller.CallerType.valueOf(in.nextString());
            if(in.peek() != JsonToken.END_OBJECT) {
                nextName = in.nextName();
                if("valueType".equals(nextName))
                    try {
                        valueType = Class.forName(in.nextString());
                    } catch (ClassNotFoundException ex) {
                        throw new SegmentException("[Segment: " + clazz + "] Getter with name " + getterName + " the valueType specified does not exist.", ex);
                    }
            }

            if(callerType == null)
                throw new SegmentException("[Segment: " + clazz + "] Getter with name " + getterName + " does not have a valid type.");
            if(element == null)
                throw new SegmentException("[Segment: " + clazz + "] Getter with name " + getterName + " does not have a value.");

            in.endObject();
            callers.add(new Caller(element, callerType, valueType));
        }
        in.endArray();
        return callers;
    }

    /**
     * Returns what value is on the next token on the JSON file stream
     */
    private Object parseConstant(JsonReader in, String clazz, String getterName) throws IOException {
        switch (in.peek()) {
            case STRING:
                return in.nextString();
            case NUMBER:
                try {
                    return in.nextInt();
                } catch (Exception ex) {
                    try {
                        return in.nextDouble();
                    } catch (Exception ex2) {
                        try {
                            return in.nextLong();
                        } catch (Exception ex3) {
                            // just ignore, it really can't get here
                            throw new SegmentException("[Segment: " + clazz + "] Getter with name " + getterName + " does not have a valid type of value.");
                        }
                    }
                }
            case BOOLEAN:
                return in.nextBoolean();
            default:
                throw new SegmentException("[Segment: " + clazz + "] Getter with name " + getterName + " does not have a valid type of value.");
        }

    }

    /**
     * Returns whether or not the protection is compatible with the mod and version
     */
    private static boolean verifyVersion(String modid, String version) {
        for(ModContainer mod : Loader.instance().getModList()) {
            if(mod.getModId().equals(modid) && mod.getVersion().startsWith(version))
                return true;
        }
        return false;
    }

}
