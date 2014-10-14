package mytown.proxies.mod;

import mytown.protection.BotaniaProtection;
import mytown.protection.Protections;

/**
 * Created by AfterWind on 10/14/2014.
 * Proxy for Botania mod
 */
public class BotaniaProxy extends ModProxy {

    public static final String MOD_ID = "Botania";

    @Override
    public String getName() {
        return "Botania";
    }

    @Override
    public String getModID() {
        return MOD_ID;
    }

    @Override
    public void load() {
        Protections.instance.addProtection(new BotaniaProtection(), MOD_ID);
    }
}
