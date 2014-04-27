package mytown;

import mytown.entities.Rank;

public class Constants {
	public static final String VERSION = "${version}";
	public static final String MODID = "MyTown";
	public static final String MODNAME = "MyTown";
	public static final String DEPENDENCIES = "after:*;required-after:Forge;required-after:MyTownCore";
	public static String CONFIG_FOLDER = "";

	public static final Rank[] DEFAULT_RANKS = { new Rank("outsider", null), new Rank("resident", null), new Rank("assistant", null), new Rank("mayor", null) };

}