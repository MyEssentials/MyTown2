package mytown.entities.flag;

import mytown.proxies.LocalizationProxy;
import mytown.proxies.mod.BuildCraftProxy;
import mytown.proxies.mod.IC2Proxy;
import mytown.proxies.mod.ModProxies;

/**
 * Created by AfterWind on 9/16/2014.
 * Flags enumeration. Enumerating all flags here
 */
public enum FlagType {
    enter(Boolean.class, null, false, null),
    breakBlocks(Boolean.class, null, false, null, true),
    accessBlocks(Boolean.class, null, false, null, true),
    placeBlocks(Boolean.class, null, false, null, true),
    pickupItems(Boolean.class, null, false, null),
    explosions(Boolean.class, null, false, null),
    // Only the values in the array are allowed
    mobs(String.class, new String[] {"all", "hostiles", "none"}, false, null),
    attackEntities(Boolean.class, null, false, null),
    useItems(Boolean.class, null, false, null),
    activateBlocks(Boolean.class, null, false, null, true),
    ic2EnergyFlow(Boolean.class, null, false, IC2Proxy.MOD_ID),
    // Only allowed in towns, not in plots
    bcBuildingMining(Boolean.class, null, true, BuildCraftProxy.MOD_ID);

    private Class<?> type;
    private String descriptionKey;
    private String protectionKey;
    private Object[] allowedValues;
    private boolean townOnly;
    // Temporary, till every flag is whitelistable
    private boolean isWhitelistable;
    private String modRequired;

    private FlagType(Class<?> type, Object[] allowedValues, Boolean townOnly, String modRequired, Boolean isWhitelistable) {
        this.type = type;
        this.descriptionKey = "mytown.flag." + this.toString();
        this.protectionKey = "mytown.protection." + this.toString();
        this.townOnly = townOnly;
        this.allowedValues = allowedValues;
        this.modRequired = modRequired;
        this.isWhitelistable = isWhitelistable;
    }

    private FlagType(Class<?> type, Object[] allowedValues, Boolean townOnly, String modRequired) {
        this(type, allowedValues, townOnly, modRequired, false);
    }

    /**
     * Type that is used by the flag
     * @return
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * If flag is town only then it's not gonna be found on plots
     * @return
     */
    public boolean isTownOnly() {
        return townOnly;
    }

    /**
     * Checks to see if this type of flag has the mod needed to load
     * @return
     */
    public boolean shouldLoad() {
        return ( this.modRequired == null || ModProxies.isProxyLoaded(modRequired));
    }

    /**
     * Gets the localized description
     * @return
     */
    public String getLocalizedDescription() {
        return LocalizationProxy.getLocalization().getLocalization(descriptionKey);
    }

    /**
     * Gets the localized message of when the flag denies an action of a player
     * @return
     */
    public String getLocalizedProtectionDenial() {
        return LocalizationProxy.getLocalization().getLocalization(protectionKey);
    }

    /**
     * If it's a flag that allows whitelists
     * @return
     */
    public boolean isWhitelistable() {
        return isWhitelistable;
    }

    /**
     * Checks to see if the value is allowed
     * @param value
     * @return
     */
    public boolean isValueAllowed(Object value) {
        if(allowedValues == null) return true;
        else {
            for(Object s : allowedValues) {
                if(s.equals(value))
                    return true;
            }
        }
        return false;
    }


}

