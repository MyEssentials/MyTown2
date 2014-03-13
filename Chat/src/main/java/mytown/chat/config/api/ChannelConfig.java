package mytown.chat.config.api;

import java.util.List;

import mytown.chat.channels.Channel;

/**
 * Loads and Saves Channels
 * @author Joe Goett
 */
public interface ChannelConfig {
	/**
	 * Load all channels from the config
	 */
	public void loadChannels();

	/**
	 * Load all channels from the List<String> Each string is formatted: name,abbreviation,format,radius,typename
	 * @param channels
	 */
	public void loadChannels(List<String> channels);
	
	/**
	 * Saves the Channel
	 * @param channel
	 */
	public void saveChannel(Channel channel);
	
	/**
	 * Saves all channels in the list
	 * @param channels
	 */
	public void saveChannels(List<Channel> channels);
}