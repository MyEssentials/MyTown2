package mytown.modules;

import mytown.entities.Resident;

/**
 * Created by AfterWind on 7/10/2014.
 * Retains a set of coordinates and the owner of the block at those coordinates
 */
public class ResidentBlockCoordsPair {
    int x, y, z, dim;
    Resident owner;
    int counter = 20;

    public ResidentBlockCoordsPair(int x, int y, int z, int dim, Resident owner) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.dim = dim;
        this.owner = owner;
    }
}
