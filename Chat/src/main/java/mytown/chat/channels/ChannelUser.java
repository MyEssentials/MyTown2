package mytown.chat.channels;

import java.util.Arrays;
import java.util.List;

import com.google.common.base.Joiner;

/**
 * Defines a user who is part of one or more channels
 * @author Joe Goett
 */
public class ChannelUser {
	private String name;
	private String activeChannel;
	private List<String> joinedChannels;
	
	public ChannelUser(String name){
		this.name = name;
	}
	
	/**
	 * Returns the users name
	 * @return
	 */
	public String getName(){
		return name;
	}
	
	/**
	 * Returns the users active channel
	 * @return
	 */
	public String getActiveChannel(){
		return activeChannel;
	}
	
	/**
	 * Sets the players currently active channel and adds it to the joinedChannels list if needed
	 * @param channel
	 */
	public void setActiveChannel(String channel){
		joinChannel(channel);
		activeChannel = channel;
	}
	
	/**
	 * If the user is in the channel
	 * @param channel
	 * @return
	 */
	public boolean isInChannel(String channel){
		return joinedChannels.contains(channel);
	}

	/**
	 * Makes the user join the channel. Returns if it was successful
	 * @param channel
	 * @return
	 */
	public boolean joinChannel(String channel){
		if (joinedChannels.contains(channel)) joinedChannels.add(channel);
		return joinedChannels.add(channel);
	}
	
	/**
	 * Makes the user join all channels in the list. Returns if it was successful
	 * @param channels
	 * @return
	 */
	public boolean joinChannels(List<String> channels){
		return joinedChannels.addAll(channels);
	}
	
	/**
	 * Makes the user join all the given channels. Returns if it was successful
	 * @param channels
	 * @return
	 */
	public boolean joinChannels(String[] channels){
		return joinChannels(Arrays.asList(channels));
	}
	
	/**
	 * Makes the user leave the channel. Returns if it was successful
	 * @param channel
	 * @return
	 */
	public boolean leaveChannel(String channel){
		return joinedChannels.remove(channel);
	}

	/**
	 * Returns a list of channels the user is part of
	 * @return
	 */
	public List<String> getChannels(){
		return joinedChannels;
	}

	@Override
	public String toString(){
		return Joiner.on(",").join(joinedChannels);
	}
}