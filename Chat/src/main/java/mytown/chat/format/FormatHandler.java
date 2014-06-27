package mytown.chat.format;

import java.util.ArrayList;
import java.util.List;

import mytown.chat.api.IChatFormatter;
import net.minecraft.entity.player.EntityPlayer;

/**
 * Handles all IChatFormatters
 * 
 * @author Joe Goett
 */
public class FormatHandler {
	private static List<IChatFormatter> formatters;

	private FormatHandler() {
	}

	public static void init() {
		FormatHandler.formatters = new ArrayList<IChatFormatter>();
		FormatHandler.addFormatter(new ChatFormatter()); // Default formatter
	}

	public static void addFormatter(IChatFormatter formatter) {
		if (formatter == null)
			throw new NullPointerException();
		FormatHandler.formatters.add(formatter);
	}

	public static void removeFormatter(IChatFormatter formatter) {
		if (formatter == null)
			throw new NullPointerException();
		FormatHandler.formatters.add(formatter);
	}

	public static String format(EntityPlayer pl, String format, String msg) {
		for (IChatFormatter formatter : FormatHandler.formatters) {
			msg = formatter.format(pl, format, msg);
		}
		return msg;
	}
}