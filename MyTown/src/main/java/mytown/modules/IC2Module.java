package mytown.modules;

import ic2.api.energy.tile.IEnergyTile;
import ic2.api.event.LaserEvent;
import mytown.MyTown;
import mytown.core.ChatUtils;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**
 * Created by AfterWind on 7/8/2014.
 *
 * IC2 mod implementation here
 */
public class IC2Module extends ModuleBase{
    public static final String ModID = "IC2";

    private Class<?> clsItemCable;
    private Class<?> clsItemBlockIC2;

    public void load() {
        MinecraftForge.EVENT_BUS.register(this);
        this.enable();
        try {
            clsItemCable = Class.forName("ic2.core.item.block.ItemCable");
            clsItemBlockIC2 = Class.forName("ic2.core.item.block.ItemBlockIC2");


        } catch (ClassNotFoundException ex) {
            ex.printStackTrace();
        }
    }

    public String getModID() {
        return IC2Module.ModID;
    }



    // EVENTS
    @ForgeSubscribe
    public void onLaserBreak(LaserEvent.LaserHitsBlockEvent ev) {
        TownBlock tblock = DatasourceProxy.getDatasource().getTownBlock(ev.owner.dimension, ev.x, ev.z, false);
        if(tblock == null)
            return;
        if(!tblock.getTown().getFlagAtCoords(ev.x, ev.y, ev.z, "breakBlocks").getValue()) {
            if(ev.owner instanceof EntityPlayer) {
                Resident res = DatasourceProxy.getDatasource().getResident(((EntityPlayer) ev.owner).username);
                if(res == null || !res.getTowns().contains(tblock.getTown())) {
                    ev.setCanceled(true);
                    return;
                }
            } else {
                ev.setCanceled(true);
                return;
            }
        }
    }

    @ForgeSubscribe
    public void onLaserExplodes(LaserEvent.LaserExplodesEvent ev) {
        TownBlock tblock = DatasourceProxy.getDatasource().getTownBlock(ev.owner.dimension, ev.lasershot.chunkCoordX, ev.lasershot.chunkCoordZ, true);
        if(tblock == null)
            return;
        if(!tblock.getTown().getFlagAtCoords((int) ev.lasershot.posX, (int) ev.lasershot.posY, (int) Math.floor(ev.lasershot.posZ), "breakBlocks").getValue()) {
            if(ev.owner instanceof EntityPlayer) {
                Resident res = DatasourceProxy.getDatasource().getResident(((EntityPlayer) ev.owner).username);
                if(res == null || !res.getTowns().contains(tblock.getTown())) {
                    ev.setCanceled(true);
                    return;
                }
            } else {
                ev.setCanceled(true);
                return;
            }
        }
    }

    @ForgeSubscribe
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
}


