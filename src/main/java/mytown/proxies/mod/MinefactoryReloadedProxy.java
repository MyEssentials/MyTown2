package mytown.proxies.mod;

import mytown.protection.MinefactoryReloadedProtection;
import mytown.protection.Protections;

/**
 * Created by AfterWind on 10/11/2014.
 * Proxy for MFR
 */
public class MinefactoryReloadedProxy extends ModProxy {
    public static final String MOD_ID = "MineFactoryReloaded";

    @Override
    public String getName() {
        return "Minefactory Reloaded";
    }

    @Override
    public String getModID() {
        return MOD_ID;
    }

    @Override
    public void load() {
        Protections.instance.addProtection(new MinefactoryReloadedProtection(), MOD_ID);
    }
}
