package mytown.protection;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mytown.MyTown;
import mytown.entities.Block;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.proxies.DatasourceProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraftforge.event.world.BlockEvent;


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
        this.trackedAnyEntity.add(EntityTNTPrimed.class);


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


        Flag<String> mobFlag = town.getFlagAtCoords(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ, "mobs");
        String value = mobFlag.getValue();

        if(value.equals("all")) {
            if(entity instanceof EntityLivingBase) {
                entity.setDead();
                return true;
            }
        } else if(value.equals("hostiles")) {
            if(trackedHostileEntities.contains(entity.getClass())) {
                entity.setDead();
                return true;
            }
        }
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

}
