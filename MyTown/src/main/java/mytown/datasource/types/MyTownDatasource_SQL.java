package mytown.datasource.types;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import mytown.Constants;
import mytown.api.datasource.MyTownDatasource;
import mytown.core.utils.config.ConfigProperty;
import mytown.entities.Nation;
import mytown.entities.Rank;
import mytown.entities.Resident;
import mytown.entities.TownBlock;
import mytown.entities.TownPlot;
import mytown.entities.flag2.TownFlag;
import mytown.entities.town.AdminTown;
import mytown.entities.town.Town;
import mytown.interfaces.ITownFlag;
import mytown.interfaces.ITownPlot;

import com.google.common.collect.Lists;

// TODO Add logging
// TODO Optimize if needed?

/**
 * Base class for all SQL based datasources
 * 
 * @author Joe Goett
 */
public abstract class MyTownDatasource_SQL extends MyTownDatasource {
	/**
	 * Used to determine how to auto increment. MySQL and SQLite uses different names
	 */
	protected static String autoIncrement = "AUTO_INCREMENT";

	protected Connection conn;
	protected Object lock = new Object();
	@ConfigProperty(category="datasource", comment="The prefix of each of the tables. <prefix>tablename")
	protected String prefix = "";

	// //////////////////////////////////////
	// Helpers
	// //////////////////////////////////////

	/**
	 * Returns a prepared statement using the given sql
	 * 
	 * @param sql
	 * @return
	 * @throws Exception
	 */
	protected PreparedStatement prepare(String sql) throws Exception {
		return prepare(sql, false);
	}

	/**
	 * Returns a PreparedStatement using the given sql
	 * 
	 * @param sql
	 * @param returnGenerationKeys
	 * @return
	 * @throws Exception
	 */
	protected PreparedStatement prepare(String sql, boolean returnGenerationKeys) throws Exception {
		if (conn == null)
			throw new SQLException("No SQL Connection");
		PreparedStatement statement = conn.prepareStatement(sql, returnGenerationKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS);

		return statement;
	}

	/**
	 * Returns the current {@link Connection} object
	 * 
	 * Useful for getting info on the connection for logging, or for getting the current warnings
	 * 
	 * @return
	 */
	public Connection getConnection() {
		return conn;
	}

	// //////////////////////////////////////
	// Implementation
	// //////////////////////////////////////

	@Override
	public void loadResidents() throws Exception {
		synchronized (lock) {
			ResultSet set = null;
			PreparedStatement statement = prepare("SELECT * FROM " + prefix + "Residents");
			set = statement.executeQuery();

			while (set.next()) {
				Resident res = new Resident(set.getString("UUID"));
				addResident(res);
			}
		}
	}

	@Override
	public void loadNations() throws Exception {
		synchronized (lock) {
			ResultSet set = null;
			PreparedStatement statement = prepare("SELECT * FROM " + prefix + "Nations");
			set = statement.executeQuery();

			while (set.next()) {
				addNation(new Nation(set.getString("Name"), set.getInt("ExtraBlocks")));
			}
		}
	}

	@Override
	public void loadTowns() throws Exception {
		synchronized (lock) {
			ResultSet set = null;
			PreparedStatement statement = prepare("SELECT * FROM " + prefix + "Towns");
			set = statement.executeQuery();

			while (set.next()) {
				if (set.getString("Type").equals("A")) {
					AdminTown town = new AdminTown(set.getString("Name"));
					town.setSpawn(set.getDouble("SpawnX"), set.getDouble("SpawnY"), set.getDouble("SpawnZ"), set.getInt("SpawnDim"));
					addTown(town);
					loadTownBlocks(town);
				} else {
					Town town = new Town(set.getString("Name"), set.getInt("ExtraBlocks"));
					town.setSpawn(set.getDouble("SpawnX"), set.getDouble("SpawnY"), set.getDouble("SpawnZ"), set.getInt("SpawnDim"));
					addTown(town);
					loadTownBlocks(town);
				}

			}
		}
	}

	@Override
	public void loadTownBlocks(Town town) throws Exception {
		synchronized (lock) {
			if (town == null)
				return;

			ResultSet set = null;
			PreparedStatement statement = prepare("SELECT * FROM " + prefix + "TownBlocks WHERE TownName=?");
			statement.setString(1, town.getName());
			set = statement.executeQuery();

			while (set.next()) {
				TownBlock block = new TownBlock(set.getInt("Id"), town, set.getInt("X"), set.getInt("Z"), set.getInt("Dim"));
				town.addTownBlock(block);
				addTownBlock(block);
			}
		}
	}

	@Override
	public void loadRanks() throws Exception {
		synchronized (lock) {
			ResultSet set = null;
			PreparedStatement statement = prepare("SELECT * FROM " + prefix + "Ranks");
			set = statement.executeQuery();

			while (set.next()) {
				ArrayList<String> list = new ArrayList<String>();
				list.addAll(Arrays.asList(set.getString("Nodes").split(" "))); // Worst workaround, need to be changed
				Rank rank = new Rank(set.getString("Name"), list, towns.get(set.getString("TownName")));
				rank.getTown().addRank(rank); //
				addRank(rank);
				log.info("Adding rank " + rank.getName() + ", " + rank.getTown().getName());
			}
		}
	}

	@Override
	public void loadPlots() throws Exception {
		synchronized (lock) {
			ResultSet set = null;
			PreparedStatement statement = prepare("SELECT * FROM " + prefix + "Plots");
			set = statement.executeQuery();

			while (set.next()) {
				// TODO: Verify for the other types of plots
				if (set.getString("Type").equals("T")) {
					int x1 = set.getInt("X1");
					int y1 = set.getInt("Y1");
					int z1 = set.getInt("Z1");
					int x2 = set.getInt("X2");
					int y2 = set.getInt("Y2");
					int z2 = set.getInt("Z2");
					int dim = set.getInt("Dim");

					String name = set.getString("Name");

					Town town = getTown(set.getString("TownName"));
					Resident owner = getResident(set.getString("Owner"));

					TownPlot plot = new TownPlot(dim, x1, y1, z1, x2, y2, z2, town, owner, name);
					System.out.println(name);
					town.addTownPlot(plot);
					addPlot(plot);
					log.info("Adding plot in town " + town.getName());
				}
			}
		}
	}

	@Override
	public void loadPlotFlags() throws Exception {
		synchronized (lock) {
			ResultSet set = null;
			PreparedStatement statement = prepare("SELECT * FROM " + prefix + "PlotFlags");
			set = statement.executeQuery();

			while (set.next()) {
				int id = set.getInt("Id");
				String name = set.getString("Name");
				String desc = set.getString("Description");
				boolean value = set.getBoolean("Value");
				String plotKey = set.getString("TownPlotKey");

				TownFlag flag = new TownFlag(name, desc, value);
				flag.setDB_ID(id);
				ITownPlot plot = plots.get(plotKey);
				plot.addFlag(flag);
			}
		}
	}

	@Override
	public void loadTownFlags() throws Exception {
		synchronized (lock) {
			ResultSet set = null;
			PreparedStatement statement = prepare("SELECT * FROM " + prefix + "TownFlags");
			set = statement.executeQuery();

			while (set.next()) {
				int id = set.getInt("Id");
				String name = set.getString("Name");
				String desc = set.getString("Description");
				boolean value = set.getBoolean("Value");
				String townName = set.getString("TownName");

				TownFlag flag = new TownFlag(name, desc, value);
				flag.setDB_ID(id);
				Town town = towns.get(townName);
				town.addFlag(flag);
			}
		}
	}

	@Override
	public void updateTown(Town town) throws Exception { // TODO Allow changing Town name?
		synchronized (lock) {
			PreparedStatement statement = prepare("UPDATE " + prefix + "Towns SET Name=?,ExtraBlocks=?,Type=?,SpawnX=?,SpawnY=?,SpawnZ=?,SpawnDim=? WHERE Name=?", true);
			statement.setString(1, town.getName());
			statement.setInt(2, town.getExtraBlocks());
			statement.setString(3, town.getName());
			statement.setDouble(4, town.getSpawnX());
			statement.setDouble(5, town.getSpawnY());
			statement.setDouble(6, town.getSpawnZ());
			statement.setInt(7, town.getSpawnDim());
			statement.setString(8, town.getType());
			statement.executeUpdate();
		}
	}

	@Override
	public void updateResident(Resident resident) throws Exception {
		synchronized (lock) {
			PreparedStatement statement = prepare("UPDATE " + prefix + "Residents SET SelectedTownName=? WHERE UUID=?", true);
			statement.setString(1, resident.getSelectedTown().getName());
			statement.setString(2, resident.getUUID());
			statement.executeUpdate();
		}
	}

	@Override
	public void updateNation(Nation nation) throws Exception { // TODO Allow changing Nation name?
		synchronized (lock) {
			PreparedStatement statement = prepare("UPDATE " + prefix + "Nations SET Name=?,ExtraBlocks=? WHERE Name=?", true);
			statement.setString(1, nation.getName());
			statement.setInt(2, nation.getExtraBlocksPerTown());
			statement.setString(3, nation.getName());
			statement.executeUpdate();
		}
	}

	@Override
	public void updateTownBlock(TownBlock block) throws Exception {
		synchronized (lock) {
			PreparedStatement statement = prepare("UPDATE " + prefix + "TownBlocks SET X=?,Z=?,Dim=? WHERE Id=?", true);
			statement.setInt(1, block.getX());
			statement.setInt(2, block.getZ());
			statement.setInt(3, block.getDim());
			statement.setInt(4, block.getId());
			statement.executeUpdate();
		}
	}

	@Override
	public void updateRank(Rank rank) throws Exception {
		synchronized (lock) {
			PreparedStatement statement = prepare("UPDATE " + prefix + "Ranks SET Name=?,Nodes=?,TownName=? WHERE Key=?");
			statement.setString(1, rank.getName());
			statement.setString(2, rank.getPermissionsWithFormat());
			statement.setString(3, rank.getTown().getName());
			statement.setString(4, rank.getKey());
			statement.executeUpdate();
			rank.updateKey();
		}
	}

	@Override
	public void updatePlot(ITownPlot plot) throws Exception {
		synchronized (lock) {
			PreparedStatement statement = prepare("UPDATE " + prefix + "Plots SET Dim=?,X1=?,Y1=?,Z1=?,X2=?,Y2=?,Z2=?,TownName=?,Owner=?,Type=?,Name=? WHERE Key=?");
			statement.setInt(1, plot.getDim());
			statement.setInt(2, plot.getStartX());
			statement.setInt(3, plot.getStartY());
			statement.setInt(4, plot.getStartZ());
			statement.setInt(5, plot.getEndX());
			statement.setInt(6, plot.getEndY());
			statement.setInt(7, plot.getEndZ());
			statement.setString(8, plot.getTown().getName());
			statement.setString(9, plot.getOwner().getUUID());
			statement.setString(10, "T");
			statement.setString(11, plot.getName());
			statement.setString(12, plot.getKey());
			statement.executeUpdate();
			plot.updateKey();
		}
	}

	@Override
	public void updatePlotFlag(ITownFlag flag) throws Exception {
		synchronized (lock) {
			PreparedStatement statement = prepare("UPDATE " + prefix + "PlotFlags SET Name=?,Description=?,Value=? WHERE Id=?");
			statement.setString(1, flag.getName());
			statement.setString(2, flag.getLocalizedDescription());
			statement.setBoolean(3, flag.getValue());
			statement.setInt(4, flag.getDB_ID());
			statement.executeUpdate();
		}
	}

	@Override
	public void updateTownFlag(ITownFlag flag) throws Exception {
		synchronized (lock) {
			PreparedStatement statement = prepare("UPDATE " + prefix + "TownFlags SET Name=?,Description=?,Value=? WHERE Id=?");
			statement.setString(1, flag.getName());
			statement.setString(2, flag.getLocalizedDescription());
			statement.setBoolean(3, flag.getValue());
			statement.setInt(4, flag.getDB_ID());
			statement.executeUpdate();
		}
	}

	@Override
	public void insertTown(Town town) throws Exception {
		synchronized (lock) {
			addTown(town);
			PreparedStatement statement = prepare("INSERT INTO " + prefix + "Towns (Name,ExtraBlocks,Type,SpawnX,SpawnY,SpawnZ,SpawnDim) VALUES (?,?,?,?,?,?,?)", true);
			statement.setString(1, town.getName());
			statement.setInt(2, town.getExtraBlocks());
			statement.setString(3, town.getType());
			statement.setDouble(4, town.getSpawnX());
			statement.setDouble(5, town.getSpawnY());
			statement.setDouble(6, town.getSpawnZ());
			statement.setInt(7, town.getSpawnDim());
			statement.executeUpdate();
			if (!(town instanceof AdminTown)) {
				for (String s : Constants.DEFAULT_RANK_VALUES.keySet()) {
					insertRank(new Rank(s, Constants.DEFAULT_RANK_VALUES.get(s), town));
				}
			}
		}
	}

	@Override
	public void insertResident(Resident resident) throws Exception {
		synchronized (lock) {
			addResident(resident);
			PreparedStatement statement = prepare("INSERT INTO " + prefix + "Residents (UUID, Joined, LastLogin) VALUES (?, ?, ?)", true);
			statement.setString(1, resident.getUUID());
			statement.setInt(2, 0);
			statement.setInt(3, 0);
			statement.executeUpdate();
		}
	}

	@Override
	public void insertNation(Nation nation) throws Exception {
		synchronized (lock) {
			addNation(nation);
			PreparedStatement statement = prepare("INSERT INTO " + prefix + "Nations (Name,ExtraBlocks) VALUES (?,?)", true);
			statement.setString(1, nation.getName());
			statement.setInt(2, nation.getExtraBlocksPerTown());
			statement.executeUpdate();
		}
	}

	@Override
	public void insertTownBlock(TownBlock townBlock) throws Exception {
		synchronized (lock) {
			addTownBlock(townBlock);
			townBlock.getTown().addTownBlock(townBlock);

			PreparedStatement statement = prepare("INSERT INTO " + prefix + "TownBlocks (X, Z, Dim, TownName) VALUES (?, ?, ?, ?)", true);
			statement.setInt(1, townBlock.getX());
			statement.setInt(2, townBlock.getZ());
			statement.setInt(3, townBlock.getDim());
			statement.setString(4, townBlock.getTown().getName());
			statement.executeUpdate();

			ResultSet rs = statement.getGeneratedKeys();
			if (!rs.next())
				throw new RuntimeException("Id wasn't returned for new TownBlock " + townBlock.getKey());

			townBlock.setId(rs.getInt(1));
		}
	}

	@Override
	public void insertRank(Rank rank) throws Exception {
		synchronized (lock) {
			addRank(rank);
			rank.getTown().addRank(rank);

			PreparedStatement statement = prepare("INSERT INTO " + prefix + "Ranks (Key, Name, Nodes, TownName) VALUES (?, ?, ?, ?)", true);
			statement.setString(1, rank.getKey());
			statement.setString(2, rank.getName());
			statement.setString(3, rank.getPermissionsWithFormat());
			statement.setString(4, rank.getTown().getName());
			statement.executeUpdate();
		}
	}

	@Override
	public void insertPlot(ITownPlot plot) throws Exception {
		synchronized (lock) {
			addPlot(plot);
			plot.getTown().addTownPlot(plot);
			PreparedStatement statement = prepare("INSERT INTO " + prefix + "Plots (Key, Dim, X1, Y1, Z1, X2, Y2, Z2, TownName, Owner, Name) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)");
			statement.setString(1, plot.getKey());
			statement.setInt(2, plot.getDim());
			statement.setInt(3, plot.getStartX());
			statement.setInt(4, plot.getStartY());
			statement.setInt(5, plot.getStartZ());
			statement.setInt(6, plot.getEndX());
			statement.setInt(7, plot.getEndY());
			statement.setInt(8, plot.getEndZ());
			statement.setString(9, plot.getTown().getName());
			statement.setString(10, plot.getOwner().getUUID());
			statement.setString(11, plot.getName());
			statement.executeUpdate();

			log.info("Added new plot in town");
		}
	}

	@Override
	public void insertPlotFlag(ITownPlot plot, ITownFlag flag) throws Exception {
		synchronized (lock) {
			plot.addFlag(flag);
			PreparedStatement statement = prepare("INSERT INTO " + prefix + "PlotFlags (Name, Description, TownPlotKey, Value) VALUES(?,?,?,?)");
			statement.setString(1, flag.getName());
			statement.setString(2, flag.getLocalizedDescription());
			statement.setString(3, plot.getKey());
			statement.setBoolean(4, flag.getValue());
			statement.executeUpdate();

			// For keeping track in the database without resorting to store additional data in the flag
			ResultSet generatedKeys = statement.getGeneratedKeys();
			if (generatedKeys.next()) {
				flag.setDB_ID(generatedKeys.getInt(1));
			} else
				throw new SQLException("Did not auto-generate key");
		}

	}

	@Override
	public void insertTownFlag(Town town, ITownFlag flag) throws Exception {
		synchronized (lock) {
			town.addFlag(flag);
			PreparedStatement statement = prepare("INSERT INTO " + prefix + "TownFlags (Name, Description, TownName, Value) VALUES(?,?,?,?)");
			statement.setString(1, flag.getName());
			statement.setString(2, flag.getLocalizedDescription());
			statement.setString(3, town.getName());
			statement.setBoolean(4, flag.getValue());
			statement.executeUpdate();

			// For keeping track in the database without resorting to store additional data in the flag
			ResultSet generatedKeys = statement.getGeneratedKeys();
			if (generatedKeys.next()) {
				flag.setDB_ID(generatedKeys.getInt(1));
			} else
				throw new SQLException("Did not auto-generate key");
		}

	}

	@Override
	public boolean deleteTown(Town town) throws Exception {
		synchronized (lock) {
			removeTown(town);
			PreparedStatement statement;

			statement = prepare("DELETE FROM " + prefix + "Towns WHERE Name=?", false);
			statement.setString(1, town.getName());

			return statement.executeUpdate() != 0;
		}
	}

	@Override
	public boolean deleteNation(Nation nation) throws Exception {
		synchronized (lock) {
			removeNation(nation);
			PreparedStatement statement;

			statement = prepare("DELETE FROM " + prefix + " Nations WHERE Name=?", false);
			statement.setString(1, nation.getName());

			return statement.executeUpdate() != 0;
		}
	}

	@Override
	public boolean deleteTownBlock(TownBlock townBlock) throws Exception {
		synchronized (lock) {
			removeTownBlock(townBlock);
			PreparedStatement statement = prepare("DELETE FROM " + prefix + " TownBlocks WHERE X=? AND Z=? AND Dim=?", false);
			statement.setInt(1, townBlock.getX());
			statement.setInt(2, townBlock.getZ());
			statement.setInt(3, townBlock.getDim());
			return statement.executeUpdate() != 0;
		}
	}

	@Override
	public boolean deleteResident(Resident resident) throws Exception {
		synchronized (lock) {
			removeResident(resident);
			PreparedStatement statement;

			statement = prepare("DELETE FROM " + prefix + " Residents WHERE UUID=?", false);
			statement.setString(1, resident.getUUID());

			return statement.executeUpdate() != 0;
		}
	}

	@Override
	public boolean deleteRank(Rank rank) throws Exception {
		if (rank.getName().equals("Resident"))
			return false;
		synchronized (lock) {
			removeRank(rank);
			rank.getTown().removeRank(rank);

			PreparedStatement statement = prepare("DELETE FROM " + prefix + "Ranks WHERE Key=?", false);
			statement.setString(1, rank.getKey());

			return statement.executeUpdate() != 0;
		}
	}

	@Override
	public boolean deletePlot(ITownPlot plot) throws Exception {
		synchronized (lock) {
			removePlot(plot);
			plot.getTown().removeTownPlot(plot);

			PreparedStatement statement = prepare("DELETE FROM " + prefix + "Plots WHERE Key=?", false);
			statement.setString(1, plot.getKey());

			return statement.executeUpdate() != 0;
		}
	}

	@Override
	public boolean deletePlotFlag(ITownFlag flag) throws Exception {
		synchronized (lock) {
			PreparedStatement statement = prepare("DELETE FROM " + prefix + "PlotFlags WHERE Name=?", false);
			statement.setString(1, flag.getName());

			return statement.executeUpdate() != 0;
		}
	}

	@Override
	public boolean deleteTownFlag(ITownFlag flag) throws Exception {
		synchronized (lock) {
			PreparedStatement statement = prepare("DELETE FROM " + prefix + "PlotFlags WHERE Name=?", false);
			statement.setString(1, flag.getName());

			return statement.executeUpdate() != 0;
		}
	}

	@Override
	public void loadResidentToTownLinks() throws Exception {
		synchronized (lock) {
			PreparedStatement statement = prepare("SELECT * FROM " + prefix + " ResidentsToTowns", true);
			ResultSet set = statement.executeQuery();

			while (set.next()) {
				Resident res = getResident(set.getString("Owner"));
				Town town = getTown(set.getString("TownName"));
				Rank rank = getRank(set.getString("Rank"));
				boolean isSelectedTown = set.getBoolean("IsSelectedTown");

				// Do actual link
				res.addTown(town);
				if (isSelectedTown) {
					res.setSelectedTown(town);
				}
				town.addResident(res, rank);
				town.addRank(rank);
			}
		}
	}

	@Override
	public void loadTownToNationLinks() throws Exception {
		synchronized (lock) {
			PreparedStatement statement = prepare("SELECT * FROM " + prefix + " TownsToNations", true);
			ResultSet set = statement.executeQuery();

			while (set.next()) {
				Town town = getTown(set.getString("TownName"));
				Nation nation = getNation(set.getString("NationName"));
				Nation.Rank rank = Nation.Rank.parse(set.getString("Rank"));

				// Do actual link
				town.addNation(nation);
				nation.addTown(town, rank);
			}
		}
	}

	@Override
	public void linkResidentToTown(Resident resident, Town town, Rank rank) throws Exception {
		synchronized (lock) {
			resident.addTown(town);
			town.addResident(resident, rank);

			PreparedStatement statement = prepare("INSERT INTO " + prefix + " ResidentsToTowns (TownName, Owner, Rank, IsSelectedTown) VALUES (?, ?, ?, ?)", true);
			statement.setString(1, town.getName());
			statement.setString(2, resident.getUUID());
			statement.setString(3, rank.getKey());
			statement.setBoolean(4, false);
			statement.executeUpdate();
		}
	}

	@Override
	public void linkTownToNation(Town town, Nation nation, Nation.Rank rank) throws Exception {
		synchronized (lock) {
			town.addNation(nation);
			nation.addTown(town, rank);

			PreparedStatement statement = prepare("INSERT INTO " + prefix + " TownsToNations (TownName, NationName, Rank) VALUES(?, ?, ?)", true);
			statement.setString(1, town.getName());
			statement.setString(2, nation.getName());
			statement.setString(3, rank.toString());
			statement.executeQuery();
		}
	}

	@Override
	public void unlinkResidentFromTown(Resident resident, Town town) throws Exception {
		synchronized (lock) {
			resident.removeResidentFromTown(town);
			town.removeResident(resident);

			PreparedStatement statement = prepare("DELETE FROM " + prefix + " ResidentsToTowns WHERE TownName=? AND Owner=?", false);
			statement.setString(1, town.getName());
			statement.setString(2, resident.getUUID());
			statement.executeUpdate();
		}
	}

	@Override
	public void unlinkTownFromNation(Town town, Nation nation) throws Exception {
		synchronized (lock) {
			nation.removeTown(town);
			town.removeNation(nation);

			PreparedStatement statement = prepare("DELETE FROM " + prefix + " TownsToNations WHERE TownName=? AND NationName=?", false);
			statement.setString(1, town.getName());
			statement.setString(2, nation.getName());
			statement.executeUpdate();
		}
	}

	@Override
	public void updateLinkResidentToTown(Resident resident, Town town) throws Exception {
		synchronized (lock) {
			PreparedStatement statement;

			// If it's the selected town then change all others not to be selected
			if (resident.getSelectedTown() == town) {
				statement = prepare("UPDATE " + prefix + "ResidentsToTowns SET IsSelectedTown=? WHERE Owner=? AND IsSelectedTown=?");
				statement.setBoolean(1, false);
				statement.setString(2, resident.getUUID());
				statement.setBoolean(3, true);
				statement.executeUpdate();
			}
			statement = prepare("UPDATE " + prefix + "ResidentsToTowns SET IsSelectedTown=?, Rank=? WHERE TownName=? AND Owner=?");
			statement.setBoolean(1, resident.getSelectedTown() == town);
			statement.setString(2, resident.getTownRank(town).getKey());
			statement.setString(3, town.getName());
			statement.setString(4, resident.getUUID());
			statement.executeUpdate();
		}
	}

	@Override
	public void updateLinkTownToNation(Town town, Nation nation) throws Exception {
		// TODO: Later

	}

	@Override
	public void dump() throws Exception {
		// TODO Finish dump()
	}

	@Override
	public void save() throws Exception {
	}

	@Override
	public void disconnect() throws Exception {
		if (conn == null)
			return;
		if (!conn.getAutoCommit()) {
			conn.commit();
		}
		conn.close();
	}

	// //////////////////////////////////////
	// Update System
	// //////////////////////////////////////

	/**
	 * Holds an SQL statement to be run to update the tables in the DB
	 * 
	 * @author Joe Goett
	 */
	protected class DBUpdate {
		/**
		 * Formatted mm.dd.yyyy.e where e increments by 1 for every update released on the same date
		 */
		public String id;
		public String code;
		public String sql;

		public DBUpdate(String id, String code, String sql) {
			this.id = id;
			this.code = code;
			this.sql = sql;
		}
	}

	protected List<DBUpdate> updates = new ArrayList<DBUpdate>();

	/**
	 * Create all the new updates
	 */
	protected void setUpdates() {
		updates.add(new DBUpdate("03.08.2014.1", "Add Updates Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Updates (" + "Id varchar(50) NOT NULL, " + "Code varchar(50) NOT NULL, " + "PRIMARY KEY(Id));"));
		updates.add(new DBUpdate("03.08.2014.2", "Add Towns Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Towns (" + "Name varchar(50) NOT NULL, " + "ExtraBlocks int NOT NULL DEFAULT 0, " + "SpawnX DOUBLE, " + "SpawnY DOUBLE, " + "SpawnZ DOUBLE, " + "SpawnDim int, " + "Type varchar(1) DEFAULT 'T', " + "PRIMARY KEY (Name));"));
		updates.add(new DBUpdate("03.08.2014.3", "Add Residents Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Residents (" + "UUID varchar(255) NOT NULL, " + "IsNPC boolean DEFAULT false, " + "Joined int NOT NULL, " + "LastLogin int NOT NULL, " + "SelectedTownName varchar(50), " + "PRIMARY KEY (UUID))")); // MC Version < 1.7 UUID is Player name. 1.7 >= UUID is Player's UUID
		updates.add(new DBUpdate("03.08.2014.4", "Add Nations Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Nations (" + "Name varchar(50) NOT NULL, " + "ExtraBlocks int NOT NULL DEFAULT 0, " + "PRIMARY KEY(Name));"));
		updates.add(new DBUpdate("03.08.2014.5", "Add TownBlocks Table", "CREATE TABLE IF NOT EXISTS " + prefix + "TownBlocks (" + "Id int " + MyTownDatasource_SQL.autoIncrement + ", " + "X int NOT NULL, " + "Z int NOT NULL, " + "Dim int NOT NULL, " + "TownName varchar(50) NOT NULL, " + "PRIMARY KEY(Id), " + "FOREIGN KEY (TownName) REFERENCES " + prefix + "Towns(Name) ON DELETE CASCADE ON UPDATE CASCADE);"));
		updates.add(new DBUpdate("05.03.2014.1", "Add Ranks Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Ranks (" + "Key varchar(100) NOT NULL, " + "Name varchar(50) NOT NULL, " + "Nodes text(10000), " + "TownName varchar(50), " + "PRIMARY KEY(Key), " + "FOREIGN KEY (TownName) REFERENCES " + prefix + " Towns(Name) ON DELETE CASCADE ON UPDATE CASCADE);"));
		updates.add(new DBUpdate("03.22.2014.1", "Add ResidentsToTowns Table", "CREATE TABLE IF NOT EXISTS " + prefix + "ResidentsToTowns (" + "Id int " + MyTownDatasource_SQL.autoIncrement + ", " + "TownName varchar(50) NOT NULL, " + "Owner varchar(255) NOT NULL, " + "Rank varchar(50), " + "IsSelectedTown boolean DEFAULT false, " + "PRIMARY KEY (Id), " + "FOREIGN KEY (TownName) REFERENCES " + prefix + "Towns(Name) ON DELETE CASCADE ON UPDATE CASCADE, " + "FOREIGN KEY (Owner) REFERENCES " + prefix + "Residents(UUID) ON DELETE CASCADE, " + "FOREIGN KEY (Rank) REFERENCES " + prefix + "Ranks(Key) ON DELETE CASCADE ON UPDATE CASCADE);"));
		updates.add(new DBUpdate("03.22.2014.2", "Add TownsToNations", "CREATE TABLE IF NOT EXISTS " + prefix + "TownsToNations (" + "Id int " + MyTownDatasource_SQL.autoIncrement + ", " + "TownName varchar(50) NOT NULL, " + "NationName varchar(50) NOT NULL, " + "Rank varchar(1) DEFAULT 'T', " + "PRIMARY KEY (Id), " + "FOREIGN KEY (TownName) REFERENCES " + prefix + "Towns(Name) ON DELETE CASCADE ON UPDATE CASCADE, " + "FOREIGN KEY (NationName) REFERENCES " + prefix + "Nations(Name) ON DELETE CASCADE ON UPDATE CASCADE);"));
		updates.add(new DBUpdate("05.23.2014.1", "Add Plots Table", "CREATE TABLE IF NOT EXISTS " + prefix + "Plots (" + "Key varchar(100), " + "Dim int NOT NULL, " + "X1 int NOT NULL, " + "Y1 int NOT NULL, " + "Z1 int NOT NULL, " + "X2 int NOT NULL, " + "Y2 int NOT NULL, " + "Z2 int NOT NULL, " + "TownName varchar(50), " + "Owner varchar(255), " + "Type varchar(1) DEFAULT 'T', " + "Name varchar(50), " + "PRIMARY KEY(Key), " + "FOREIGN KEY(TownName) REFERENCES " + prefix + "Towns(Name) ON DELETE CASCADE ON UPDATE CASCADE, " + "FOREIGN KEY(Owner) REFERENCES " + prefix + "Residents(UUID) ON DELETE CASCADE)"));
		updates.add(new DBUpdate("05.23.2014.2", "Add TownFlags", "CREATE TABLE IF NOT EXISTS " + prefix + "TownFlags(" + "Id integer, " + "Name varchar(50) NOT NULL, " + "Description varchar(200), " + "TownName varchar(50) NOT NULL, " + "Value boolean NOT NULL, " + "PRIMARY KEY(Id), " + "FOREIGN KEY(TownName) REFERENCES " + prefix + "Towns(Name) ON DELETE CASCADE ON UPDATE CASCADE)"));
		updates.add(new DBUpdate("05.23.2014.3", "Add PlotFlags", "CREATE TABLE IF NOT EXISTS " + prefix + "PlotFlags(" + "Id integer, " + "Name varchar(50) NOT NULL, " + "Description varchar(200), " + "TownPlotKey varchar(100) NOT NULL, " + "Value boolean NOT NULL, " + "PRIMARY KEY(Id), " + "FOREIGN KEY(TownPlotKey) REFERENCES " + prefix + "Plots(Key) ON DELETE CASCADE ON UPDATE CASCADE)"));
	}

	/**
	 * Does the actual updates on the DB
	 * 
	 * @throws Exception
	 */
	protected void doUpdates() throws Exception {
		List<String> ids = Lists.newArrayList();
		PreparedStatement statement;
		try {
			statement = prepare("SELECT * FROM " + prefix + "Updates");
			ResultSet rs = statement.executeQuery();

			while (rs.next()) {
				ids.add(rs.getString("Id"));
			}
		} catch (Exception e) {
		} // Ignore. Just missing the updates table for now

		for (DBUpdate update : updates) {
			if (ids.contains(update.id)) {
				continue; // Skip updates already done
			}

			// Update!
			log.info("Running update %s - %s", update.id, update.code);
			statement = prepare(update.sql);
			statement.execute();

			// Insert the update key so as to not run the update again
			statement = prepare("INSERT INTO " + prefix + "Updates (Id,Code) VALUES(?,?)", true);
			statement.setString(1, update.id);
			statement.setString(2, update.code);
			statement.execute();
		}
	}
}