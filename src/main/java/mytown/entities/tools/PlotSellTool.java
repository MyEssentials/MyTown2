package mytown.entities.tools;

import myessentials.chat.api.ChatManager;
import myessentials.entities.api.BlockPos;
import myessentials.entities.api.tool.Tool;
import myessentials.entities.api.tool.ToolManager;
import myessentials.localization.api.LocalManager;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.signs.SellSign;
import mytown.util.MyTownUtils;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

/**
 * A tool which places signs that, when right clicked, sells the plots to that person.
 */
public class PlotSellTool extends Tool {

    private int price;
    private boolean restricted = false;
    private Resident owner;

    public PlotSellTool(Resident owner, int price) {
        super(owner.getPlayer(), LocalManager.get("myessentials.tool.name", LocalManager.get("mytown.tool.plot.sell.name")).getLegacyFormattedText()[0]);
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
        return LocalManager.get("mytown.tool.plot.sell.description", price, restricted).getLegacyFormattedText();
    }

    @Override
    public void onShiftRightClick() {
        this.restricted = !this.restricted;
        updateDescription();
        ChatManager.send(owner.getPlayer(), "myessentials.tool.mode", LocalManager.get("mytown.tool.plot.sell.property"), restricted);
    }

    protected boolean hasPermission(Town town, BlockPos bp) {
        World world = MinecraftServer.getServer().worldServerForDimension(bp.getDim());

        if(world.getBlock(bp.getX(), bp.getY(), bp.getZ()) != Blocks.air) {
            return false;
        }

        if(town == null) {
            ChatManager.send(owner.getPlayer(), "mytown.cmd.err.notInTown", owner.townsContainer.getMainTown());
            return false;
        }

        Plot plot = town.plotsContainer.get(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
        if(plot == null) {
            ChatManager.send(owner.getPlayer(), "mytown.cmd.err.plot.sell.notInPlot", town);
            return false;
        }
        if(!plot.ownersContainer.contains(owner) && !plot.getTown().hasPermission(owner, "mytown.bypass.plot")) {
            ChatManager.send(owner.getPlayer(), "mytown.cmd.err.plot.noPermission");
            return false;
        }
        return true;
    }
}
