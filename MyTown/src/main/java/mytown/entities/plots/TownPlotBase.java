// To be "TownPlot"

package mytown.entities.plots;

import java.util.List;

import mytown.entities.Resident;
import mytown.entities.Town;
import mytown.entities.TownPlot;

import org.lwjgl.util.Point;

public abstract class TownPlotBase {
	
	public int id;
	public int x1, z1, x2, z2;
	public int dim;
	protected Town town;
	protected float price = 0;
	protected String ownerUUID = "";
	protected Resident owner = null;

	public TownPlotBase setPlotStart(int x, int z) {
		x1 = x;
		z1 = z;

		return this;
	}

	public TownPlotBase setPlotEnd(int x, int z) {
		x2 = x;
		z2 = z;

		return this;
	}
	
	/**
	 * Returns the Town this TownPlot belongs to
	 * @return
	 */
	
	
	
	public Town getTown() {
		return town;
	}

	public float getPrice() {
		return price;
	}

	public TownPlotBase setPrice(float price) {
		this.price = price;

		return this;
	}

	public Resident getOwner() {
		return owner;
	}

	public String getOwnerUUID() {
		return ownerUUID;
	}

	public TownPlotBase setOwner(Resident owner) {
		this.owner = owner;
		ownerUUID = owner.getUUID();

		return this;
	}
}
