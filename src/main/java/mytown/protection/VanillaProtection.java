package mytown.protection;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.Wild;
import mytown.entities.flag.FlagType;
import mytown.util.BlockPos;
import mytown.util.Formatter;
import mytown.util.MyTownUtils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.item.EntityMinecartTNT;
import net.minecraft.entity.item.EntityTNTPrimed;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.*;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.entity.projectile.EntityWitherSkull;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.event.entity.player.FillBucketEvent;
import net.minecraftforge.event.entity.player.PlayerOpenContainerEvent;

import java.util.ArrayList;
import java.util.List;


/**
 * Created by AfterWind on 9/2/2014.
 * All vanilla protection goes here
 */
public class VanillaProtection extends Protection {
    public VanillaProtection() {
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

        this.trackedEntities.add(EntityPlayer.class);

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

        this.activatedBlocks.add(Blocks.stone_button);
        this.activatedBlocks.add(Blocks.lever);
        this.activatedBlocks.add(Blocks.wooden_button);
        this.activatedBlocks.add(Blocks.cake);
        this.activatedBlocks.add(Blocks.dragon_egg);
        this.activatedBlocks.add(Blocks.jukebox);
        this.activatedBlocks.add(Blocks.noteblock);
        this.activatedBlocks.add(Blocks.trapdoor);
        this.activatedBlocks.add(Blocks.wooden_door);
        // TODO: Check for upper part of the door
        this.activatedBlocks.add(Blocks.fence_gate);

        this.explosiveBlocks.add(EntityTNTPrimed.class);
        this.explosiveBlocks.add(EntityWitherSkull.class);
        this.explosiveBlocks.add(EntityMinecartTNT.class);

        this.trackedTileEntities.add(TileEntityPiston.class);

        isHandlingEvents = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkEntity(Entity entity) {
        // This is first since I don't want any premature return statements
        Town town = MyTownUtils.getTownAtPosition(entity.dimension, (int) entity.posX >> 4, (int) entity.posZ >> 4);
        if (town == null) {

            //Wild explosives
            if (explosiveBlocks.contains(entity.getClass())) {
                //MyTown.instance.log.info("Checking entity with explosives.");
                if (!(Boolean) Wild.getInstance().getFlag(FlagType.explosions).getValue()) {
                    //Temporary
                    if (entity instanceof EntityWitherSkull)
                        return false;

                    return true;
                }
            }
        } else {
            String value = (String) town.getValueAtCoords(entity.dimension, (int) entity.posX, (int) entity.posY, (int) entity.posZ, FlagType.mobs);
            if (value.equals("none")) {
                if (entity instanceof EntityLivingBase) {
                    return true;
                }
            } else if (value.equals("hostiles")) {
                if (hostileEntities.contains(entity.getClass())) {
                    return true;
                }
            }

            boolean explosionValue = (Boolean) town.getValueAtCoords(entity.dimension, (int) entity.posX, (int) entity.posY, (int) entity.posZ, FlagType.explosions);
            if (!explosionValue) {
                if (explosiveBlocks.contains(entity.getClass())) {
                    return true;
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkTileEntity(TileEntity te) {
        if (te instanceof TileEntityPiston) {
            Town town = MyTownUtils.getTownAtPosition(te.getWorldObj().provider.dimensionId, te.xCoord >> 4, te.zCoord >> 4);
            if (town != null) {
                boolean placeFlag = (Boolean) town.getValueAtCoords(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.modifyBlocks);
                if (!placeFlag && !town.hasBlockWhitelist(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.modifyBlocks)) {
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
                town = MyTownUtils.getTownAtPosition(te.getWorldObj().provider.dimensionId, x >> 4, z >> 4);
                if (town != null) {
                    boolean placeFlag = (Boolean) town.getValueAtCoords(te.getWorldObj().provider.dimensionId, x, y, z, FlagType.modifyBlocks);
                    if (!placeFlag) {
                        town.notifyEveryone(FlagType.modifyBlocks.getLocalizedTownNotification());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @Override
    public List<FlagType> getFlagTypeForTile(TileEntity te) {
        List<FlagType> list = new ArrayList<FlagType>();
        list.add(FlagType.modifyBlocks);
        return list;
    }

    /* ---- EventHandlers ---- */

    @Override
    public boolean hasToCheckEntity(Entity entity) {
        return true;
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public void onPlayerOpenContainer(PlayerOpenContainerEvent ev) {
        //TODO: To be implemented... maybe
    }

    @SubscribeEvent
    public void onBucketFill(FillBucketEvent ev) {
        Resident res = getDatasource().getOrMakeResident(ev.entityPlayer);
        Town town = MyTownUtils.getTownAtPosition(ev.world.provider.dimensionId, ev.target.blockX >> 4, ev.target.blockZ >> 4);
        if (town != null) {
            boolean itemFlag = (Boolean) town.getValueAtCoords(ev.world.provider.dimensionId, ev.target.blockX, ev.target.blockY, ev.target.blockZ, FlagType.useItems);
            if (!itemFlag && !town.checkPermission(res, FlagType.useItems)) {
                ev.setCanceled(true);
                res.protectionDenial(FlagType.useItems.getLocalizedProtectionDenial(), Formatter.formatOwnersToString(town.getOwnersAtPosition(ev.world.provider.dimensionId, ev.target.blockX, ev.target.blockY, ev.target.blockZ)));
            }
        }
    }
}
