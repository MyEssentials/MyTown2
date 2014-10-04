package mytown.proxies.mod;


import mytown.protection.BloodMagicProtection;
import mytown.protection.Protections;

/**
 * Created by AfterWind on 9/22/2014.
 * BloodMagic mod proxy
 */
public class BloodMagicProxy extends ModProxy{
    public static final String MOD_ID = "AWWayofTime";

    @Override
    public String getName() {
        return "BloodMagic";
    }

    @Override
    public String getModID() {
        return MOD_ID;
    }

    @Override
    public void load() {
        Protections.instance.addProtection(new BloodMagicProtection(), MOD_ID);
    }
}
