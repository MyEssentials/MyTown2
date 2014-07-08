package mytown.handler;

import mytown.Constants;
import mytown.core.ChatUtils;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.town.Town;
import mytown.interfaces.ITownFlag;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

/**
 * Created by AfterWind on 7/8/2014.
 * Vanilla event handlers
 */
public class VanillaEventHandler {

    @ForgeSubscribe
    public void onEnterChunk(EntityEvent.EnteringChunk ev) {
        if (!(ev.entity instanceof EntityPlayer))
            return;
        if (ev.entity.worldObj.isRemote)
            return; // So that it's not called twice :P
        EntityPlayer pl = (EntityPlayer) ev.entity;
        try {
            Resident res = DatasourceProxy.getDatasource().getOrMakeResident(pl);
            res.checkLocation(ev.oldChunkX, ev.oldChunkZ, ev.newChunkX, ev.newChunkZ, pl.dimension);
            if (res.isMapOn()) {
                res.sendMap();
            }
        } catch (Exception e) {
            e.printStackTrace(); // TODO Change?
        }
    }

    @ForgeSubscribe
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        // TODO: Implement wilderness perms too
        if (!DatasourceProxy.getDatasource().hasTownBlock(String.format(TownBlock.keyFormat, ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4)))
            return;
        else {
            Town town = DatasourceProxy.getDatasource().getTownBlock(String.format(TownBlock.keyFormat, ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4)).getTown();
            ITownFlag flag = town.getFlagAtCoords(ev.x, ev.y, ev.z, "breakBlocks");
            if (flag == null)
                return;
            if (flag.getValue())
                return;
            if (DatasourceProxy.getDatasource().getResident(ev.getPlayer().username).isPartOfTown(town))
                return;
            ev.setCanceled(true);
        }
    }

    @ForgeSubscribe
    public void onItemPickup(EntityItemPickupEvent ev) {
        Resident res = DatasourceProxy.getDatasource().getResident(ev.entityPlayer.username);
        TownBlock townBlock = DatasourceProxy.getDatasource().getTownBlock(ev.entityPlayer.dimension, ev.item.chunkCoordX, ev.item.chunkCoordZ, true);
        if(townBlock == null)
            return;
        if(res == null)
            return;
        if(!townBlock.getTown().getFlagAtCoords((int)ev.item.posX, ((int) ev.item.posY), ((int) ev.item.posZ), "pickup").getValue() &&
               res.getTowns().contains(townBlock.getTown()) ) {
            ev.setCanceled(true);
            return;
        }
    }


    @ForgeSubscribe
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (ev.entityPlayer.worldObj.isRemote)
            return;

        Resident res = DatasourceProxy.getDatasource().getResident(ev.entityPlayer.username);
        if(res == null) {
            return;
        }
        ItemStack currentStack = ev.entityPlayer.inventory.getCurrentItem();
        if (currentStack == null) {
            return;
        }

        if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {

            // In-Town specific interactions from here
            TownBlock tblock = DatasourceProxy.getDatasource().getTownBlock(ev.entity.dimension, ev.x, ev.z, false);
            if(tblock == null) {
                return;
            }
            if(currentStack.getItem() instanceof ItemBlock) {
                // Checking if player wants to use an item here
                if(!tblock.getTown().getFlagAtCoords(ev.x, ev.y, ev.z, "useItems").getValue() && !res.getTowns().contains(tblock.getTown())) {
                    ev.setCanceled(true);
                    return;
                }
            } else {
                // Checking if a player wants to access a block here
                if(!tblock.getTown().getFlagAtCoords(ev.x, ev.y, ev.z, "accessBlocks").getValue() && !res.getTowns().contains(tblock.getTown())) {
                    ev.setCanceled(true);
                    return;
                }
            }



            // Checking for the selector item
            if (currentStack.getItem().equals(Item.hoeWood) && currentStack.getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {

                if (res.isFirstPlotSelectionActive() && res.isSecondPlotSelectionActive()) {
                    ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.cmd.err.plot.alreadySelected");
                } else {
                    boolean result = res.selectBlockForPlot(ev.entityPlayer.dimension, ev.x, ev.y, ev.z);
                    if (result) {
                        if (!res.isSecondPlotSelectionActive()) {
                            ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.notification.town.plot.selectionStart");
                        } else {
                            ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.notification.town.plot.selectionEnd");
                        }
                    } else
                        ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.cmd.err.plot.selectionFailed");

                }
                System.out.println(String.format("Player has selected: %s;%s;%s", ev.x, ev.y, ev.z));
            }
        } else if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            String itemName = currentStack.getUnlocalizedName();
            TownBlock tblock = DatasourceProxy.getDatasource().getTownBlock(ev.entity.dimension, ((int) ev.entity.posX),(int)Math.floor(ev.entity.posZ), false);

            // Math.floor() is needed for some reason... TODO: Find a better way maybe?

            if(tblock == null) {
                return;
            }

            if(!tblock.getTown().getFlagAtCoords(((int) ev.entity.posX), ((int) ev.entity.posY), ((int) Math.floor(ev.entity.posZ)), "useItems").getValue() && !res.getTowns().contains(tblock.getTown())) {
                if(itemName.equals("ic2.itemToolMiningLaser")) {// I don't think there's a better way of doing this :P
                    ev.setCanceled(true);
                    return;
                }
            }
            

        }
    }


}
