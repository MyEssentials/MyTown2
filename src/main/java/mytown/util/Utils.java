package mytown.util;

import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Block;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;

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
        Block block = getDatasource().getBlock(dim, x, z);
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
        return  getTownAtPosition(entity.dimension, entity.chunkCoordX, entity.chunkCoordZ);
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
        String flagLore = lore.getStringTagAt(2); // Second in line
        return flagLore.substring(8); // We use hacks in here
    }

    public static String getTownNameFromLore(EntityPlayer player) {
        ItemStack currentStack = player.inventory.getCurrentItem();
        NBTTagList lore = currentStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
        String flagLore = lore.getStringTagAt(3);
        return flagLore.substring(8);
    }

    /**
     * Gets the datasource
     *
     * @return
     */
    public static MyTownDatasource getDatasource() {return DatasourceProxy.getDatasource(); }
}
