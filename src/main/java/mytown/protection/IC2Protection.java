package mytown.protection;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.Optional;
import mytown.MyTown;
import mytown.entities.Block;
import mytown.entities.Resident;
import mytown.entities.flag.Flag;
import mytown.proxies.DatasourceProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Created by AfterWind on 7/8/2014.
 * <p/>
 * IC2 mod implementation here
 */
public class IC2Protection extends Protection {
    public static final String ModID = "IC2";

    private Class<?> clsItemCable;
    private Class<?> clsItemBlockIC2;

    @SuppressWarnings("unchecked")
    public IC2Protection() {
        MyTown.instance.log.info("Initializing IC2 protection...");
        isHandlingEvents = true;
        try {
            this.explosiveBlocks.add((Class<? extends Entity>) Class.forName("ic2.core.block.EntityNuke"));
            this.explosiveBlocks.add((Class<? extends Entity>) Class.forName("ic2.core.block.EntityItnt"));
            clsItemCable = Class.forName("ic2.core.item.block.ItemCable");
            clsItemBlockIC2 = Class.forName("ic2.core.item.block.ItemBlockIC2");
        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkEntity(Entity entity) {

        MyTown.instance.log.info("Checking entity: " + entity.toString());

        if (explosiveBlocks.contains(entity.getClass())) {
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

    // EVENTS
    @SubscribeEvent
    @Optional.Method(modid=ModID)
    public void onLaserBreak(ic2.api.event.LaserEvent.LaserHitsBlockEvent ev) {
        MyTown.instance.log.info("Detected laser break.");
        Block tblock = DatasourceProxy.getDatasource().getBlock(ev.owner.dimension, ev.x >> 4, ev.z >> 4);
        if (tblock == null)
            return;
        MyTown.instance.log.info("Block is not null, checking...");

        Flag<Boolean> breakFlag = tblock.getTown().getFlagAtCoords(ev.owner.dimension, ev.x, ev.y, ev.z, "breakBlocks");
        if (!breakFlag.getValue()) {
            if (ev.owner instanceof EntityPlayer) {
                MyTown.instance.log.info("Found things and stuff...");
                Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.owner);
                //TODO: Check for permission node
                if (!res.getTowns().contains(tblock.getTown())) {
                    ev.setCanceled(true);
                    ev.lasershot.setDead();
                }
            } else {
                ev.setCanceled(true);
                ev.lasershot.setDead();
            }
        }
    }

    @SubscribeEvent
    @Optional.Method(modid=ModID)
    public void onLaserExplodes(ic2.api.event.LaserEvent.LaserExplodesEvent ev) {
        MyTown.instance.log.info("Detected explosion.");
        Block tblock = DatasourceProxy.getDatasource().getBlock(ev.owner.dimension, ev.lasershot.chunkCoordX, ev.lasershot.chunkCoordZ);
        if (tblock == null)
            return;
        Flag<Boolean> breakFlag = tblock.getTown().getFlagAtCoords(ev.lasershot.dimension, (int) ev.lasershot.posX, (int) ev.lasershot.posY, (int) Math.floor(ev.lasershot.posZ), "breakBlocks");
        if (!breakFlag.getValue()) {
            if (ev.owner instanceof EntityPlayer) {
                Resident res = DatasourceProxy.getDatasource().getOrMakeResident(ev.owner);
                if (res == null || !res.getTowns().contains(tblock.getTown())) {
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


