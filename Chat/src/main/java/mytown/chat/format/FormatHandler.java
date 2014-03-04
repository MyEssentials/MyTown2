package mytown.chat.format;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Handles all IChatFormatters
 * @author Joe Goett
 */
public class FormatHandler {
	private List<IChatFormatter> formatters;
	
	public FormatHandler(){
		formatters = new ArrayList<IChatFormatter>();
		addFormatter(new ChatFormatter());  // Default formatter
	}
	
	/**
	 * Adds an IChatFormatter
	 * @param formatter
	 */
	public void addFormatter(IChatFormatter formatter){
		formatters.add(formatter);
	}
	
	/**
	 * Removes an IChatFormatter
	 * @param formatter
	 */
	public void removeFormatter(IChatFormatter formatter){
		formatters.remove(formatter);
	}
	
	/**
	 * Formats the message from the given player
	 * @param player
	 * @param format
	 * @param message
	 * @return
	 */
	public String format(EntityPlayer player, String format, String message){
		for (IChatFormatter formatter : formatters){
			message = formatter.format(player, format, message);
		}
		return message;
	}
}