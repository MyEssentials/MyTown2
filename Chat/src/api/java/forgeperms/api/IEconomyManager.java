package forgeperms.api;

public interface IEconomyManager {
	/**
	 * Gets the name of the Economy Handler, used in debugging and logging
	 * 
	 * @return
	 */
	public String getName();

	/**
	 * Determines if the economy manager has bank support
	 * 
	 * @return
	 */
	public boolean hasBankSupport();

	/**
	 * Returns if you have to right click with the item to pay
	 * 
	 * @return
	 */
	public boolean rightClickToPay();

	/**
	 * Loads the economy handler
	 * 
	 * @return
	 */
	public boolean load();

	/**
	 * Gets the load error string
	 * 
	 * @return
	 */
	public String getLoadError();

	/**
	 * Formats the amount into a human readable form
	 * 
	 * @param itemID
	 * @param amount
	 * @return
	 */
	public String format(String itemID, double amount);

	/**
	 * Returns the given players balance
	 * 
	 * @param playerName
	 * @param itemID
	 * @param world
	 * @return
	 */
	public double playerBalance(String playerName, String itemID, String world);

	/**
	 * Returns whether the player has the amount in their account
	 * 
	 * @param playerName
	 * @param world
	 * @param itemID
	 * @param amount
	 * @return
	 */
	public boolean playerHas(String playerName, String world, String itemID, double amount);

	/**
	 * Withdraws the amount from the given player
	 * 
	 * @param playerName
	 * @param world
	 * @param itemID
	 * @param amount
	 * @return
	 */
	public boolean playerWithdraw(String playerName, String world, String itemID, double amount);

	/**
	 * Gives the player the given amount
	 * 
	 * @param playerName
	 * @param world
	 * @param itemID
	 * @param amount
	 * @return
	 */
	public boolean playerDeposit(String playerName, String world, String itemID, double amount);

	/**
	 * Returns the given bank balance
	 * 
	 * @param playerName
	 * @param itemID
	 * @param world
	 * @return
	 */
	public double bankBalance(String name, String itemID);

	/**
	 * Returns whether the bank has the amount
	 * 
	 * @param playerName
	 * @param world
	 * @param itemID
	 * @param amount
	 * @return
	 */
	public boolean bankHas(String name, String itemID, double amount);

	/**
	 * Withdraws the amount from the given bank
	 * 
	 * @param playerName
	 * @param world
	 * @param itemID
	 * @param amount
	 * @return
	 */
	public boolean bankWithdraw(String name, String itemID, double amount);

	/**
	 * Gives the bank the given amount
	 * 
	 * @param playerName
	 * @param world
	 * @param itemID
	 * @param amount
	 * @return
	 */
	public boolean bankDeposit(String name, String itemID, double amount);
}