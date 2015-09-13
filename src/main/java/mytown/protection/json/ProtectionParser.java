package mytown.protection.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.MyTown;
import myessentials.entities.Volume;
import mytown.api.container.GettersContainer;
import mytown.entities.flag.FlagType;
import mytown.protection.Protection;
import mytown.protection.ProtectionHandler;
import mytown.protection.segment.*;
import mytown.protection.segment.caller.CallerField;
import mytown.protection.segment.caller.CallerMethod;
import mytown.protection.segment.enums.BlockType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.enums.ItemType;
import mytown.protection.segment.caller.Caller;
import mytown.protection.segment.getter.Getter;
import mytown.protection.segment.getter.GetterConstant;
import mytown.protection.segment.getter.GetterDynamic;
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
    private static final Gson gson = new GsonBuilder()
            .registerTypeAdapter(Caller.class, new CallerSerializer()).registerTypeAdapter(Getter.class, new GetterSerializer())
            .registerTypeAdapter(Protection.class, new ProtectionSerializer())
            .registerTypeAdapter(Segment.class, new SegmentSerializer())
            .registerTypeAdapter(Volume.class, new VolumeSerializer())
            .setPrettyPrinting().create();

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
        ProtectionHandler.instance.reset();
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
                        ProtectionHandler.instance.addProtection(protection);
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
            ProtectionHandler.instance.addProtection(vanillaProtection);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private static void createModel() {
        List<Segment> segments = new ArrayList<Segment>();

        segments.add(new SegmentBlock(net.minecraft.block.BlockButton.class, FlagType.ACTIVATE, false, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockDoor.class, FlagType.ACTIVATE, false, null, null, BlockType.RIGHT_CLICK, -1, new Volume(0, -1, 0, 0, 1, 0)));
        segments.add(new SegmentBlock(net.minecraft.block.BlockLever.class, FlagType.ACTIVATE, false, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockFenceGate.class, FlagType.ACTIVATE, false, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockDragonEgg.class, FlagType.ACTIVATE, false, null, null, BlockType.ANY_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockCake.class, FlagType.ACTIVATE, false, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockTrapDoor.class, FlagType.ACTIVATE, false, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockJukebox.class, FlagType.ACTIVATE, false, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockRedstoneRepeater.class, FlagType.ACTIVATE, false, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockRedstoneComparator.class, FlagType.ACTIVATE, false, null, null, BlockType.RIGHT_CLICK, -1, null));

        segments.add(new SegmentBlock(net.minecraft.block.BlockAnvil.class, FlagType.ACCESS, false, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockCauldron.class, FlagType.ACCESS, false, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockContainer.class, FlagType.ACCESS, false, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(new SegmentBlock(net.minecraft.block.BlockBed.class, FlagType.ACCESS, false, null, null, BlockType.RIGHT_CLICK, -1, null));

        segments.add(new SegmentEntity(net.minecraft.entity.monster.EntityMob.class, FlagType.MOBS, "passives", null, null, EntityType.TRACKED));
        segments.add(new SegmentEntity(net.minecraft.entity.EntityAgeable.class, FlagType.PVE, false, null, null, EntityType.PROTECT));
        GettersContainer getters = new GettersContainer();
        getters.add(new GetterConstant(5));
        segments.add(new SegmentEntity(net.minecraft.entity.monster.EntityCreeper.class, FlagType.EXPLOSIONS, false, null, getters, EntityType.TRACKED));
        getters = new GettersContainer();
        getters.add(new GetterConstant("range", 5));
        List<Caller> callers = new ArrayList<Caller>();
        callers.add(new CallerField("field_94084_b", null));
        callers.add(new CallerMethod("func_70005_c_", null));
        getters.add(new GetterDynamic("owner", callers));
        segments.add(new SegmentEntity(net.minecraft.entity.item.EntityTNTPrimed.class, FlagType.EXPLOSIONS, false, null, getters, EntityType.TRACKED));
        segments.add(new SegmentEntity(net.minecraft.entity.item.EntityItemFrame.class, FlagType.PVE, false, null, null, EntityType.PROTECT));

        segments.add(new SegmentItem(net.minecraft.item.ItemMonsterPlacer.class, FlagType.USAGE, false, null, null, ItemType.RIGHT_CLICK_BLOCK, true, null, false));
        segments.add(new SegmentItem(net.minecraft.item.ItemMonsterPlacer.class, FlagType.USAGE, false, null, null, ItemType.RIGHT_CLICK_ENTITY, false, null, false));
        segments.add(new SegmentItem(net.minecraft.item.ItemShears.class, FlagType.USAGE, false, null, null, ItemType.RIGHT_CLICK_ENTITY, false, null, false));
        segments.add(new SegmentItem(net.minecraft.item.ItemHangingEntity.class, FlagType.USAGE, false, null, null, ItemType.RIGHT_CLICK_BLOCK, true, null, false));

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
