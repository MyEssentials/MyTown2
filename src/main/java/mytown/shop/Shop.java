package mytown.shop;

import cpw.mods.fml.common.registry.GameRegistry;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;

/**
 * Created by AfterWind on 4/4/2015.
 * A shop object that provides information about the items that are sold/bought, the amount and price.
 */
public class Shop {

    public int dim, x, y, z;
    public int db_ID;
    public ItemStack itemStack;
    public int price;
    public ShopType type;

    public Shop(String itemString, int amount, int price, ShopType type, int dim, int x, int y, int z) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        String[] split = itemString.split(":");
        Item item = GameRegistry.findItem(split[0], split[1]);
        itemStack = new ItemStack(item, amount, split.length > 2 ? Integer.parseInt(split[2]) : 0);
        this.price = price;
        this.type = type;
    }

    public int getAmount() {
        return itemStack.stackSize;
    }

    public void setDb_ID(int id) {
        this.db_ID = id;
    }
}
