package mytown.entities.tools;

import mytown.MyTown;
import mytown.config.Config;
import myessentials.thread.DelayedThread;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.handlers.VisualsHandler;
import mytown.util.MyTownUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Tool that selects two corners of a plot and creates it.
 */
public class PlotSelectionTool extends Tool {

    private static final String NAME = EnumChatFormatting.BLUE + "Selector"; // TODO: Get localization for it, maybe?
    private static final String DESCRIPTION_HEADER_1 = EnumChatFormatting.DARK_AQUA + "Select 2 blocks to make a plot.";
    private static final String DESCRIPTION_HEADER_2 = EnumChatFormatting.DARK_AQUA + "Shift right-click air to change modes.";
    private static final String DESCRIPTION_NAME = EnumChatFormatting.DARK_AQUA + "Name: ";
    private static final String DESCRIPTION_MODE = EnumChatFormatting.DARK_AQUA + "Height dependent: ";

    /**
     * Using integers instead of BlockPos because we want each plot to have a unique set of coordinates.
     */
    private Selection selectionFirst, selectionSecond;
    private String plotName;
    private boolean heightDependent = true;

    public PlotSelectionTool(Resident owner, String plotName) {
        super(owner, NAME);
        this.plotName = plotName;
        giveItemStack(createItemStack(Items.wooden_hoe, DESCRIPTION_HEADER_1, DESCRIPTION_HEADER_2, DESCRIPTION_NAME + plotName, DESCRIPTION_MODE + heightDependent));
    }

    @Override
    public void onItemUse(int dim, int x, int y, int z, int face) {
        Town town = MyTownUtils.getTownAtPosition(dim, x >> 4, z >> 4);

        if(!hasPermission(town, dim, x, y, z)) {
            resetSelection(true, 0);
            return;
        }

        if (selectionFirst != null && selectionFirst.dim != dim) {
            owner.sendMessage(getLocal().getLocalization("mytown.cmd.err.plot.selection.otherDimension"));
            return;
        }

        if (selectionFirst == null) {
            // selectionSecond = null;
            selectionFirst = new Selection(dim, x, y, z);
            // This is marked twice :P
            if(owner.getPlayer() instanceof EntityPlayerMP) {
                VisualsHandler.instance.markBlock(x, y, z, dim, Blocks.redstone_block, (EntityPlayerMP) owner.getPlayer(), owner.getPlayer());
            }

        } else {
            selectionSecond = new Selection(dim, x, y, z);
            createPlotFromSelection();
        }
    }

    @Override
    public void onShiftRightClick() {
        heightDependent = !heightDependent;
        setDescription(DESCRIPTION_MODE + heightDependent, 3);
        owner.sendMessage(getLocal().getLocalization("mytown.notification.tool.mode", "heightDependent", heightDependent));
    }

    public void resetSelection(boolean resetBlocks, int delay) {
        this.selectionFirst = null;
        this.selectionSecond = null;

        if(resetBlocks && owner.getPlayer() instanceof EntityPlayerMP) {
            if(delay <= 0) {
                VisualsHandler.instance.unmarkBlocks((EntityPlayerMP) owner.getPlayer(), owner.getPlayer());
            } else {
                try {
                    new DelayedThread(delay, VisualsHandler.class.getMethod("unmarkBlocks", EntityPlayerMP.class, Object.class), VisualsHandler.instance, owner.getPlayer(), owner.getPlayer()).start();
                } catch (Exception ex) {
                    MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
                }
            }
        }
    }

    @Override
    protected boolean hasPermission(Town town, int dim, int x, int y, int z) {
        if (town == null || town != owner.townsContainer.getMainTown() && selectionFirst != null || selectionFirst != null && town != selectionFirst.town) {
            owner.sendMessage(getLocal().getLocalization("mytown.cmd.err.plot.selection.outside"));
            return false;
        }
        if (!town.plotsContainer.canResidentMakePlot(owner)) {
            owner.sendMessage(getLocal().getLocalization("mytown.cmd.err.plot.limit", town.plotsContainer.getMaxPlots()));
            return false;
        }
        for(Plot plot : town.plotsContainer) {
            if(plot.getName().equals(plotName)) {
                owner.sendMessage(getLocal().getLocalization("mytown.cmd.err.plot.name", plotName));
                return false;
            }
        }
        return true;
    }

    private boolean expandVertically() {
        if(selectionFirst == null || selectionSecond == null)
            return false;

        selectionFirst.y = 0;
        selectionSecond.y = MinecraftServer.getServer().worldServerForDimension(selectionSecond.dim).getActualHeight() - 1;

        if(owner.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.unmarkBlocks((EntityPlayerMP) owner.getPlayer(), owner.getPlayer());

        if(owner.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.markPlotBorders(selectionFirst.x, selectionFirst.y, selectionFirst.z, selectionSecond.x, selectionSecond.y, selectionSecond.z, selectionFirst.dim, (EntityPlayerMP) owner.getPlayer(), owner.getPlayer());

        return true;
    }


    private void createPlotFromSelection() {
        normalizeSelection();
        if(!isProperSize()) {
            resetSelection(true, 0);
            return;
        }

        int lastX = 1000000, lastZ = 1000000;
        for (int i = selectionFirst.x; i <= selectionSecond.x; i++) {
            for (int j = selectionFirst.z; j <= selectionSecond.z; j++) {

                // Verifying if it's in town
                if (i >> 4 != lastX || j >> 4 != lastZ) {
                    lastX = i >> 4;
                    lastZ = j >> 4;
                    TownBlock block = MyTownUniverse.instance.blocks.get(selectionFirst.dim, lastX, lastZ);
                    if (block == null || block.getTown() != selectionFirst.town) {
                        owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.plot.outside"));
                        resetSelection(true, 0);
                        return;
                    }
                }

                // Verifying if it's inside another plot
                for (int k = selectionFirst.y; k <= selectionSecond.y; k++) {
                    Plot plot = selectionFirst.town.plotsContainer.get(selectionFirst.dim, i, k, j);
                    if (plot != null) {
                        owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.plot.insideOther", plot.getName()));
                        resetSelection(true, 0);
                        return;
                    }
                }
            }
        }

        Plot plot = MyTownUniverse.instance.newPlot(plotName, selectionFirst.town, selectionFirst.dim, selectionFirst.x, selectionFirst.y, selectionFirst.z, selectionSecond.x, selectionSecond.y, selectionSecond.z);
        resetSelection(true, 5);

        getDatasource().savePlot(plot);
        getDatasource().linkResidentToPlot(owner, plot, true);
        owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.plot.created"));
        deleteItemStack();
    }

    private void normalizeSelection() {
        if(!heightDependent) {
            expandVertically();
        } else {
            if(owner.getPlayer() instanceof EntityPlayerMP)
                VisualsHandler.instance.markCorners(selectionFirst.x, selectionFirst.y, selectionFirst.z, selectionSecond.x, selectionSecond.y, selectionSecond.z, selectionFirst.dim, (EntityPlayerMP) owner.getPlayer());
        }

        if (selectionSecond.x < selectionFirst.x) {
            int aux = selectionFirst.x;
            selectionFirst.x = selectionSecond.x;
            selectionSecond.x = aux;
        }
        if (selectionSecond.y < selectionFirst.y) {
            int aux = selectionFirst.y;
            selectionFirst.y = selectionSecond.y;
            selectionSecond.y = aux;
        }
        if (selectionSecond.z < selectionFirst.z) {
            int aux = selectionFirst.z;
            selectionFirst.z = selectionSecond.z;
            selectionSecond.z = aux;
        }
    }

    private boolean isProperSize() {
        if(!(selectionFirst.town instanceof AdminTown)) {
            if((Math.abs(selectionFirst.x - selectionSecond.x) + 1) * (Math.abs(selectionFirst.z - selectionSecond.z) + 1) < Config.minPlotsArea
                    || Math.abs(selectionFirst.y - selectionSecond.y) + 1 < Config.minPlotsHeight) {
                owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.plot.tooSmall", Config.minPlotsArea, Config.minPlotsHeight));
                return false;
            } else if((Math.abs(selectionFirst.x - selectionSecond.x) + 1) * (Math.abs(selectionFirst.z - selectionSecond.z) + 1) > Config.maxPlotsArea
                    || Math.abs(selectionFirst.y - selectionSecond.y) + 1 > Config.maxPlotsHeight) {
                owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.plot.tooLarge", Config.maxPlotsArea, Config.maxPlotsHeight));
                return false;
            }
        }
        return true;
    }

    private class Selection {
        private int x, y, z, dim;
        private Town town;

        public Selection(int dim, int x, int y, int z) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.dim = dim;
            // Not checking for null since this should not be created if the town is null.
            this.town = MyTownUniverse.instance.blocks.get(dim, x >> 4, z >> 4).getTown();
        }
    }
}
