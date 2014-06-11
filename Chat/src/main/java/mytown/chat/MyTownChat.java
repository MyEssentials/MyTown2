package mytown.chat;

import mytown.chat.api.IChannelType;
import mytown.chat.api.IChatFormatter;
import mytown.chat.channels.ChannelHandler;
import mytown.chat.cmd.CmdChannel;
import mytown.chat.format.FormatHandler;
import mytown.core.utils.Log;
import mytown.core.utils.command.CommandUtils;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;
import cpw.mods.fml.common.event.FMLServerStartingEvent;

// TODO Localization!

/**
 * Entry point for MyTownChat module
 * 
 * @author Joe Goett
 */
@Mod(modid = "MyTownChat", name = "MyTownChat", version = "2.0", dependencies = "required-after:Forge;required-after:MyTownCore")
public class MyTownChat {
	@Mod.Instance
	public static MyTownChat Instance;
	public ChatHandler chatHandler;

	// Loggers
	public Log chatLog;

	/**
	 * Loads config and registers ChatHandler to the EVENT_BUS
	 * 
	 * @param ev
	 *            FMLPreInitializationEvent
	 */
	@Mod.EventHandler
	public void preInit(FMLPreInitializationEvent ev) {
		// Setup loggers
		chatLog = new Log(ev.getModLog());
		
		ChannelHandler.init();
		FormatHandler.init();

		// Setup chat handler
		MinecraftForge.EVENT_BUS.register(chatHandler = new ChatHandler());
	}

	/**
	 * Handles IMCEvents for registering types, channels, and formatters to the ChatHandler
	 * 
	 * @param ev
	 *            FMLInterModComms.IMCEvent
	 */
	@Mod.EventHandler
	public void imc(FMLInterModComms.IMCEvent ev) {
		for (IMCMessage msg : ev.getMessages()) {
			try {
				if (msg.key.equals("registerType")) { // Register IChannelType
					Class<?> possibleChType = Class.forName(msg.getStringValue());
					if (!possibleChType.isInstance(IChannelType.class)) throw new Exception("Unknown ChannelType");
					ChannelHandler.addChannelType((IChannelType) possibleChType.newInstance());
				} else if (msg.key.equals("registerChannel")) { // Add channel to wait list
					ChannelHandler.addChannelIMC(msg.getStringValue());
				} else if (msg.key.equals("registerFormatter")) { // Register IChatFormatter
					Class<?> possibleFormatter = Class.forName(msg.getStringValue());
					if (!possibleFormatter.isInstance(IChatFormatter.class)) throw new Exception("Unknown IChatFormatter");
					FormatHandler.addFormatter((IChatFormatter) possibleFormatter.newInstance());
				}
			} catch (Exception e) {
				chatLog.warning("Failed to %s from %s because %s", msg.key, msg.getSender(), e.getMessage());
			}
		}
	}

	@Mod.EventHandler
	public void serverStarting(FMLServerStartingEvent ev) {
		CommandUtils.registerCommand(new CmdChannel());
	}
}