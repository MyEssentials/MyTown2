package mytown.entities.tools;

import myessentials.Localization;
import myessentials.utils.PlayerUtils;
import mytown.MyTown;
import mytown.datasource.MyTownDatasource;
import mytown.datasource.MyTownUniverse;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.proxies.DatasourceProxy;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

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
        if(owner.getPlayer().inventory.hasItemStack(itemStack) && owner.toolContainer.get() != null) {
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.inventory.tool.already"));
            PlayerUtils.takeItemFromPlayer(owner.getPlayer(), owner.toolContainer.get().getItemStack(), 1);
        }

        PlayerUtils.giveItemToPlayer(owner.getPlayer(), itemStack, 1);
        owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.tool.gained"));
    }

    protected void deleteItemStack() {
        PlayerUtils.takeItemFromPlayer(owner.getPlayer(), getItemStack(), 1);
        owner.toolContainer.remove();
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

    protected Localization getLocal() {
        return MyTown.instance.LOCAL;
    }

    public static boolean interact(PlayerInteractEvent ev) {
        if(!(ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR || ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK)) {
            return false;
        }

        ItemStack currentStack = ev.entityPlayer.inventory.getCurrentItem();
        if(currentStack == null) {
            return false;
        }

        Resident res = MyTownUniverse.instance.getOrMakeResident(ev.entityPlayer);
        Tool currentTool = res.toolContainer.get();
        if(currentTool == null) {
            return false;
        }
        if(currentTool.getItemStack() == currentStack) {
            if (ev.entityPlayer.isSneaking() && ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
                currentTool.onShiftRightClick();
            } else if (ev.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                currentTool.onItemUse(ev.world.provider.dimensionId, ev.x, ev.y, ev.z, ev.face);
            }
            return true;
        }
        return false;
    }
}
