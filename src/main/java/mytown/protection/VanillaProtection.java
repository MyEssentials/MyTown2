package mytown.protection;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mytown.MyTown;
import mytown.api.events.TownEvent;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.util.BlockPos;
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
import net.minecraft.init.Items;
import net.minecraft.item.ItemBucket;
import net.minecraft.item.ItemStack;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityPiston;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.common.DimensionManager;
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

        this.trackedTileEntities.add(TileEntityPiston.class);

        isHandlingEvents = true;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkEntity(Entity entity) {
        // This is first since I don't want any premature return statements
        if(super.checkEntity(entity))
            return true;



        // Town only checks here

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkTileEntity(TileEntity te) {
        if(te instanceof TileEntityPiston) {
            Town town = Utils.getTownAtPosition(te.getWorldObj().provider.dimensionId, te.xCoord >> 4, te.zCoord >> 4);
            if (town != null) {
                Flag<Boolean> placeFlag = town.getFlagAtCoords(te.getWorldObj().provider.dimensionId, te.xCoord, te.yCoord, te.zCoord, FlagType.placeBlocks);
                if (!placeFlag.getValue()) {
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
                if (town != null) {
                    Flag<Boolean> placeFlag = town.getFlagAtCoords(te.getWorldObj().provider.dimensionId, x, y, z, FlagType.placeBlocks);
                    if (!placeFlag.getValue()) {
                        //TODO: Create a flag only for this
                        town.notifyEveryone(FlagType.placeBlocks.getLocalizedProtectionDenial());
                        return true;
                    }
                }
            }
        }

        return false;
    }

    @SuppressWarnings("unchecked")
    @Override
    public boolean checkItemUsage(ItemStack itemStack, Resident res, BlockPos bp) {
        if(itemStack.getItem() instanceof ItemBucket) {
            if(res != null) {
                MovingObjectPosition pos = Utils.getMovingObjectPositionFromPlayer(res.getPlayer().worldObj, res.getPlayer(), false);
                if(pos != null) {
                    //TODO: Properly check for fluid pickup
                    if (pos.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        int x = pos.blockX;
                        int y = pos.blockY;
                        int z = pos.blockZ;

                        switch (pos.sideHit) {
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

                        Town town = Utils.getTownAtPosition(res.getPlayer().dimension, x >> 4, z >> 4);
                        if (town != null) {
                            Flag<Boolean> itemUsage = town.getFlagAtCoords(res.getPlayer().dimension, x, y, z, FlagType.useItems);
                            if (!itemUsage.getValue() && !town.checkPermission(res, FlagType.useItems, res.getPlayer().dimension, x, y, z)) {
                                res.sendMessage(FlagType.useItems.getLocalizedProtectionDenial());
                                return true;
                            }
                        }
                    } else if (pos.typeOfHit == MovingObjectPosition.MovingObjectType.MISS) {
                        //MyTown.instance.log.info("It missed! Not checking!");
                    }
                }
            } else {
                int x = bp.x;
                int y = bp.y;
                int z = bp.z;

                // TODO: Chenge to global protections instead of te only
                switch (ThermalExpansionProtection.getFacing(DimensionManager.getWorld(bp.dim).getTileEntity(bp.x, bp.y, bp.z))) {
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

                Town town = Utils.getTownAtPosition(bp.dim, x >> 4, z >> 4);
                if (town != null) {
                    Flag<Boolean> itemUsage = town.getFlagAtCoords(bp.dim, x, y, z, FlagType.useItems);
                    if (!itemUsage.getValue()) {
                        town.notifyEveryone(FlagType.useItems.getLocalizedProtectionDenial());
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
