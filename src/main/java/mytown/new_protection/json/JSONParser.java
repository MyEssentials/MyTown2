package mytown.new_protection.json;

import buildcraft.factory.TileQuarry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.MyTown;
import mytown.new_protection.Protection;
import mytown.new_protection.Protections;
import mytown.new_protection.segment.Getter;
import mytown.new_protection.segment.IBlockModifier;
import mytown.new_protection.segment.Segment;
import mytown.new_protection.segment.SegmentTileEntity;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 1/1/2015.
 * JSON Parser used to parse protection files.
 */
public class JSONParser {

    public static String folderPath;
    private static Gson gson;
    private static FileWriter writer;
    private static FileReader reader;

    public static boolean start() {

        initJSON();
        File folder = new File(folderPath);
        if(!folder.exists()) {
            if(!folder.mkdir())
                return false;
            createModel();
        }

        String[] extensions = new String[1];
        extensions[0] = "json";

        for (File file : FileUtils.listFiles(folder, extensions, true)) {
            try {
                reader = new FileReader(file);
                MyTown.instance.log.info("Loading protection file: " + file.getName());
                Protections.protections.add(gson.fromJson(reader, Protection.class));
                reader.close();

            } catch (IOException ex) {
                MyTown.instance.log.error("Encountered error when parsing a JSON protection.");
                ex.printStackTrace();
            }
        }

        return true;
    }

    private static void initJSON() {
        gson = new GsonBuilder().registerTypeAdapter(Protection.class, new ProtectionTypeAdapter()).setPrettyPrinting().create();
    }

    @SuppressWarnings("unchecked")
    private static void createModel() {
        List<Getter>[] getters = new ArrayList[6];
        getters[0] = new ArrayList<Getter>();
        getters[0].add(new Getter("getBox", Getter.GetterType.methodObject));
        getters[0].add(new Getter("xMin", Getter.GetterType.fieldInt));

        getters[1] = new ArrayList<Getter>();
        getters[1].add(new Getter("getBox", Getter.GetterType.methodObject));
        getters[1].add(new Getter("zMin", Getter.GetterType.fieldInt));

        getters[2] = new ArrayList<Getter>();
        getters[2].add(new Getter("getBox", Getter.GetterType.methodObject));
        getters[2].add(new Getter("xMax", Getter.GetterType.fieldInt));

        getters[3] = new ArrayList<Getter>();
        getters[3].add(new Getter("getBox", Getter.GetterType.methodObject));
        getters[3].add(new Getter("zMax", Getter.GetterType.fieldInt));

        List<Segment> segments = new ArrayList<Segment>();
        segments.add(new SegmentTileEntity(TileQuarry.class, getters, IBlockModifier.Shape.rectangular));

        Protection protection = new Protection("BuildCraft|Factory", segments);
        try {
            writer = new FileWriter(folderPath + "/BuildCraft-Factory.json");
            gson.toJson(protection, Protection.class, writer);
            writer.close();

        } catch (Exception ex) {
            MyTown.instance.log.error("Failed to create protection model :( ");
            ex.printStackTrace();
        }
    }
}
