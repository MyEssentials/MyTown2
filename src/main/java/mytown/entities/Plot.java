package mytown.entities;

import myessentials.entities.Volume;
import mypermissions.proxies.PermissionProxy;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.handlers.VisualsHandler;
import mytown.new_datasource.MyTownUniverse;
import net.minecraft.entity.player.EntityPlayerMP;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class Plot {
    private int dbID;
    private final int dim, x1, y1, z1, x2, y2, z2;
    private Town town;
    private String key, name;

    public final Flag.Container flagsContainer = new Flag.Container();
    public final Resident.Container membersContainer = new Resident.Container();
    public final Resident.Container ownersContainer = new Resident.Container();

    public Plot(String name, Town town, int dim, int x1, int y1, int z1, int x2, int y2, int z2) {
        if (x1 > x2) {
            int aux = x2;
            x2 = x1;
            x1 = aux;
        }

        if (z1 > z2) {
            int aux = z2;
            z2 = z1;
            z1 = aux;
        }

        if (y1 > y2) {
            int aux = y2;
            y2 = y1;
            y1 = aux;
        }
        // Second parameter is always highest
        this.name = name;
        this.town = town;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
        this.dim = dim;

        updateKey();
    }

    public boolean isCoordWithin(int dim, int x, int y, int z) {
        return dim == this.dim && x1 <= x && x <= x2 && y1 <= y && y <= y2 && z1 <= z && z <= z2;
    }

    public boolean hasPermission(Resident res, FlagType<Boolean> flagType) {
        if(flagType.configurable ? flagsContainer.getValue(flagType) : flagType.defaultValue) {
            return true;
        }

        if(res == null) {
            return false;
        }

        if(!(membersContainer.contains(res) || ownersContainer.contains(res))) {
            boolean permissionBypass = PermissionProxy.getPermissionManager().hasPermission(res.getUUID(), flagType.getBypassPermission());
            if(!permissionBypass) {
                res.protectionDenial(flagType, ownersContainer.toString());
                return false;
            }
        }
        return true;
    }

    @Override
    public String toString() {
        return String.format("Plot: {Name: %s, Dim: %s, Start: [%s, %s, %s], End: [%s, %s, %s]}", name, dim, x1, y1, z1, x2, y2, z2);
    }

    public Volume toVolume() {
        return new Volume(x1, y1, z1, x2, y2, z2);
    }

    public int getDim() {
        return dim;
    }

    public int getStartX() {
        return x1;
    }

    public int getStartY() {
        return y1;
    }

    public int getStartZ() {
        return z1;
    }

    public int getEndX() {
        return x2;
    }

    public int getEndY() {
        return y2;
    }

    public int getEndZ() {
        return z2;
    }

    public int getStartChunkX() {
        return x1 >> 4;
    }

    public int getStartChunkZ() {
        return z1 >> 4;
    }

    public int getEndChunkX() {
        return x2 >> 4;
    }

    public int getEndChunkZ() {
        return z2 >> 4;
    }

    public Town getTown() {
        return town;
    }

    public String getKey() {
        return key;
    }

    /**
     * Updates the key of the plot if any changes have been made to it.
     */
    private void updateKey() {
        key = String.format("%s;%s;%s;%s;%s;%s;%s", dim, x1, y1, z1, x2, y2, z2);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDbID(int id) {
        this.dbID = id;
    }

    public int getDbID() {
        return this.dbID;
    }

    public static class Container extends ArrayList<Plot> {

        private int maxPlots;

        public Container() {
            this.maxPlots = -1;
        }

        public Container(int maxPlots) {
            this.maxPlots = maxPlots;
        }

        public void remove(Plot plot) {
            for (int x = plot.getStartChunkX(); x <= plot.getEndChunkX(); x++) {
                for (int z = plot.getStartChunkZ(); z <= plot.getEndChunkZ(); z++) {
                    TownBlock b = MyTownUniverse.instance.blocks.get(plot.getDim(), x, z);
                    if (b != null) {
                        for(Iterator<Plot> it = b.plotsContainer.iterator(); it.hasNext(); ) {
                            Plot plotInBlock = it.next();
                            if(plot == plotInBlock) {
                                it.remove();
                            }
                        }
                    }
                }
            }
            super.remove(plot);
        }

        public Plot get(String name) {
            for(Plot plot : this) {
                if(plot.getName().equals(name))
                    return plot;
            }
            return null;
        }

        public Plot get(int dim, int x, int y, int z) {
            for(Plot plot : this) {
                if(plot.isCoordWithin(dim, x, y, z)) {
                    return plot;
                }
            }
            return null;
        }

        public Plot get(Resident res) {
            return get(res.getPlayer().dimension, (int) Math.floor(res.getPlayer().posX), (int) Math.floor(res.getPlayer().posY), (int) Math.floor(res.getPlayer().posZ));
        }

        @Override
        public Plot get(int plotID) {
            for(Plot plot : this) {
                if(plot.getDbID() == plotID) {
                    return plot;
                }
            }
            return null;
        }

        public List<Plot> getPlotsOwned(Resident res) {
            List<Plot> list = new ArrayList<Plot>();
            for (Plot plot : this) {
                if (plot.ownersContainer.contains(res))
                    list.add(plot);
            }
            return list;
        }

        public int getAmountPlotsOwned(Resident res) {
            int plotsOwned = 0;
            for (Plot plot : this) {
                if (plot.ownersContainer.contains(res))
                    plotsOwned++;
            }
            return plotsOwned;
        }

        public int getMaxPlots() {
            return this.maxPlots;
        }

        public void setMaxPlots(int maxPlots) {
            this.maxPlots = maxPlots;
        }

        public boolean canResidentMakePlot(Resident res) {
            return maxPlots == -1 || getAmountPlotsOwned(res) < maxPlots;
        }

        public void show(Resident res) {
            if(res.getPlayer() instanceof EntityPlayerMP) {
                for (Plot plot : this) {
                    VisualsHandler.instance.markPlotBorders(plot, (EntityPlayerMP) res.getPlayer());
                }
            }
        }

        public void hide(Resident res) {
            if(res.getPlayer() instanceof EntityPlayerMP) {
                for (Plot plot : this) {
                    VisualsHandler.instance.unmarkBlocks((EntityPlayerMP) res.getPlayer(), plot);
                }
            }
        }
    }
}
