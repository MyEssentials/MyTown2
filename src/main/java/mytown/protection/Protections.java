package mytown.protection;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import mytown.MyTown;
import mytown.core.Localization;
import mytown.entities.Block;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.BlockEvent;

import javax.xml.crypto.Data;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 9/2/2014.
 * Class handling all the protections
 */
public class Protections {
    public List<Protection> protections;


    public static Protections instance = new Protections();
    public Protections() {

        MyTown.instance.log.info("Protections initializing started...");
        protections = new ArrayList<Protection>();
        protections.add(new VanillaProtection());

        for(Protection prot : protections) {
            if(prot.isHandlingEvents) {
                MinecraftForge.EVENT_BUS.register(prot);
            }
        }
    }
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void tick(TickEvent.WorldTickEvent ev) {
        // Why does it return only a generic List? :S
        for (Entity entity : (List<Entity>) ev.world.loadedEntityList) {
            for (Protection prot : protections) {
                if (prot.checkEntity(entity)) {
                    MyTown.instance.log.info("Entity " + entity.toString() + " was ATOMICALLY DISINTEGRATED!");
                }
            }
        }
        for (TileEntity te : (List<TileEntity>) ev.world.loadedTileEntityList) {
            for (Protection prot : protections) {
                if (prot.checkTileEntity(te)) {
                    MyTown.instance.log.info("TileEntity " + te.toString() + " was ATOMICALLY DISINTEGRATED!");
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
            Flag<Boolean> attackFlag = town.getFlagAtCoords(ev.target.dimension, (int)ev.target.posX, (int)ev.target.posY, (int)ev.target.posZ, "attackEntities");
            if(!attackFlag.getValue()) {
                for (Protection prot : protections) {
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

            TileEntity te = ev.entityPlayer.worldObj.getTileEntity(ev.x, ev.y, ev.z);

            // In case the player wants to access a block... checking if player is shifting too
            if(te != null && !(currentStack != null && currentStack.getItem() instanceof ItemBlock && ev.entityPlayer.isSneaking())) {
                Flag<Boolean> accessFlag = tblock.getTown().getFlagAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, "accessBlocks");

                // Checking if a player wants to access a block here
                //TODO: Check for permission instead
                if(!accessFlag.getValue() && !res.getTowns().contains(tblock.getTown())) {
                    res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.protection.vanilla.access"));
                    ev.setCanceled(true);
                    return;
                }
            }
            if(currentStack == null) {
                Flag<Boolean> activateFlag = tblock.getTown().getFlagAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, "activateBlocks");
                //TODO: Check for permission
                if(!activateFlag.getValue() && !res.hasTown(tblock.getTown())) {
                    for(Protection prot : protections) {
                        if(prot.activatedBlocks.contains(ev.world.getBlock(ev.x, ev.y, ev.z))) {
                            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.protection.vanilla.activateBlock"));
                            ev.setCanceled(true);
                            return;
                        }
                    }
                }
            }
            if(currentStack != null && currentStack.getItem() instanceof ItemBlock) {
                Flag<Boolean> placeFlag = tblock.getTown().getFlagAtCoords(ev.world.provider.dimensionId, x, y, z, "placeBlocks");
                //TODO: Check for permission instead
                if(!placeFlag.getValue() && !res.getTowns().contains(tblock.getTown())) {
                    res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.protection.vanilla.place"));
                    ev.setCanceled(true);
                    return;
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
            Flag<Boolean> flag = town.getFlagAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, "breakBlocks");
            if (flag == null)
                return;
            if (flag.getValue())
                return;
            // TODO: Instead, check for the permission at one point
            if (DatasourceProxy.getDatasource().getOrMakeResident(ev.getPlayer()).hasTown(town))
                return;
            res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.protection.vanilla.break"));
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
            Flag<Boolean> pickupFlag = town.getFlagAtCoords(ev.entityPlayer.dimension, (int)ev.entityPlayer.posX, (int)ev.entityPlayer.posY, (int)ev.entityPlayer.posZ, "pickupItems");
            if(!pickupFlag.getValue()) {
                //TODO: Check for protection
                if(!res.hasTown(town)) {
                    //TODO: Maybe centralise this too
                    if(counter == 0) {
                        res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.protection.vanilla.pickup"));
                        counter = 100;
                    } else
                      counter--;
                    ev.setCanceled(true);
                    return;
                }
            }
        }
    }



}
