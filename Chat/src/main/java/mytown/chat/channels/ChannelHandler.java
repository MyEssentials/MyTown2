package mytown.chat.channels;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import mytown.chat.channels.types.Global;
import mytown.chat.channels.types.Local;
import mytown.chat.channels.types.Permission;

/**
 * Handles all channels
 * 
 * @author Joe Goett
 */
public class ChannelHandler {
	private Map<String, Channel> channels;
	private Map<String, ChannelUser> users;
	private Map<String, IChannelType> types;

	/**
	 * Initializes the ChannelHandler's internal Map
	 */
	public ChannelHandler() {
		channels = new HashMap<String, Channel>();
		users = new HashMap<String, ChannelUser>();
		types = new HashMap<String, IChannelType>();

		registerDefaultTypes();
	}

	/**
	 * Registers all the built-in IChannelTypes
	 */
	private void registerDefaultTypes() {
		registerType(new Global());
		registerType(new Local());
		registerType(new Permission());
	}

	// ////////////////////////////////////////////////////////////
	// Channel Methods
	// ////////////////////////////////////////////////////////////

	/**
	 * Registers a channel with the handler
	 * 
	 * @param channel
	 */
	public void registerChannel(Channel channel) {
		if (channel == null) return; // TODO Throw exception/log?
		if (channels.containsKey(channel.name)) return; // TODO Throw exception/log?
		channels.put(channel.name, channel);
	}

	/**
	 * Removes the channel of the given name
	 * 
	 * @param name
	 */
	public void removeChannel(String name) {
		channels.remove(name);
	}

	/**
	 * Removes the channel from the handler
	 */
	public void removeChannel(Channel channel) {
		removeChannel(channel.name);
	}

	/**
	 * Gets the channel with the given name
	 * 
	 * @param name
	 * @return
	 */
	public Channel getChannel(String name) {
		return channels.get(name);
	}

	/**
	 * Returns a Collection of all channels
	 * 
	 * @return
	 */
	public Collection<Channel> getChannels() {
		return channels.values();
	}

	/**
	 * Returns a Map of all channels
	 * 
	 * @return
	 */
	public Map<String, Channel> getChannelMap() {
		return channels;
	}

	// ////////////////////////////////////////////////////////////
	// User Methods
	// ////////////////////////////////////////////////////////////

	/**
	 * Returns the ChannelUser with the given name
	 * 
	 * @param name
	 * @return
	 */
	public ChannelUser getUser(String name) {
		return users.get(name);
	}

	/**
	 * Returns a list of all the users channels
	 * 
	 * @param name
	 * @return
	 */
	public List<String> getUsersChannels(String name) {
		return getUser(name).getChannels();
	}

	public String getActiveChannel(String name) {
		return getUser(name).getActiveChannel();
	}

	public Channel getActiveChannelObject(String name) {
		return channels.get(getActiveChannel(name));
	}

	/**
	 * Returns the underlying Map<String, String>
	 * 
	 * @return
	 */
	public Map<String, ChannelUser> getUsers() {
		return users;
	}

	// ////////////////////////////////////////////////////////////
	// Type Methods
	// ////////////////////////////////////////////////////////////

	/**
	 * Registers an IChannelType
	 * 
	 * @param type
	 */
	public void registerType(IChannelType type) {
		if (type == null) return; // TODO Thow exception/log?
		types.put(type.name(), type);
	}

	/**
	 * Remove an IChannelType of the given name
	 * 
	 * @param name
	 */
	public void removeType(String name) {
		types.remove(name);
	}

	/**
	 * Removes the given IChannelType
	 * 
	 * @param type
	 */
	public void removeType(IChannelType type) {
		removeType(type.name());
	}

	/**
	 * Returns the type of the given name, or null if undefined
	 * 
	 * @param type
	 * @return
	 */
	public IChannelType getType(String name) {
		return types.get(name);
	}

	/**
	 * Returns a collection of IChannelTypes
	 * 
	 * @return
	 */
	public Collection<IChannelType> getChannelTypes() {
		return types.values();
	}

	/**
	 * Returns a Map of IChannelTypes
	 * 
	 * @return
	 */
	public Map<String, IChannelType> getChannelTypesMap() {
		return types;
	}
}