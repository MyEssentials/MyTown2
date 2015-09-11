package mytown.protection;

import myessentials.entities.BlockPos;
import mytown.entities.BlockWhitelist;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.ProtectionFlagType;
import mytown.proxies.DatasourceProxy;
import mytown.util.MyTownUtils;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Utilities for the protections
 */
public class ProtectionUtils {

    private ProtectionUtils() {

    }

    /**
     * Adds to the whitelist of the specified town. Used when placing blocks.
     */
    public static void addToBlockWhitelist(Class<? extends TileEntity> te, int dim, int x, int y, int z, Town town) {
        for (Protection prot : ProtectionHandler.instance.getProtectionList()) {
            if (prot.isTileTracked(te))
                for (ProtectionFlagType flagType : prot.getFlagsForTile(te)) {
                    if (!town.blockWhitelistsContainer.contains(dim, x, y, z, flagType)) {
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
        for (Protection prot : ProtectionHandler.instance.getProtectionList()) {
            if (prot.isTileTracked(te))
                for (ProtectionFlagType flagType : prot.getFlagsForTile(te)) {
                    BlockWhitelist bw = town.blockWhitelistsContainer.get(dim, x, y, z, flagType);
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
        for (Protection prot : ProtectionHandler.instance.getProtectionList())
            if (prot.checkTileEntity(te))
                return true;
        return false;
    }

    /**
     * Checks the item usage with all the protections
     */
    public static boolean checkItemUsage(ItemStack stack, Resident res, BlockPos bp, int face) {
/*        for (Protection prot : Protections.instance.getProtectionList())
            if (prot.checkItem(stack, res, bp, face))
                return true;*/
        return false;
    }



    /**
     * Checks the block if it can be activated by a right-click
     */
    public static boolean checkActivatedBlocks(Block block, int meta) {
        for (Protection prot : ProtectionHandler.instance.getProtectionList()) {
            if (prot.isBlockTracked(block.getClass(), meta))
                return true;
        }
        return false;
    }
    /**
     * Checks if an entity is hostile
     */
    public static boolean isEntityTracked(Class<? extends Entity> ent) {
        for (Protection prot : ProtectionHandler.instance.getProtectionList()) {
            if (prot.isEntityTracked(ent)) {
                return true;
            }
        }
        return false;
    }

    public static boolean isTileEntityOwnable(Class<? extends TileEntity> clsTe) {
        for(Protection protection : ProtectionHandler.instance.getProtectionList()) {
            if(protection.isTileEntityOwnable(clsTe))
                return true;
        }
        return false;
    }

    public static List<ProtectionFlagType> getFlagsForTile(Class<? extends TileEntity> te) {
        List<ProtectionFlagType> flags = new ArrayList<ProtectionFlagType>();
        for(Protection protection : ProtectionHandler.instance.getProtectionList()) {
            if(protection.isTileTracked(te))
                flags.addAll(protection.getFlagsForTile(te));
        }
        return flags;
    }

    /**
     * Checks if the block whitelist is still valid
     */
    public static boolean isBlockWhitelistValid(BlockWhitelist bw) {
        // Delete if the town is gone
        if (MyTownUtils.getTownAtPosition(bw.getDim(), bw.getX() >> 4, bw.getZ() >> 4) == null)
            return false;

        if (bw.getFlagType() == ProtectionFlagType.ACTIVATE
                && !checkActivatedBlocks(MinecraftServer.getServer().worldServerForDimension(bw.getDim()).getBlock(bw.getX(), bw.getY(), bw.getZ()), MinecraftServer.getServer().worldServerForDimension(bw.getDim()).getBlockMetadata(bw.getX(), bw.getY(), bw.getZ())))
            return false;
        if (bw.getFlagType() == ProtectionFlagType.MODIFY || bw.getFlagType() == ProtectionFlagType.ACTIVATE || bw.getFlagType() == ProtectionFlagType.USAGE) {
            TileEntity te = MinecraftServer.getServer().worldServerForDimension(bw.getDim()).getTileEntity(bw.getX(), bw.getY(), bw.getZ());
            if (te == null)
                return false;
            return getFlagsForTile(te.getClass()).contains(bw.getFlagType());
        }
        return true;
    }

    public static boolean canEntityTrespassPvp(Class<? extends Entity> entity) {
        for(Protection protection : ProtectionHandler.instance.getProtectionList()) {
            if(protection.canEntityTrespassPvp(entity))
                return true;
        }
        return false;
    }

    public static void saveBlockOwnersToDB() {
        for(Map.Entry<TileEntity, Resident> set : ProtectionHandler.instance.ownedTileEntities.entrySet()) {
            DatasourceProxy.getDatasource().saveBlockOwner(set.getValue(), set.getKey().getWorldObj().provider.dimensionId, set.getKey().xCoord, set.getKey().yCoord, set.getKey().zCoord);
        }
    }

    /**
     * Method called by the ThreadPlacementCheck after it found a TileEntity
     */
    public static synchronized void addTileEntity(TileEntity te, Resident res) {
        ProtectionHandler.instance.ownedTileEntities.put(te, res);
        if(ProtectionHandler.instance.activePlacementThreads != 0)
            ProtectionHandler.instance.activePlacementThreads--;
    }

    public static synchronized void placementThreadTimeout() {
        ProtectionHandler.instance.activePlacementThreads--;
    }
}
