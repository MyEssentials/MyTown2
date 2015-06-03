package mytown.protection.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.MyTown;
import mytown.entities.flag.FlagType;
import mytown.protection.Protection;
import mytown.protection.Protections;
import mytown.protection.segment.*;
import mytown.protection.segment.enums.BlockType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.enums.ItemType;
import mytown.protection.segment.getter.Getters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.exception.ExceptionUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON Parser used to parse protection files.
 */
public class ProtectionParser {

    private ProtectionParser() {

    }

    private static String folderPath;
    private static Gson gson;

    public static void setFolderPath(String folderPath) {
        ProtectionParser.folderPath = folderPath;
    }

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
        Protection vanillaProtection = null;
        for (File file : FileUtils.listFiles(folder, extensions, true)) {
            try {
                FileReader reader = new FileReader(file);
                MyTown.instance.LOG.info("Loading protection file: " + file.getName());
                Protection protection = gson.fromJson(reader, Protection.class);
                if(protection != null) {
                    if (protection.modid.equals("Minecraft")) {
                        vanillaProtection = protection;
                    } else {
                        MyTown.instance.LOG.info("Adding protection for mod: " + protection.modid);
                        Protections.getInstance().addProtection(protection);
                    }
                }
                reader.close();

            } catch (Exception ex) {
                MyTown.instance.LOG.error("Encountered error when parsing protection file: " + file.getName());
                MyTown.instance.LOG.error(ExceptionUtils.getFullStackTrace(ex));
            }
        }
        if(vanillaProtection != null) {
            MyTown.instance.LOG.info("Adding vanilla protection.");
            Protections.getInstance().addProtection(vanillaProtection);
        }

        return true;
    }

    private static void initJSON() {
        gson = new GsonBuilder().registerTypeAdapter(Protection.class, new ProtectionTypeAdapter()).setPrettyPrinting().create();
    }

    @SuppressWarnings("unchecked")
    private static void createModel() {
        List<Segment> segments = new ArrayList<Segment>();

        segments.add(new SegmentBlock(net.minecraft.block.BlockButton.class, new Getters(), FlagType.ACTIVATE_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));
        segments.add(new SegmentBlock(net.minecraft.block.BlockDoor.class, new Getters(), FlagType.ACTIVATE_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));
        segments.add(new SegmentBlock(net.minecraft.block.BlockLever.class, new Getters(), FlagType.ACTIVATE_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));
        segments.add(new SegmentBlock(net.minecraft.block.BlockFenceGate.class, new Getters(), FlagType.ACTIVATE_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));
        segments.add(new SegmentBlock(net.minecraft.block.BlockDragonEgg.class, new Getters(), FlagType.ACTIVATE_BLOCKS, false, null, BlockType.ANY_CLICK, -1));
        segments.add(new SegmentBlock(net.minecraft.block.BlockCake.class, new Getters(), FlagType.ACTIVATE_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));
        segments.add(new SegmentBlock(net.minecraft.block.BlockTrapDoor.class, new Getters(), FlagType.ACTIVATE_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));
        segments.add(new SegmentBlock(net.minecraft.block.BlockJukebox.class, new Getters(), FlagType.ACTIVATE_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));
        segments.add(new SegmentBlock(net.minecraft.block.BlockRedstoneRepeater.class, new Getters(), FlagType.ACTIVATE_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));
        segments.add(new SegmentBlock(net.minecraft.block.BlockRedstoneComparator.class, new Getters(), FlagType.ACTIVATE_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));

        segments.add(new SegmentBlock(net.minecraft.block.BlockAnvil.class, new Getters(), FlagType.ACCESS_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));
        segments.add(new SegmentBlock(net.minecraft.block.BlockCauldron.class, new Getters(), FlagType.ACCESS_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));
        segments.add(new SegmentBlock(net.minecraft.block.BlockContainer.class, new Getters(), FlagType.ACCESS_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));
        segments.add(new SegmentBlock(net.minecraft.block.BlockBed.class, new Getters(), FlagType.ACCESS_BLOCKS, false, null, BlockType.RIGHT_CLICK, -1));

        segments.add(new SegmentEntity(net.minecraft.entity.monster.EntityMob.class, new Getters(), FlagType.MOBS, "hostiles", null, EntityType.TRACKED));
        segments.add(new SegmentEntity(net.minecraft.entity.EntityAgeable.class, new Getters(), FlagType.PROTECTED_ENTITIES, true, null, EntityType.PROTECT));
        Getters getters = new Getters();
        getters.addConstant("range", 5);
        segments.add(new SegmentEntity(net.minecraft.entity.monster.EntityCreeper.class, getters, FlagType.EXPLOSIONS, false, null, EntityType.TRACKED));
        getters = new Getters();
        getters.addConstant("range", 5);
        segments.add(new SegmentEntity(net.minecraft.entity.item.EntityTNTPrimed.class, getters, FlagType.EXPLOSIONS, false, null, EntityType.TRACKED));
        segments.add(new SegmentEntity(net.minecraft.entity.item.EntityItemFrame.class, new Getters(), FlagType.PROTECTED_ENTITIES, true, null, EntityType.PROTECT));

        segments.add(new SegmentItem(net.minecraft.item.ItemMonsterPlacer.class, new Getters(), FlagType.USE_ITEMS, false, null, ItemType.RIGHT_CLICK_BLOCK, true));
        segments.add(new SegmentItem(net.minecraft.item.ItemMonsterPlacer.class, new Getters(), FlagType.USE_ITEMS, false, null, ItemType.RIGHT_CLICK_ENTITY, false));
        segments.add(new SegmentItem(net.minecraft.item.ItemShears.class, new Getters(), FlagType.USE_ITEMS, false, null, ItemType.RIGHT_CLICK_ENTITY, false));
        segments.add(new SegmentItem(net.minecraft.item.ItemHangingEntity.class, new Getters(), FlagType.USE_ITEMS, false, null, ItemType.RIGHT_CLICK_BLOCK, true));

        Protection protection = new Protection("Minecraft", segments);
        try {
            FileWriter writer = new FileWriter(folderPath + "/Minecraft.json");
            gson.toJson(protection, Protection.class, writer);
            writer.close();

        } catch (Exception ex) {
            MyTown.instance.LOG.error("Failed to create protection model :( ");
            MyTown.instance.LOG.error(ExceptionUtils.getFullStackTrace(ex));
        }
    }
}
