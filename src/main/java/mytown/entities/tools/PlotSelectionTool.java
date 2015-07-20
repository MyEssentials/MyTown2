package mytown.entities.tools;

import mytown.MyTown;
import mytown.config.Config;
import myessentials.thread.DelayedThread;
import mytown.datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.handlers.VisualsHandler;
import mytown.proxies.DatasourceProxy;
import mytown.proxies.LocalizationProxy;
import mytown.util.MyTownUtils;
import mytown.util.exceptions.MyTownCommandException;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.util.EnumChatFormatting;
import net.minecraftforge.common.DimensionManager;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Tool that selects two corners of a plot and creates it.
 */
public class PlotSelectionTool extends Tool {

    private static final String NAME = EnumChatFormatting.BLUE + "Selector"; // TODO: Get localization for it, maybe?
    private static final String DESCRIPTION_HEADER = EnumChatFormatting.DARK_AQUA + "Select 2 blocks to make a plot. Shift right-click to change modes.";
    private static final String DESCRIPTION_NAME = EnumChatFormatting.DARK_AQUA + "Name: ";
    private static final String DESCRIPTION_MODE = EnumChatFormatting.DARK_AQUA + "Height dependant: ";

    /**
     * Using integers instead of BlockPos because we want each plot to have a unique set of coordinates.
     */
    private Selection selectionFirst, selectionSecond;
    private String plotName;
    private boolean heightDependant = true;

    public PlotSelectionTool(Resident owner, String plotName) {
        super(owner, NAME);
        this.plotName = plotName;
        giveItemStack(createItemStack(Items.wooden_hoe, DESCRIPTION_HEADER, DESCRIPTION_NAME + plotName, DESCRIPTION_MODE + heightDependant));
    }

    @Override
    public void onItemUse(int dim, int x, int y, int z, int face) {
        Town town = MyTownUtils.getTownAtPosition(dim, x >> 4, z >> 4);

        if(!hasPermission(town, dim, x, y, z)) {
            resetSelection(true, 0);
            return;
        }

        if (selectionFirst != null && selectionFirst.dim != dim) {
            owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.selection.otherDimension"));
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
        heightDependant = !heightDependant;
        setDescription(DESCRIPTION_MODE + heightDependant, 2);
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
        if (town == null || town != owner.getSelectedTown() && selectionFirst != null || selectionFirst != null && town != selectionFirst.town) {
            owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.selection.outside"));
            return false;
        }
        if (!town.canResidentMakePlot(owner)) {
            owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.limit", town.getMaxPlots()));
            return false;
        }
        for(Plot plot : town.getPlots()) {
            if(plot.getName().equals(plotName)) {
                owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.name", plotName));
                return false;
            }
        }
        return true;
    }

    private boolean expandVertically() {
        if(selectionFirst == null || selectionSecond == null)
            return false;

        selectionFirst.y = 0;
        selectionSecond.y = DimensionManager.getWorld(selectionSecond.dim).getActualHeight() - 1;

        if(owner.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.unmarkBlocks((EntityPlayerMP) owner.getPlayer(), owner.getPlayer());

        if(owner.getPlayer() instanceof EntityPlayerMP)
            VisualsHandler.instance.markPlotBorders(selectionFirst.x, selectionFirst.y, selectionFirst.z, selectionSecond.x, selectionSecond.y, selectionSecond.z, selectionFirst.dim, (EntityPlayerMP) owner.getPlayer(), owner.getPlayer());

        return true;
    }


    private void createPlotFromSelection() {
        normalizeSelection();

        int lastX = 1000000, lastZ = 1000000;
        for (int i = selectionFirst.x; i <= selectionSecond.x; i++) {
            for (int j = selectionFirst.z; j <= selectionSecond.z; j++) {

                // Verifying if it's in town
                if (i >> 4 != lastX || j >> 4 != lastZ) {
                    lastX = i >> 4;
                    lastZ = j >> 4;
                    if (!getDatasource().hasBlock(selectionFirst.dim, lastX, lastZ, selectionFirst.town)) {
                        owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.outside"));
                        resetSelection(true, 0);
                        return;
                    }
                }

                // Verifying if it's inside another plot
                for (int k = selectionFirst.y; k <= selectionSecond.y; k++) {
                    Plot plot = selectionFirst.town.getPlotAtCoords(selectionFirst.dim, i, k, j);
                    if (plot != null) {
                        owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.cmd.err.plot.insideOther", plot.getName()));
                        resetSelection(true, 0);
                        return;
                    }
                }
            }
        }

        Plot plot = DatasourceProxy.getDatasource().newPlot(plotName, selectionFirst.town, selectionFirst.dim, selectionFirst.x, selectionFirst.y, selectionFirst.z, selectionSecond.x, selectionSecond.y, selectionSecond.z);
        resetSelection(true, 5);

        getDatasource().savePlot(plot);
        getDatasource().linkResidentToPlot(owner, plot, true);
        owner.sendMessage(LocalizationProxy.getLocalization().getLocalization("mytown.notification.plot.created"));
        deleteItemStack();
    }

    private void normalizeSelection() {
        if(!heightDependant) {
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

    private boolean verifyDimensions() {
        if(!(selectionFirst.town instanceof AdminTown)) {
            if((Math.abs(selectionFirst.x - selectionSecond.x) + 1) * (Math.abs(selectionFirst.z - selectionSecond.z) + 1) < Config.minPlotsArea
                    || Math.abs(selectionFirst.y - selectionSecond.y) + 1 < Config.minPlotsHeight) {
                resetSelection(true, 0);
                throw new MyTownCommandException("mytown.cmd.err.plot.tooSmall", Config.minPlotsArea, Config.minPlotsHeight);
            } else if((Math.abs(selectionFirst.x - selectionSecond.x) + 1) * (Math.abs(selectionFirst.z - selectionSecond.z) + 1) > Config.maxPlotsArea
                    || Math.abs(selectionFirst.y - selectionSecond.y) + 1 > Config.maxPlotsHeight) {
                resetSelection(true, 0);
                throw new MyTownCommandException("mytown.cmd.err.plot.tooLarge", Config.maxPlotsArea, Config.maxPlotsHeight);
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
            this.town = MyTownUniverse.instance.getTownBlock(dim, x >> 4, z >> 4).getTown();
        }
    }
}
