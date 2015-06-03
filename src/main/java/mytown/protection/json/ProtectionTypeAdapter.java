package mytown.protection.json;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import mytown.MyTown;
import mytown.entities.flag.FlagType;
import mytown.protection.Protection;
import mytown.protection.segment.*;
import mytown.protection.segment.enums.BlockType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.enums.ItemType;
import mytown.protection.segment.getter.Caller;
import mytown.protection.segment.getter.Getters;
import mytown.util.exceptions.SegmentException;
import org.apache.commons.lang.exception.ExceptionUtils;
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
            out.name("class").value(segment.theClass.getName());
            out.name("type").value("tileEntity");
            out.name("condition").value(StringUtils.join(segment.conditionString, " "));
            out.name("flag");
            if(segment.flag.getType() == Boolean.class && segment.denialValue == Boolean.FALSE) {
                out.value(segment.flag.toString());
            } else {
                out.beginObject();
                out.name("name").value(segment.flag.toString());
                out.name("denialValue");
                if(segment.denialValue instanceof Boolean) {
                    out.value((Boolean)segment.denialValue);
                } else {
                    out.value(segment.denialValue.toString());
                }
                out.endObject();
            }
            for (Map.Entry<String, List<Caller>> entry : segment.getters.getCallersMap().entrySet()) {
                out.name(entry.getKey()).beginArray();
                for(Caller caller : entry.getValue()) {
                    out.beginObject();
                    out.name("element").value(caller.element);
                    out.name("type").value(caller.type.toString());
                    out.endObject();
                }
                out.endArray();
            }
            for(Map.Entry<String, Object> entry : segment.getters.getConstantsMap().entrySet()) {
                out.name(entry.getKey()).value((Integer)entry.getValue());
            }
            out.endObject();
        }
        for(SegmentBlock segment : value.segmentsBlocks) {
            out.beginObject();
            out.name("class").value(segment.theClass.getName());
            out.name("type").value("block");
            out.name("blockType").value(segment.type.toString());
            out.name("meta").value(segment.meta);
            out.name("flag");
            if(segment.flag.getType() == Boolean.class && segment.denialValue == Boolean.FALSE) {
                out.value(segment.flag.toString());
            } else {
                out.beginObject();
                out.name("name").value(segment.flag.toString());
                out.name("denialValue");
                if(segment.denialValue instanceof Boolean) {
                    out.value((Boolean)segment.denialValue);
                } else {
                    out.value(segment.denialValue.toString());
                }
                out.endObject();
            }
            if(segment.conditionString != null)
                out.name("condition").value(StringUtils.join(segment.conditionString, " "));
            for (Map.Entry<String, List<Caller>> entry : segment.getters.getCallersMap().entrySet()) {
                out.name(entry.getKey()).beginArray();
                for(Caller caller : entry.getValue()) {
                    out.beginObject();
                    out.name("element").value(caller.element);
                    out.name("type").value(caller.type.toString());
                    out.endObject();
                }
                out.endArray();
            }
            for(Map.Entry<String, Object> entry : segment.getters.getConstantsMap().entrySet()) {
                out.name(entry.getKey()).value((Integer)entry.getValue());
            }
            out.endObject();
        }
        for(SegmentEntity segment : value.segmentsEntities) {
            out.beginObject();
            out.name("class").value(segment.theClass.getName());
            out.name("type").value("entity");
            out.name("entityType").value(segment.type.toString());
            out.name("flag");
            if(segment.flag.getType() == Boolean.class && segment.denialValue == Boolean.FALSE) {
                out.value(segment.flag.toString());
            } else {
                out.beginObject();
                out.name("name").value(segment.flag.toString());
                out.name("denialValue");
                if(segment.denialValue instanceof Boolean) {
                    out.value((Boolean)segment.denialValue);
                } else {
                    out.value(segment.denialValue.toString());
                }
                out.endObject();
            }
            if(segment.conditionString != null)
                out.name("condition").value(StringUtils.join(segment.conditionString, " "));
            for (Map.Entry<String, List<Caller>> entry : segment.getters.getCallersMap().entrySet()) {
                out.name(entry.getKey()).beginArray();
                for(Caller caller : entry.getValue()) {
                    out.beginObject();
                    out.name("element").value(caller.element);
                    out.name("type").value(caller.type.toString());
                    out.endObject();
                }
                out.endArray();
            }
            for(Map.Entry<String, Object> entry : segment.getters.getConstantsMap().entrySet()) {
                out.name(entry.getKey()).value((Integer)entry.getValue());
            }
            out.endObject();
        }
        for(SegmentItem segment : value.segmentsItems) {
            out.beginObject();
            out.name("class").value(segment.theClass.getName());
            out.name("type").value("item");
            out.name("itemType").value(segment.type.toString());
            out.name("onAdjacent").value(segment.onAdjacent);
            if(segment.conditionString != null)
                out.name("condition").value(StringUtils.join(segment.conditionString, " "));
            out.name("flag");
            if(segment.flag.getType() == Boolean.class && segment.denialValue == Boolean.FALSE) {
                out.value(segment.flag.toString());
            } else {
                out.beginObject();
                out.name("name").value(segment.flag.toString());
                out.name("denialValue");
                if(segment.denialValue instanceof Boolean) {
                    out.value((Boolean)segment.denialValue);
                } else {
                    out.value(segment.denialValue.toString());
                }
                out.endObject();
            }
            for (Map.Entry<String, List<Caller>> entry : segment.getters.getCallersMap().entrySet()) {
                out.name(entry.getKey()).beginArray();
                for(Caller caller : entry.getValue()) {
                    out.beginObject();
                    out.name("element").value(caller.element);
                    out.name("type").value(caller.type.toString());
                    out.endObject();
                }
                out.endArray();
            }
            for(Map.Entry<String, Object> entry : segment.getters.getConstantsMap().entrySet()) {
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
            if (nextName.equals("modid")) {
                modid = in.nextString();
                if (modid == null)
                    throw new IOException("Missing modid for a protection!");
                if(!Loader.isModLoaded(modid) && !modid.equals("Minecraft")) {
                    MyTown.instance.LOG.info("   Skipped protection because the mod " + modid + " wasn't loaded.");
                    return null;
                } else if(version != null) {
                    if(!verifyVersion(modid, version)) {
                        MyTown.instance.LOG.info("   Skipped protection because it doesn't support the version loaded of mod: " + modid + " (" + version + ")");
                        return null;
                    }
                }
            } else if(nextName.equals("version")) {
                version = in.nextString();
                if(modid != null && !modid.equals("Vanilla") && !verifyVersion(modid, version)) {
                    MyTown.instance.LOG.info("   Skipped protection because it doesn't support the version loaded of mod: " + modid + " (" + version + ")");
                    return null;
                }
            } else if (nextName.equals("segments")) {
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

                    boolean isAdjacent = false;
                    boolean hasOwner = false;
                    int meta = -1;
                    Getters getters = new Getters();

                    in.beginObject();
                    try {
                        while (!in.peek().equals(JsonToken.END_OBJECT)) {
                            nextName = in.nextName();
                            if (nextName.equals("class")) {
                                try {
                                    clazz = Class.forName(in.nextString());
                                } catch (ClassNotFoundException ex) {
                                    throw new SegmentException("[Segment: " + clazz + "] Class " + clazz + " is invalid!");
                                }
                                getters.setName(clazz.getName());
                                continue;
                            }
                            if (nextName.equals("type")) {
                                type = in.nextString();
                                if (type == null)
                                    throw new SegmentException("[Segment: " + clazz + "] Segment is missing type!");
                                continue;
                            }
                            if (nextName.equals("condition")) {
                                condition = in.nextString();
                                continue;
                            }
                            if (nextName.equals("flag")) {
                                if(in.peek() == JsonToken.BEGIN_OBJECT) {
                                    in.beginObject();
                                    if(in.nextName().equals("name"))
                                        flag = FlagType.valueOf(in.nextString());
                                    if(in.nextName().equals("denialValue"))
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
                                if(flag.getType() != denialValue.getClass())
                                    throw new SegmentException("[Segment: " + clazz + "] The segment does not have a valid flag denial value!");
                                continue;
                            }
                            // Checking if clazz and type is not null before anything else.
                            if (clazz == null)
                                throw new SegmentException("[Protection: " + modid + "] Class is not being specified in one of the segments.");
                            if (type == null)
                                throw new SegmentException("[Segment: " + clazz + "] Type is specified after the type-specific data.");

                            if (type.equals("entity")) {
                                if (nextName.equals("entityType")) {
                                    entityType = EntityType.valueOf(in.nextString());
                                    if (entityType == null)
                                        throw new SegmentException("[Segment: " + clazz + "] Invalid entity type.");
                                    continue;
                                }
                            }
                            if (type.equals("item")) {
                                if (nextName.equals("itemType")) {
                                    itemType = ItemType.valueOf(in.nextString());
                                    if (itemType == null)
                                        throw new SegmentException("[Segment: " + clazz + "] Invalid item type.");
                                    continue;
                                }
                                if (nextName.equals("isAdjacent")) {
                                    isAdjacent = in.nextBoolean();
                                    continue;
                                }

                            }
                            if (type.equals("block")) {
                                if (nextName.equals("meta")) {
                                    meta = in.nextInt();
                                    continue;
                                }
                                if(nextName.equals("blockType")) {
                                    blockType = BlockType.valueOf(in.nextString());
                                    if(blockType == null)
                                        throw new SegmentException("[Segment: " + clazz + "] Invalid block type.");
                                    continue;
                                }
                            }
                            if(type.equals("tileEntity")) {
                                if(nextName.equals("hasOwner")) {
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
                                if (type.equals("tileEntity")) {
                                    // Log if the segment is using default protection
                                    if (getters.getCallersMap().get("xMin") == null || getters.getCallersMap().get("xMax") == null || getters.getCallersMap().get("zMin") == null || getters.getCallersMap().get("zMax") == null) {
                                        MyTown.instance.LOG.info("   [Segment: " + clazz + "] Could not find one of the getters (xMin, xMax, zMin, zMax). Using default protection size.");

                                        // Removing all of them since it will only create problems if left there
                                        getters.removeGetter("xMin");
                                        getters.removeGetter("xMax");
                                        getters.removeGetter("zMin");
                                        getters.removeGetter("zMax");
                                    }
                                    segment = new SegmentTileEntity(clazz, getters, flag, denialValue, condition, hasOwner);
                                } else if (type.equals("entity")) {
                                    if (entityType == null) {
                                        MyTown.instance.LOG.info("   entityType is not specified, defaulting to 'TRACKED'.");
                                        entityType = EntityType.TRACKED;
                                    }
                                    segment = new SegmentEntity(clazz, getters, flag, denialValue, condition, entityType);
                                } else if (type.equals("item")) {
                                    if(itemType == null) {
                                        MyTown.instance.LOG.info("   itemType is not specified, defaulting to 'RIGHT_CLICK_BLOCK'.");
                                        itemType = ItemType.RIGHT_CLICK_BLOCK;
                                    }
                                    segment = new SegmentItem(clazz, getters, flag, denialValue, condition, itemType, isAdjacent);
                                } else if (type.equals("block")) {
                                    if(blockType == null) {
                                        MyTown.instance.LOG.info("   blockType is not specified, defaulting to 'RIGHT_CLICK'.");
                                        blockType = BlockType.RIGHT_CLICK;
                                    }
                                    segment = new SegmentBlock(clazz, getters, flag, denialValue, condition, blockType, meta);
                                }
                            }
                        } catch (SegmentException ex) {
                            // This catch is for missing elements or other runtime verifiable  conditions.
                            MyTown.instance.LOG.error("  " + ExceptionUtils.getFullStackTrace(ex));
                            MyTown.instance.LOG.error("  Segment will NOT be added, reload configs to try again.");
                        }
                    } catch (SegmentException ex) {
                        // This catch is for parsing issues when reading the segment.

                        MyTown.instance.LOG.error("  " + ExceptionUtils.getFullStackTrace(ex));
                        MyTown.instance.LOG.error("  Segment will NOT be added, reload configs to try again.");
                        // Skipping everything in the segment if it errors
                        while(!in.peek().equals(JsonToken.END_OBJECT))
                            in.skipValue();
                        in.endObject();
                    }
                    if (segment == null)
                        MyTown.instance.LOG.error("  [Segment: " + clazz + "] Segment was not properly initialized!");
                    else {
                        // Precheck for configurations
                        //if(segment instanceof SegmentEntity && ((SegmentEntity) segment).type == EntityType.explosive && Config.useExtraEvents)
                        //    MyTown.instance.log.info("  [Segment:" + segment.theClass.getName() + "] Omitting segment because use of extra events is enabled.");
                        //else {
                            MyTown.instance.LOG.info("   Added segment for class: " + segment.theClass.getName());
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
            if(nextName.equals("element"))
                element = in.nextString();
            nextName = in.nextName();
            if(nextName.equals("type"))
                callerType = Caller.CallerType.valueOf(in.nextString());
            if(in.peek() != JsonToken.END_OBJECT) {
                nextName = in.nextName();
                if(nextName.equals("valueType"))
                    try {
                        valueType = Class.forName(in.nextString());
                    } catch (ClassNotFoundException ex) {
                        throw new SegmentException("[Segment: " + clazz + "] Getter with name " + getterName + " the valueType specified does not exist.");
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
