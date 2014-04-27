package mytown.entities;

// TODO Add Comments

public class TownPlot {
	public int id;
	public int x1, y1, z1;
	public int x2, y2, z2;
	public int dim;
	private Town town;
	private float price = 0;
	private String ownerUUID = "";
	private Resident owner = null;

	public TownPlot(Town town, int x1, int y1, int z1, int x2, int y2, int z2) {
		this.town = town;
		setPlotStart(x1, y1, z1);
		setPlotEnd(x2, y2, z2);
	}

	public TownPlot setPlotStart(int x, int y, int z) {
		x1 = x;
		y1 = y;
		z1 = z;

		return this;
	}

	public TownPlot setPlotEnd(int x, int y, int z) {
		x2 = x;
		y2 = y;
		z2 = z;

		return this;
	}

	/**
	 * Returns the Town this TownPlot belongs to
	 * 
	 * @return
	 */
	public Town getTown() {
		return town;
	}

	public float getPrice() {
		return price;
	}

	public TownPlot setPrice(float price) {
		this.price = price;

		return this;
	}

	public Resident getOwner() {
		return owner;
	}

	public String getOwnerUUID() {
		return ownerUUID;
	}

	public TownPlot setOwner(Resident owner) {
		this.owner = owner;
		ownerUUID = owner.getUUID();

		return this;
	}
}