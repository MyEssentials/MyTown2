package mytown.protection;

import cpw.mods.fml.common.FMLCommonHandler;
import mytown.MyTown;
import mytown.core.entities.Volume;
import mytown.core.utils.WorldUtils;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.Wild;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.*;
import mytown.protection.segment.enums.BlockType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.enums.ItemType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.core.entities.BlockPos;
import mytown.core.entities.ChunkPos;
import mytown.util.Formatter;
import mytown.util.exceptions.ConditionException;
import mytown.util.exceptions.GetterException;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.apache.commons.lang3.exception.ExceptionUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * An object which offers protection for a specific mod and version
 */
public class Protection {

    public final String modid;
    public final String version;

    public final List<SegmentTileEntity> segmentsTiles = new ArrayList<SegmentTileEntity>();
    public final List<SegmentEntity> segmentsEntities = new ArrayList<SegmentEntity>();
    public final List<SegmentItem> segmentsItems = new ArrayList<SegmentItem>();
    public final List<SegmentBlock> segmentsBlocks = new ArrayList<SegmentBlock>();

    public Protection(String modid, List<Segment> segments) {
        this(modid, "", segments);
    }

    public Protection(String modid, String version, List<Segment> segments) {

        for(Segment segment : segments) {
            if(segment instanceof SegmentTileEntity)
                segmentsTiles.add((SegmentTileEntity)segment);
            else if(segment instanceof SegmentEntity)
                segmentsEntities.add((SegmentEntity) segment);
            else if(segment instanceof SegmentItem)
                segmentsItems.add((SegmentItem)segment);
            else if(segment instanceof SegmentBlock)
                segmentsBlocks.add((SegmentBlock)segment);
        }

        this.modid = modid;
        this.version = version;
    }

    public boolean checkTileEntity(TileEntity te) {
        for(Iterator<SegmentTileEntity> it = segmentsTiles.iterator(); it.hasNext();) {
            SegmentTileEntity segment = it.next();
            if(segment.getCheckClass().isAssignableFrom(te.getClass())) {
                try {
                    if(segment.checkCondition(te)) {

                        int x1 = segment.getX1(te);
                        int z1 = segment.getZ1(te);
                        int x2 = segment.getX2(te);
                        int z2 = segment.getZ2(te);

                        List<ChunkPos> chunks = WorldUtils.getChunksInBox(x1, z1, x2, z2);
                        boolean inWild = false;
                        for (ChunkPos chunk : chunks) {
                            TownBlock block = getDatasource().getBlock(te.getWorldObj().provider.dimensionId, chunk.getX(), chunk.getZ());
                            if(block == null) {
                                inWild = true;
                            } else {
                                if(segment.hasOwner()) {
                                    Resident res = Protections.instance.getOwnerForTileEntity(te);
                                    if (res == null || !block.getTown().checkPermission(res, segment.getFlag(), segment.getDenialValue()))
                                        return true;
                                } else if (!(Boolean) block.getTown().getValue(segment.getFlag()) && !block.getTown().hasBlockWhitelist(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.MODIFY)) {
                                    block.getTown().notifyEveryone(FlagType.MODIFY.getLocalizedTownNotification());
                                    return true;
                                }
                            }
                        }
                        if(inWild && Wild.instance.getValue(segment.getFlag()).equals(segment.getDenialValue())) {
                            if (segment.hasOwner()) {
                                Resident res = Protections.instance.getOwnerForTileEntity(te);
                                if (res == null || !Wild.instance.checkPermission(res, segment.getFlag(), segment.getDenialValue()))
                                    return true;

                            } else {
                                return true;
                            }
                        }
                    }
                } catch (Exception ex) {
                    MyTown.instance.LOG.error("Failed to check tile entity: " + te.getClass().getSimpleName() + "( " + te.xCoord + ", " + te.yCoord + ", " + te.zCoord + " | WorldID: " + te.getWorldObj().provider.dimensionId + " )");
                    MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
                    // Disabling protection if something errors.
                    if(ex instanceof GetterException || ex instanceof ConditionException) {
                        this.disableSegment(it, segment, ex.getMessage());
                    } else {
                        MyTown.instance.LOG.error("Skipping...");
                    }
                }
                return false;
            }
        }
        return false;
    }

    // TODO: Add condition check
    public boolean checkEntity(Entity entity) {
        for(SegmentEntity segment : segmentsEntities) {
            if (segment.getCheckClass().isAssignableFrom(entity.getClass())) {
                if(segment.getType() == EntityType.TRACKED) {
                    if (segment.checkCondition(entity)) {
                        int range = segment.getRange(entity);
                        TownBlock block;
                        Resident owner = segment.getOwner(entity);
                        if (range == 0) {
                            block = getDatasource().getBlock(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);
                            if (block == null) {
                                if (owner == null) {
                                    if (Wild.instance.getValue(segment.getFlag()).equals(segment.getDenialValue()))
                                        return true;
                                } else {
                                    if (!Wild.instance.checkPermission(owner, segment.getFlag(), segment.getDenialValue()))
                                        return true;
                                }
                            } else {
                                if (owner == null) {
                                    if (block.getTown().getValueAtCoords(entity.dimension, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ), segment.getFlag()).equals(segment.getDenialValue()))
                                        return true;
                                } else {
                                    if (!block.getTown().checkPermission(owner, segment.getFlag(), segment.getDenialValue(), entity.dimension, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ)))
                                        return true;
                                }
                            }
                        } else {
                            List<ChunkPos> chunks = WorldUtils.getChunksInBox((int) Math.floor(entity.posX - range), (int) Math.floor(entity.posZ - range), (int) Math.floor(entity.posX + range), (int) Math.floor(entity.posZ + range));
                            // Just so that it doesn't check more than once for Wild permissions
                            boolean inWild = false;
                            for (ChunkPos chunk : chunks) {
                                block = getDatasource().getBlock(entity.dimension, chunk.getX(), chunk.getZ());
                                if (block == null) {
                                    inWild = true;
                                } else {
                                    if (owner == null) {
                                        if (block.getTown().getValue(segment.getFlag()).equals(segment.getDenialValue()))
                                            return true;
                                    } else {
                                        if (!block.getTown().checkPermission(owner, segment.getFlag(), segment.getDenialValue()))
                                            return true;
                                    }
                                }
                            }
                            if (inWild) {
                                if (owner == null) {
                                    if (Wild.instance.getValue(segment.getFlag()).equals(segment.getDenialValue()))
                                        return true;
                                } else {
                                    if (!Wild.instance.checkPermission(owner, segment.getFlag(), segment.getDenialValue()))
                                        return true;
                                }
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checking item usage for right click on block
     */
    public boolean checkItem(ItemStack item, Resident res, BlockPos bp, int face) {

        for(Iterator<SegmentItem> it = segmentsItems.iterator(); it.hasNext();) {
            SegmentItem segment = it.next();
            if(segment.getType() == ItemType.RIGHT_CLICK_BLOCK && segment.getCheckClass().isAssignableFrom(item.getItem().getClass())) {
                ForgeDirection direction = ForgeDirection.getOrientation(face);
                if(segment.isOnAdjacent()) {
                    bp = new BlockPos(bp.getX() + direction.offsetX, bp.getY() + direction.offsetY, bp.getZ() + direction.offsetZ, bp.getDim());
                }
                try {
                    if (segment.checkCondition(item)) {
                        int range = segment.getRange(item);
                        TownBlock block;
                        if(range == 0) {
                            block = getDatasource().getBlock(bp.getDim(), bp.getX() >> 4, bp.getZ() >> 4);
                            if(block == null) {
                                if (!Wild.instance.checkPermission(res, segment.getFlag(), segment.getDenialValue())) {
                                    res.sendMessage(segment.getFlag().getLocalizedProtectionDenial());
                                    if(segment.hasClientUpdate())
                                        sendClientUpdate(segment.getClientUpdateCoords(), bp, (EntityPlayerMP) res.getPlayer(), direction);
                                    return true;
                                }
                            } else {
                                if (!block.getTown().checkPermission(res, segment.getFlag(), segment.getDenialValue(), bp.getDim(), bp.getX(), bp.getY(), bp.getZ())) {
                                    res.protectionDenial(segment.getFlag().getLocalizedProtectionDenial(), Formatter.formatOwnersToString(block.getTown(), bp.getDim(), bp.getX(), bp.getY(), bp.getZ()));
                                    if(segment.hasClientUpdate())
                                        sendClientUpdate(segment.getClientUpdateCoords(), bp, (EntityPlayerMP) res.getPlayer(), direction);
                                    return true;
                                }
                            }
                        } else {
                            List<ChunkPos> chunks = WorldUtils.getChunksInBox(bp.getX() - range, bp.getZ() - range, bp.getX() + range, bp.getZ() + range);
                            boolean inWild = false;
                            for (ChunkPos chunk : chunks) {
                                block = getDatasource().getBlock(bp.getDim(), chunk.getX(), chunk.getZ());
                                if (block == null) {
                                    inWild = true;
                                } else {
                                    if (!block.getTown().checkPermission(res, segment.getFlag(), segment.getDenialValue())) {
                                        res.protectionDenial(segment.getFlag().getLocalizedProtectionDenial(), LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", block.getTown().getMayor() == null ? "SERVER ADMINS" : block.getTown().getMayor().getPlayerName()));
                                        if(segment.hasClientUpdate())
                                            sendClientUpdate(segment.getClientUpdateCoords(), bp, (EntityPlayerMP) res.getPlayer(), direction);
                                        return true;
                                    }
                                }
                            }
                            if (inWild && !Wild.instance.checkPermission(res, segment.getFlag(), segment.getDenialValue())) {
                                res.sendMessage(segment.getFlag().getLocalizedProtectionDenial());
                                if(segment.hasClientUpdate())
                                    sendClientUpdate(segment.getClientUpdateCoords(), bp, (EntityPlayerMP) res.getPlayer(), direction);
                                return true;
                            }
                        }
                    }
                } catch (Exception ex) {
                    MyTown.instance.LOG.error("Failed to check item use on " + item.getDisplayName() + " at the player " + res.getPlayerName() + "( " + bp.getX()
                            + ", " + bp.getY() + ", " + bp.getZ() + " | WorldID: " + bp.getDim() + " )");
                    MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
                    if(ex instanceof GetterException || ex instanceof ConditionException) {
                        this.disableSegment(it, segment, ex.getMessage());
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checking item usage for right click on entity
     */
    public boolean checkEntityRightClick(ItemStack item, Resident res, Entity entity) {
        for(Iterator<SegmentEntity> it = segmentsEntities.iterator(); it.hasNext();) {
            SegmentEntity segment = it.next();
            if(segment.getType() == EntityType.PROTECT && segment.getCheckClass().isAssignableFrom(entity.getClass())) {
                TownBlock block = getDatasource().getBlock(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);
                if(block == null) {
                    if(!Wild.instance.checkPermission(res, segment.getFlag(), segment.getDenialValue())) {
                        res.sendMessage(FlagType.PVE.getLocalizedProtectionDenial());
                        return true;
                    }
                } else {
                    if(!block.getTown().checkPermission(res, segment.getFlag(), segment.getDenialValue(), entity.dimension, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ))) {
                        res.protectionDenial(FlagType.PVE.getLocalizedProtectionDenial(), Formatter.formatOwnersToString(block.getTown(), entity.dimension, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ)));
                        return true;
                    }
                }
            }
        }

        if(item == null)
            return false;

        for(Iterator<SegmentItem> it = segmentsItems.iterator(); it.hasNext();) {
            SegmentItem segment = it.next();
            if(segment.getType() == ItemType.RIGHT_CLICK_BLOCK && segment.getCheckClass().isAssignableFrom(item.getItem().getClass())) {
                try {
                    if (segment.checkCondition(item)) {
                        int range = segment.getRange(item);
                        TownBlock block;
                        if(range == 0) {
                            block = getDatasource().getBlock(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);
                            if(block == null) {
                                if (!Wild.instance.checkPermission(res, segment.getFlag(), segment.getDenialValue())) {
                                    res.sendMessage(segment.getFlag().getLocalizedProtectionDenial());
                                    return true;
                                }
                            } else {
                                if (!block.getTown().checkPermission(res, segment.getFlag(), segment.getDenialValue(), entity.dimension, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ))) {
                                    res.protectionDenial(segment.getFlag().getLocalizedProtectionDenial(), Formatter.formatOwnersToString(block.getTown(), entity.dimension, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ)));
                                    return true;
                                }
                            }
                        } else {
                            List<ChunkPos> chunks = WorldUtils.getChunksInBox((int) Math.floor(entity.posX - range), (int) Math.floor(entity.posZ - range), (int) Math.floor(entity.posX + range), (int) Math.floor(entity.posZ + range));
                            boolean inWild = false;
                            for (ChunkPos chunk : chunks) {
                                block = getDatasource().getBlock(entity.dimension, chunk.getX(), chunk.getZ());
                                if (block == null) {
                                    inWild = true;
                                } else {
                                    if (!block.getTown().checkPermission(res, segment.getFlag(), segment.getDenialValue())) {
                                        res.protectionDenial(segment.getFlag().getLocalizedProtectionDenial(), LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", block.getTown().getMayor() == null ? "SERVER ADMINS" : block.getTown().getMayor().getPlayerName()));
                                        return true;
                                    }
                                }
                            }
                            if (inWild && !Wild.instance.checkPermission(res, segment.getFlag(), segment.getDenialValue())) {
                                res.sendMessage(segment.getFlag().getLocalizedProtectionDenial());
                                return true;
                            }
                        }
                    }
                } catch (Exception ex) {
                    MyTown.instance.LOG.error("Failed to check item use on " + item.getDisplayName() + " at the player " + res.getPlayerName() + "( " + (int)entity.posX
                            + ", " + (int)entity.posY + ", " + (int)entity.posZ + " | WorldID: " + entity.dimension + " )");
                    MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
                    if(ex instanceof GetterException || ex instanceof ConditionException) {
                        this.disableSegment(it, segment, ex.getMessage());
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checking item usage for right click on air
     */
    public boolean checkItem(ItemStack item, Resident res) {
        for(Iterator<SegmentItem> it = segmentsItems.iterator(); it.hasNext();) {
            SegmentItem segment = it.next();
            if(segment.getType() == ItemType.RIGHT_CLICK_AIR && segment.getCheckClass().isAssignableFrom(item.getItem().getClass())) {
                EntityPlayer entity = res.getPlayer();

                try {
                    if (segment.checkCondition(item)) {
                        int range = segment.getRange(item);
                        TownBlock block;
                        if(range == 0) {
                            block = getDatasource().getBlock(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);
                            if(block == null) {
                                if (!Wild.instance.checkPermission(res, segment.getFlag(), segment.getDenialValue())) {
                                    res.sendMessage(segment.getFlag().getLocalizedProtectionDenial());
                                    return true;
                                }
                            } else {
                                if (!block.getTown().checkPermission(res, segment.getFlag(), segment.getDenialValue(), entity.dimension, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ))) {
                                    res.protectionDenial(segment.getFlag().getLocalizedProtectionDenial(), Formatter.formatOwnersToString(block.getTown(), entity.dimension, (int) Math.floor(entity.posX), (int) Math.floor(entity.posY), (int) Math.floor(entity.posZ)));
                                    return true;
                                }
                            }
                        } else {
                            List<ChunkPos> chunks = WorldUtils.getChunksInBox((int) Math.floor(entity.posX - range), (int) Math.floor(entity.posZ - range), (int) Math.floor(entity.posX + range), (int) Math.floor(entity.posZ + range));
                            boolean inWild = false;
                            for (ChunkPos chunk : chunks) {
                                block = getDatasource().getBlock(entity.dimension, chunk.getX(), chunk.getZ());
                                if (block == null) {
                                    inWild = true;
                                } else {
                                    if (!block.getTown().checkPermission(res, segment.getFlag(), segment.getDenialValue())) {
                                        res.protectionDenial(segment.getFlag().getLocalizedProtectionDenial(), LocalizationProxy.getLocalization().getLocalization("mytown.notification.town.owners", block.getTown().getMayor() == null ? "SERVER ADMINS" : block.getTown().getMayor().getPlayerName()));
                                        return true;
                                    }
                                }
                            }
                            if (inWild && !Wild.instance.checkPermission(res, segment.getFlag(), segment.getDenialValue())) {
                                res.sendMessage(segment.getFlag().getLocalizedProtectionDenial());
                                return true;
                            }
                        }
                    }
                } catch (Exception ex) {
                    MyTown.instance.LOG.error("Failed to check item use on " + item.getDisplayName() + " at the player " + res.getPlayerName() + "( "
                            + ", " + entity.posX + ", " + entity.posY + ", " + entity.posZ + " | WorldID: " + entity.dimension + " )");

                    if(ex instanceof GetterException || ex instanceof ConditionException) {
                        this.disableSegment(it, segment, ex.getMessage());
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checking right click actions on blocks.
     */
    public boolean checkBlockInteraction(Resident res, BlockPos bp, PlayerInteractEvent.Action action) {
        Block blockType = DimensionManager.getWorld(bp.getDim()).getBlock(bp.getX(), bp.getY(), bp.getZ());
        for(SegmentBlock segment : segmentsBlocks) {
            if(segment.getCheckClass().isAssignableFrom(blockType.getClass())
                    && (segment.getMeta() == -1 || segment.getMeta() == DimensionManager.getWorld(bp.getDim()).getBlockMetadata(bp.getX(), bp.getY(), bp.getZ()))
                    && (segment.getType() == BlockType.ANY_CLICK || segment.getType() == BlockType.RIGHT_CLICK && action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || segment.getType() == BlockType.LEFT_CLICK && action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)) {
                if(segment.getFlag() == FlagType.ACCESS || segment.getFlag() == FlagType.ACTIVATE) {
                    TownBlock block = getDatasource().getBlock(bp.getDim(), bp.getX() >> 4, bp.getZ() >> 4);
                    if(block == null) {
                        if(!Wild.instance.checkPermission(res, segment.getFlag(), segment.getDenialValue())) {
                            res.sendMessage(segment.getFlag().getLocalizedProtectionDenial());
                            if(segment.hasClientUpdate())
                                sendClientUpdate(segment.getClientUpdateCoords(), bp, (EntityPlayerMP) res.getPlayer(), null);
                            return true;
                        }
                    } else {
                        if(!block.getTown().checkPermission(res, segment.getFlag(), segment.getDenialValue(), bp.getDim(), bp.getX(), bp.getY(), bp.getZ())) {
                            res.protectionDenial(segment.getFlag().getLocalizedProtectionDenial(), Formatter.formatOwnersToString(block.getTown(), bp.getDim(), bp.getX(), bp.getY(), bp.getZ()));
                            if(segment.hasClientUpdate())
                                sendClientUpdate(segment.getClientUpdateCoords(), bp, (EntityPlayerMP) res.getPlayer(), null);
                            return true;
                        }
                    }
                }
            }
        }

        return false;
    }

    public void sendClientUpdate(Volume updateVolume, BlockPos center, EntityPlayerMP player, ForgeDirection face) {
        World world = DimensionManager.getWorld(center.getDim());
        int x, y, z;

        if(face != null)
            updateVolume = translateVolume(updateVolume, face);

        for (int i = updateVolume.getMinX(); i <= updateVolume.getMaxX(); i++) {
            for (int j = updateVolume.getMinY(); j <= updateVolume.getMaxY(); j++) {
                for (int k = updateVolume.getMinZ(); k <= updateVolume.getMaxZ(); k++) {
                    x = center.getX() + i;
                    y = center.getY() + j;
                    z = center.getZ() + k;

                    S23PacketBlockChange packet = new S23PacketBlockChange(x, y, z, world);
                    packet.field_148884_e = world.getBlockMetadata(x, y, z);
                    FMLCommonHandler.instance().getMinecraftServerInstance().getConfigurationManager().sendPacketToAllPlayers(packet);
                }
            }
        }
    }

    public Volume translateVolume(Volume volume, ForgeDirection direction) {
        if(direction == ForgeDirection.UNKNOWN)
            return volume;

        switch (direction) {
            case DOWN:
                volume = new Volume(volume.getMinX(), -volume.getMaxZ(), volume.getMinY(), volume.getMaxX(), volume.getMinZ(), volume.getMaxY());
                break;
            case UP:
                volume = new Volume(volume.getMinX(), volume.getMinZ(), volume.getMinY(), volume.getMaxX(), volume.getMaxZ(), volume.getMaxY());
                break;
            case NORTH:
                volume = new Volume(volume.getMinX(), volume.getMinY(), - volume.getMaxZ(), volume.getMaxX(), volume.getMaxY(), volume.getMinZ());
                break;
            case WEST:
                volume = new Volume(- volume.getMaxZ(), volume.getMinY(), volume.getMinX(), volume.getMinZ(), volume.getMaxY(), volume.getMaxX());
                break;
            case EAST:
                volume = new Volume(volume.getMinZ(), volume.getMinY(), volume.getMinX(), volume.getMaxZ(), volume.getMaxY(), volume.getMaxX());
                break;
            case SOUTH:
                // The orientation on South is already the correct one, no translation needed.
                break;
        }
        return volume;
    }


    /**
     * Gets the flags which the type of TileEntity is checked against.
     */
    public List<FlagType> getFlagsForTile(Class<? extends TileEntity> te) {
        List<FlagType> flags = new ArrayList<FlagType>();
        for(SegmentTileEntity segment : segmentsTiles) {
            if(segment.getCheckClass().isAssignableFrom(te))
                flags.add(segment.getFlag());
        }
        return flags;
    }

    public EntityType getEntityType(Class<? extends Entity> entity) {
        for(SegmentEntity segment : segmentsEntities) {
            if (segment.getCheckClass().isAssignableFrom(entity)) {
                return segment.getType();
            }
        }
        return null;
    }

    public boolean isEntityProtected(Class<? extends Entity> entity) {
        EntityType type = getEntityType(entity);
        return type != null && type == EntityType.PROTECT;
    }

    /**
     * Returns whether or not the entity should be checked each tick.
     */
    public boolean isEntityTracked(Class<? extends Entity> entity) {
        EntityType type = getEntityType(entity);
        return type != null && (type == EntityType.TRACKED);
    }

    public boolean isEntityOwnable(Class<? extends Entity> entity) {
        for(SegmentEntity segment : segmentsEntities) {
            if(segment.getCheckClass().isAssignableFrom(entity) && segment.hasOwner())
                return true;
        }
        return false;
    }

    public boolean isTileTracked(Class<? extends TileEntity> te) {
        for(SegmentTileEntity segment : segmentsTiles) {
            if(segment.getCheckClass().isAssignableFrom(te))
                return true;
        }
        return false;
    }

    public boolean isBlockTracked(Class<? extends Block> block, int meta) {
        for(SegmentBlock segment : segmentsBlocks) {
            if(segment.getCheckClass().isAssignableFrom(block) &&( segment.getMeta() == -1 || segment.getMeta() == meta))
                return true;
        }
        return false;
    }

    public boolean isTileEntityOwnable(Class<? extends TileEntity> te) {
        for(SegmentTileEntity segment : segmentsTiles) {
            if(segment.getCheckClass().isAssignableFrom(te) && segment.hasOwner())
                return true;
        }
        return false;
    }

    public boolean canEntityTrespassPvp(Class<? extends Entity> entity) {
        for(SegmentEntity segment : segmentsEntities) {
            if(segment.getCheckClass().isAssignableFrom(entity) && segment.getType() == EntityType.PVP) {
                return true;
            }
        }
        return false;
    }

    /*  ---- Protection instance utilities ---- */

    private void disableSegment(Iterator<? extends Segment> it, Segment segment, String message) {
        it.remove();
        MyTown.instance.LOG.error(message);
        MyTown.instance.LOG.error("Disabling segment for " + segment.getCheckClass().getName() + " in protection " + this.modid + ".");
        MyTown.instance.LOG.info("Reload protections to enable it again.");
    }
    private void disable() {
        Protections.instance.removeProtection(this);
    }

    public static MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
}
