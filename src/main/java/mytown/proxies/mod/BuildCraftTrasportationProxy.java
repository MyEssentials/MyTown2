package mytown.proxies.mod;

import mytown.protection.BuildCraftTransportProtection;
import mytown.protection.Protections;

/**
 * Created by AfterWind on 9/21/2014.
 * Buildcraft Transportation proxy
 */
public class BuildCraftTrasportationProxy extends ModProxy {
    public static final String MOD_ID = "BuildCraft|Transport";

    @Override
    public String getName() {
        return "BuildCraft Transport";
    }

    @Override
    public String getModID() {
        return MOD_ID;
    }

    @Override
    public void load() {
        Protections.instance.addProtection(new BuildCraftTransportProtection(), MOD_ID);
    }
}
