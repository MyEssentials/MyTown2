package mytown.modules;

import mytown.Constants;
import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.town.Town;
import mytown.interfaces.ITownFlag;
import mytown.modules.IC2Module;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.block.Block;
import net.minecraft.block.BlockPistonBase;
import net.minecraft.block.BlockPistonExtension;
import net.minecraft.block.BlockPistonMoving;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityBrewingStand;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.world.World;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.world.BlockEvent;

/**
 * Created by AfterWind on 7/8/2014.
 * Vanilla event handlers
 * Although this includes only vanilla EVENTS it may have some implementation for other mods
 */
public class VanillaModule extends ModuleBase {

    public static final String ModID = "Vanilla";

    @Override
    public void load() {

    }

    @Override
    public String getModID() {
        return ModID;
    }


    // TODO: Make proper functions for all this mess...
    @Override
    public boolean check(ResidentBlockCoordsPair blockPair) {
        Resident resident = blockPair.owner;
        TownBlock tblock = getDatasource().getTownBlock(blockPair.dim, blockPair.x, blockPair.z, false);
        World world = MinecraftServer.getServer().worldServerForDimension(blockPair.dim);
        Block block = Block.blocksList[world.getBlockId(blockPair.x, blockPair.y, blockPair.z)];

        if(block == null)
            return true;

        if(block instanceof BlockPistonBase || block instanceof BlockPistonExtension) {
            int[] dx = {0, 1, 0, -1};
            int[] dz = {1, 0, -1, 0};

            for(int i = 0; i < 4; i++) {
                TownBlock tblockPiston = getDatasource().getTownBlock(blockPair.dim, blockPair.x + dx[i], blockPair.z + dz[i], false);
                if(tblockPiston == null)
                    continue;
                if(!resident.getTowns().contains(tblockPiston.getTown()) && !tblockPiston.getTown().getFlagAtCoords(blockPair.x, blockPair.y, blockPair.z, "build").getValue()) {
                    world.destroyBlock(blockPair.x, blockPair.y, blockPair.z, false);
                    resident.getPlayer().inventory.addItemStackToInventory(new ItemStack(block.getPickBlock(null, world, blockPair.x, blockPair.y, blockPair.z).itemID, 1, world.getBlockMetadata(blockPair.x, blockPair.y, blockPair.z)));
                    resident.sendLocalizedMessage(getLocal(), "mytown.protection.vanilla.placeBlocks");
                    return false;
                }
            }
        }

        if(tblock == null)
            return true;



        if(!resident.getTowns().contains(tblock.getTown()) && !tblock.getTown().getFlagAtCoords(blockPair.x, blockPair.y, blockPair.z, "build").getValue()) {
            world.destroyBlock(blockPair.x, blockPair.y, blockPair.z, false);
            resident.getPlayer().inventory.addItemStackToInventory(new ItemStack(block.getPickBlock(null, world, blockPair.x, blockPair.y, blockPair.z).itemID, 1, world.getBlockMetadata(blockPair.x, blockPair.y, blockPair.z)));
            resident.sendLocalizedMessage(getLocal(), "mytown.protection.vanilla.placeBlocks");
            return false;
        }



        return true;
    }

    @Override
    public boolean check(TileEntity te, Resident resident) {
        return true;
    }

    @Override
    public boolean isEntityInstance(TileEntity te) {
        return false;
    }

    // EVENTS

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
            TownBlock tblock = DatasourceProxy.getDatasource().getTownBlock(ev.entity.dimension, ev.x, ev.z, false);

            // Checking for the selector item
            if (currentStack != null && tblock != null && currentStack.getItem().equals(Item.hoeWood) && currentStack.getDisplayName().equals(Constants.EDIT_TOOL_NAME)) {

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
            TileEntity te = ev.entityPlayer.worldObj.getBlockTileEntity(ev.x, ev.y, ev.z);

            if(currentStack != null && te != null && tblock != null) {
                // Checking if a player wants to access a block here
                if(!tblock.getTown().getFlagAtCoords(ev.x, ev.y, ev.z, "accessBlocks").getValue() && !res.getTowns().contains(tblock.getTown())) {
                    ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.protection.vanilla.access");
                    ev.setCanceled(true);
                    return;
                }
            }
            if(te == null && currentStack != null && currentStack.getItem() instanceof ItemBlock)
                UniversalChecker.instance.addToChecklist(new ResidentBlockCoordsPair(x, y, z, ev.entityPlayer.dimension, getDatasource().getResident(ev.entityPlayer.username)));
        }

    }


}
