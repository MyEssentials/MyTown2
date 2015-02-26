package mytown.entities.flag;

import mytown.proxies.LocalizationProxy;
import mytown.proxies.mod.ModProxies;

/**
 * Created by AfterWind on 9/16/2014.
 * Flags enumeration. Enumerating all flags here
 */
public enum FlagType {
    // CONSTRUCTOR: class, value, allowedValues, wildPerm, defaultWildPerm, townOnly, mod, isWhitelistable <false>

    // Allows entering the area
    enter(Boolean.class, true, null, false, true, false, null),

    // Allows opening GUIs and right-clicking TileEntities
    accessBlocks(Boolean.class, false, null, true, true, false, null, true),

    // Allows pickup of items.
    pickupItems(Boolean.class, true, null, false, true, false, null),

    // Allows pvp
    pvp(Boolean.class, false, null, false, false, false, null),

    // Only the values in the array are allowed
    // Allows modifying which types of mods are allowed in the town.
    mobs(String.class, "all", new String[]{"all", "hostiles", "none"}, false, "all", false, null),

    // Allows outsiders to hurt passive and other types of entities.
    protectedEntities(Boolean.class, false, null, false, true, false, null),

    // Allows the use of some items such as: Bucket, Spawn Eggs etc.
    useItems(Boolean.class, false, null, false, true, false, null),

    // Allows to activate blocks such as: Buttons, Doors etc.
    activateBlocks(Boolean.class, false, null, true, true, false, null, true),

    // ---- Flags that don't go in plots. ----
    // Allows modifying blocks.
    modifyBlocks(Boolean.class, false, null, true, true, true, null, true),

    // Allows explosions.
    explosions(Boolean.class, false, null, true, false, true, null),;

    private Class<?> type;
    private String descriptionKey;
    private String protectionKey;
    private String notifyKey;
    private String bypassPerm;
    private Object[] allowedValues;
    private boolean townOnly;
    private Object defaultValue;
    private boolean isUsableForTowns;
    private boolean isWhitelistable;
    private String modRequired;
    private boolean isWildPerm;
    private Object defaultWildPerm;

    private FlagType(Class<?> type, Object defaultValue, Object[] allowedValues, boolean wildPerm, Object defaultWildPerm, boolean townOnly, String modRequired, boolean isWhitelistable) {
        this.type = type;
        this.descriptionKey = "mytown.flag." + this.toString();
        this.protectionKey = "mytown.protection." + this.toString();
        this.notifyKey = "mytown.protection.notify." + this.toString();
        this.bypassPerm = "mytown.protection.bypass." + this.toString();
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
     *
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
    public boolean isWildPerm() {
        return this.isWildPerm;
    }

    /**
     * Returns the permission needed to bypass the protection.
     *
     * @return
     */
    public String getBypassPermission() {
        return this.bypassPerm;
    }

    /**
     * Gets the default wild perm.
     *
     * @return
     */
    public Object getDefaultWildPerm() {
        return this.defaultWildPerm;
    }

    /**
     * Gets the default value
     *
     * @return
     */
    public Object getDefaultValue() {
        return this.defaultValue;
    }

    /**
     * Sets the default value and returns if the value was set
     *
     * @param obj
     * @return
     */
    public boolean setDefaultValue(Object obj) {
        if (type.isAssignableFrom(obj.getClass())) {
            defaultValue = obj;
            return true;
        }
        return false;
    }


    /**
     * Returns if this type of flag should be in a town
     *
     * @return
     */
    public boolean isUsableForTowns() {
        return this.isUsableForTowns;
    }

    /**
     * Sets whether or not the flagtype is changable/usable in a town
     *
     * @param bool
     */
    public void setIsUsableForTowns(boolean bool) {
        this.isUsableForTowns = bool;
    }

    /**
     * If flag is town only then it's not gonna be found on plots
     *
     * @return
     */
    public boolean isTownOnly() {
        return townOnly;
    }

    /**
     * Checks to see if this type of flag has the mod needed to load
     *
     * @return
     */
    public boolean shouldLoad() {
        return (this.modRequired == null || ModProxies.isProxyLoaded(modRequired));
    }

    /**
     * Gets the localized description
     *
     * @return
     */
    public String getLocalizedDescription() {
        return LocalizationProxy.getLocalization().getLocalization(descriptionKey);
    }

    /**
     * Gets the localized message of when the flag denies an action of a player
     *
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
     *
     * @return
     */
    public boolean isWhitelistable() {
        return isWhitelistable;
    }

    /**
     * Checks to see if the value is allowed
     *
     * @param value
     * @return
     */
    public boolean isValueAllowed(Object value) {
        if (allowedValues == null) return true;
        else {
            for (Object s : allowedValues) {
                if (s.equals(value))
                    return true;
            }
        }
        return false;
    }


}

