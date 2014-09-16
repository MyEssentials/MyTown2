package mytown.protection;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import ic2.api.energy.tile.IEnergyTile;
import ic2.api.event.LaserEvent;
import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.entities.Block;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 7/8/2014.
 *
 * IC2 mod implementation here
 */
public class IC2Protection extends Protection {

    private Class<? extends TileEntity> clsTileEntityElectricMachine;
    private Class<? extends TileEntity> clsTileEntityCable;
    private Class<? extends TileEntity> clsTileEntityBaseGenerator;


    @SuppressWarnings("unchecked")
    public IC2Protection() {
        MyTown.instance.log.info("Initializing IC2 protection...");
        isHandlingEvents = true;
        try {
            this.explosiveBlocks.add((Class<? extends Entity>)Class.forName("ic2.core.block.EntityNuke"));
            this.explosiveBlocks.add((Class<? extends Entity>)Class.forName("ic2.core.block.EntityItnt"));

            clsTileEntityBaseGenerator = (Class<? extends TileEntity>)Class.forName("ic2.core.block.generator.tileentity.TileEntityBaseGenerator");
            clsTileEntityElectricMachine = (Class<? extends TileEntity>)Class.forName("ic2.core.block.machine.tileentity.TileEntityElectricMachine");
            clsTileEntityCable = (Class<? extends TileEntity>)Class.forName("ic2.core.block.wiring.TileEntityCable");

            this.trackedTileEntities.add(clsTileEntityBaseGenerator);
            this.trackedTileEntities.add(clsTileEntityCable);
            this.trackedTileEntities.add(clsTileEntityElectricMachine);
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }


    @SuppressWarnings("unchecked")
    @Override
    public boolean checkEntity(Entity entity) {

        MyTown.instance.log.info("Checking entity: " + entity.toString());

        if(explosiveBlocks.contains(entity.getClass())) {
            return true;
        }

        /*
        if(clsEntityLaser.isInstance(entity)) {
            MyTown.instance.log.info("Laser found!");
            Town town = getTownFromEntity(entity);
            Flag<Boolean> breakFlag = town.getFlagAtCoords(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ, "breakBlocks");
            if(!breakFlag.getValue()) {
                return true;
            }
        }
        */

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkTileEntity(TileEntity te) {
        //MyTown.instance.log.info("It's alive!" + te);
        //FIXME: This doesn't work for some reason

        List<TileEntity> nearbyTiles = new ArrayList<TileEntity>();
        if(clsTileEntityBaseGenerator.isAssignableFrom(te.getClass())) {
            // If generator, then check for cable or machine
            nearbyTiles.addAll(Utils.getNearbyTileEntity(te, clsTileEntityCable));
            nearbyTiles.addAll(Utils.getNearbyTileEntity(te, clsTileEntityElectricMachine));
        } else if(clsTileEntityCable.isAssignableFrom(te.getClass())) {
            // If cable then check for cable, machine or generator
            nearbyTiles.addAll(Utils.getNearbyTileEntity(te, clsTileEntityCable));
            nearbyTiles.addAll(Utils.getNearbyTileEntity(te, clsTileEntityElectricMachine));
            nearbyTiles.addAll(Utils.getNearbyTileEntity(te, clsTileEntityBaseGenerator));
        } else if(clsTileEntityElectricMachine.isAssignableFrom(te.getClass())) {
            // If machine then check for generator or cable
            nearbyTiles.addAll(Utils.getNearbyTileEntity(te, clsTileEntityBaseGenerator));
            nearbyTiles.addAll(Utils.getNearbyTileEntity(te, clsTileEntityCable));
        }

        for(TileEntity tile : nearbyTiles) {
            Town town = Utils.getTownAtPosition(tile.getWorldObj().provider.dimensionId, tile.xCoord >> 4, tile.zCoord >> 4);
            if(town != null) {
                Flag<Boolean> energyFlag = town.getFlagAtCoords(tile.getWorldObj().provider.dimensionId, tile.xCoord, tile.yCoord, tile.zCoord, FlagType.ic2EnergyFlow);
                if(!energyFlag.getValue()) {

                    Town townAtEntity = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, te.xCoord >> 4, te.zCoord >> 4);
                    if(townAtEntity != null && town == townAtEntity) {
                        Plot plot1 = town.getPlotAtCoords(tile.getWorldObj().provider.dimensionId, tile.xCoord, tile.yCoord, tile.zCoord);
                        Plot plot2 = townAtEntity.getPlotAtCoords(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord);
                        if(plot1 != plot2) {
                            // If 2 different plots on the same town then we invalidate
                            MyTown.instance.log.info("TileEntity " + tile + " has been disabled, because it's too close to town " + town.getName());
                            town.notifyEveryone(getLocal().getLocalization("mytown.protection.ic2.energy"));
                            return true;
                        }
                    } else {
                        // If wild near town or town near other town then we invalidate
                        MyTown.instance.log.info("TileEntity " + tile + " has been disabled, because it's too close to town " + town.getName());
                        town.notifyEveryone(getLocal().getLocalization("mytown.protection.ic2.energy"));
                        return true;
                    }
                }
            }
        }

        return false;
    }



    // EVENTS
    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onLaserBreak(LaserEvent.LaserHitsBlockEvent ev) {
        MyTown.instance.log.info("Detected laser break.");
        Block tblock = DatasourceProxy.getDatasource().getBlock(ev.owner.dimension, ev.x >> 4, ev.z >> 4);
        if(tblock == null)
            return;
        MyTown.instance.log.info("Block is not null, checking...");

        Flag<Boolean> breakFlag = tblock.getTown().getFlagAtCoords(ev.owner.dimension, ev.x, ev.y, ev.z, FlagType.breakBlocks);
        if(!breakFlag.getValue()) {
            if(ev.owner instanceof EntityPlayer) {
                MyTown.instance.log.info("Found things and stuff...");
                Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.owner);
                //TODO: Check for permission node
                if(!res.getTowns().contains(tblock.getTown())) {
                    ev.setCanceled(true);
                    ev.lasershot.setDead();
                }
            } else {
                ev.setCanceled(true);
                ev.lasershot.setDead();
            }
        }
    }


    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onLaserExplodes(LaserEvent.LaserExplodesEvent ev) {
        MyTown.instance.log.info("Detected explosion.");
        Block tblock = DatasourceProxy.getDatasource().getBlock(ev.owner.dimension, ev.lasershot.chunkCoordX, ev.lasershot.chunkCoordZ);
        if(tblock == null)
            return;
        Flag<Boolean> breakFlag = tblock.getTown().getFlagAtCoords(ev.lasershot.dimension, (int) ev.lasershot.posX, (int) ev.lasershot.posY, (int) Math.floor(ev.lasershot.posZ), FlagType.breakBlocks);
        if(!breakFlag.getValue()) {
            if(ev.owner instanceof EntityPlayer) {
                Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.owner);
                if(res == null || !res.getTowns().contains(tblock.getTown())) {
                    ev.setCanceled(true);
                }
            } else {
                ev.setCanceled(true);
            }
        }
    }


    /*
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent ev) {

        if (ev.entityPlayer.worldObj.isRemote)
            return;

        Resident res = DatasourceProxy.getDatasource().getResident(ev.entityPlayer.username);
        if (res == null) {
            return;
        }
        ItemStack currentStack = ev.entityPlayer.inventory.getCurrentItem();

        if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            int x = ev.x, y = ev.y, z = ev.z; // Coords for the block that WILL be placed
            switch (ev.face) {
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
            //currentStack.

            if(currentStack.getItem() instanceof IEnergyTile)
                System.out.println("This is TileEntity check working");
            // IC2 Power grid protection
            //System.out.println(clsItemBlockIC2.isAssignableFrom(currentStack.getItem().getClass()));
            //System.out.println(currentStack.getItem().getClass().toString());
            //System.out.println(clsItemBlockIC2.toString());

            if (currentStack != null && (clsItemBlockIC2.isAssignableFrom(currentStack.getItem().getClass()) || clsItemCable.isAssignableFrom(currentStack.getItem().getClass()))
                    && MyTown.instance.isModuleEnabled(IC2Module.ModID)) {
                int[] dx = {1, -1, 0, 0};
                int[] dz = {0, 0, 1, -1};

                for (int i = 0; i < 4; i++) {
                    TownBlock tblock = DatasourceProxy.getDatasource().getTownBlock(ev.entityPlayer.dimension, x + dx[i], z + dz[i], false);
                    if (tblock == null || res.getTowns().contains(tblock.getTown()) || !tblock.getTown().getFlagAtCoords(x + dx[i], y, z + dz[i], "ic2").getValue())
                        continue;
                    TileEntity te = ev.entityPlayer.worldObj.getBlockTileEntity(x + dx[i], y, z + dz[i]);
                    if (te == null) {
                        continue;
                    }
                    if (te instanceof IEnergyTile) {
                        ChatUtils.sendLocalizedChat(ev.entityPlayer, LocalizationProxy.getLocalization(), "mytown.protection.ic2.grid");
                        ev.setCanceled(true);
                        return;
                    }
                }
            }
        } else if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            String itemName = currentStack.getUnlocalizedName();
            TownBlock tblock = DatasourceProxy.getDatasource().getTownBlock(ev.entity.dimension, ((int) ev.entity.posX), (int) Math.floor(ev.entity.posZ), false);

            // Math.floor() is needed for some reason... TODO: Find a better way maybe?

            if (tblock == null) {
                return;
            }

            if (!tblock.getTown().getFlagAtCoords(((int) ev.entity.posX), ((int) ev.entity.posY), ((int) Math.floor(ev.entity.posZ)), "useItems").getValue() && !res.getTowns().contains(tblock.getTown())) {
                if (itemName.equals("ic2.itemToolMiningLaser")) {// I don't think there's a better way of doing this :P
                    ev.setCanceled(true);
                    return;
                }
            }
        }
    }
    */
}


