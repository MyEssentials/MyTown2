package mytown.chat.config;

import java.io.File;
import java.util.List;

import mytown.chat.MyTownChat;
import mytown.chat.channels.Channel;
import mytown.chat.channels.ChannelHandler;
import mytown.chat.channels.IChannelType;
import mytown.chat.config.api.ChannelConfig;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;
import net.minecraftforge.common.Property.Type;

public class ChannelLoader extends Configuration implements ChannelConfig {
	ConfigCategory channels;
	ChannelHandler channelHandler;
	
	public ChannelLoader(File file){
		super(file);
		channels = this.getCategory("channels");
		channelHandler = MyTownChat.INSTANCE.chatHandler.getChannelHandler();
	}
	
	/**
	 * Returns a channel based on the given String array
	 * @param channelDetails
	 * @return
	 */
	private Channel loadChannel(String[] channelDetails){
		IChannelType type = channelHandler.getType(channelDetails[4]);
		if (type == null) return null;  // TODO Log?
		return new Channel(channelDetails[0], channelDetails[1], channelDetails[2], Integer.parseInt(channelDetails[3]), type);
	}

	/**
	 * Returns a channel based on the given String
	 * @param channelString
	 * @return
	 */
	private Channel loadChannel(String channelString){
		String[] channelDetails = channelString.split(",");
		if (channelDetails.length < 5) return null;  // TODO Log?
		return loadChannel(channelDetails);
	}

	@Override
	public void loadChannels() {
//		if (reload) channels.clear(); TODO Allow Reloading later
		channels.setComment("Below defines all the channels\nSet them as such: S:name=name,abbreviation,format,radius,typename\n<n> should be some unique number for the channel");
		if (channels.size() <= 0) return;  // TODO Add default channels to config
		
		for (Property prop : channels.getValues().values()){
			Channel ch = loadChannel(prop.getString());
			if (ch == null) continue; // TODO Log?
			ch.configKey = prop.getName();
			channelHandler.registerChannel(ch);
		}
	}

	@Override
	public void loadChannels(List<String> channels) {
		loadChannels();
		for (String chStr : channels){
			Channel ch = loadChannel(chStr);
			if (ch == null) continue; // TODO Log?
			channelHandler.registerChannel(ch);
		}
	}

	@Override
	public void saveChannel(Channel channel) {
		channels.put(channel.name, new Property(channel.name, channel.toString(), Type.STRING));
	}

	@Override
	public void saveChannels(List<Channel> channels) {
		for (Channel ch : channels){
			saveChannel(ch);
		}
		save();
	}
}