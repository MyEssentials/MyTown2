package mytown.entities.tools;

import mytown.core.utils.PlayerUtils;
import mytown.datasource.MyTownDatasource;
import mytown.entities.Resident;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

/**
 * A wrapper interface for one of the item class that only executes on the server-side.
 */
public abstract class Tool {

    protected Resident owner;
    protected ItemStack itemStack;

    public abstract void onItemUse(int dim, int x, int y, int z, int face);

    public ItemStack getItemStack() {
        return this.itemStack;
    }


    protected void giveItemStack() {
        if(owner.getPlayer().inventory.hasItemStack(itemStack)) {
            owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.inventory"));
        } else {
            PlayerUtils.giveItemToPlayer(owner.getPlayer(), itemStack, 1);
        }
    }

    protected void deleteItemStack() {
        PlayerUtils.takeItemFromPlayer(owner.getPlayer(), itemStack, 1);
        owner.removeCurrentTool();
    }

    protected void createItemStack(Item item, String name, String... description) {
        itemStack = new ItemStack(item);
        itemStack.setStackDisplayName(name);
        NBTTagList lore = new NBTTagList();
        for(String s : description) {
            lore.appendTag(new NBTTagString(s));
        }
        itemStack.getTagCompound().getCompoundTag("display").setTag("Lore", lore);
    }

    protected void setDescription(String description, int line) {
        NBTTagList lore = itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
        NBTTagList newLore = new NBTTagList();
        for(int i = 0; i < lore.tagCount(); i++) {
            if(i == line) {
                newLore.appendTag(new NBTTagString(description));
            } else {
                newLore.appendTag(new NBTTagString(lore.getStringTagAt(i)));
            }
        }
        itemStack.getTagCompound().getCompoundTag("display").setTag("Lore", newLore);
    }

    protected String getDescription(int line) {
        NBTTagList lore = itemStack.getTagCompound().getCompoundTag("display").getTagList("Lore", 8);
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
