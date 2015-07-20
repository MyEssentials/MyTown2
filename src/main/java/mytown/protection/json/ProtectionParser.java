package mytown.protection.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.MyTown;
import myessentials.entities.Volume;
import mytown.entities.flag.FlagType;
import mytown.protection.Protection;
import mytown.protection.Protections;
import mytown.protection.segment.*;
import mytown.protection.segment.enums.BlockType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.enums.ItemType;
import mytown.protection.segment.getter.Caller;
import mytown.protection.segment.getter.Getters;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

/**
 * JSON Parser used to parse protection files.
 */
public class ProtectionParser {

    private static String folderPath;
    private static final Gson gson = new GsonBuilder().registerTypeAdapter(Protection.class, new ProtectionTypeAdapter()).setPrettyPrinting().create();

    private ProtectionParser() {
    }

    public static void setFolderPath(String folderPath) {
        ProtectionParser.folderPath = folderPath;
    }

    public static boolean start() {
        File folder = new File(folderPath);
        if(!folder.exists()) {
            if(!folder.mkdir())
                return false;
            createModel();
        }

        String[] extensions = new String[1];
        extensions[0] = "json";
        Protections.instance.reset();
        Protection vanillaProtection = null;
        for (File file : FileUtils.listFiles(folder, extensions, true)) {
            try {
                FileReader reader = new FileReader(file);
                MyTown.instance.LOG.info("Loading protection file: {}", file.getName());
                Protection protection = gson.fromJson(reader, Protection.class);
                if(protection != null) {
                    if ("Minecraft".equals(protection.modid)) {
                        vanillaProtection = protection;
                    } else {
                        MyTown.instance.LOG.info("Adding protection for mod: {}", protection.modid);
                        Protections.instance.addProtection(protection);
                    }
                }
                reader.close();

            } catch (Exception ex) {
                MyTown.instance.LOG.error("Encountered error when parsing protection file: {}", file.getName());
                MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
            }
        }
        if(vanillaProtection != null) {
            MyTown.instance.LOG.info("Adding vanilla protection.");
            Protections.instance.addProtection(vanillaProtection);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private static void createModel() {
        List<Segment> segments = new ArrayList<Segment>();

        segments.add(new SegmentBlock(net.minecraft.block.BlockButton.class, new Getters(), FlagType.ACTIVATE, false, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockDoor.class, new Getters(), FlagType.ACTIVATE, false, null, BlockType.RIGHT_CLICK, -1, new Volume(0, -1, 0, 0, 1, 0)));
        segments.add(new SegmentBlock(net.minecraft.block.BlockLever.class, new Getters(), FlagType.ACTIVATE, false, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockFenceGate.class, new Getters(), FlagType.ACTIVATE, false, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockDragonEgg.class, new Getters(), FlagType.ACTIVATE, false, null, BlockType.ANY_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockCake.class, new Getters(), FlagType.ACTIVATE, false, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockTrapDoor.class, new Getters(), FlagType.ACTIVATE, false, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockJukebox.class, new Getters(), FlagType.ACTIVATE, false, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockRedstoneRepeater.class, new Getters(), FlagType.ACTIVATE, false, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockRedstoneComparator.class, new Getters(), FlagType.ACTIVATE, false, null, BlockType.RIGHT_CLICK, -1, null));

        segments.add(new SegmentBlock(net.minecraft.block.BlockAnvil.class, new Getters(), FlagType.ACCESS, false, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockCauldron.class, new Getters(), FlagType.ACCESS, false, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockContainer.class, new Getters(), FlagType.ACCESS, false, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockBed.class, new Getters(), FlagType.ACCESS, false, null, BlockType.RIGHT_CLICK, -1, null));

        segments.add(new SegmentEntity(net.minecraft.entity.monster.EntityMob.class, new Getters(), FlagType.MOBS, "passives", null, EntityType.TRACKED));
        segments.add(new SegmentEntity(net.minecraft.entity.EntityAgeable.class, new Getters(), FlagType.PVE, false, null, EntityType.PROTECT));
        Getters getters = new Getters();
        getters.addConstant("range", 5);
        segments.add(new SegmentEntity(net.minecraft.entity.monster.EntityCreeper.class, getters, FlagType.EXPLOSIONS, false, null, EntityType.TRACKED));
        getters = new Getters();
        getters.addConstant("range", 5);
        List<Caller> callers = new ArrayList<Caller>();
        callers.add(new Caller("field_94084_b", Caller.CallerType.FIELD, null));
        callers.add(new Caller("func_70005_c_", Caller.CallerType.METHOD, null));
        getters.addCallers("owner", callers);
        segments.add(new SegmentEntity(net.minecraft.entity.item.EntityTNTPrimed.class, getters, FlagType.EXPLOSIONS, false, null, EntityType.TRACKED));
        segments.add(new SegmentEntity(net.minecraft.entity.item.EntityItemFrame.class, new Getters(), FlagType.PVE, false, null, EntityType.PROTECT));

        segments.add(new SegmentItem(net.minecraft.item.ItemMonsterPlacer.class, new Getters(), FlagType.USAGE, false, null, ItemType.RIGHT_CLICK_BLOCK, true, null, false));
        segments.add(new SegmentItem(net.minecraft.item.ItemMonsterPlacer.class, new Getters(), FlagType.USAGE, false, null, ItemType.RIGHT_CLICK_ENTITY, false, null, false));
        segments.add(new SegmentItem(net.minecraft.item.ItemShears.class, new Getters(), FlagType.USAGE, false, null, ItemType.RIGHT_CLICK_ENTITY, false, null, false));
        segments.add(new SegmentItem(net.minecraft.item.ItemHangingEntity.class, new Getters(), FlagType.USAGE, false, null, ItemType.RIGHT_CLICK_BLOCK, true, null, false));

        Protection protection = new Protection("Minecraft", segments);
        try {
            FileWriter writer = new FileWriter(folderPath + "/Minecraft.json");
            gson.toJson(protection, Protection.class, writer);
            writer.close();

        } catch (Exception ex) {
            MyTown.instance.LOG.error("Failed to create protection model :( ");
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
        }
    }
}
