package mytown.proxies.mod;

import mytown.protection.AppliedEnergisticsProtection;
import mytown.protection.Protections;

/**
 * Created by AfterWind on 10/3/2014.
 * Applied Energistics mod proxy.
 */
public class AppliedEnergisticsProxy extends ModProxy {
    public static final String MOD_ID = "appliedenergistics2";

    @Override
    public String getName() {
        return "Applied Energistics 2";
    }

    @Override
    public String getModID() {
        return MOD_ID;
    }

    @Override
    public void load() {
        Protections.instance.addProtection(new AppliedEnergisticsProtection(), MOD_ID);
    }
}
