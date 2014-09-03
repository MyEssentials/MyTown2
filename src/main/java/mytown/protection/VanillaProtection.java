package mytown.protection;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import mytown.MyTown;
import mytown.entities.Block;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import net.minecraftforge.event.world.BlockEvent;

import javax.xml.crypto.Data;


/**
 * Created by AfterWind on 9/2/2014.
 * All vanilla protection goes here
 */
public class VanillaProtection extends Protection {
    public VanillaProtection() {
        MyTown.instance.log.info("Vanilla protection initializing...");
        // Since it will have most of the event handlers
        isHandlingEvents = true;

        //this.trackedAnyEntity.add(EntityPlayer.class);

        this.trackedHostileEntities.add(EntityCreeper.class);
        this.trackedHostileEntities.add(EntityZombie.class);
        this.trackedHostileEntities.add(EntityArrow.class);
        this.trackedHostileEntities.add(EntityGhast.class);
        this.trackedHostileEntities.add(EntitySkeleton.class);
        this.trackedHostileEntities.add(EntityPigZombie.class);
        this.trackedHostileEntities.add(EntitySpider.class);
        this.trackedHostileEntities.add(EntityBlaze.class);
        this.trackedHostileEntities.add(EntityCaveSpider.class);
        this.trackedHostileEntities.add(EntitySilverfish.class);
        this.trackedHostileEntities.add(EntityEnderman.class);
        this.trackedHostileEntities.add(EntityMagmaCube.class);
        this.trackedHostileEntities.add(EntitySlime.class);
        this.trackedHostileEntities.add(EntityWitch.class);
        this.trackedHostileEntities.add(EntityWither.class);
        this.trackedHostileEntities.add(EntityWitherSkull.class);
        this.trackedHostileEntities.add(EntityDragon.class);
        // If the flag "mobs" is set to "none"
        this.trackedAnyEntity.add(EntityLivingBase.class);

        // Hi, my name is TNT
        //this.trackedAnyEntity.add(EntityTNTPrimed.class);

        this.protectedEntities.add(EntityHorse.class);
        this.protectedEntities.add(EntityOcelot.class);
        this.protectedEntities.add(EntityWolf.class);
        this.protectedEntities.add(EntityChicken.class);
        this.protectedEntities.add(EntityCow.class);
        this.protectedEntities.add(EntitySheep.class);
        this.protectedEntities.add(EntityVillager.class);
        this.protectedEntities.add(EntityIronGolem.class);
        this.protectedEntities.add(EntityMooshroom.class);
        this.protectedEntities.add(EntityPig.class);
        this.protectedEntities.add(EntitySnowman.class);

        this.trackedTileEntities.add(TileEntityPiston.class);
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkEntity(Entity entity) {
        //TODO: Implement wilderness
        Town town = Town.getTownAtPosition(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);
        if(town == null)
            return false;

        if(entity instanceof EntityTNTPrimed) {
            Flag<Boolean> explosionsFlag = town.getFlagAtCoords(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ, "explosions");
            if(!explosionsFlag.getValue()) {
                entity.setDead();
                return true;
            }
            return false;
        }

        if(entity instanceof EntityPlayer) {
            Flag<Boolean> enterFlag = town.getFlagAtCoords(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ, "enter");
            Plot plot = town.getPlotAtCoords(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ);
            if(!enterFlag.getValue()) {
                Resident res = getDatasource().getOrMakeResident(entity);
                if(!town.hasResident(res)) {
                    res.respawnPlayer();
                    res.sendMessage("You have been moved because you can't access this place!");
                    return true;
                } else if(plot != null && !(plot.getResidents().contains(res) || plot.getOwners().contains(res))) {
                    res.respawnPlayer();
                    res.sendMessage("You have been moved because you can't access this place!");
                    return true;
                }
            }
            return false;
        }

        if(super.checkEntity(entity))
            return true;
        return false;
    }

    @Override
    public boolean checkTileEntity(TileEntity te) {
        if(trackedTileEntities.contains(te.getClass())) {
            // TODO: idkhowtoworkthissomebodyhalp
        }
        return false;
    }

    /* ---- EventHandlers ---- */

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerBreaksBlock(BlockEvent.BreakEvent ev) {
        // TODO: Implement wilderness perms too
        Block block = DatasourceProxy.getDatasource().getBlock(ev.world.provider.dimensionId, ev.x >> 4, ev.z >> 4);
        if (block != null) {
            Town town = block.getTown();
            Flag<Boolean> flag = town.getFlagAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, "breakBlocks");
            if (flag == null)
                return;
            if (flag.getValue())
                return;
            // TODO: Instead, check for the permission at one point
            if (DatasourceProxy.getDatasource().getOrMakeResident(ev.getPlayer()).hasTown(town))
                return;

            ev.setCanceled(true);
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerOpenContainer(PlayerOpenContainerEvent ev) {
        //TODO: To be implemented... maybe
    }

    @SuppressWarnings("unchecked")
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerAttackEntityEvent(AttackEntityEvent ev) {
        // TODO: More wilderness goes here
        Block block = DatasourceProxy.getDatasource().getBlock(ev.target.dimension, ev.target.chunkCoordX, ev.target.chunkCoordZ);
        if(block != null) {
            Town town = block.getTown();
            Flag<Boolean> attackFlag = town.getFlagAtCoords(ev.target.dimension, (int)ev.target.posX, (int)ev.target.posY, (int)ev.target.posZ, "attackEntities");
            if(!attackFlag.getValue() && protectedEntities.contains(ev.target.getClass())) {
                // TODO: Check for permission instead
                if(!DatasourceProxy.getDatasource().getOrMakeResident(ev.entityPlayer).hasTown(town)) {
                    ev.setCanceled(true);
                }
            }
        }
    }


    @SuppressWarnings("unchecked")
    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent ev) {
        if (ev.entityPlayer.worldObj.isRemote)
            return;

        Resident res = getDatasource().getOrMakeResident(ev.entityPlayer);
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
            Block tblock = getDatasource().getBlock(ev.entity.dimension, ev.x >> 4, ev.z >> 4);
            if(tblock == null)
                return;

            TileEntity te = ev.entityPlayer.worldObj.getTileEntity(ev.x, ev.y, ev.z);

            // In case the player wants to access a block... checking if player is shifting too
            if(te != null && !(currentStack != null && currentStack.getItem() instanceof ItemBlock && res.getPlayer().isSneaking())) {
                Flag<Boolean> accessFlag = tblock.getTown().getFlagAtCoords(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, "accessBlocks");

                // Checking if a player wants to access a block here
                //TODO: Check for permission instead
                if(!accessFlag.getValue() && !res.getTowns().contains(tblock.getTown())) {
                    res.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.protection.vanilla.access"));
                    ev.setCanceled(true);
                    return;
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

}
