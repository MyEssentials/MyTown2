package mytown.chat.channels;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;

/**
 * Handles all channels
 * @author Joe Goett
 */
public class ChannelHandler {
	private Map<String, Channel> channels;
	
	/**
	 * Initializes the ChannelHandler's internal Map
	 */
	public ChannelHandler(){
		channels = new HashMap<String, Channel>();
	}
	
	/**
	 * Registers a command with the handler
	 * @param channel
	 */
	public void registerChannel(Channel channel){
		if (channel == null) return;  // TODO Throw exception/log
		channels.put(channel.name, channel);
	}
	
	/**
	 * Returns a Collection of all channels
	 * @return
	 */
	public Collection<Channel> getChannels(){
		return channels.values();
	}
	
	/**
	 * Returns a Map of all channels
	 * @return
	 */
	public Map<String, Channel> getChannelMap(){
		return channels;
	}

	/**
	 * Gets the channel of the player
	 * @param player
	 * @return
	 */
	public Channel getChannel(EntityPlayer player){
		return channels.get("global");  // TODO Make it return a channel!
	}
}