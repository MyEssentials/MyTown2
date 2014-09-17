package mytown.protection;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mytown.MyTown;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.block.ITileEntityProvider;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 9/2/2014.
 * Class handling all the protections
 */
public class Protections {
    public Map<String, Protection> protections;

    public Map<TileEntity, Boolean> checkedTileEntities;
    public Map<Entity, Boolean> checkedEntities;

    private int ticker = 20;
    private int tickStart = 20;

    public static Protections instance = new Protections();
    public Protections() {

        MyTown.instance.log.info("Protections initializing started...");
        protections = new HashMap<String, Protection>();
        checkedTileEntities = new HashMap<TileEntity, Boolean>();
        checkedEntities = new HashMap<Entity, Boolean>();
        addProtection(new VanillaProtection(), "");
    }

    public void addProtection(Protection prot, String modid) {
        protections.put(modid, prot);

        if(prot.isHandlingEvents) {
            MinecraftForge.EVENT_BUS.register(prot);
        }
    }

    private void addToBlockWhitelist(Class<? extends TileEntity> te, int dim, int x, int y, int z, Town town) {
        for(Protection prot : protections.values()) {
            if(prot.getFlagTypeForTile(te) != null)
                for(FlagType flagType : prot.getFlagTypeForTile(te)) {
                    if (flagType != null) {
                        MyTown.instance.log.info("Ooooh shiny! Found flag " + flagType.toString());
                        if (!town.hasBlockWhitelist(dim, x, y, z, flagType, 0)) {
                            BlockWhitelist bw = new BlockWhitelist(dim, x, y, z, flagType, 0);
                            DatasourceProxy.getDatasource().saveBlockWhitelist(bw, town);
                        } else {
                            MyTown.instance.log.info("Flag already is there.");
                        }
                    }
                }
        }
    }

    private void removeFromWhitelist(Class<? extends TileEntity> te, int dim, int x, int y, int z, Town town) {
        for(Protection prot : protections.values()) {
            if(prot.getFlagTypeForTile(te) != null)
                for(FlagType flagType : prot.getFlagTypeForTile(te)) {
                    if (flagType != null) {
                        if (town.hasBlockWhitelist(dim, x, y, z, flagType, 0)) {
                            DatasourceProxy.getDatasource().deleteBlockWhitelist(town.getBlockWhitelist(dim, x, y, z, flagType, 0), town);
                            MyTown.instance.log.info("Removed flag " + flagType + " from block!");
                        }
                    }
                }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent ev) {
        // Checking the checked maps (check-ception?)
        if(ticker == 0) {
            //MyTown.instance.log.info("Updating check maps.");
            for (Map.Entry<Entity, Boolean> entry : checkedEntities.entrySet()) {
                entry.setValue(false);
            }
            for(Map.Entry<TileEntity, Boolean> entry : checkedTileEntities.entrySet()) {
                entry.setValue(false);
            }
            ticker = MinecraftServer.getServer().worldServers.length * tickStart;
        } else {
            ticker--;
        }


        // Why does it return only a generic List? :S
        // TODO: Rethink this system a couple million times before you come up with the best algorithm :P
        for (Entity entity : (List<Entity>) ev.world.loadedEntityList) {

            for (Protection prot : protections.values()) {
                if (prot.hasToCheckEntity(entity)) {
                    if ((checkedEntities.get(entity) == null || !checkedEntities.get(entity)) && prot.checkEntity(entity)) {
                        if(!(entity instanceof EntityPlayer)) {
                            MyTown.instance.log.info("Entity " + entity.toString() + " was ATOMICALLY DISINTEGRATED!");
                            checkedEntities.remove(entity);
                            entity.setDead();
                        } else {
                            MyTown.instance.log.info("Player " + entity.toString() + " was respawned!");
                            checkedEntities.put(entity, false);
                        }
                    } else {
                        checkedEntities.put(entity, true);
                    }
                }
            }
        }
        for (TileEntity te : (List<TileEntity>) ev.world.loadedTileEntityList) {
            //MyTown.instance.log.info("Checking tile: " + te.toString());
            for (Protection prot : protections.values()) {
                // Prechecks go here
                if (prot.hasToCheckTileEntity(te) && !prot.checkForWhitelist(te)) {
                    // Checks go here
                    if((checkedTileEntities.get(te) == null || !checkedTileEntities.get(te)) && prot.checkTileEntity(te)) {
                        //ev.world.removeTileEntity(te.xCoord, te.yCoord, te.zCoord);
                        te.getBlockType().dropBlockAsItem(te.getWorldObj(), te.xCoord, te.yCoord, te.zCoord, 1, te.blockMetadata);
                        te.getWorldObj().setBlock(te.xCoord, te.yCoord, te.zCoord, Blocks.air);
                        MyTown.instance.log.info("TileEntity " + te.toString() + " was ATOMICALLY DISINTEGRATED!");
                    } else {
                        checkedTileEntities.put(te, true);
                    }
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttackEntityEvent(AttackEntityEvent ev) {
        // TODO: More wilderness goes here
        Block block = DatasourceProxy.getDatasource().getBlock(ev.target.dimension, ev.target.chunkCoordX, ev.target.chunkCoordZ);
        if(block != null) {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
            Town town = block.getTown();
            Flag<Boolean> attackFlag = town.getFlagAtCoords(ev.target.dimension, (int)ev.target.posX, (int)ev.target.posY, (int)ev.target.posZ, FlagType.attackEntities);
            if(!attackFlag.getValue()) {
                for (Protection prot : protections.values()) {
                    if (prot.protectedEntities.contains(ev.target.getClass())) {
                        // TODO: Check for permission instead
                        if (!DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer).hasTown(town)) {
                            ev.setCanceled(true);
                            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.protection.vanilla.animalCruelty"));
                        }
                    }
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (ev.entityPlayer.worldObj.isRemote)
            return;

        Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
        if(res == null) {
            return;
        }
        ItemStack currentStack = ev.entityPlayer.inventory.getCurrentItem();


        if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            // DEV STUFF
            TileEntity te23 = ev.world.getTileEntity(ev.x, ev.y, ev.z);
            if(te23 != null) {
                MyTown.instance.log.info("Player clicked: " + te23);
                MyTown.instance.log.info("Invalid:" + te23.isInvalid() + " Can update: " + te23.canUpdate() + " " + (te23.xCoord >> 4) + ", " + (te23.zCoord >> 4));
            }

            //System.out.println(currentStack.getItem().getUnlocalizedName());
            //System.out.println(Block.blocksList[ev.entityPlayer.worldObj.getBlockId(ev.x, ev.y, ev.z)].getUnlocalizedName());

            int x = ev.x, y = ev.y, z = ev.z; // Coords for the block that WILL be placed
            switch(ev.face)
            {
                case 0:
                    y--;
                    break;
                case 1:
                    y++;
                    break;
                case 2:
                    z--;
                    break;
                case 3:
                    z++;
                    break;
                case 4:
                    x--;
                    break;
                case 5:
                    x++;
                    break;
            }

            // In-Town specific interactions from here
            Block tblock = DatasourceProxy.getDatasource().getBlock(ev.entity.dimension, ev.x >> 4, ev.z >> 4);
            if(tblock == null)
                return;

            Plot plot = tblock.getTown().getPlotAtResident(res);
            TileEntity te = ev.entityPlayer.worldObj.getTileEntity(ev.x, ev.y, ev.z);

            //TODO: Verify properly
            // In case the player wants to access a block... checking if player is shifting too
            if(te != null && !(currentStack != null && currentStack.getItem() instanceof ItemBlock && ev.entityPlayer.isSneaking()) && !tblock.getTown().hasBlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.accessBlocks, 0)) {
                Flag<Boolean> accessFlag = tblock.getTown().getFlagAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.accessBlocks);

                // If it's a town whitelist then it's universal
                if(tblock.getTown().hasBlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.accessBlocks, 0))
                    return;

                // If it's a plot only whitelist then only residents of the same town can access
                Plot plotAtBlock = tblock.getTown().getPlotAtCoords(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord);
                if(plotAtBlock != null) {
                    if (tblock.getTown().hasBlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.accessBlocks, plotAtBlock.getDb_ID()) && res.hasTown(tblock.getTown()))
                        return;
                }
                // Checking if a player wants to access a block here
                //TODO: Check for permission instead
                if(!accessFlag.getValue() && !res.getTowns().contains(tblock.getTown())) {
                    res.sendMessage(accessFlag.flagType.getLocalizedProtectionDenial());
                    ev.setCanceled(true);
                    return;
                }
            }
            if(currentStack == null) {
                Flag<Boolean> activateFlag = tblock.getTown().getFlagAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.activateBlocks);
                //TODO: Check for permission

                // If it's a town whitelist then it's universal
                if(tblock.getTown().hasBlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.activateBlocks, 0))
                    return;

                Plot plotAtBlock = tblock.getTown().getPlotAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z);
                // If it's a plot only whitelist then only residents of the same town can access
                if(plotAtBlock != null) {
                    if (tblock.getTown().hasBlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.activateBlocks, plotAtBlock.getDb_ID()) && res.hasTown(tblock.getTown()))
                        return;
                }

                if(!activateFlag.getValue() && !res.hasTown(tblock.getTown()) && !tblock.getTown().hasBlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.activateBlocks, 0)) {
                    for(Protection prot : protections.values()) {
                        if(prot.activatedBlocks.contains(ev.world.getBlock(ev.x, ev.y, ev.z))) {
                            res.sendMessage(activateFlag.flagType.getLocalizedProtectionDenial());
                            ev.setCanceled(true);
                            return;
                        }
                    }
                }
            }
            if(currentStack != null && currentStack.getItem() instanceof ItemBlock) {
                Flag<Boolean> placeFlag = tblock.getTown().getFlagAtCoords(ev.world.provider.dimensionId, x, y, z, FlagType.placeBlocks);
                //TODO: Check for permission instead

                if(!placeFlag.getValue() && !res.hasTown(tblock.getTown())) {
                    res.sendMessage(placeFlag.flagType.getLocalizedProtectionDenial());
                    ev.setCanceled(true);
                    return;
                }
                MyTown.instance.log.info("Checking for te...");
                if(res.hasTown(tblock.getTown()) && ((ItemBlock)currentStack.getItem()).field_150939_a instanceof ITileEntityProvider) {
                    // Getting TileEntity TYPE by creating a tileentity from the itileentityprovider inside blockcontainer inside itemblock... lol
                    Class<? extends TileEntity> clsTe = ((ITileEntityProvider)((ItemBlock)currentStack.getItem()).field_150939_a).createNewTileEntity(ev.world, currentStack.getItemDamage()).getClass();
                    MyTown.instance.log.info("Found tile entity type from block, it's: " + clsTe.toString());
                    addToBlockWhitelist(clsTe, ev.world.provider.dimensionId, x, y, z, tblock.getTown());
                }
            }

            // if(te == null && currentStack != null && currentStack.getItem() instanceof ItemBlock)
            //    UniversalChecker.instance.addToChecklist(new ResidentBlockCoordsPair(x, y, z, ev.entityPlayer.dimension, getDatasource().getResident(ev.entityPlayer.username)));
        }
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        // TODO: Implement wilderness perms too
        Block block = DatasourceProxy.getDatasource().getBlock(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4);
        if (block != null) {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.getPlayer());
            Town town = block.getTown();
            Flag<Boolean> flag = town.getFlagAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.breakBlocks);
            if (flag == null)
                return;
            if (flag.getValue())
                return;
            // TODO: Instead, check for the permission at one point
            Plot plot = town.getPlotAtResident(res);

            // If it's a town whitelist then it's universal
            if(town.hasBlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.breakBlocks, 0))
                return;

            // If it's a plot only whitelist then only residents of the same town can access
            if(plot != null) {
                if(town.hasBlockWhitelist(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, FlagType.breakBlocks, plot.getDb_ID()) && res.hasTown(town))
                    return;
            }

            if(ev.block instanceof ITileEntityProvider)
                removeFromWhitelist(((ITileEntityProvider) ev.block).createNewTileEntity(ev.world, ev.blockMetadata).getClass(), ev.world.provider.dimensionId, ev.x, ev.y, ev.z, town);
            if (DatasourceProxy.getDatasource().getOrMakeResident(ev.getPlayer()).hasTown(town))
                return;
            res.sendMessage(flag.flagType.getLocalizedProtectionDenial());
            ev.setCanceled(true);
        }
    }


    /*
    // Pretty sure it's a Forge bug... need to update

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onItemUse(PlayerUseItemEvent ev) {
        Block block = DatasourceProxy.getDatasource().getBlock(ev.entityPlayer.dimension, ev.entityPlayer.chunkCoordX, ev.entityPlayer.chunkCoordZ);
        if(block != null) {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
            Town town = block.getTown();
            Flag<Boolean> useFlag = town.getFlagAtCoords(ev.entityPlayer.dimension, (int)ev.entityPlayer.posX, (int)ev.entityPlayer.posY, (int)ev.entityPlayer.posZ, "useItems");
            if(!useFlag.getValue()) {
                for(Protection protection : Protections.instance.protections) {
                    //TODO: Check for protection
                    if(protection.itemUsageProtection.contains(ev.item.getItem().getClass()) && !res.hasTown(town)) {
                        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.protection.vanilla.itemuse"));
                        ev.setCanceled(true);
                        return;
                    }
                }
            }
        }
    }
    */


    private int counter = 0;

    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onItemPickup(EntityItemPickupEvent ev) {
        Block block = DatasourceProxy.getDatasource().getBlock(ev.entityPlayer.dimension, ev.entityPlayer.chunkCoordX, ev.entityPlayer.chunkCoordZ);
        if(block != null) {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer);
            Town town = block.getTown();
            Flag<Boolean> pickupFlag = town.getFlagAtCoords(ev.entityPlayer.dimension, (int)ev.entityPlayer.posX, (int)ev.entityPlayer.posY, (int)ev.entityPlayer.posZ, FlagType.pickupItems);
            if(!pickupFlag.getValue()) {
                //TODO: Check for protection
                if(!res.hasTown(town)) {
                    //TODO: Maybe centralise this too
                    if(counter == 0) {
                        res.sendMessage(pickupFlag.flagType.getLocalizedProtectionDenial());
                        counter = 100;
                    } else
                      counter--;
                    ev.setCanceled(true);
                    return;
                }
            }
        }
    }

    /*
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onSpawn(EntityJoinWorldEvent ev) {
        if(DatasourceProxy.getDatasource() == null)
            return;
        Block block = DatasourceProxy.getDatasource().getBlock(ev.world.provider.dimensionId, ev.entity.chunkCoordX, ev.entity.chunkCoordZ);
        if(block == null)
            return;
        for(Protection prot : protections) {
            Flag<String> mobsFlag = block.getTown().getFlagAtCoords(ev.world.provider.dimensionId, (int) ev.entity.posX, (int) ev.entity.posY, (int) ev.entity.posZ, "mobs");
            if (mobsFlag.getValue().equals("all")) {
                if (!(ev.entity instanceof EntityPlayer)) {
                    ev.setCanceled(true);
                    return;
                }
            } else if (mobsFlag.getValue().equals("hostiles")) {
                if (prot.hostileEntities.contains(ev.entity.getClass())) {
                    ev.setCanceled(true);
                    return;
                }
            }
        }
    }
    */



}
