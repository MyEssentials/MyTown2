package mytown.proxies.mod;

import mytown.protection.BuildCraftProtection;
import mytown.protection.Protections;

/**
 * Created by AfterWind on 9/15/2014.
 * Proxy for BuildCraft stuff
 */
public class BuildCraftProxy extends ModProxy {
    public static final String MOD_ID = "BuildCraft|Factory";

    @Override
    public String getModID() {
        return MOD_ID;
    }

    @Override
    public String getName() {
        return "BuildCraft";
    }

    @Override
    public void load() {
        Protections.instance.addProtection(new BuildCraftProtection(), MOD_ID);
    }
}
