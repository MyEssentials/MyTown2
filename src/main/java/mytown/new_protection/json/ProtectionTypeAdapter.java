package mytown.new_protection.json;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import mytown.new_protection.Protection;
import mytown.new_protection.segment.*;
import mytown.new_protection.segment.enums.EntityType;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 1/1/2015.
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
        for(Segment segment : value.segmentsTiles) {
            out.beginObject();
            out.name("class").value(segment.theClass.getName());

                out.name("type").value("tileEntity");

                for(int i = 0; i < 4; i++) {
                    switch (i) {
                        case 0:
                            out.name("x1").beginArray();
                            break;
                        case 1:
                            out.name("z1").beginArray();
                            break;
                        case 2:
                            out.name("x2").beginArray();
                            break;
                        case 3:
                            out.name("z2").beginArray();
                            break;
                    }

                    for (Getter getter : ((SegmentTileEntity) segment).coordGetters[i]) {
                        out.beginObject();
                        out.name("element").value(getter.element);
                        out.name("type").value(getter.type.toString());
                        out.endObject();
                    }

                    out.endArray();
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
        List<Segment> segments = new ArrayList<Segment>();

        String nextName;
        in.beginObject();
        while(!in.peek().equals(JsonToken.END_OBJECT)) {

            nextName = in.nextName();
            if (nextName.equals("modid")) {
                modid = in.nextString();
                if (modid == null)
                    throw new IOException("Missing modid for a protection!");
            } else if (nextName.equals("segments")) {
                in.beginArray();
                while (in.hasNext()) {

                    Segment segment = null;
                    String clazz = null, type = null;
                    EntityType entityType = null;
                    List<Getter>[] getters = new ArrayList[4];
                    Map<String, List<Getter>> extraGettersMap = new HashMap<String, List<Getter>>();

                    in.beginObject();
                    while (!in.peek().equals(JsonToken.END_OBJECT)) {
                        nextName = in.nextName();
                        if (nextName.equals("class")) {
                            clazz = in.nextString();
                        } else if (nextName.equals("type")) {
                            type = in.nextString();
                            if (type == null)
                                throw new IOException("The segment for class " + clazz + " does not have a type!");
                            if (type.equals("tileEntity"))
                                getters = new ArrayList[4];
                        } else if (clazz == null) {
                            // Checking if clazz and type is not null before anything else.
                            throw new IOException("Class is not being specified in the protection with modid " + modid + ".");
                        } else if (type == null) {
                            throw new IOException("Type is specified after the type-specific data for segment with class " + clazz + ".");
                        } else if (type.equals("tileEntity") && nextName.equals("x1")) {
                            getters[0] = parseGetters(in, clazz, nextName);
                        } else if (type.equals("tileEntity") && nextName.equals("z1")) {
                            getters[1] = parseGetters(in, clazz, nextName);
                        } else if (type.equals("tileEntity") && nextName.equals("x2")) {
                            getters[2] = parseGetters(in, clazz, nextName);
                        } else if (type.equals("tileEntity") && nextName.equals("z2")) {
                            getters[3] = parseGetters(in, clazz, nextName);
                        } else if (type.equals("entity")) {
                            if (nextName.equals("entityType")) {
                                entityType = EntityType.valueOf(in.nextString());
                                if (entityType == null)
                                    throw new IOException("Invalid entity type for segment with class " + clazz + ". Please choose hostile, passive or tracked.");
                            }
                        } else if (type.equals("item")) {

                        } else {
                            // If it gets that means that it should be some extra data that will be used in checking something
                            extraGettersMap.put(nextName, parseGetters(in, clazz, nextName));
                        }

                    }
                    if (type != null) {
                        if (type.equals("tileEntity") && checks(getters)) {
                            try {
                                segment = new SegmentTileEntity(Class.forName(clazz), extraGettersMap, getters, IBlockModifier.Shape.rectangular);
                            } catch (ClassNotFoundException ex) {
                                throw new IOException("Class " + clazz + " is invalid!");
                            }
                        } else if(type.equals("entity")){
                            if(entityType == null)
                                throw new IOException("EntityType is null for segment with class " + clazz);
                            try {
                                segment = new SegmentEntity(Class.forName(clazz), extraGettersMap, entityType);
                            } catch (ClassNotFoundException ex) {
                                throw new IOException("Class " + clazz + " is invalid!");
                            }
                        } else if(type.equals("item")) {
                            try {
                                segment = new SegmentItem(Class.forName(clazz), extraGettersMap);
                            } catch (ClassNotFoundException ex) {
                                throw new IOException("Class " + clazz + " is invalid!");
                            }
                        }
                    }


                    in.endObject();
                    if (segment == null)
                        throw new IOException("Segment with class " + clazz + " was not properly initialized!");
                    segments.add(segment);
                }
                in.endArray();
            }
        }
        in.endObject();
        return new Protection(modid, segments);
    }

    private List<Getter> parseGetters(JsonReader in, String clazz, String getterName) throws IOException {
        in.beginArray();
        List<Getter> getters = new ArrayList<Getter>();
        String nextName;
        while(in.hasNext()) {
            in.beginObject();
            String element = null;
            Getter.GetterType getterType = null;

            nextName = in.nextName();
            if(nextName.equals("element"))
                element = in.nextString();
            nextName = in.nextName();
            if(nextName.equals("type"))
                getterType = Getter.GetterType.valueOf(in.nextString());

            if(getterType == null)
                throw new IOException(getterName + " getter for segment with class " + clazz + " has an invalid getter type.");
            if(element == null)
                throw new IOException(getterName + " getter for segment with class " + clazz + " does not have a value.");
            in.endObject();
            getters.add(new Getter(element, getterType));
        }
        in.endArray();
        return getters;
    }

    private boolean checks(List<Getter>[] getters) {
        if(getters[0] == null || getters[1] == null || getters[2] == null || getters[3] == null) {
            return false;
        }
        return true;
    }
}
