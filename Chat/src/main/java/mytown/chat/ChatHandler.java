package mytown.chat;

import mytown.chat.channels.Channel;
import mytown.chat.channels.ChannelHandler;
import mytown.chat.format.FormatHandler;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;

/**
 * Handles ServerChatEvent's and CommandEvent's
 * @author Joe Goett
 */
public class ChatHandler {
	private ChannelHandler channelHandler;
	private FormatHandler formatHandler;
	
	public ChatHandler(){
		channelHandler = new ChannelHandler();
		formatHandler = new FormatHandler();
	}
	
	@ForgeSubscribe
	public void serverChat(ServerChatEvent ev){
		// TODO Run ev.message through regex filter
		Channel ch = channelHandler.getChannel(ev.player);
		String msg = formatHandler.format(ev.player, ch.format, ev.message);
		ch.sendMessage(ev.player, msg);
	}
	
	@ForgeSubscribe
	public void onCommand(CommandEvent ev){
		// TODO Run through regex filter
	}

	/**
	 * Returns the underlying ChannelHandler
	 * @return
	 */
	public ChannelHandler getChannelHandler(){
		return channelHandler;
	}
	
	/**
	 * Returns the underlying FormatHandler
	 * @return
	 */
	public FormatHandler getFormatHandler(){
		return formatHandler;
	}
}