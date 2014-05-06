package mytown.chat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import mytown.chat.channels.IChannelType;
import mytown.chat.config.ChannelLoader;
import mytown.chat.config.Config;
import mytown.chat.config.UserLoader;
import mytown.chat.format.IChatFormatter;
import mytown.core.Log;
import net.minecraftforge.common.MinecraftForge;
import cpw.mods.fml.common.FMLLog;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLInterModComms;
import cpw.mods.fml.common.event.FMLInterModComms.IMCMessage;
import cpw.mods.fml.common.event.FMLPostInitializationEvent;
import cpw.mods.fml.common.event.FMLPreInitializationEvent;

/**
 * Entry point for MyTownChat module
 * 
 * @author Joe Goett
 */
@Mod(modid = "MyTownChat", name = "MyTownChat", version = "2.0", dependencies = "required-after:Forge;required-after:MyTownCore")
public class MyTownChat {
	@Mod.Instance
	public static MyTownChat INSTANCE;
	private List<String> channelWaitList;
	public ChatHandler chatHandler;

	// Configs
	public Config config;
	public ChannelLoader channelConfig;
	public UserLoader userConfig;

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
		channelWaitList = new ArrayList<String>();

		// Setup configs
		config = new Config(new File(ev.getModConfigurationDirectory(), "/MyTown/Chat/Chat.cfg"));
		channelConfig = new ChannelLoader(new File(ev.getModConfigurationDirectory(), "/MyTown/Chat/Channels.cfg"));
		userConfig = new UserLoader(new File(ev.getModConfigurationDirectory(), "/MyTown/Chat/Users.cfg"));

		// Setup loggers
		chatLog = new Log("MyTownChat", FMLLog.getLogger());

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
				if (msg.key.equals("register_type")) { // Register IChannelType
					Class<?> possibleChType = Class.forName(msg.getStringValue());
					if (!possibleChType.isInstance(IChannelType.class)) throw new Exception("Unknown ChannelType");
					chatHandler.getChannelHandler().registerType((IChannelType) possibleChType.newInstance());
				} else if (msg.key.equals("register_channel")) { // Add channel to wait list
					channelWaitList.add(msg.getStringValue());
				} else if (msg.key.equals("register_formatter")) { // Register IChatFormatter
					Class<?> possibleFormatter = Class.forName(msg.getStringValue());
					if (!possibleFormatter.isInstance(IChatFormatter.class)) throw new Exception("Unknown IChatFormatter");
					chatHandler.getFormatHandler().addFormatter((IChatFormatter) possibleFormatter.newInstance());
				}
			} catch (Exception e) {
				chatLog.warning("Failed to %s from %s because %s", msg.key, msg.getSender(), e.getMessage());
			}
		}
	}

	/**
	 * Loads channels and users from the config
	 * 
	 * @param ev
	 *            FMLPostInitializationEvent
	 */
	@Mod.EventHandler
	public void postInit(FMLPostInitializationEvent ev) {
		channelConfig.loadChannels(channelWaitList);
		userConfig.loadUsers();
	}
}