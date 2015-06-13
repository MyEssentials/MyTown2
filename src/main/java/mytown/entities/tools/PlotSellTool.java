package mytown.entities.tools;

import mytown.datasource.MyTownUniverse;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.Constants;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntitySign;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.DimensionManager;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * A tool which places signs that, when right clicked, sells the plots to that person.
 */
public class PlotSellTool extends Tool {

    private static final String NAME = "Plot Sell";
    private static final String DESCRIPTION_HEADER = "Right-click in a plot to place a sell sign.";
    private static final String DESCRIPTION_PRICE = "Price: ";

    /**
     * Used for identifying whether or not this is a shop sign or not.
     */
    private static final String IDENTIFIER = EnumChatFormatting.DARK_BLUE + "Plot Sell";

    public PlotSellTool(Resident owner, int price) {
        this.owner = owner;
        createItemStack(Items.wooden_hoe, NAME, DESCRIPTION_HEADER, DESCRIPTION_PRICE + price);
        giveItemStack();
    }

    @Override
    public void onItemUse(int dim, int x, int y, int z, int face) {
        ForgeDirection direction = ForgeDirection.getOrientation(face);
        x = x + direction.offsetX;
        y = y + direction.offsetY;
        z = z + direction.offsetZ;

        World world = DimensionManager.getWorld(dim);

        if(world.getBlock(x, y, z) != Blocks.air)
            return;

        TownBlock townBlock= MyTownUniverse.instance.getTownBlock(dim, x >> 4, z >> 4);
        if(townBlock == null)
            return;

        int price = getPriceFromLore();

        Plot plot = townBlock.getTown().getPlotAtCoords(dim, x, y, z);
        if(plot == null) {
            owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.sell.notInPlot", townBlock.getTown().getName()));
            return;
        }
        if(!plot.hasOwner(owner)) {
            owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.notOwner"));
            return;
        }
        createShopSign(world, x, y, z, face, price);
    }

    private void createShopSign(World world, int x, int y, int z, int face, int price) {
        ForgeDirection direction = ForgeDirection.getOrientation(face);
        if(direction == ForgeDirection.DOWN || face == 1) {
            int i1 = MathHelper.floor_double((double) ((owner.getPlayer().rotationYaw + 180.0F) * 16.0F / 360.0F) + 0.5D) & 15;
            world.setBlock(x, y, z, Blocks.standing_sign, i1, 3);
        } else {
            world.setBlock(x, y, z, Blocks.wall_sign, face, 3);
        }

        TileEntitySign te = (TileEntitySign)world.getTileEntity(x, y, z);

        String[] signText = new String[4];
        signText[0] = "";
        signText[1] = IDENTIFIER;
        signText[2] = "" + EnumChatFormatting.GOLD + price;
        signText[3] = "";
        te.signText = signText;
    }

    private int getPriceFromLore() {
        String priceLore = getDescription(0);
        return Integer.parseInt(priceLore.substring(DESCRIPTION_PRICE.length()));
    }
}
