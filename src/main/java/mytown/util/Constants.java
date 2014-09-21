package mytown.util;

import net.minecraft.util.EnumChatFormatting;

import java.util.List;
import java.util.Map;

public class Constants {
    public static final String VERSION = "@VERSION@";
    public static final String MODID = "MyTown2";
    public static final String MODNAME = "MyTown 2";
    public static final String DEPENDENCIES = "after:*;required-after:Forge;required-after:MyTownCore";
    public static String CONFIG_FOLDER = "";
    public static final String TICK_HANDLER_LABEL = "MyTownTickHandler";
    public static final int DEFAULT_BLOCK_CHANGE_COUNTER = 100;
    public static final String EDIT_TOOL_NAME = EnumChatFormatting.BLUE + "Selector"; // TODO: Get localization for it, maybe?
    public static final String EDIT_TOOL_DESCRIPTION_PLOT = EnumChatFormatting.DARK_AQUA + "Select 2 blocks to make a plot.";
    public static final String EDIT_TOOL_DESCRIPTION_BLOCK_WHITELIST = EnumChatFormatting.DARK_AQUA + "Select block for bypassing protection.";
    public static final String EDIT_TOOL_DESCRIPTION_BLOCK_MODE_TOWN = EnumChatFormatting.DARK_AQUA + "MODE: (TOWN) Bypass for everyone";
    public static final String EDIT_TOOL_DESCRIPTION_BLOCK_MODE_PLOT = EnumChatFormatting.DARK_AQUA + "MODE: (PLOT) Bypass for residents";

    public static Map<String, List<String>> DEFAULT_RANK_VALUES;
}