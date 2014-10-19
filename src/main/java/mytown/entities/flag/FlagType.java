package mytown.entities.flag;

import cpw.mods.fml.common.registry.GameRegistry;
import mytown.proxies.LocalizationProxy;
import mytown.proxies.mod.*;
import net.minecraft.block.Block;
import net.minecraft.tileentity.TileEntity;

/**
 * Created by AfterWind on 9/16/2014.
 * Flags enumeration. Enumerating all flags here
 */
public enum FlagType {
    // CONSTRUCTOR: class, value, allowedValues, wildPerm, defaultWildPerm, townOnly, mod, isWhitelistable <false>
    enter(Boolean.class, true, null, false, true, false, null),
    breakBlocks(Boolean.class, false, null, true, true, false, null),
    accessBlocks(Boolean.class, false, null, true, true, false, null, true),
    placeBlocks(Boolean.class, false, null, true, true, false, null),
    pickupItems(Boolean.class, true, null, false, true, false, null),
    explosions(Boolean.class, false, null, true, false, false, null),
    // Only the values in the array are allowed
    mobs(String.class, "all", new String[] {"all", "hostiles", "none"}, false, "all", false, null),
    attackEntities(Boolean.class, false, null, false, true, false, null),
    useItems(Boolean.class, false, null, false, true, false, null),
    activateBlocks(Boolean.class, false, null, true, true, false, null, true),
    pumps(Boolean.class, true, null, false, true, false, ModProxies.EXTRA_UTILITIES_MOD_ID);

    private Class<?> type;
    private String descriptionKey;
    private String protectionKey;
    private String notifyKey;
    private Object[] allowedValues;
    private boolean townOnly;
    private Object defaultValue;
    private boolean isUsableForTowns;
    // Temporary, till every flag is whitelistable
    private boolean isWhitelistable;
    private String modRequired;
    private boolean isWildPerm;
    private Object defaultWildPerm;

    private FlagType(Class<?> type, Object defaultValue, Object[] allowedValues, boolean wildPerm, Object defaultWildPerm, boolean townOnly, String modRequired, boolean isWhitelistable) {
        this.type = type;
        this.descriptionKey = "mytown.flag." + this.toString();
        this.protectionKey = "mytown.protection." + this.toString();
        this.notifyKey = "mytown.protection.notify." + this.toString();
        this.townOnly = townOnly;
        this.allowedValues = allowedValues;
        this.modRequired = modRequired;
        this.isWhitelistable = isWhitelistable;
        this.isUsableForTowns = true;
        this.defaultValue = defaultValue;
        this.isWildPerm = wildPerm;
        this.defaultWildPerm = defaultWildPerm;
    }

    private FlagType(Class<?> type, Object defaultValue, Object[] allowedValues, boolean wildPerm, Object defaultWildPerm, boolean townOnly, String modRequired) {
        this(type, defaultValue, allowedValues, wildPerm, defaultWildPerm, townOnly, modRequired, false);
    }

    /**
     * Type that is used by the flag
     * @return
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * Returns if this flag is found in the wild >:D
     *
     * @return
     */
    public boolean isWildPerm() { return this.isWildPerm; }

    /**
     * Gets the default wild perm.
     *
     * @return
     */
    public Object getDefaultWildPerm() { return this.defaultWildPerm; }

    /**
     * Gets the default value
     *
     * @return
     */
    public Object getDefaultValue() { return this.defaultValue; }

    /**
     * Sets the default value and returns if the value was set
     *
     * @param obj
     * @return
     */
    public boolean setDefaultValue(Object obj) {
        if(type.isAssignableFrom(obj.getClass())) {
            defaultValue = obj;
            return true;
        }
        return false;
    }


    /**
     *  Returns if this type of flag should be in a town
      * @return
     */
    public boolean isUsableForTowns() { return this.isUsableForTowns; }

    /**
     * Sets whether or not the flagtype is changable/usable in a town
     *
     * @param bool
     */
    public void setIsUsableForTowns(boolean bool) { this.isUsableForTowns = bool; }

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
        return (this.modRequired == null || ModProxies.isProxyLoaded(modRequired));
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
     * Gets the localized message that is sent to all players on the town when something tried to bypass protection.
     *
     * @return
     */
    public String getLocalizedTownNotification() {
        return LocalizationProxy.getLocalization().getLocalization(notifyKey);
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

