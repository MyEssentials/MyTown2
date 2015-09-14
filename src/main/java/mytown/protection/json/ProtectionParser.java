package mytown.protection.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import mytown.MyTown;
import myessentials.entities.Volume;
import mytown.api.container.GettersContainer;
import mytown.entities.flag.FlagType;
import mytown.protection.Protection;
import mytown.protection.ProtectionHandler;
import mytown.protection.ProtectionUtils;
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
            .registerTypeAdapter(Caller.class, new CallerSerializer())
            .registerTypeAdapter(Getter.class, new GetterSerializer())
            .registerTypeAdapter(Protection.class, new ProtectionSerializer())
            .registerTypeAdapter(Segment.class, new SegmentSerializer())
            .registerTypeAdapter(Volume.class, new VolumeSerializer())
            .registerTypeAdapter(FlagType.class, new FlagTypeSerializer())
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
        ProtectionUtils.protections.clear();
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
                        ProtectionUtils.protections.add(protection);
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
            ProtectionUtils.protections.add(vanillaProtection);
        }

        return true;
    }

    @SuppressWarnings("unchecked")
    private static void createModel() {
        List<Segment> segments = new ArrayList<Segment>();

        segments.add(createSegmentBlock(net.minecraft.block.BlockButton.class, FlagType.ACTIVATE, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(createSegmentBlock(net.minecraft.block.BlockDoor.class, FlagType.ACTIVATE, null, null, BlockType.RIGHT_CLICK, -1, new Volume(0, -1, 0, 0, 1, 0)));
        segments.add(createSegmentBlock(net.minecraft.block.BlockLever.class, FlagType.ACTIVATE, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(createSegmentBlock(net.minecraft.block.BlockFenceGate.class, FlagType.ACTIVATE, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(createSegmentBlock(net.minecraft.block.BlockDragonEgg.class, FlagType.ACTIVATE, null, null, BlockType.ANY_CLICK, -1, null));
        segments.add(createSegmentBlock(net.minecraft.block.BlockCake.class, FlagType.ACTIVATE, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(createSegmentBlock(net.minecraft.block.BlockTrapDoor.class, FlagType.ACTIVATE, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(createSegmentBlock(net.minecraft.block.BlockJukebox.class, FlagType.ACTIVATE, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(createSegmentBlock(net.minecraft.block.BlockRedstoneRepeater.class, FlagType.ACTIVATE, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(createSegmentBlock(net.minecraft.block.BlockRedstoneComparator.class, FlagType.ACTIVATE, null, null, BlockType.RIGHT_CLICK, -1, null));

        segments.add(createSegmentBlock(net.minecraft.block.BlockAnvil.class, FlagType.ACCESS, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(createSegmentBlock(net.minecraft.block.BlockCauldron.class, FlagType.ACCESS, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(createSegmentBlock(net.minecraft.block.BlockContainer.class, FlagType.ACCESS, null, null, BlockType.RIGHT_CLICK, -1, null));
        segments.add(createSegmentBlock(net.minecraft.block.BlockBed.class, FlagType.ACCESS, null, null, BlockType.RIGHT_CLICK, -1, null));

        segments.add(createSegmentEntity(net.minecraft.entity.monster.EntityMob.class, FlagType.MOBS, null, null, EntityType.TRACKED));
        segments.add(createSegmentEntity(net.minecraft.entity.EntityAgeable.class, FlagType.PVE, null, null, EntityType.PROTECT));
        GettersContainer getters = new GettersContainer();
        getters.add(new GetterConstant("range", 5));
        segments.add(createSegmentEntity(net.minecraft.entity.monster.EntityCreeper.class, FlagType.EXPLOSIONS, null, getters, EntityType.TRACKED));
        getters = new GettersContainer();
        getters.add(new GetterConstant("range", 5));
        List<Caller> callers = new ArrayList<Caller>();
        callers.add(new CallerField("field_94084_b", null));
        callers.add(new CallerMethod("func_70005_c_", null));
        getters.add(new GetterDynamic("owner", callers));
        segments.add(createSegmentEntity(net.minecraft.entity.item.EntityTNTPrimed.class, FlagType.EXPLOSIONS, null, getters, EntityType.TRACKED));
        segments.add(createSegmentEntity(net.minecraft.entity.item.EntityItemFrame.class, FlagType.PVE, null, null, EntityType.PROTECT));

        segments.add(createSegmentItem(net.minecraft.item.ItemMonsterPlacer.class, FlagType.USAGE, null, null, ItemType.RIGHT_CLICK_BLOCK, true, null, false));
        segments.add(createSegmentItem(net.minecraft.item.ItemMonsterPlacer.class, FlagType.USAGE, null, null, ItemType.RIGHT_CLICK_ENTITY, false, null, false));
        segments.add(createSegmentItem(net.minecraft.item.ItemShears.class, FlagType.USAGE, null, null, ItemType.RIGHT_CLICK_ENTITY, false, null, false));
        segments.add(createSegmentItem(net.minecraft.item.ItemHangingEntity.class, FlagType.USAGE, null, null, ItemType.RIGHT_CLICK_BLOCK, true, null, false));

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

    private static SegmentBlock createSegmentBlock(Class<?> clazz, FlagType<Boolean> flagType, String conditionString, GettersContainer getters, BlockType blockType, int meta, Volume clientUpdateCoords) {
        SegmentBlock segment = new SegmentBlock(meta, clientUpdateCoords);
        if(getters != null) {
            segment.getters.addAll(getters);
        }

        segment.setCheckClass(clazz);
        segment.types.add(blockType);
        segment.flags.add(flagType);
        segment.setConditionString(conditionString);
        return segment;
    }

    private static SegmentEntity createSegmentEntity(Class<?> clazz, FlagType<Boolean> flagType, String conditionString, GettersContainer getters, EntityType entityType) {
        SegmentEntity segment = new SegmentEntity();
        if(getters != null) {
            segment.getters.addAll(getters);
        }
        segment.setCheckClass(clazz);
        segment.types.add(entityType);
        segment.flags.add(flagType);
        segment.setConditionString(conditionString);
        return segment;
    }

    private static SegmentItem createSegmentItem(Class<?> clazz, FlagType<Boolean> flagType, String conditionString, GettersContainer getters, ItemType itemType, boolean onAdjacent, Volume clientUpdateCoords, boolean directionalClientUpdate) {
        SegmentItem segment = new SegmentItem(onAdjacent, clientUpdateCoords, directionalClientUpdate);
        if(getters != null) {
            segment.getters.addAll(getters);
        }
        segment.setCheckClass(clazz);
        segment.types.add(itemType);
        segment.flags.add(flagType);
        segment.setConditionString(conditionString);
        return segment;
    }

    private static SegmentTileEntity createSegmentTileEntity(Class<?> clazz, FlagType<Boolean> flagType, String conditionString, GettersContainer getters, boolean hasOwner) {
        SegmentTileEntity segment = new SegmentTileEntity(hasOwner);
        if(getters != null) {
            segment.getters.addAll(getters);
        }
        segment.setCheckClass(clazz);
        segment.flags.add(flagType);
        segment.setConditionString(conditionString);
        return segment;
    }



}
