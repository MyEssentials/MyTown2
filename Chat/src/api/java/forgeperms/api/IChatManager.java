package forgeperms.api;

public interface IChatManager {
    /**
     * Returns the name of the chat handler in use. Used in console output and debugging.
     * @return
     */
    public String getName();
    
    /**
     * Loads the economy handler
     * @return
     */
    public boolean load();
    
    /**
     * Gets the load error string
     * @return
     */
    public String getLoadError();
    
    /**
     * Gets the prefix of the player in the given world
     * @param world
     * @param player
     * @return
     */
    public String getPlayerPrefix(String world, String player);
    
    /**
     * Gets the suffix of the player in the given world
     * @param world
     * @param player
     * @return
     */
    public String getPlayerSuffix(String world, String player);
    
    /**
     * Sets the prefix of the player in the given world
     * @param world
     * @param player
     * @param prefix
     */
    public void setPlayerPrefix(String world, String player, String prefix);
    
    /**
     * Sets the suffix of the player in the given world
     * @param world
     * @param player
     * @param suffix
     */
    public void setPlayerSuffix(String world, String player, String suffix);
    
    /**
     * Gets the prefix of the group in the given world
     * @param world
     * @param group
     * @return
     */
    public String getGroupPrefix(String world, String group);
    
    /**
     * Gets the suffix of the group in the given world
     * @param world
     * @param group
     * @return
     */
    public String getGroupSuffix(String world, String group);
    
    /**
     * Sets the prefix of the group in the given world
     * @param world
     * @param group
     * @param prefix
     */
    public void setGroupPrefix(String world, String group, String prefix);
    
    /**
     * Sets the suffix of the group in the given world
     * @param world
     * @param group
     * @param suffix
     */
    public void setGroupSuffix(String world, String group, String suffix);
    
    /**
     * Sees if the player is in the group in the given world
     * @param world
     * @param player
     * @param group
     * @return
     */
    public boolean playerInGroup(String world, String player, String group);
    
    /**
     * Gets the groups the player is in in the given world
     * @param world
     * @param player
     * @return
     */
    public String[] getPlayerGroups(String world, String player);
    
    /**
     * Gets the users primary group in the given world
     * @param world
     * @param player
     * @return
     */
    public String getPrimaryGroup(String world, String player);
    
    /**
     * Gets a String option node, if the chat system supports them
     * @param world
     * @param playerName
     * @param node
     * @param defaultValue
     * @return
     */
    public String getPlayerInfoString(String world, String playerName, String node, String defaultValue);

    /**
     * Gets a Integer option node, if the chat system supports them
     * @param world
     * @param playerName
     * @param node
     * @param defaultValue
     * @return
     */
    public int getPlayerInfoInteger(String world, String playerName, String node, int defaultValue);

    /**
     * Gets a Double option node, if the chat system supports them
     * @param world
     * @param playerName
     * @param node
     * @param defaultValue
     * @return
     */
    public double getPlayerInfoDouble(String world, String playerName, String node, double defaultValue);

    /**
     * Gets a Boolean option node, if the chat system supports them
     * @param world
     * @param playerName
     * @param node
     * @param defaultValue
     * @return
     */
    public boolean getPlayerInfoBoolean(String world, String playerName, String node, boolean defaultValue);
    
    /**
     * Sets the given player's option node to the value
     * @param world
     * @param playerName
     * @param node
     * @param value
     * @return
     */
    public void setPlayerInfoString(String world, String playerName, String node, String value);

    /**
     * Sets the given player's option node to the value
     * @param world
     * @param playerName
     * @param node
     * @param value
     * @return
     */
    public void setPlayerInfoInteger(String world, String playerName, String node, int value);

    /**
     * Sets the given player's option node to the value
     * @param world
     * @param playerName
     * @param node
     * @param value
     * @return
     */
    public void setPlayerInfoDouble(String world, String playerName, String node, double value);
    
    /**
     * Sets the given player's option node to the value
     * @param world
     * @param playerName
     * @param node
     * @param value
     * @return
     */
    public void setPlayerInfoBoolean(String world, String playerName, String node, boolean value);
    
    /**
     * Gets a String option node, if the chat system supports them
     * @param world
     * @param group
     * @param node
     * @param defaultValue
     * @return
     */
    public String getGroupInfoString(String world, String group, String node, String defaultValue);

    /**
     * Gets a Integer option node, if the chat system supports them
     * @param world
     * @param group
     * @param node
     * @param defaultValue
     * @return
     */
    public int getGroupInfoInteger(String world, String group, String node, int defaultValue);

    /**
     * Gets a Double option node, if the chat system supports them
     * @param world
     * @param group
     * @param node
     * @param defaultValue
     * @return
     */
    public double getGroupInfoDouble(String world, String group, String node, double defaultValue);

    /**
     * Gets a Boolean option node, if the chat system supports them
     * @param world
     * @param group
     * @param node
     * @param defaultValue
     * @return
     */
    public boolean getGroupInfoBoolean(String world, String group, String node, boolean defaultValue);

    /**
     * Sets the given group's option node to the value
     * @param world
     * @param group
     * @param node
     * @param value
     * @return
     */
    public void setGroupInfoString(String world, String group, String node, String value);

    /**
     * Sets the given group's option node to the value
     * @param world
     * @param group
     * @param node
     * @param value
     * @return
     */
    public void setGroupInfoInteger(String world, String group, String node, int value);

    /**
     * Sets the given group's option node to the value
     * @param world
     * @param group
     * @param node
     * @param value
     * @return
     */
    public void setGroupInfoDouble(String world, String group, String node, double value);

    /**
     * Sets the given group's option node to the value
     * @param world
     * @param group
     * @param node
     * @param value
     * @return
     */
    public void setGroupInfoBoolean(String world, String group, String node, boolean value);
}