package mytown.entities.tools;

import myessentials.entities.BlockPos;
import myessentials.entities.tool.Tool;
import myessentials.entities.tool.ToolManager;
import mytown.MyTown;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.signs.SellSign;
import mytown.util.MyTownUtils;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * A tool which places signs that, when right clicked, sells the plots to that person.
 */
public class PlotSellTool extends Tool {

    private static final String NAME = MyTown.instance.LOCAL.getLocalization("mytown.tool.plot.sell.name");
    private static final String DESCRIPTION_HEADER_1 = MyTown.instance.LOCAL.getLocalization("mytown.tool.plot.sell.description.header1");
    private static final String DESCRIPTION_HEADER_2 = MyTown.instance.LOCAL.getLocalization("mytown.tool.plot.sell.description.header2");
    private static final String DESCRIPTION_PRICE = MyTown.instance.LOCAL.getLocalization("mytown.tool.plot.sell.description.price")+" ";
    private static final String DESCRIPTION_MODE = MyTown.instance.LOCAL.getLocalization("mytown.tool.plot.sell.description.mode")+" ";

    private int price;
    private boolean restricted = false;
    private Resident owner;

    public PlotSellTool(Resident owner, int price) {
        super(owner.getPlayer(), NAME);
        this.owner = owner;
        this.price = price;
    }

    @Override
    public void onItemUse(BlockPos bp, int face) {
        ForgeDirection direction = ForgeDirection.getOrientation(face);
        bp = new BlockPos(bp.getX() + direction.offsetX, bp.getY() + direction.offsetY, bp.getZ() + direction.offsetZ, bp.getDim());

        Town town = MyTownUtils.getTownAtPosition(bp.getDim(), bp.getX() >> 4, bp.getZ() >> 4);
        if(!hasPermission(town, bp)) {
            return;
        }
        new SellSign(bp, face, owner, price, restricted);
        ToolManager.instance.remove(this);
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
                DESCRIPTION_HEADER_1,
                DESCRIPTION_HEADER_2,
                DESCRIPTION_PRICE + price,
                DESCRIPTION_MODE + restricted
        };
    }

    @Override
    public void onShiftRightClick() {
        this.restricted = !this.restricted;
        updateDescription();
        owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.tool.mode", MyTown.instance.LOCAL.getLocalization("mytown.tool.plot.sell.mode"), restricted));
    }

    protected boolean hasPermission(Town town, BlockPos bp) {
        World world = MinecraftServer.getServer().worldServerForDimension(bp.getDim());

        if(world.getBlock(bp.getX(), bp.getY(), bp.getZ()) != Blocks.air) {
            return false;
        }

        if(town == null) {
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.notInTown", owner.townsContainer.getMainTown().getName()));
            return false;
        }

        Plot plot = town.plotsContainer.get(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
        if(plot == null) {
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.plot.sell.notInPlot", town.getName()));
            return false;
        }
        if(!plot.ownersContainer.contains(owner) && !plot.getTown().hasPermission(owner, "mytown.bypass.plot")) {
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.plot.noPermission"));
            return false;
        }
        return true;
    }
}
