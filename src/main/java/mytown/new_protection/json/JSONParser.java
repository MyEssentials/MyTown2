package mytown.new_protection.json;

import buildcraft.factory.TileQuarry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import mytown.MyTown;
import mytown.entities.flag.FlagType;
import mytown.new_protection.Protection;
import mytown.new_protection.Protections;
import mytown.new_protection.segment.Getter;
import mytown.new_protection.segment.IBlockModifier;
import mytown.new_protection.segment.Segment;
import mytown.new_protection.segment.SegmentTileEntity;
import mytown.proxies.mod.ModProxies;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
        Protections.getInstance().init();
        for (File file : FileUtils.listFiles(folder, extensions, true)) {
            try {
                reader = new FileReader(file);
                MyTown.instance.log.info("Loading protection file: " + file.getName());
                Protection protection = gson.fromJson(reader, Protection.class);
                if(protection != null) {
                    if (protection.modid.equals("Vanilla")) {
                        MyTown.instance.log.info("   Adding vanilla protection.");
                    } else {
                        MyTown.instance.log.info("   Adding protection for mod: " + protection.modid);
                    }
                    Protections.getInstance().addProtection(protection);
                }
                reader.close();

            } catch (Exception ex) {
                MyTown.instance.log.error("Encountered error when parsing protection file: " + file.getName());
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
        Map<String, List<Getter>> gettersMap = new HashMap<String, List<Getter>>();
        List<Getter> getters = new ArrayList<Getter>();
        getters.add(new Getter("getBox", Getter.GetterType.method));
        getters.add(new Getter("xMin", Getter.GetterType.field));
        gettersMap.put("X1", getters);

        getters = new ArrayList<Getter>();
        getters.add(new Getter("getBox", Getter.GetterType.method));
        getters.add(new Getter("zMin", Getter.GetterType.field));
        gettersMap.put("Z1", getters);

        getters = new ArrayList<Getter>();
        getters.add(new Getter("getBox", Getter.GetterType.method));
        getters.add(new Getter("xMax", Getter.GetterType.field));
        gettersMap.put("X2", getters);

        getters = new ArrayList<Getter>();
        getters.add(new Getter("getBox", Getter.GetterType.method));
        getters.add(new Getter("zMax", Getter.GetterType.field));
        gettersMap.put("Z2", getters);

        getters = new ArrayList<Getter>();
        getters.add(new Getter("blockMetadata", Getter.GetterType.field));
        gettersMap.put("meta", getters);


        List<Segment> segments = new ArrayList<Segment>();
        segments.add(new SegmentTileEntity(TileQuarry.class, gettersMap, FlagType.modifyBlocks, "meta != -1", IBlockModifier.Shape.rectangular));

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
