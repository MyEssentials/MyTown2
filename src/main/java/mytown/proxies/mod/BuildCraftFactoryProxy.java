package mytown.proxies.mod;

import mytown.protection.BuildCraftFactoryProtection;
import mytown.protection.Protections;

/**
 * Created by AfterWind on 9/15/2014.
 * Proxy for BuildCraft stuff
 */
public class BuildCraftFactoryProxy extends ModProxy {
    public static final String MOD_ID = "BuildCraft|Factory";

    @Override
    public String getModID() {
        return MOD_ID;
    }

    @Override
    public String getName() {
        return "BuildCraft Factory";
    }

    @Override
    public void load() {
        Protections.instance.addProtection(new BuildCraftFactoryProtection(), MOD_ID);
    }
}
