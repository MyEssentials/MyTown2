package mytown.proxies.mod.MyTownChat;

import mytown.proxies.mod.ModProxy;
import cpw.mods.fml.common.event.FMLInterModComms;

public class MyTownChatModProxy extends ModProxy {
	@Override
	public String getName() {
		return "MyTownChat";
	}

	@Override
	public String getModID() {
		return "MyTownChat";
	}

	@Override
	public void load() { // TODO See why the formatter and channel type is not being registered correctly
		// Register Formatters
		FMLInterModComms.sendMessage("MyTownChat", "registerFormatter", "mytown.proxies.mod.MyTownChat.MyTownChatFormatter");

		// Register Channel Types
		FMLInterModComms.sendMessage("MyTownChat", "registerType", "mytown.proxies.mod.MyTownChat.TownChatChannelType");
	}
}