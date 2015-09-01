package mytown.entities.tools;

import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.proxies.LocalizationProxy;
import mytown.util.MyTownUtils;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.server.MinecraftServer;
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

    private static final String NAME = EnumChatFormatting.BLUE + "Plot Sell";
    private static final String DESCRIPTION_HEADER = EnumChatFormatting.DARK_AQUA + "Right-click in a plot to place a sell sign.";
    private static final String DESCRIPTION_PRICE = EnumChatFormatting.DARK_AQUA + "Price: ";

    /**
     * Used for identifying whether or not this is a shop sign or not.
     */
    private static final String IDENTIFIER = EnumChatFormatting.DARK_BLUE + "Plot Sale";

    public PlotSellTool(Resident owner, int price) {
        super(owner, NAME);
        giveItemStack(createItemStack(Items.wooden_hoe, DESCRIPTION_HEADER, DESCRIPTION_PRICE + price));
    }

    @Override
    public void onItemUse(int dim, int x, int y, int z, int face) {
        ForgeDirection direction = ForgeDirection.getOrientation(face);
        x = x + direction.offsetX;
        y = y + direction.offsetY;
        z = z + direction.offsetZ;

        Town town = MyTownUtils.getTownAtPosition(dim, x >> 4, z >> 4);
        if(!hasPermission(town, dim, x, y, z)) {
            return;
        }

        World world = MinecraftServer.getServer().worldServerForDimension(dim);

        int price = getPriceFromLore();

        createShopSign(world, x, y, z, face, price);
        deleteItemStack();
    }

    @Override
    protected boolean hasPermission(Town town, int dim, int x, int y, int z) {
        World world = MinecraftServer.getServer().worldServerForDimension(dim);

        if(world.getBlock(x, y, z) != Blocks.air) {
            return false;
        }

        if(town == null) {
            owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.notInTown", owner.townsContainer.getMainTown().getName()));
            return false;
        }

        Plot plot = town.plotsContainer.get(dim, x, y, z);
        if(plot == null) {
            owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.sell.notInPlot", town.getName()));
            return false;
        }
        if(!plot.ownersContainer.contains(owner) && !plot.getTown().hasPermission(owner, "mytown.bypass.plot")) {
            owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.noPermission"));
            return false;
        }
        return true;
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
        String priceLore = getDescription(1);
        return Integer.parseInt(priceLore.substring(DESCRIPTION_PRICE.length()));
    }
}
