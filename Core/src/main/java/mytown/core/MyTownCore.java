package mytown.core;

import mytown.core.utils.command.CommandUtils;
import cpw.mods.fml.common.Mod;
import cpw.mods.fml.common.event.FMLServerAboutToStartEvent;

@Mod(modid = "MyTownCore", name="MyTownCore", version="2.0", dependencies="required-after:Forge")
public class MyTownCore {
	@Mod.EventHandler
	public void serverAboutToStart(FMLServerAboutToStartEvent ev) {
		try {
			CommandUtils.init();
		} catch (Exception e) {
			// TODO Log
		}
	}
}