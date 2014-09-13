package mytown.entities;

import mytown.datasource.MyTownUniverse;

/**
 * Created by AfterWind on 9/4/2014.
 * Stores coords and flagname to give whitelist for that block on the flag specified
 */
public class BlockWhitelist {

    private int db_id;
    public int dim, x, y, z;
    private String flagName;
    private int plotID;

    public BlockWhitelist(int dim, int x, int y, int z, String flagName, int plotID) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.flagName = flagName;
        this.plotID = plotID;
    }

    public String getFlagName() {
        return this.flagName;
    }

    public int getPlotID() {
        return plotID;
    }

    public Plot getPlot() {
        if(plotID != -1) {
            return MyTownUniverse.getInstance().getPlotsMap().get(plotID);
        } else {
            return null;
        }
    }

    public void setDb_ID(int id) {this.db_id = id; }
    public int getDb_id() {return this.db_id; }
}
