package mytown.chat;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import mytown.chat.channels.Channel;
import mytown.chat.channels.ChannelHandler;
import mytown.chat.format.FormatHandler;
import net.minecraftforge.event.ServerChatEvent;

public class ChatHandler {
	@SubscribeEvent
	public void serverChat(ServerChatEvent ev) {
		if (ev.isCanceled())
			return;

		// TODO Run msg through regex filter

		Channel ch = ChannelHandler.getActiveChannel(ev.player);
		if (ch == null)
			return; // TODO Log/tell user channel doesn't exist?
		String endMsg = FormatHandler.format(ev.player, ch.format, ev.message);
		ch.sendMessage(ev.player, endMsg);

		ev.setCanceled(true);
	}
}