package mytown;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Constants {
	public static final String VERSION = "@VERSION@";
	public static final String MODID = "MyTown2";
	public static final String MODNAME = "MyTown 2";
	public static final String DEPENDENCIES = "after:*;required-after:Forge;required-after:MyTownCore";
	public static String CONFIG_FOLDER = "";

	// TODO Allow configuring them
	public static Map<String, ArrayList<String>> DEFAULT_RANK_VALUES = new HashMap<String, ArrayList<String>>();

}