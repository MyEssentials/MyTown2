package mytown.entities.flag;

import mytown.proxies.LocalizationProxy;

/**
 * Flags enumeration. Enumerating all flags here
 */
public enum FlagType {
    // CONSTRUCTOR: class, value, allowedValues, wildPerm, defaultWildPerm, townOnly, mod, isWhitelistable <false>

    // Allows entering the area
    ENTER(Boolean.class, true, null, false, true, Property.ALL, null),

    // Allows opening GUIs and right-clicking TileEntities
    ACCESS_BLOCKS(Boolean.class, false, null, true, true, Property.ALL, null, true),

    // Allows pickup of items.
    PICKUP_ITEMS(Boolean.class, true, null, true, true, Property.ALL, null),

    // Allows pvp
    PVP(Boolean.class, false, null, true, true, Property.ALL, null),

    // Only the values in the array are allowed
    // Allows modifying which types of mods are allowed in the town.
    MOBS(String.class, "all", new String[]{"all", "hostiles", "none"}, true, "all", Property.ALL, null),

    // Allows outsiders to hurt passive and other types of entities.
    PROTECTED_ENTITIES(Boolean.class, false, null, true, false, Property.ALL, null),

    // Allows the use of some items such as: Bucket, Spawn Eggs etc.
    USE_ITEMS(Boolean.class, false, null, true, true, Property.ALL, null),

    // Allows to activate blocks such as: Buttons, Doors etc.
    ACTIVATE_BLOCKS(Boolean.class, false, null, true, true, Property.ALL, null, true),

    // Allows fake players to bypass some of the flags (MODIFY_BLOCKS, USE_ITEMS, PROTECTED_ENTITIES)
    ALLOW_FAKE_PLAYERS(Boolean.class, true, null, true, true, Property.ALL, null),

    // ---- Flags that don't go in plots. ----

    // Allows modifying blocks.
    MODIFY_BLOCKS(Boolean.class, false, null, true, true, Property.TOWN_ONLY, null, true),

    // Allows explosions.
    EXPLOSIONS(Boolean.class, false, null, true, true, Property.TOWN_ONLY, null),

    // Allows normal residents to have permission outside their claimed plots.
    RESTRICTED_TOWN_PERMS(Boolean.class, false, null, false, false, Property.TOWN_ONLY, null),

    // Allows other towns to be created nearby
    NEARBY_TOWNS(Boolean.class, false, null, false, false, Property.TOWN_ONLY, null);

    private Class<?> type;
    private Object[] allowedValues;
    private Object defaultValue;
    private Property property;
    private boolean canTownsModify;
    private boolean isWhitelistable;
    private boolean isWildPerm;
    private Object defaultWildPerm;

    FlagType(Class<?> type, Object defaultValue, Object[] allowedValues, boolean wildPerm, Object defaultWildPerm, Property property, String modRequired, boolean isWhitelistable) {
        this.type = type;
        this.property = property;
        this.allowedValues = allowedValues;
        this.isWhitelistable = isWhitelistable;
        this.canTownsModify = true;
        this.defaultValue = defaultValue;
        this.isWildPerm = wildPerm;
        this.defaultWildPerm = defaultWildPerm;
    }

    FlagType(Class<?> type, Object defaultValue, Object[] allowedValues, boolean wildPerm, Object defaultWildPerm, Property property, String modRequired) {
        this(type, defaultValue, allowedValues, wildPerm, defaultWildPerm, property, modRequired, false);
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
        return "mytown.protection.bypass." + this.toString();
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
        return property == Property.TOWN_ONLY;
    }

    /**
     * Gets the localized description
     */
    public String getLocalizedDescription() {
        return LocalizationProxy.getLocalization().getLocalization("mytown.flag." + this.toString());
    }

    /**
     * Gets the localized message of when the flag denies an action of a player
     */
    public String getLocalizedProtectionDenial() {
        return LocalizationProxy.getLocalization().getLocalization("mytown.protection." + this.toString());
    }

    /**
     * Gets the localized message that is sent to all players on the town when something tried to bypass protection.
     */
    public String getLocalizedTownNotification() {
        return LocalizationProxy.getLocalization().getLocalization("mytown.protection.notify." + this.toString());
    }

    public boolean isWhitelistable() {
        return isWhitelistable;
    }

    public boolean isValueAllowed(Object value) {
        if (allowedValues == null)
            return true;
        else {
            for (Object s : allowedValues) {
                if (s.equals(value))
                    return true;
            }
        }
        return false;
    }

    public enum Property {
        ALL,
        TOWN_ONLY,
        PLOT_ONLY
    }

}

