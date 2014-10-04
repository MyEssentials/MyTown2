package mytown.entities;

import mytown.datasource.MyTownUniverse;
import mytown.entities.flag.FlagType;

/**
 * Created by AfterWind on 9/4/2014.
 * Stores coords and flagname to give whitelist for that block on the flag specified
 */
public class BlockWhitelist {

    public int dim, x, y, z;
    public boolean isDeleted;

    private int db_id;
    private FlagType flagType;



    public BlockWhitelist(int dim, int x, int y, int z, FlagType flagType) {
        this.dim = dim;
        this.x = x;
        this.y = y;
        this.z = z;
        this.flagType = flagType;
    }

    public FlagType getFlagType() {
        return this.flagType;
    }

    public void delete() { this.isDeleted = true; }

    public void setDb_ID(int id) {
        this.db_id = id;
    }

    public int getDb_id() {
        return this.db_id;
    }
}
