package mytown.protection;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import mytown.api.container.SegmentsContainer;
import mytown.protection.segment.*;
import mytown.util.exceptions.ProtectionParseException;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * An object which offers protection for a specific mod and version
 */
public class Protection {

    public final String modid;
    public final String version;

    public final SegmentsContainer<SegmentTileEntity> segmentsTiles = new SegmentsContainer<SegmentTileEntity>();
    public final SegmentsContainer<SegmentEntity> segmentsEntities = new SegmentsContainer<SegmentEntity>();
    public final SegmentsContainer<SegmentItem> segmentsItems = new SegmentsContainer<SegmentItem>();
    public final SegmentsContainer<SegmentBlock> segmentsBlocks = new SegmentsContainer<SegmentBlock>();

    public Protection(String modid) {
        this(modid, "");
    }

    public Protection(String modid, String version) {
        this.modid = modid;
        this.version = version;
    }

    private void addSegments(List<Segment> segments) {
        for(Segment segment : segments) {
            if(segment instanceof SegmentTileEntity) {
                segmentsTiles.add((SegmentTileEntity) segment);
            } else if(segment instanceof SegmentEntity) {
                segmentsEntities.add((SegmentEntity) segment);
            } else if(segment instanceof SegmentItem) {
                segmentsItems.add((SegmentItem) segment);
            } else if(segment instanceof SegmentBlock) {
                segmentsBlocks.add((SegmentBlock) segment);
            }
        }
    }

    public static class Serializer implements JsonSerializer<Protection>, JsonDeserializer<Protection> {
        @Override
        public JsonElement serialize(Protection protection, Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            json.addProperty("modid", protection.modid);
            if(!protection.version.equals("")) {
                json.addProperty("version", protection.version);
            }
            List<Segment> segments = new ArrayList<Segment>();
            segments.addAll(protection.segmentsBlocks);
            segments.addAll(protection.segmentsEntities);
            segments.addAll(protection.segmentsItems);
            segments.addAll(protection.segmentsTiles);

            json.add("segments", context.serialize(segments, new TypeToken<List<Segment>>() {}.getType()));

            return json;
        }

        @Override
        public Protection deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();
            if(!jsonObject.has("modid")) {
                throw new ProtectionParseException("Missing modid identifier");
            }

            String modid = jsonObject.get("modid").getAsString();
            String version = "";
            if(jsonObject.has("version")) {
                version = jsonObject.get("version").getAsString();
            }
            Protection protection = new Protection(modid, version);

            if(jsonObject.has("segments")) {
                protection.addSegments((List<Segment>) context.deserialize(jsonObject.get("segments"), new TypeToken<List<Segment>>() {}.getType()));
            }

            return protection;
        }
    }
}