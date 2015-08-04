package mytown.api.container;

import com.google.common.collect.ImmutableList;
import mytown.entities.Plot;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.handlers.VisualsHandler;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.List;

public class PlotsContainer {

    private ArrayList<Plot> plots = new ArrayList<Plot>();
    private int maxPlots;

    public PlotsContainer(int maxPlots) {
        this.maxPlots = maxPlots;
    }

    public void addPlot(Plot plot) {
        plots.add(plot);
    }

    /*
    public void removePlot(Plot plot) {
        for (int x = plot.getStartChunkX(); x <= plot.getEndChunkX(); x++) {
            for (int z = plot.getStartChunkZ(); z <= plot.getEndChunkZ(); z++) {
                TownBlock b = getBlockAtCoords(plot.getDim(), x, z);
                if (b != null) {
                    b.removePlot(plot);
                }
            }
        }
        plots.remove(plot);
    }
    */

    public boolean hasPlot(Plot plot) {
        return plots.contains(plot);
    }

    public List<Plot> getPlotsOwned(Resident res) {
        List<Plot> list = new ArrayList<Plot>();
        for (Plot plot : plots) {
            if (plot.hasOwner(res))
                list.add(plot);
        }
        return list;
    }

    public int getAmountPlotsOwned(Resident res) {
        int plotsOwned = 0;
        for (Plot plot : plots) {
            if (plot.hasOwner(res))
                plotsOwned++;
        }
        return plotsOwned;
    }

    public Plot getPlot(String name) {
        for(Plot plot : plots) {
            if(plot.getName().equals(name))
                return plot;
        }
        return null;
    }

    /*
    public Plot getPlotAtCoords(int dim, int x, int y, int z) {
        TownBlock b = getBlockAtCoords(dim, x >> 4, z >> 4);
        if (b != null) {
            return b.getPlotAtCoords(dim, x, y, z);
        }

        return null;
    }


    public Plot getPlotAtResident(Resident res) {
        return getPlotAtCoords(res.getPlayer().dimension, (int) Math.floor(res.getPlayer().posX), (int) Math.floor(res.getPlayer().posY), (int) Math.floor(res.getPlayer().posZ));
    }
    */

    public ImmutableList<Plot> getPlots() {
        return ImmutableList.copyOf(plots);
    }

    public boolean canResidentMakePlot(Resident res) {
        return getAmountPlotsOwned(res) >= maxPlots;
    }

    public void showPlots(Resident res) {
        if(res.getPlayer() instanceof EntityPlayerMP) {
            for (Plot plot : plots) {
                VisualsHandler.instance.markPlotBorders(plot, (EntityPlayerMP) res.getPlayer());
            }
        }
    }

    public void hidePlots(Resident res) {
        if(res.getPlayer() instanceof EntityPlayerMP) {
            for (Plot plot : plots) {
                VisualsHandler.instance.unmarkBlocks((EntityPlayerMP) res.getPlayer(), plot);
            }
        }
    }
}
