package mytown.modules;

import mytown.handler.IC2EventHandler;
import mytown.interfaces.IModule;
import net.minecraftforge.common.MinecraftForge;

/**
 * Created by AfterWind on 7/8/2014.
 *
 * IC2 mod implementation here
 */
public class IC2Module implements IModule{
    public static final String IC2ModID = "IC2";

    public void load() {
        MinecraftForge.EVENT_BUS.register(new IC2EventHandler());
    }

    public String getModID() {
        return IC2Module.IC2ModID;
    }

}
