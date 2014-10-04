package mytown.proxies.mod;

import mytown.protection.ExtraUtilitiesProtection;
import mytown.protection.Protections;

/**
 * Created by AfterWind on 9/22/2014.
 * ExtraUtilities mod proxy
 */
public class ExtraUtilitiesProxy extends ModProxy {
    public static final String MOD_ID = "ExtraUtilities";

    @Override
    public String getName() {
        return "ExtraUtilities";
    }

    @Override
    public void load() {
        Protections.instance.addProtection(new ExtraUtilitiesProtection(), MOD_ID);
    }

    @Override
    public String getModID() {
        return MOD_ID;
    }
}
