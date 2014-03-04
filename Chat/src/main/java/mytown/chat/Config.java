package mytown.chat;

import java.io.File;

import mytown.chat.channels.Channel;
import mytown.chat.channels.ChannelHandler;
import mytown.chat.channels.types.Global;
import net.minecraftforge.common.ConfigCategory;
import net.minecraftforge.common.Configuration;
import net.minecraftforge.common.Property;

/**
 * Loads Chat config
 * @author Joe Goett
 */
public class Config extends Configuration {
	public Config(File file){
		super(file);
		loadChannels(getCategory("channels"));
	}
	
	/**
	 * Loads all channels from config
	 * @param channels
	 */
	private void loadChannels(ConfigCategory channels){
		ChannelHandler channelHandler = MyTownChat.INSTANCE.chatHandler.getChannelHandler(); 
		for (Property prop : channels.getValues().values()){
			String chStr[] = prop.getString().split(",");
			if (chStr.length < 5) continue;  // TODO Log?
			Channel ch = new Channel(chStr[0], chStr[1], chStr[2], Integer.parseInt(chStr[3]), new Global());  // TODO: Dynamic channel type!
			channelHandler.registerChannel(ch);
		}
	}
}