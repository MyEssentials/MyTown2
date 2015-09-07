package mytown.entities.tools;

import mytown.datasource.MyTownUniverse;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.blocks.SellSign;
import mytown.util.MyTownUtils;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * A tool which places signs that, when right clicked, sells the plots to that person.
 */
public class PlotSellTool extends Tool {

    private static final String NAME = EnumChatFormatting.BLUE + "Plot Sell";
    private static final String DESCRIPTION_HEADER_1 = EnumChatFormatting.DARK_AQUA + "Right-click in a plot to place a sell sign.";
    private static final String DESCRIPTION_HEADER_2 = EnumChatFormatting.DARK_AQUA + "Shift-right-click air to change modes.";
    private static final String DESCRIPTION_PRICE = EnumChatFormatting.DARK_AQUA + "Price: ";
    private static final String DESCRIPTION_MODE = EnumChatFormatting.DARK_AQUA + "Restricted to residents: ";

    /**
     * Used for identifying whether or not this is a shop sign or not.
     */
    private static final String IDENTIFIER = EnumChatFormatting.DARK_BLUE + "Plot Sale";

    private int price;
    private boolean restricted = false;

    public PlotSellTool(Resident owner, int price) {
        super(owner, NAME);
        this.price = price;
        giveItemStack(createItemStack(Items.wooden_hoe, DESCRIPTION_HEADER_1, DESCRIPTION_HEADER_2, DESCRIPTION_PRICE + price, DESCRIPTION_MODE + restricted));
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

        Plot plot = MyTownUniverse.instance.plots.get(dim, x, y, z);

        if(plot.signContainer.get() != null) {
            plot.signContainer.get().deleteSignBlock();
        }
        plot.signContainer.set(new SellSign(dim, x, y, z, face, owner, price, restricted));
        deleteItemStack();
    }

    @Override
    public void onShiftRightClick() {
        this.restricted = !this.restricted;
        setDescription(DESCRIPTION_MODE + restricted, 3);
        owner.sendMessage(getLocal().getLocalization("mytown.notification.tool.mode", "Restricted to residents", restricted));
    }

    @Override
    protected boolean hasPermission(Town town, int dim, int x, int y, int z) {
        World world = MinecraftServer.getServer().worldServerForDimension(dim);

        if(world.getBlock(x, y, z) != Blocks.air) {
            return false;
        }

        if(town == null) {
            owner.sendMessage(getLocal().getLocalization("mytown.cmd.err.notInTown", owner.townsContainer.getMainTown().getName()));
            return false;
        }

        Plot plot = town.plotsContainer.get(dim, x, y, z);
        if(plot == null) {
            owner.sendMessage(getLocal().getLocalization("mytown.cmd.err.plot.sell.notInPlot", town.getName()));
            return false;
        }
        if(!plot.ownersContainer.contains(owner) && !plot.getTown().hasPermission(owner, "mytown.bypass.plot")) {
            owner.sendMessage(getLocal().getLocalization("mytown.cmd.err.plot.noPermission"));
            return false;
        }
        return true;
    }
}
