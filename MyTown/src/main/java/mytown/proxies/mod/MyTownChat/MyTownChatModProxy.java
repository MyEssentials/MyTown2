package mytown.proxies.mod.MyTownChat;

import mytown.proxies.mod.ModProxy;
import cpw.mods.fml.common.event.FMLInterModComms;

public class MyTownChatModProxy extends ModProxy {
	@Override
	public String getName() {
		return "MyTownChat";
	}

	@Override
	public void preInit() {
	}

	@Override
	public void init() {
		// Register Formatters
		FMLInterModComms.sendMessage("MyTownChat", "registerFormatter", "mytown.proxies.mod.MyTownChat.MyTownChatFormatter");

		// Register Channel Types
		FMLInterModComms.sendMessage("MyTownChat", "registerType", "mytown.proxies.mod.MyTownChat.TownChatChannelType");
	}

	@Override
	public void postInit() {
	}
}