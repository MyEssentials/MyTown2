package mytown.proxies.mod;

import mytown.protection.Protections;
import mytown.protection.ThermalExpansionProtection;

/**
 * Created by AfterWind on 9/27/2014.
 * Thermal Expansion proxy.
 */
public class ThermalExpansionProxy extends ModProxy {
    public static final String MOD_ID = "ThermalExpansion";

    @Override
    public String getName() {
        return "Thermal Expansion";
    }

    @Override
    public String getModID() {
        return MOD_ID;
    }

    @Override
    public void load() {
        Protections.instance.addProtection(new ThermalExpansionProtection(), MOD_ID);
    }
}
