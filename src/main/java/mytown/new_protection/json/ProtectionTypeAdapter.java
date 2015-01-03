package mytown.new_protection.json;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import mytown.new_protection.Protection;
import mytown.new_protection.segment.*;

import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

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
        for(Segment segment : value.segments) {
            out.beginObject();
            out.name("class").value(segment.theClass.getName());
            if(segment instanceof SegmentEntity) {
                out.name("type").value("entity");
            } else if(segment instanceof SegmentTileEntity) {
                out.name("type").value("tileEntity");

                for(int i = 0; i < 4; i++) {
                    switch (i) {
                        case 0:   out.name("x1").beginArray(); break;
                        case 1:   out.name("z1").beginArray(); break;
                        case 2:   out.name("x2").beginArray(); break;
                        case 3:   out.name("z2").beginArray(); break;
                    }

                    for(Getter getter : ((SegmentTileEntity) segment).getters[i]) {
                        out.beginObject();
                        out.name("element").value(getter.element);
                        out.name("type").value(getter.type.toString());
                        out.endObject();
                    }

                    out.endArray();
                }

            } else if(segment instanceof SegmentItem) {
                out.name("type").value("item");
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
        List<Getter>[] getters = new ArrayList[4];
        EntityType entityType;
        String nextName;
        in.beginObject();
        while(!in.peek().equals(JsonToken.END_OBJECT)) {
            nextName = in.nextName();
            if (nextName.equals("modid")) {
                modid = in.nextString();
                if (modid == null)
                    throw new IOException("You haven't introduced a modid!");
            } else if (nextName.equals("segments")) {
                in.beginArray();
                while (in.hasNext()) {
                    in.beginObject();
                    Segment segment = null;
                    String clazz = null, type = null;
                    while(!in.peek().equals(JsonToken.END_OBJECT)) {
                        nextName = in.nextName();
                        if (nextName.equals("class")) {
                            clazz = in.nextString();
                        } else if (nextName.equals("type")) {
                            type = in.nextString();
                            if (type == null)
                                throw new IOException("The segment for class " + clazz + " does not have a type!");
                            if(type.equals("tileEntity"))
                                getters = new ArrayList[4];
                        // If type is null and it gets here then an error is tossed
                        } else if (type != null) {
                            if(type.equals("tileEntity")) {

                                if (nextName.equals("x1")) {
                                    getters[0] = parseGetters(in, clazz);
                                } else if (nextName.equals("z1")) {
                                    getters[1] = parseGetters(in, clazz);
                                } else if (nextName.equals("x2")) {
                                    getters[2] = parseGetters(in, clazz);
                                } else if (nextName.equals("z2")) {
                                    getters[3] = parseGetters(in, clazz);
                                }
                                if(checks(getters)) {
                                    try {
                                        segment = new SegmentTileEntity(Class.forName(clazz), getters, IBlockModifier.Shape.rectangular);
                                    } catch (ClassNotFoundException ex) {
                                        throw new IOException("Class " + clazz + " is invalid!");
                                    }
                                }
                            } else if (type.equals("entity")) {
                                if(nextName.equals("entityType")) {
                                    entityType = EntityType.valueOf(in.nextString());
                                    if(entityType == null)
                                        throw new IOException("Invalid entity type for class " + clazz + ". Please choose hostile, passive or tracked.");
                                }
                            } else if (type.equals("item")) {

                            }
                        } else {
                            throw new IOException("Protection for class " + clazz + " failed to load completely because the type is specified after the type-specific data. Please change the order!");
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

    private List<Getter> parseGetters(JsonReader in, String clazz) throws IOException {
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

            if(element == null || getterType == null)
                throw new IOException("The segment for class " + clazz + " has a invalid getter type or missing one value.");
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
