package mytown.entities.tools;

import myessentials.entities.api.BlockPos;
import myessentials.entities.api.tool.Tool;
import myessentials.entities.api.tool.ToolManager;
import mytown.MyTown;
import mytown.config.Config;
import myessentials.thread.DelayedThread;
import mytown.new_datasource.MyTownUniverse;
import mytown.entities.*;
import mytown.handlers.VisualsHandler;
import mytown.util.MyTownUtils;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.server.MinecraftServer;
import org.apache.commons.lang3.exception.ExceptionUtils;

/**
 * Tool that selects two corners of a plot and creates it.
 */
public class PlotSelectionTool extends Tool {

    private static final String NAME = MyTown.instance.LOCAL.getLocalization("mytown.tool.plot.selection.name").getUnformattedTextForChat();
    private static final String DESCRIPTION_HEADER_1 = MyTown.instance.LOCAL.getLocalization("mytown.tool.plot.selection.description.header1").getUnformattedTextForChat();
    private static final String DESCRIPTION_HEADER_2 = MyTown.instance.LOCAL.getLocalization("mytown.tool.plot.selection.description.header2").getUnformattedTextForChat();
    private static final String DESCRIPTION_NAME = MyTown.instance.LOCAL.getLocalization("mytown.tool.plot.selection.description.name").getUnformattedTextForChat() + " ";
    private static final String DESCRIPTION_MODE = MyTown.instance.LOCAL.getLocalization("mytown.tool.plot.selection.description.mode").getUnformattedTextForChat() + " ";

    private Selection selectionFirst, selectionSecond;
    private String plotName;
    private boolean heightDependent = true;
    private Resident owner;

    public PlotSelectionTool(Resident owner, String plotName) {
        super(owner.getPlayer(), NAME);
        this.owner = owner;
        this.plotName = plotName;
    }

    @Override
    public void onItemUse(BlockPos bp, int face) {
        Town town = MyTownUtils.getTownAtPosition(bp.getDim(), bp.getX() >> 4, bp.getZ() >> 4);

        if(!hasPermission(town)) {
            resetSelection(true, 0);
            return;
        }

        if (selectionFirst != null && selectionFirst.dim != bp.getDim()) {
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.plot.selection.otherDimension"));
            return;
        }

        if (selectionFirst == null) {
            // selectionSecond = null;
            selectionFirst = new Selection(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
            // This is marked twice :P
            if(owner.getPlayer() instanceof EntityPlayerMP) {
                VisualsHandler.instance.markBlock(bp.getX(), bp.getY(), bp.getZ(), bp.getDim(), Blocks.redstone_block, (EntityPlayerMP) owner.getPlayer(), owner.getPlayer());
            }

        } else {
            selectionSecond = new Selection(bp.getDim(), bp.getX(), bp.getY(), bp.getZ());
            createPlotFromSelection();
        }
    }

    @Override
    protected String[] getDescription() {
        return new String[] {
                DESCRIPTION_HEADER_1,
                DESCRIPTION_HEADER_2,
                DESCRIPTION_NAME + plotName,
                DESCRIPTION_MODE + heightDependent
        };
    }

    @Override
    public void onShiftRightClick() {
        heightDependent = !heightDependent;
        updateDescription();
        owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.tool.mode", MyTown.instance.LOCAL.getLocalization("mytown.tool.plot.description.mode"), heightDependent));
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

    protected boolean hasPermission(Town town) {
        if (town == null || town != owner.townsContainer.getMainTown() && selectionFirst != null || selectionFirst != null && town != selectionFirst.town) {
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.plot.selection.outside"));
            return false;
        }
        if (!town.plotsContainer.canResidentMakePlot(owner)) {
            owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.plot.limit", town.plotsContainer.getMaxPlots()));
            return false;
        }
        for(Plot plot : town.plotsContainer) {
            if(plot.getName().equals(plotName)) {
                owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.plot.name", plotName));
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

        MyTown.instance.datasource.savePlot(plot);
        MyTown.instance.datasource.linkResidentToPlot(owner, plot, true);
        owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.notification.plot.created"));
        ToolManager.instance.remove(this);
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
            if((Math.abs(selectionFirst.x - selectionSecond.x) + 1) * (Math.abs(selectionFirst.z - selectionSecond.z) + 1) < Config.instance.minPlotsArea.get()
                    || Math.abs(selectionFirst.y - selectionSecond.y) + 1 < Config.instance.minPlotsHeight.get()) {
                owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.plot.tooSmall", Config.instance.minPlotsArea.get(), Config.instance.minPlotsHeight.get()));
                return false;
            } else if((Math.abs(selectionFirst.x - selectionSecond.x) + 1) * (Math.abs(selectionFirst.z - selectionSecond.z) + 1) > Config.instance.maxPlotsArea.get()
                    || Math.abs(selectionFirst.y - selectionSecond.y) + 1 > Config.instance.maxPlotsHeight.get()) {
                owner.sendMessage(MyTown.instance.LOCAL.getLocalization("mytown.cmd.err.plot.tooLarge", Config.instance.maxPlotsArea.get(), Config.instance.maxPlotsHeight.get()));
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
