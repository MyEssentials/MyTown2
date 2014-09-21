package mytown.protection;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mytown.MyTown;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.util.Utils;
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
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by AfterWind on 9/2/2014.
 * All vanilla protection goes here
 */
public class VanillaProtection extends Protection {
    public VanillaProtection() {
        super();
        MyTown.instance.log.info("Vanilla protection initializing...");

        //this.anyEntity.add(EntityPlayer.class);

        this.hostileEntities.add(EntityCreeper.class);
        this.hostileEntities.add(EntityZombie.class);
        this.hostileEntities.add(EntityArrow.class);
        this.hostileEntities.add(EntityGhast.class);
        this.hostileEntities.add(EntitySkeleton.class);
        this.hostileEntities.add(EntityPigZombie.class);
        this.hostileEntities.add(EntitySpider.class);
        this.hostileEntities.add(EntityBlaze.class);
        this.hostileEntities.add(EntityCaveSpider.class);
        this.hostileEntities.add(EntitySilverfish.class);
        this.hostileEntities.add(EntityEnderman.class);
        this.hostileEntities.add(EntityMagmaCube.class);
        this.hostileEntities.add(EntitySlime.class);
        this.hostileEntities.add(EntityWitch.class);
        this.hostileEntities.add(EntityWither.class);
        this.hostileEntities.add(EntityWitherSkull.class);
        this.hostileEntities.add(EntityDragon.class);
        // If the flag "mobs" is set to "none"
        this.anyEntity.add(EntityLivingBase.class);

        this.trackedEntities.add(EntityPlayer.class);
        // Hi, my name is TNT
        //this.anyEntity.add(EntityTNTPrimed.class);

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

        this.explosiveBlocks.add(EntityTNTPrimed.class);



        isHandlingEvents = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkEntity(Entity entity) {
        //TODO: Implement wilderness
        Town town = Utils.getTownAtPosition(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);
        if(town == null)
            return false;

        if(entity instanceof EntityTNTPrimed) {
            Flag<Boolean> explosionsFlag = town.getFlagAtCoords(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ, FlagType.explosions);
            if(!explosionsFlag.getValue()) {
                return true;
            }
            return false;
        }

        if(entity instanceof EntityPlayer) {
            Flag<Boolean> enterFlag = town.getFlagAtCoords(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ, FlagType.enter);
            Plot plot = town.getPlotAtCoords(entity.dimension, (int)entity.posX, (int)entity.posY, (int)entity.posZ);
            if(!enterFlag.getValue()) {
                Resident res = getDatasource().getOrMakeResident(entity);
                if(!town.hasResident(res)) {
                    res.respawnPlayer();
                    res.sendMessage("§cYou have been moved because you can't access this place!");
                    return true;
                } else if(plot != null && !(plot.getResidents().contains(res) || plot.getOwners().contains(res))) {
                    res.respawnPlayer();
                    res.sendMessage("§cYou have been moved because you can't access this place!");
                    return true;
                }
            }
            return false;
        }

        if(super.checkEntity(entity))
            return true;
        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkTileEntity(TileEntity te) {
        MyTown.instance.log.info("Found vanilla te " + te.xCoord + ", " + te.yCoord + ", " + te.zCoord);

        Town town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, te.xCoord >> 4, te.zCoord >> 4);
        if(town != null) {
            Flag<Boolean> placeFlag = town.getFlagAtCoords(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.placeBlocks);
            if(!placeFlag.getValue()) {
                return true;
            }
        } else {
            TileEntityPiston piston = (TileEntityPiston) te;
            int x = te.xCoord, y = te.yCoord, z = te.zCoord;
            switch (piston.getPistonOrientation()) {
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
            town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, x >> 4, z >> 4);
            if(town != null) {
                Flag<Boolean> placeFlag = town.getFlagAtCoords(te.getWorldObj().provider.dimensionId, x, y, z, FlagType.placeBlocks);
                if(!placeFlag.getValue()) {
                    //TODO: Create a flag only for this
                    town.notifyEveryone(FlagType.placeBlocks.getLocalizedProtectionDenial());
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public boolean hasToCheckEntity(Entity e) {
        return super.hasToCheckEntity(e) || e instanceof EntityPlayer;
    }

    @Override
    public List<FlagType> getFlagTypeForTile(TileEntity te) {
        List<FlagType> list = new ArrayList<FlagType>();
        list.add(FlagType.accessBlocks);
        list.add(FlagType.activateBlocks);
        return list;
    }

    /* ---- EventHandlers ---- */



    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerOpenContainer(PlayerOpenContainerEvent ev) {
        //TODO: To be implemented... maybe
    }
}
