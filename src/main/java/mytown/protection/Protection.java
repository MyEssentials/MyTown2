package mytown.protection;

import cpw.mods.fml.common.FMLCommonHandler;
import myessentials.entities.BlockPos;
import myessentials.entities.Volume;
import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.entities.flag.FlagType;
import mytown.protection.segment.*;
import mytown.protection.segment.enums.BlockType;
import mytown.protection.segment.enums.EntityType;
import mytown.protection.segment.enums.ItemType;
import mytown.proxies.DatasourceProxy;
import mytown.util.exceptions.ConditionException;
import mytown.util.exceptions.GetterException;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S23PacketBlockChange;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
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
                        Volume teBox = new Volume(segment.getX1(te), segment.getY1(te), segment.getZ1(te), segment.getX2(te), segment.getY2(te), segment.getZ2(te));
                        int dim = te.getWorldObj().provider.dimensionId;
                        Resident owner = segment.hasOwner() ? Protections.instance.getOwnerForTileEntity(te) : null;
                        if (!hasPermission(owner, segment, dim, teBox)) {
                            return true;
                        }
                    }
                } catch (Exception ex) {
                    MyTown.instance.LOG.error("Failed to check tile entity: {} ({}, {}, {}, Dim: {})", te.getClass().getSimpleName(), te.xCoord, te.yCoord, te.zCoord, te.getWorldObj().provider.dimensionId);
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
                        Resident owner = segment.getOwner(entity);
                        int dim = entity.dimension;
                        int x = (int) Math.floor(entity.posX);
                        int y = (int) Math.floor(entity.posY);
                        int z = (int) Math.floor(entity.posZ);

                        if(range == 0) {
                            if (!hasPermission(owner, segment, dim, x, y, z)) {
                                return true;
                            }
                        } else {                            
                            Volume rangeBox = new Volume(x-range, y-range, z-range, x+range, y+range, z+range);
                            if (!hasPermission(owner, segment, dim, rangeBox)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Checking item usage for left or right click on block
     */
    public boolean checkItem(ItemStack item, ItemType type, Resident res, BlockPos bp, int face) {

        for(Iterator<SegmentItem> it = segmentsItems.iterator(); it.hasNext();) {
            SegmentItem segment = it.next();
            if(segment.getType() == type && segment.getCheckClass().isAssignableFrom(item.getItem().getClass())) {
                ForgeDirection direction = ForgeDirection.getOrientation(face);
                if(segment.isOnAdjacent()) {
                    bp = new BlockPos(bp.getX() + direction.offsetX, bp.getY() + direction.offsetY, bp.getZ() + direction.offsetZ, bp.getDim());
                }
                if (!segment.isDirectionalClientUpdate()) {
                    direction = null;
                }
                try {
                    if (segment.checkCondition(item)) {
                        int range = segment.getRange(item);
                        int dim = bp.getDim();
                        int x = bp.getX();
                        int y = bp.getY();
                        int z = bp.getZ();
                        boolean isProtected;

                        if(range == 0) {
                            isProtected = !hasPermission(res, segment, dim, x, y, z);
                        } else {                            
                            Volume rangeBox = new Volume(x-range, y-range, z-range, x+range, y+range, z+range);
                            isProtected = !hasPermission(res, segment, dim, rangeBox);
                        }
                        
                        if(isProtected) {
                            if (segment.hasClientUpdate()) {
                                sendClientUpdate(segment.getClientUpdateCoords(), bp, (EntityPlayerMP) res.getPlayer(), direction);
                            }
                            return true;
                        }
                    }
                } catch (Exception ex) {
                    MyTown.instance.LOG.error("Failed to check item use on {} at the player {} ({})", item.getDisplayName(), res.getPlayerName(), bp);
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
                int dim = entity.dimension;
                int x = (int) Math.floor(entity.posX);
                int y = (int) Math.floor(entity.posY);
                int z = (int) Math.floor(entity.posZ);
                
                if (!hasPermission(res, segment, dim, x, y, z)) {
                    return true;
                }
            }
        }

        if(item == null)
            return false;

        for(Iterator<SegmentItem> it = segmentsItems.iterator(); it.hasNext();) {
            SegmentItem segment = it.next();
            if(segment.getType() == ItemType.RIGHT_CLICK_ENTITY && segment.getCheckClass().isAssignableFrom(item.getItem().getClass())) {
                try {
                    if (segment.checkCondition(item)) {
                        int range = segment.getRange(item);
                        int dim = entity.dimension;
                        int x = (int) Math.floor(entity.posX);
                        int y = (int) Math.floor(entity.posY);
                        int z = (int) Math.floor(entity.posZ);

                        if(range == 0) {
                            if (!hasPermission(res, segment, dim, x, y, z)) {
                                return true;
                            }
                        } else {                            
                            Volume rangeBox = new Volume(x-range, y-range, z-range, x+range, y+range, z+range);
                            if (!hasPermission(res, segment, dim, rangeBox)) {
                                return true;
                            }
                        }
                    }
                } catch (Exception ex) {
                    MyTown.instance.LOG.error("Failed to check item use on {} at the player {} ({}, {}, {} | Dim: {})", item.getDisplayName(), res.getPlayerName(), entity.posX, entity.posY, entity.posZ, entity.dimension);
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
                        int dim = entity.dimension;
                        int x = (int) Math.floor(entity.posX);
                        int y = (int) Math.floor(entity.posY);
                        int z = (int) Math.floor(entity.posZ);

                        if(range == 0) {
                            if (!hasPermission(res, segment, dim, x, y, z)) {
                                return true;
                            }
                        } else {                            
                            Volume rangeBox = new Volume(x-range, y-range, z-range, x+range, y+range, z+range);
                            if (!hasPermission(res, segment, dim, rangeBox)) {
                                return true;
                            }
                        }
                    }
                } catch (Exception ex) {
                    MyTown.instance.LOG.error("Failed to check item use on {} at the player {} ({}, {}, {} | Dim: {})", item.getDisplayName(), res.getPlayerName(), entity.posX, entity.posY, entity.posZ, entity.dimension);

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
        Block blockType = MinecraftServer.getServer().worldServerForDimension(bp.getDim()).getBlock(bp.getX(), bp.getY(), bp.getZ());
        for(SegmentBlock segment : segmentsBlocks) {
            if(segment.getCheckClass().isAssignableFrom(blockType.getClass())
                    && (segment.getMeta() == -1 || segment.getMeta() == MinecraftServer.getServer().worldServerForDimension(bp.getDim()).getBlockMetadata(bp.getX(), bp.getY(), bp.getZ()))
                    && (segment.getType() == BlockType.ANY_CLICK || segment.getType() == BlockType.RIGHT_CLICK && action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK || segment.getType() == BlockType.LEFT_CLICK && action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK)) {
                int dim = bp.getDim();
                int x = bp.getX();
                int y = bp.getY();
                int z = bp.getZ();
                
                if (!hasPermission(res, segment, dim, x, y, z)) {
                    if(segment.hasClientUpdate())
                        sendClientUpdate(segment.getClientUpdateCoords(), bp, (EntityPlayerMP) res.getPlayer(), null);
                    return true;
                }
            }
        }

        return false;
    }

    public void sendClientUpdate(Volume updateVolume, BlockPos center, EntityPlayerMP player, ForgeDirection face) {
        World world = MinecraftServer.getServer().worldServerForDimension(center.getDim());
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

    public boolean hasPermission(Resident res, Segment segment, int dim, int x, int y, int z) {
        TownBlock townBlock = MyTownUniverse.instance.blocks.get(dim, x >> 4, z >> 4);
        if(townBlock == null) {
            if (res == null) {
                return !Wild.instance.flagsContainer.getValue(segment.getFlag()).equals(segment.getDenialValue());
            } else {
                if (!Wild.instance.hasPermission(res, segment.getFlag(), segment.getDenialValue())) {
                    res.sendMessage(segment.getFlag().getLocalizedProtectionDenial());
                    return false;
                }
            }
        } else {
            Town town = townBlock.getTown();
            if (res == null) {
                return !town.getValueAtCoords(dim, x, y, z, segment.getFlag()).equals(segment.getDenialValue());
            } else {
                if (!town.hasPermission(res, segment.getFlag(), segment.getDenialValue(), dim, x, y, z)) {
                    res.protectionDenial(segment.getFlag(), town.formatOwners(dim, x, y, z));
                    return false;
                }
            }
        }
        return true;
    }

    public boolean hasPermission(Resident res, Segment segment, int dim, Volume area) {
        boolean inWild = false;

        for (int townBlockX = area.getMinX() >> 4; townBlockX <= area.getMaxX() >> 4; townBlockX++) {
            for (int townBlockZ = area.getMinZ() >> 4; townBlockZ <= area.getMaxZ() >> 4; townBlockZ++) {
                TownBlock townBlock = MyTownUniverse.instance.blocks.get(dim, townBlockX, townBlockZ);
                
                if (townBlock == null) {
                    inWild = true;
                } else {
                    Town town = townBlock.getTown();
                    Volume rangeBox = townBlock.getAreaLimit(area);
                    int totalIntersectArea = 0;

                    // Check every plot in the current TownBlock and sum all plot areas
                    for (Plot plot : townBlock.plotsContainer) {
                        int plotIntersectArea = plot.getIntersectingArea(rangeBox);
                        if (plotIntersectArea > 0) {
                            if (res == null) {
                                if (plot.flagsContainer.getValue(segment.getFlag()).equals(segment.getDenialValue())) {
                                    return false;
                                }
                            } else {
                                if (!plot.hasPermission(res, segment.getFlag(), segment.getDenialValue())) {
                                    res.protectionDenial(segment.getFlag(), MyTown.instance.LOCAL.getLocalization("mytown.notification.town.owners", town.residentsMap.getMayor() == null ? "SERVER ADMINS" : town.residentsMap.getMayor().getPlayerName()));
                                    return false;
                                }
                            }
                        }
                        totalIntersectArea += plotIntersectArea;
                    }

                    // If plot area sum is not equal to range area, check town permission
                    if (totalIntersectArea != getArea(rangeBox)) {
                        if (res == null) {
                            if (town.flagsContainer.getValue(segment.getFlag()).equals(segment.getDenialValue())) {
                                return false;
                            }
                        } else {
                            if (!town.hasPermission(res, segment.getFlag(), segment.getDenialValue())) {
                                res.protectionDenial(segment.getFlag(), MyTown.instance.LOCAL.getLocalization("mytown.notification.town.owners", town.residentsMap.getMayor() == null ? "SERVER ADMINS" : town.residentsMap.getMayor().getPlayerName()));
                                return false;
                            }
                        }
                    }
                }
            }
        }

        if (inWild) {
            if (res == null) {
                if (Wild.instance.flagsContainer.getValue(segment.getFlag()).equals(segment.getDenialValue())) {
                    return false;
                }
            } else {
                if (!Wild.instance.hasPermission(res, segment.getFlag(), segment.getDenialValue())) {
                    res.sendMessage(segment.getFlag().getLocalizedProtectionDenial());
                    return false;
                }
            }
        }
        
        return true;
    }

    public int getArea(Volume rangeBox) {
        return ((rangeBox.getMaxX() - rangeBox.getMinX()) + 1) *
               ((rangeBox.getMaxY() - rangeBox.getMinY()) + 1) *
               ((rangeBox.getMaxZ() - rangeBox.getMinZ()) + 1);
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
        MyTown.instance.LOG.error("Disabling segment for {} in protection {}.", segment.getCheckClass().getName(), this.modid);
        MyTown.instance.LOG.info("Reload protections to enable it again.");
    }
    private void disable() {
        Protections.instance.removeProtection(this);
    }

    public static MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
}
