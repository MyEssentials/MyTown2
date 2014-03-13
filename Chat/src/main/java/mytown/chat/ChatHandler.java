package mytown.chat;

import mytown.chat.channels.Channel;
import mytown.chat.channels.ChannelHandler;
import mytown.chat.format.FormatHandler;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.CommandEvent;
import net.minecraftforge.event.ForgeSubscribe;
import net.minecraftforge.event.ServerChatEvent;
import cpw.mods.fml.common.IPlayerTracker;

/**
 * Handles ServerChatEvent's and CommandEvent's
 * @author Joe Goett
 */
public class ChatHandler implements IPlayerTracker{
	private ChannelHandler channelHandler;
	private FormatHandler formatHandler;
	
	public ChatHandler(){
		channelHandler = new ChannelHandler();
		formatHandler = new FormatHandler();
	}
	
	/**
	 * Filters and Formats a ServerChatEvent
	 * @param ev ServerChatEvent
	 */
	@ForgeSubscribe
	public void serverChat(ServerChatEvent ev){
		if (ev.isCanceled()) return;
		
		// TODO Run ev.message through regex filter
		Channel ch = channelHandler.getActiveChannelObject(ev.player.getCommandSenderName());
		if (ch == null) return;  // TODO Throw Exception/Log?
		String msg = formatHandler.format(ev.player, ch.format, ev.message);
		ch.sendMessage(ev.player, msg);
		
		ev.setCanceled(true);
	}
	
	/**
	 * Filters and Formats a CommandEvent
	 * @param ev CommandEvent
	 */
	@ForgeSubscribe
	public void onCommand(CommandEvent ev){
		// TODO Run through regex filter
	}

	public void playerJoin(){
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

	
	///////////////////////////////////////////////////
	// Player Tracker
	///////////////////////////////////////////////////
	
	@Override
	public void onPlayerLogin(EntityPlayer player) {
		// TODO Auto-generated method stub
	}
	

	@Override
	public void onPlayerLogout(EntityPlayer player) {
		// TODO Auto-generated method stub
	}
	

	@Override
	public void onPlayerChangedDimension(EntityPlayer player) {
		// TODO Auto-generated method stub
	}
	

	@Override
	public void onPlayerRespawn(EntityPlayer player) {
		// TODO Auto-generated method stub
	}
}