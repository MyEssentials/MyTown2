package mytown;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.util.EnumChatFormatting;

public class Constants {
	public static final String VERSION = "@VERSION@";
	public static final String MODID = "MyTown2";
	public static final String MODNAME = "MyTown 2";
	public static final String DEPENDENCIES = "after:*;required-after:Forge;required-after:MyTownCore";
	public static String CONFIG_FOLDER = "";



	public static final String EDIT_TOOL_NAME = EnumChatFormatting.BLUE + "Selector"; // TODO: Get localization for it, maybe?



	// TODO Allow configuring them
	public static Map<String, List<String>> DEFAULT_RANK_VALUES = new HashMap<String, List<String>>();
    public static String DEFAULT_RANK;
    public static String DEFAULT_SUPER_RANK; // basically the mayor

}