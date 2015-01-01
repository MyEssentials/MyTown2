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
            out.name("class").value(segment.theClass.toString());
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

        if(in.peek().equals(JsonToken.BEGIN_OBJECT)) {
            in.beginObject();
            String nextName = in.nextName();
            if(nextName.equals("modid")) {
                modid = in.nextString();
            }
            if(modid == null)
                throw new IOException("Modid does not exist!");

            nextName = in.nextName();
            if(nextName.equals("segments")) {
                in.beginArray();
                while (in.hasNext()) {
                    in.beginObject();
                    String clazz = null, type = null;

                    nextName = in.nextName();
                    if(nextName.equals("class"))
                        clazz = in.nextString();

                    nextName = in.nextName();
                    if(nextName.equals("type"))
                        type = in.nextString();

                    if(type == null)
                        throw new IOException("The segment for class " + clazz + " does not have a type!");



                    if(type.equals("tileEntity")) {
                        SegmentTileEntity segment;
                        List<Getter>[] getters = new ArrayList[4];
                        nextName = in.nextName();
                        if(nextName.equals("x1")) {
                            getters[0] = parseGetters(in, clazz);
                        }

                        nextName = in.nextName();
                        if(nextName.equals("z1")) {
                            getters[1] = parseGetters(in, clazz);
                        }

                        nextName = in.nextName();
                        if(nextName.equals("x2")) {
                            getters[2] = parseGetters(in, clazz);
                        }

                        nextName = in.nextName();
                        if(nextName.equals("z2")) {
                            getters[3] = parseGetters(in, clazz);
                        }
                        try {
                            segment = new SegmentTileEntity(Class.forName(clazz), getters, IBlockModifier.Shape.rectangular);
                        } catch (ClassNotFoundException ex) {
                            throw new IOException("Class " + clazz + " is invalid!");
                        }
                    } else if(type.equals("entity")) {

                    } else if(type.equals("item")) {

                    }


                    in.endObject();
                }
                in.endArray();

            }
            in.endObject();
        }
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



}
