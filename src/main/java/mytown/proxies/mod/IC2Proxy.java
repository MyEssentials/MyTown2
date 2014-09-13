package mytown.proxies.mod;

import mytown.protection.IC2Protection;
import mytown.protection.Protections;

/**
 * Created by AfterWind on 9/8/2014.
 * IC2 proxy
 */
public class IC2Proxy extends ModProxy {
    public static final String MOD_ID = "IC2";

    @Override
    public String getName() {
        return "Industrial Craft 2";
    }

    @Override
    public void load() {
        Protections.instance.addProtection(new IC2Protection());
    }

    @Override
    public String getModID() {
        return MOD_ID;
    }
}
