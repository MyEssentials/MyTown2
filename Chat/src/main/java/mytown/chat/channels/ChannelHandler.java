package mytown.chat.channels;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import mytown.chat.Config;
import mytown.chat.MyTownChat;
import mytown.chat.api.IChannelType;
import mytown.core.ChatUtils;
import mytown.core.utils.Log;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTBase;

// TODO Maybe allow different ways of storing (DB, file, etc)?

public class ChannelHandler {
	private static Log log;
	private static Map<String, IChannelType> channelTypes;
	private static List<String> channelIMCWaitList;
	private static Map<String, Channel> channels;
	
	public static void init() {
		channelTypes = new Hashtable<String, IChannelType>();
		channels = new Hashtable<String, Channel>();
		channelIMCWaitList = new ArrayList<String>();
		log = MyTownChat.Instance.chatLog.createChild("ChannelHandler");
	}
	
	// /////////////////////////////////
	// Channel Types
	// /////////////////////////////////
	
	public static void addChannelType(IChannelType type) {
		channelTypes.put(type.name(), type);
	}
	
	public static void removeChannelType(String typeName) {
		channelTypes.remove(typeName);
	}
	
	public static void removeChannelType(IChannelType type) {
		removeChannelType(type.name());
	}
	
	public static IChannelType getChannelType(String typeName) {
		return channelTypes.get(typeName);
	}
	
	public static List<Channel> getChannels() {
		return (List<Channel>) channels.values();
	}
	
	/**
	 * Returns all channel names starting with str
	 * @param str
	 * @return
	 */
	public static List<String> getChannelNames(String str) {
		List<String> chs = new ArrayList<String>();
		for (String ch : channels.keySet()) {
			if (ch.startsWith(str)) {
				chs.add(ch);
			}
		}
		return chs;
	}
	
	public static List<String> getChannelNames() {
		List<String> channelNamesList = new ArrayList<String>();
		channelNamesList.addAll(channels.keySet());
		return channelNamesList;
	}
	
	// /////////////////////////////////
	// Channels
	// /////////////////////////////////
	
	public static void addChannel(Channel ch) {
		if (channels.containsKey(ch.name)) {
			log.warning("Channel %s already exists", ch.name);
			return;
		}
		channels.put(ch.name, ch);
	}
	
	public static void removeChannel(String channelName) {
		channels.remove(channelName);
	}
	
	public static void removeChannel(Channel ch) {
		removeChannel(ch.name);
	}
	
	public static void addChannelIMC(String channel) {
		if (channel == null) throw new NullPointerException();
		channelIMCWaitList.add(channel);
	}
	
	private static void loadChannel(String str) {
		String[] args = str.split(",");
		if (args.length < 5) return;
		int radius = Integer.parseInt(args[3]);
		IChannelType type = getChannelType(args[4]);
		if (type == null) {
			log.warning("Unknown channel type %s for channel %s", type, args[0]);
			return;
		}
		addChannel(new Channel(args[0], args[1], args[2], radius, type));
	}
	
	// TODO Finish channel loading
	public static void loadChannels() {
		// Load all Channel's from the config
		for (String chString : Config.channels) {
			loadChannel(chString);
		}
		// Load all Channel's from IMC Events
		for (String chString : channelIMCWaitList) {
			loadChannel(chString);
		}
	}
	
	// /////////////////////////////////
	// User
	// /////////////////////////////////
	
	public static void joinChannel(ICommandSender sender, String ch) {
		if (!(sender instanceof EntityPlayer)) {
			ChatUtils.sendChat(sender, "You must be a player to join a channel");
			return;
		}
		if (!channels.containsKey(ch)) {
			ChatUtils.sendChat(sender, "That channel doesn't exist!");
			return;
		}
		EntityPlayer pl = (EntityPlayer) sender;
		pl.getEntityData().getCompoundTag("mytown").getCompoundTag("chat").getCompoundTag("channels").setBoolean(ch, true);
	}
	
	public static void leaveChannel(ICommandSender sender, String ch) {
		if (!(sender instanceof EntityPlayer)) {
			ChatUtils.sendChat(sender, "You must be a player to leave a channel");
			return;
		}
		if (!channels.containsKey(ch)) {
			ChatUtils.sendChat(sender, "That channel doesn't exist!");
			return;
		}
		EntityPlayer pl = (EntityPlayer) sender;
		pl.getEntityData().getCompoundTag("mytown").getCompoundTag("chat").getCompoundTag("channels").removeTag(ch);
	}
	
	/**
	 * Sets the senders active channel, joining the channel if needed
	 * @param sender
	 * @param ch
	 */
	public static void setActiveChannel(ICommandSender sender, String ch) {
		if (!(sender instanceof EntityPlayer)) {
			ChatUtils.sendChat(sender, "You must be a player to focus a channel");
			return;
		}
		if (!channels.containsKey(ch)) {
			ChatUtils.sendChat(sender, "That channel doesn't exist!");
			return;
		}
		EntityPlayer pl = (EntityPlayer) sender;
		pl.getEntityData().getCompoundTag("mytown").getCompoundTag("chat").setString("activeChannel", ch);
		joinChannel(sender, ch);
	}
	
	public static Channel getActiveChannel(ICommandSender sender) {
		if (!(sender instanceof EntityPlayer)) {
			ChatUtils.sendChat(sender, "You must be a player to be part of a channel");
			return null;
		}
		EntityPlayer pl = (EntityPlayer) sender;
		String activeChannelName = pl.getEntityData().getCompoundTag("mytown").getCompoundTag("chat").getString("activeChannel");
		activeChannelName = activeChannelName.isEmpty() ? Config.defaultChannel : activeChannelName;
		return channels.get(activeChannelName);
	}
	
	/**
	 * Returns all of the senders joined channels
	 * @param sender
	 * @return
	 */
	public static List<String> getChannels(ICommandSender sender) {
		if (!(sender instanceof EntityPlayer)) {
			ChatUtils.sendChat(sender, "You must be a player to get a list of channels");
			return null;
		}
		List<String> playersChannels = new ArrayList<String>();
		EntityPlayer pl = (EntityPlayer) sender;
		Collection<?> tags = pl.getEntityData().getCompoundTag("mytown").getCompoundTag("chat").getCompoundTag("channels").getTags();
		for (Object obj : tags) {
			if (!(obj instanceof NBTBase)) continue;
			playersChannels.add(((NBTBase)obj).getName());
		}
		return playersChannels;
	}
	
	/**
	 * Gets all commands sender is part of that start with str
	 * @param sender
	 * @param str
	 * @return
	 */
	public static List<String> getChannels(ICommandSender sender, String str) {
		if (!(sender instanceof EntityPlayer)) {
			ChatUtils.sendChat(sender, "You must be a player to get a list of channels");
			return null;
		}
		List<String> playersChannels = new ArrayList<String>();
		EntityPlayer pl = (EntityPlayer) sender;
		Collection<?> tags = pl.getEntityData().getCompoundTag("mytown").getCompoundTag("chat").getCompoundTag("channels").getTags();
		for (Object obj : tags) {
			if (!(obj instanceof NBTBase)) continue;
			NBTBase tag = (NBTBase)obj;
			if (!tag.getName().startsWith(str)) continue;
			playersChannels.add(tag.getName());
		}
		return playersChannels;
	}
}