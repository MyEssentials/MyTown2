package mytown.protection.segment;

import net.minecraft.entity.player.EntityPlayer;

import java.util.Collections;

public class ClientInventoryUpdate {
    private byte mode;
    public ClientInventoryUpdate(int mode) {
        this.mode = (byte)mode;
    }

    public int getMode(){
        return mode;
    }

    public void send(EntityPlayer player) {
        if(mode == 1)
            Collections.fill(player.inventoryContainer.inventoryItemStacks, null);
        else if(mode == 2) {
            // Inventory slots: http://hydra-media.cursecdn.com/minecraft.gamepedia.com/8/8c/Items_slot_number.JPG?version=8c2b6c3bebb8855b34f07f232a9e6d6f
            int inventorySlot = player.inventory.currentItem;

            // Container slots: http://wiki.vg/images/1/13/Inventory-slots.png
            int containerSlot = inventorySlot < 9? 36 + inventorySlot : inventorySlot;
            player.inventoryContainer.inventoryItemStacks.set(containerSlot, null);
        }
        player.inventoryContainer.detectAndSendChanges();
    }
}
