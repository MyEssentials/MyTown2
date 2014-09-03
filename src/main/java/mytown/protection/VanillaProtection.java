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
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import net.minecraftforge.event.world.BlockEvent;

import javax.xml.crypto.Data;


/**
 * Created by AfterWind on 9/2/2014.
 * All vanilla protection goes here
 */
public class VanillaProtection extends Protection {
    public VanillaProtection() {
        super();
        MyTown.instance.log.info("Vanilla protection initializing...");

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

        this.activatedBlocks.add(Blocks.stone_button);
        this.activatedBlocks.add(Blocks.lever);
        this.activatedBlocks.add(Blocks.wooden_button);
        this.activatedBlocks.add(Blocks.cake);
        this.activatedBlocks.add(Blocks.dragon_egg);
        this.activatedBlocks.add(Blocks.jukebox);
        this.activatedBlocks.add(Blocks.noteblock);
        this.activatedBlocks.add(Blocks.trapdoor);
        this.activatedBlocks.add(Blocks.wooden_door);
        //this.activatedBlocks.add(Blocks.);
        this.activatedBlocks.add(Blocks.fence_gate);
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



    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerOpenContainer(PlayerOpenContainerEvent ev) {
        //TODO: To be implemented... maybe
    }


}
