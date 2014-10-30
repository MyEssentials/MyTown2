package mytown.util;

import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.entities.TownBlock;
import mytown.entities.BlockWhitelist;
import mytown.entities.Town;
import mytown.entities.flag.FlagType;
import mytown.proxies.DatasourceProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.io.File;
import java.lang.reflect.Method;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by AfterWind on 9/9/2014.
 * Utils class for random useful things
 */
public class Utils {
    /**
     * Returns the town at the specified position or null if nothing found.
     *
     * @param dim
     * @param x
     * @param z
     * @return
     */
    public static Town getTownAtPosition(int dim, int x, int z) {
        TownBlock block = getDatasource().getBlock(dim, x, z);
        if(block == null)
            return null;
        return block.getTown();
    }

    /**
     * Gets the town at the entity's position
     *
     * @param entity
     * @return
     */
    protected static Town getTownFromEntity(Entity entity) {
        return getTownAtPosition(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);
    }

    /**
     * Returns the Flag name from the selector's Lore tag
     *
     * @param player
     * @return
     */
    public static String getFlagNameFromLore(EntityPlayer player) {
        ItemStack currentStack = player.inventory.getCurrentItem();
        NBTTagList lore = currentStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
        String flagLore = lore.getStringTagAt(1);
        return flagLore.substring(8); // We use hacks in here
    }

    /**
     * Returns the Town name from the selector's Lore tag
     *
     * @param player
     * @return
     */
    public static String getTownNameFromLore(EntityPlayer player) {
        ItemStack currentStack = player.inventory.getCurrentItem();
        NBTTagList lore = currentStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
        String flagLore = lore.getStringTagAt(2);
        return flagLore.substring(8);
    }

    /**
     * Gets the nearby tile entities of the specified tile entity and of the specified type
     *
     * @param te
     * @param type
     * @return
     */
    public static List<TileEntity> getNearbyTileEntity(TileEntity te, Class<? extends TileEntity> type) {
        List<TileEntity> result = new ArrayList<TileEntity>();
        int[] dx = {0, 1, 0, -1, 0, 0};
        int[] dy = {1, 0, -1, 0, 0, 0};
        int[] dz = {0, 0, 0, 0, 1, -1};

        for(int i = 0; i < 6; i++) {
            TileEntity found = te.getWorldObj().getTileEntity(te.xCoord + dx[i], te.yCoord + dy[i], te.zCoord + dz[i]);
            if(found != null && type.isAssignableFrom(found.getClass())) {
                MyTown.instance.log.info("Found tile entity " + found + " for class " + type.getName());
                result.add(found);
            }
        }
        return result;
    }

    /**
     * Transforms a box made out of actual coordinates to a list of all the chunks that this box is in
     *
     * @param minX
     * @param minZ
     * @param maxX
     * @param maxZ
     * @return
     */
    public static List<ChunkPos> getChunksInBox(int minX, int minZ, int maxX, int maxZ) {
        List<ChunkPos> list = new ArrayList<ChunkPos>();
        for(int i = minX >> 4; i <= maxX >> 4; i++) {
            for(int j = minZ >> 4; j <= maxZ >> 4; j++) {
                list.add(new ChunkPos(i, j));
            }
        }
        return list;
    }

    /**
     * Searches if the specified block is whitelisted in any town
     *
     * @param dim
     * @param x
     * @param y
     * @param z
     * @param flagType
     * @return
     */
    public static boolean isBlockWhitelisted(int dim, int x, int y, int z, FlagType flagType) {
        Town town = getTownAtPosition(dim, x >> 4, z >> 4);
        if(town == null) return false;
        BlockWhitelist bw = town.getBlockWhitelist(dim, x, y, z, flagType);
        if(bw != null) {
            if(bw.isDeleted) {
                getDatasource().deleteBlockWhitelist(bw, town);
                return false;
            }
            return true;
        }
        return false;
    }

    /**
     * Gets all the blocks nearby the specified block position and returns only the positions which have a town in it
     * Not checking on the y axis!
     *
     * @param block
     * @return
     */
    public static List<BlockPos> getPositionNearby(BlockPos block) {
        List<BlockPos> list = new ArrayList<BlockPos>();
        int[] dx = {0, 1, 0, -1};
        int[] dz = {1, 0, -1, 0};

        for(int i = 0; i < 4; i++) {
            Town town = getTownAtPosition(block.dim, (block.x + dx[i]) >> 4, (block.z + dz[i]) >> 4);
            if(town != null) {
                list.add(new BlockPos(block.x + dx[i], block.y, block.z + dz[i], block.dim));
            }
        }
        return list;
    }

    /**
     * Gets all towns in a range
     *
     * @param dim
     * @param x
     * @param z
     * @param rangeX
     * @param rangeZ
     * @return
     */
    public static List<Town> getTownsInRange(int dim, int x, int z, int rangeX, int rangeZ) {
        List<Town> list = new ArrayList<Town>();
        for(int i = x - rangeX; i <= x + rangeX; i++) {
            for(int j = z - rangeZ; j <= z + rangeZ; j++) {
                Town town = getTownAtPosition(dim, i >> 4, j >> 4);
                if(town != null)
                    list.add(town);
            }
        }
        return list;
    }

    /**
     * Drops the specified itemstack in the worls as a EntityItem
     *
     * @param world
     * @param x
     * @param y
     * @param z
     * @param itemStack
     */
    public static void dropAsEntity(World world, int x, int y, int z, ItemStack itemStack)
    {
        if (itemStack == null) {
            return;
        }
        double f = 0.7D;
        double dx = world.rand.nextFloat() * f + (1.0D - f) * 0.5D;
        double dy = world.rand.nextFloat() * f + (1.0D - f) * 0.5D;
        double dz = world.rand.nextFloat() * f + (1.0D - f) * 0.5D;

        EntityItem entityItem = new EntityItem(world, x + dx, y + dy, z + dz, itemStack);
        //entityItem.field_145804_b = 10;
        world.spawnEntityInWorld(entityItem);
    }

    /**
     * Gets the position at which the player is looking
     *
     * @param p_77621_1_
     * @param p_77621_2_
     * @param p_77621_3_
     * @return
     */
    public static MovingObjectPosition getMovingObjectPositionFromPlayer(World p_77621_1_, EntityPlayer p_77621_2_, boolean p_77621_3_)
    {
        float f = 1.0F;
        float f1 = p_77621_2_.prevRotationPitch + (p_77621_2_.rotationPitch - p_77621_2_.prevRotationPitch) * f;
        float f2 = p_77621_2_.prevRotationYaw + (p_77621_2_.rotationYaw - p_77621_2_.prevRotationYaw) * f;
        double d0 = p_77621_2_.prevPosX + (p_77621_2_.posX - p_77621_2_.prevPosX) * (double)f;
        double d1 = p_77621_2_.prevPosY + (p_77621_2_.posY - p_77621_2_.prevPosY) * (double)f + (double)(p_77621_1_.isRemote ? p_77621_2_.getEyeHeight() - p_77621_2_.getDefaultEyeHeight() : p_77621_2_.getEyeHeight()); // isRemote check to revert changes to ray trace position due to adding the eye height clientside and player yOffset differences
        double d2 = p_77621_2_.prevPosZ + (p_77621_2_.posZ - p_77621_2_.prevPosZ) * (double)f;
        Vec3 vec3 = Vec3.createVectorHelper(d0, d1, d2);
        float f3 = MathHelper.cos(-f2 * 0.017453292F - (float) Math.PI);
        float f4 = MathHelper.sin(-f2 * 0.017453292F - (float)Math.PI);
        float f5 = -MathHelper.cos(-f1 * 0.017453292F);
        float f6 = MathHelper.sin(-f1 * 0.017453292F);
        float f7 = f4 * f5;
        float f8 = f3 * f5;
        double d3 = 5.0D;
        if (p_77621_2_ instanceof EntityPlayerMP)
        {
            d3 = ((EntityPlayerMP)p_77621_2_).theItemInWorldManager.getBlockReachDistance();
        }
        Vec3 vec31 = vec3.addVector((double)f7 * d3, (double)f6 * d3, (double)f8 * d3);
        return p_77621_1_.func_147447_a(vec3, vec31, p_77621_3_, !p_77621_3_, false);
    }


    public static void addSoftwareLibrary(File file) {
        try {
            Method method = URLClassLoader.class.getDeclaredMethod("addURL", new Class[]{URL.class});
            method.setAccessible(true);
            method.invoke(ClassLoader.getSystemClassLoader(), file.toURI().toURL());
        } catch (Exception e) {
            MyTown.instance.log.error("Failed adding file " + file.getAbsolutePath() + " to classpath!");
        }
    }

    /**
     * Gets the datasource
     *
     * @return
     */
    public static MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }
}
