package mytown.protection;

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
        for (Protection prot : Protections.instance.protections.values()) {
            if (prot.trackedTileEntities.contains(te))
                for (FlagType flagType : prot.getFlagTypeForTile(te)) {
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
        for (Protection prot : Protections.instance.protections.values()) {
            if (prot.trackedTileEntities.contains(te))
                for (FlagType flagType : prot.getFlagTypeForTile(te)) {
                    BlockWhitelist bw = town.getBlockWhitelist(dim, x, y, z, flagType);
                    if (bw != null) {
                        bw.delete();
                    }
                }
        }
    }

    /**
     * Gets the flags that this tile entity might have a whitelist for
     */
    public static List<FlagType> getFlagTypesForTile(TileEntity te) {
        List<FlagType> list = new ArrayList<FlagType>();
        for (Protection prot : Protections.instance.protections.values())
            if (prot.hasToCheckTileEntity(te))
                list.addAll(prot.getFlagTypeForTile(te.getClass()));
        return list;
    }

    /**
     * Checks the tile entity with all the protections
     */
    public static boolean checkTileEntity(TileEntity te) {
        for (Protection prot : Protections.instance.protections.values())
            if (prot.checkTileEntity(te))
                return true;
        return false;
    }

    /**
     * Checks the item usage with all the protections
     */
    public static boolean checkItemUsage(ItemStack stack, Resident res, BlockPos bp) {
        for (Protection prot : Protections.instance.protections.values())
            if (prot.checkItemUsage(stack, res, bp))
                return true;
        return false;
    }


    /**
     * Checks the block if it can be activated by a right-click
     */
    public static boolean checkActivatedBlocks(Block block) {
        for (Protection prot : Protections.instance.protections.values()) {
            if (prot.activatedBlocks.contains(block))
                return true;
        }
        return false;
    }

    /**
     * Checks if an entity is hostile
     */
    public static boolean checkIsEntityHostile(Class<? extends Entity> ent) {
        for (Protection prot : Protections.instance.protections.values()) {
            if (prot.hostileEntities.contains(ent)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if the block whitelist is still valid
     */
    public static boolean isBlockWhitelistValid(BlockWhitelist bw) {
        // TODO: Maybe make this better
        // Delete if the town is gone
        if (MyTownUtils.getTownAtPosition(bw.dim, bw.x >> 4, bw.z >> 4) == null)
            return false;

        if (bw.getFlagType() == FlagType.activateBlocks && !checkActivatedBlocks(DimensionManager.getWorld(bw.dim).getBlock(bw.x, bw.y, bw.z)))
            return false;
        if ((bw.getFlagType() == FlagType.modifyBlocks || bw.getFlagType() == FlagType.activateBlocks || bw.getFlagType() == FlagType.useItems || bw.getFlagType() == FlagType.pumps)) {
            TileEntity te = DimensionManager.getWorld(bw.dim).getTileEntity(bw.x, bw.y, bw.z);
            if (te == null) return false;
            return getFlagTypesForTile(te).contains(bw.getFlagType());
        }
        return true;
    }


}
