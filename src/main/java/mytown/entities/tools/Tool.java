package mytown.entities.tools;

import myessentials.utils.PlayerUtils;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

/**
 * A wrapper interface for one of the item class that only executes on the server-side.
 */
public abstract class Tool {

    protected Resident owner;

    /**
     * This is used as an identifier to find the itemstack in the player's inventory.
     */
    protected String toolName;

    protected Tool(Resident owner, String toolName) {
        this.owner = owner;
        this.toolName = toolName;
    }

    public abstract void onItemUse(int dim, int x, int y, int z, int face);

    protected abstract boolean hasPermission(Town town, int dim, int x, int y, int z);

    public void onShiftRightClick() {
    }

    public ItemStack getItemStack() {
        return PlayerUtils.getItemStackFromPlayer(owner.getPlayer(), Items.wooden_hoe, toolName);
    }

    protected void giveItemStack(ItemStack itemStack) {
        if(owner.getPlayer().inventory.hasItemStack(itemStack) && owner.getCurrentTool() != null) {
            owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.inventory.tool.already"));
            PlayerUtils.takeItemFromPlayer(owner.getPlayer(), owner.getCurrentTool().getItemStack(), 1);
        }

        PlayerUtils.giveItemToPlayer(owner.getPlayer(), itemStack, 1);
    }

    protected void deleteItemStack() {
        PlayerUtils.takeItemFromPlayer(owner.getPlayer(), getItemStack(), 1);
        owner.removeCurrentTool();
    }

    protected ItemStack createItemStack(Item item, String... description) {
        ItemStack itemStack = new ItemStack(item);
        itemStack.setStackDisplayName(toolName);
        NBTTagList lore = new NBTTagList();
        for(String s : description) {
            lore.appendTag(new NBTTagString(s));
        }
        itemStack.getTagCompound().getCompoundTag("display").setTag("Lore", lore);
        return itemStack;
    }

    protected void setDescription(String description, int line) {
        NBTTagList lore = getItemStack().getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
        NBTTagList newLore = new NBTTagList();
        for(int i = 0; i < lore.tagCount(); i++) {
            if(i == line) {
                newLore.appendTag(new NBTTagString(description));
            } else {
                newLore.appendTag(new NBTTagString(lore.getStringTagAt(i)));
            }
        }
        getItemStack().getTagCompound().getCompoundTag("display").setTag("Lore", newLore);
    }

    protected String getDescription(int line) {
        NBTTagList lore = getItemStack().getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
        for(int i = 0; i < lore.tagCount(); i++) {
            if(i == line) {
                return lore.getStringTagAt(i);
            }
        }
        return null;
    }


    protected MyTownDatasource getDatasource() {
        return DatasourceProxy.getDatasource();
    }


}
