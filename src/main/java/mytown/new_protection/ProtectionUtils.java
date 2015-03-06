package mytown.new_protection;

import mytown.entities.BlockWhitelist;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.util.BlockPos;
import mytown.util.MyTownUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.DimensionManager;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 12/1/2014.
 * Utilities for the protections
 */
public class ProtectionUtils {

    /**
     * Adds to the whitelist of the specified town. Used when placing blocks.
     */
    public static void addToBlockWhitelist(Class<? extends TileEntity> te, int dim, int x, int y, int z, Town town) {
        for (Protection prot : Protections.getInstance().getProtections()) {
            if (prot.isTileTracked(te))
                for (FlagType flagType : prot.getFlagsForTile(te)) {
                    if (!town.hasBlockWhitelist(dim, x, y, z, flagType)) {
                        BlockWhitelist bw = new BlockWhitelist(dim, x, y, z, flagType);
                        DatasourceProxy.getDatasource().saveBlockWhitelist(bw, town);
                    }
                }
        }
    }

    /**
     * Removes from the whitelist. Used when breaking blocks.
     */
    public static void removeFromWhitelist(Class<? extends TileEntity> te, int dim, int x, int y, int z, Town town) {
        for (Protection prot : Protections.getInstance().getProtections()) {
            if (prot.isTileTracked(te))
                for (FlagType flagType : prot.getFlagsForTile(te)) {
                    BlockWhitelist bw = town.getBlockWhitelist(dim, x, y, z, flagType);
                    if (bw != null) {
                        bw.delete();
                    }
                }
        }
    }

    /**
     * Checks the tile entity with all the protections
     */
    public static boolean checkTileEntity(TileEntity te) {
        for (Protection prot : Protections.getInstance().getProtections())
            if (prot.checkTileEntity(te))
                return true;
        return false;
    }

    /**
     * Checks the item usage with all the protections
     */
    public static boolean checkItemUsage(ItemStack stack, Resident res, BlockPos bp, int face) {
        for (Protection prot : Protections.getInstance().getProtections())
            if (prot.checkItem(stack, res, bp, face))
                return true;
        return false;
    }



    /**
     * Checks the block if it can be activated by a right-click
     */
    public static boolean checkActivatedBlocks(Block block, int meta) {
        for (Protection prot : Protections.getInstance().getProtections()) {
            if (prot.isBlockTracked(block.getClass(), meta))
                return true;
        }
        return false;
    }
    /**
     * Checks if an entity is hostile
     */
    public static boolean isEntityTracked(Class<? extends Entity> ent) {
        for (Protection prot : Protections.getInstance().getProtections()) {
            if (prot.isEntityTracked(ent)) {
                return true;
            }
        }
        return false;
    }

    public static List<FlagType> getFlagsForTile(Class<? extends TileEntity> te) {
        List<FlagType> flags = new ArrayList<FlagType>();
        for(Protection protection : Protections.getInstance().getProtections()) {
            if(protection.isTileTracked(te))
                flags.addAll(protection.getFlagsForTile(te));
        }
        return flags;
    }

    /**
     * Checks if the block whitelist is still valid
     */
    public static boolean isBlockWhitelistValid(BlockWhitelist bw) {
        // TODO: Maybe make this better
        // Delete if the town is gone
        if (MyTownUtils.getTownAtPosition(bw.dim, bw.x >> 4, bw.z >> 4) == null)
            return false;

        if (bw.getFlagType() == FlagType.activateBlocks
                && !(checkActivatedBlocks(DimensionManager.getWorld(bw.dim).getBlock(bw.x, bw.y, bw.z), DimensionManager.getWorld(bw.dim).getBlockMetadata(bw.x, bw.y, bw.z))))
            return false;
        if ((bw.getFlagType() == FlagType.modifyBlocks || bw.getFlagType() == FlagType.activateBlocks || bw.getFlagType() == FlagType.useItems)) {
            TileEntity te = DimensionManager.getWorld(bw.dim).getTileEntity(bw.x, bw.y, bw.z);
            if (te == null) return false;
            return getFlagsForTile(te.getClass()).contains(bw.getFlagType());
        }
        return true;
    }


    /*
    public static Object getInfoFromGetters(Map<String, List<Getter>> extraGettersMap, String getterName, Class<?> returnType, String segmentName, Object instance, Object parameter) {
        Object lastInstance = instance;
        List<Getter> getterList = extraGettersMap.get(getterName);
        try {
            for (Getter getter : getterList) {
                switch (getter.type) {
                    case field:
                        Field fieldObject = lastInstance.getClass().getField(getter.element);
                        fieldObject.setAccessible(true);
                        lastInstance = fieldObject.get(lastInstance);
                        break;
                    case method:
                        Method methodObject = lastInstance.getClass().getDeclaredMethod(getter.element);
                        methodObject.setAccessible(true);
                        try {
                            lastInstance = methodObject.invoke(lastInstance);
                        } catch (IllegalArgumentException ex) {
                            try {
                                lastInstance = methodObject.invoke(lastInstance, parameter);
                            } catch (IllegalArgumentException ex2) {
                                // Throwing the original exception.
                                throw ex;
                            }
                        }
                        break;
                    case formula:
                        //lastInstance = getInfoFromFormula()

                        break;
                }
            }
            if(!returnType.isAssignableFrom(lastInstance.getClass()))
                throw new RuntimeException("[Segment: "+ segmentName +"] Got wrong type of class at a getter! Expected: " + returnType.getName());
            return lastInstance;
        } catch(NoSuchFieldException nfex) {
            MyTown.instance.log.error("[Segment:"+ segmentName +"] Encountered a problem when getting a field from " + object.toString());
            nfex.printStackTrace();
        } catch (IllegalAccessException iaex) {
            MyTown.instance.log.error("[Segment:"+ segmentName +"] This type of thing should not happen.");
            iaex.printStackTrace();
        } catch (NoSuchMethodException nmex) {
            MyTown.instance.log.error("[Segment:"+ segmentName +"] Encountered a problem when getting a method from " + object.toString());
            nmex.printStackTrace();
        } catch (InvocationTargetException itex) {
            MyTown.instance.log.error("[Segment:"+ segmentName +"] The returned object was not of the expected type!");
            itex.printStackTrace();
        }
        return null;
    }


    public static int getInfoFromFormula(String formula, Map<String, List<Getter>> extraGettersMap, Object object, String segment, Object parameter) {
        int result = -1;

        String[] elements = formula.split(" ");

        // Replace all the getters with proper numbers, assume getters that are invalid as being numbers
        for(String element : elements) {
            if(!element.equals("+") && !element.equals("-") && !element.equals("*") && !element.equals("/") && !element.equals("^")) {
                if(extraGettersMap.get(element) != null) {
                    int info = (Integer) getInfoFromGetters(extraGettersMap.get(element), object, Integer.class, segment, parameter);

                    // Replace all occurrences with the value that it got.
                    // Spaces are needed to not replace parts of other getters.
                    formula = formula.replace(" " + element + " ", " " + String.valueOf(info) + " ");
                }
            }
        }

        MyTown.instance.log.info("Got formula at the end: " + formula);
        MyTown.instance.log.info("Trying to parse it.");

        Interpreter interpreter = new Interpreter();
        try {
            interpreter.eval("result = " + formula);
            result = (Integer)interpreter.get("result");
        } catch (EvalError ex) {
            ex.printStackTrace();
        }

        return result;
    }
    */
}
