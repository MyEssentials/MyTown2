package mytown.new_protection.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.MyTown;
import mytown.entities.flag.FlagType;
import mytown.new_protection.Protection;
import mytown.new_protection.Protections;
import mytown.new_protection.segment.*;
import mytown.new_protection.segment.enums.EntityType;
import mytown.new_protection.segment.enums.ItemType;
import mytown.new_protection.segment.getter.Caller;
import mytown.new_protection.segment.getter.Getters;
import net.minecraft.tileentity.TileEntityDispenser;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
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
        Protections.getInstance().reset();
        for (File file : FileUtils.listFiles(folder, extensions, true)) {
            try {
                reader = new FileReader(file);
                MyTown.instance.log.info("Loading protection file: " + file.getName());
                Protection protection = gson.fromJson(reader, Protection.class);
                if(protection != null) {
                    if (protection.modid.equals("Minecraft")) {
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
        List<Segment> segments = new ArrayList<Segment>();

        segments.add(new SegmentBlock(net.minecraft.block.BlockButton.class, new Getters(), FlagType.activateBlocks, null, 0));
        segments.add(new SegmentBlock(net.minecraft.block.BlockDoor.class, new Getters(), FlagType.activateBlocks, null, 0));
        segments.add(new SegmentBlock(net.minecraft.block.BlockLever.class, new Getters(), FlagType.activateBlocks, null, 0));
        segments.add(new SegmentBlock(net.minecraft.block.BlockFenceGate.class, new Getters(), FlagType.activateBlocks, null, 0));
        segments.add(new SegmentBlock(net.minecraft.block.BlockDragonEgg.class, new Getters(), FlagType.activateBlocks, null, 0));
        segments.add(new SegmentBlock(net.minecraft.block.BlockCake.class, new Getters(), FlagType.activateBlocks, null, 0));
        segments.add(new SegmentBlock(net.minecraft.block.BlockTrapDoor.class, new Getters(), FlagType.activateBlocks, null, 0));
        segments.add(new SegmentBlock(net.minecraft.block.BlockJukebox.class, new Getters(), FlagType.activateBlocks, null, 0));

        segments.add(new SegmentBlock(net.minecraft.block.BlockContainer.class, new Getters(), FlagType.accessBlocks, null, 0));

        segments.add(new SegmentEntity(net.minecraft.entity.monster.EntityMob.class, new Getters(), null, EntityType.hostile));
        segments.add(new SegmentEntity(net.minecraft.entity.EntityAgeable.class, new Getters(), null, EntityType.passive));
        Getters getters = new Getters();
        getters.addConstant("range", 5);
        segments.add(new SegmentEntity(net.minecraft.entity.monster.EntityCreeper.class, getters, null, EntityType.explosive));
        getters = new Getters();
        getters.addConstant("range", 5);
        segments.add(new SegmentEntity(net.minecraft.entity.item.EntityTNTPrimed.class, getters, null, EntityType.explosive));
        segments.add(new SegmentEntity(net.minecraft.entity.item.EntityItemFrame.class, new Getters(), null, EntityType.passive));

        segments.add(new SegmentItem(net.minecraft.item.ItemMonsterPlacer.class, new Getters(), FlagType.useItems, null, ItemType.rightClickBlock, true));
        segments.add(new SegmentItem(net.minecraft.item.ItemMonsterPlacer.class, new Getters(), FlagType.useItems, null, ItemType.rightClickEntity, false));
        segments.add(new SegmentItem(net.minecraft.item.ItemShears.class, new Getters(), FlagType.useItems, null, ItemType.rightClickEntity, false));
        segments.add(new SegmentItem(net.minecraft.item.ItemHangingEntity.class, new Getters(), FlagType.useItems, null, ItemType.rightClickBlock, true));

        Protection protection = new Protection("Minecraft", segments);
        try {
            writer = new FileWriter(folderPath + "/Minecraft.json");
            gson.toJson(protection, Protection.class, writer);
            writer.close();

        } catch (Exception ex) {
            MyTown.instance.log.error("Failed to create protection model :( ");
            ex.printStackTrace();
        }
    }
}
