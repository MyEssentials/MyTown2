package mytown.new_protection.json;

import buildcraft.factory.TileQuarry;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.MyTown;
import mytown.entities.flag.FlagType;
import mytown.new_protection.Protection;
import mytown.new_protection.Protections;
import mytown.new_protection.segment.getter.Caller;
import mytown.new_protection.segment.IBlockModifier;
import mytown.new_protection.segment.Segment;
import mytown.new_protection.segment.SegmentTileEntity;
import mytown.new_protection.segment.getter.Getters;
import net.minecraft.tileentity.TileEntityDispenser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
                        MyTown.instance.log.info("Adding vanilla protection.");
                    } else {
                        MyTown.instance.log.info("Adding protection for mod: " + protection.modid);
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
        Class clazz;
        try {
            clazz = Class.forName("buildcraft.factory.TileQuarry");
        } catch (Exception ex) {
            // TODO: Change to something else, or it might confusing to some people
            clazz = TileEntityDispenser.class;
        }

        Getters getters = new Getters();
        getters.setName(clazz.getName());

        List<Caller> callers = new ArrayList<Caller>();
        callers.add(new Caller("getBox", Caller.CallerType.method));
        callers.add(new Caller("xMin", Caller.CallerType.field));
        getters.addCallers("X1", callers);

        callers = new ArrayList<Caller>();
        callers.add(new Caller("getBox", Caller.CallerType.method));
        callers.add(new Caller("zMin", Caller.CallerType.field));
        getters.addCallers("Z1", callers);

        callers = new ArrayList<Caller>();
        callers.add(new Caller("getBox", Caller.CallerType.method));
        callers.add(new Caller("xMax", Caller.CallerType.field));
        getters.addCallers("X2", callers);

        callers = new ArrayList<Caller>();
        callers.add(new Caller("getBox", Caller.CallerType.method));
        callers.add(new Caller("zMax", Caller.CallerType.field));
        getters.addCallers("Z2", callers);

        callers = new ArrayList<Caller>();
        callers.add(new Caller("blockMetadata", Caller.CallerType.field));
        getters.addCallers("meta", callers);


        List<Segment> segments = new ArrayList<Segment>();
        segments.add(new SegmentTileEntity(clazz, getters, FlagType.modifyBlocks, "meta != -1", IBlockModifier.Shape.rectangular));

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
