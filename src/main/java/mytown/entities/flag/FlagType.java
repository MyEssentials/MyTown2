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
    private boolean canTownsModify;
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
        this.canTownsModify = true;
        this.defaultValue = defaultValue;
        this.isWildPerm = wildPerm;
        this.defaultWildPerm = defaultWildPerm;
    }

    private FlagType(Class<?> type, Object defaultValue, Object[] allowedValues, boolean wildPerm, Object defaultWildPerm, boolean townOnly, String modRequired) {
        this(type, defaultValue, allowedValues, wildPerm, defaultWildPerm, townOnly, modRequired, false);
    }

    /**
     * Gets the type that the Flag that uses this FlagType stores.
     */
    public Class<?> getType() {
        return this.type;
    }

    /**
     * Returns if this flag is found in the wild.
     */
    public boolean isWildPerm() {
        return this.isWildPerm;
    }

    /**
     * Returns the permission needed to bypass the protection.
     */
    public String getBypassPermission() {
        return this.bypassPerm;
    }

    /**
     * Returns the default value that this FlagType has as a Wild flag.
     */
    public Object getDefaultWildPerm() {
        return this.defaultWildPerm;
    }

    public Object getDefaultValue() {
        return this.defaultValue;
    }

    public boolean setDefaultValue(Object obj) {
        if (type.isAssignableFrom(obj.getClass())) {
            defaultValue = obj;
            return true;
        }
        return false;
    }


    public boolean canTownsModify() {
        return this.canTownsModify;
    }

    public void setModifiableForTowns(boolean bool) {
        this.canTownsModify = bool;
    }

    public boolean isTownOnly() {
        return townOnly;
    }

    /**
     * Checks to see if this type of flag has the mod needed to load
     */
    public boolean shouldLoad() {
        return (this.modRequired == null || ModProxies.isProxyLoaded(modRequired));
    }

    /**
     * Gets the localized description
     */
    public String getLocalizedDescription() {
        return LocalizationProxy.getLocalization().getLocalization(descriptionKey);
    }

    /**
     * Gets the localized message of when the flag denies an action of a player
     */
    public String getLocalizedProtectionDenial() {
        return LocalizationProxy.getLocalization().getLocalization(protectionKey);
    }

    /**
     * Gets the localized message that is sent to all players on the town when something tried to bypass protection.
     */
    public String getLocalizedTownNotification() {
        return LocalizationProxy.getLocalization().getLocalization(notifyKey);
    }

    public boolean isWhitelistable() {
        return isWhitelistable;
    }

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

