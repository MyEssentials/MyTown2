package mytown.entities.flag;

import mytown.proxies.LocalizationProxy;

/**
 * Flags enumeration. Enumerating all flags here
 */
public enum FlagType {
    // CONSTRUCTOR: class, value, allowedValues, wildPerm, defaultWildPerm, townOnly, isWhitelistable <false>

    // allows entering the area
    ENTER(Boolean.class, true, null, false, true, Property.all),

    // allows opening GUIs and right-clicking TileEntities
    ACCESS(Boolean.class, false, null, true, true, Property.all, true),

    // allows pickup of items.
    PICKUP(Boolean.class, true, null, true, true, Property.all),

    // allows pvp
    PVP(Boolean.class, false, null, true, true, Property.all),

    // Only the values in the array are allowed
    // allows modifying which types of mods are allowed in the town.
    MOBS(String.class, "all", new String[]{"all", "passives", "none"}, true, "all", Property.all),

    // allows outsiders to hurt passive and other types of entities.
    PVE(Boolean.class, false, null, true, true, Property.all),

    // allows the use of some items such as: Bucket, Spawn Eggs etc.
    USAGE(Boolean.class, false, null, true, true, Property.all),

    // allows to activate blocks such as: Buttons, Doors etc.
    ACTIVATE(Boolean.class, false, null, true, true, Property.all, true),

    // allows fake players to bypass some of the flags (MODIFY, USAGE, PVE)
    FAKERS(Boolean.class, true, null, true, true, Property.all),

    // ---- Flags that don't go in plots. ----

    // allows modifying blocks.
    MODIFY(Boolean.class, false, null, true, true, Property.townOnly),

    // allows explosions.
    EXPLOSIONS(Boolean.class, false, null, true, true, Property.townOnly),

    // allows normal residents to have permission outside their claimed plots.
    RESTRICTIONS(Boolean.class, false, null, false, false, Property.townOnly),

    // allows other nearby towns to be created nearby
    NEARBY(Boolean.class, false, null, false, false, Property.townOnly);

    private Class<?> type;
    private Object[] allowedValues;
    private Object defaultValue;
    private Property property;
    private boolean canTownsModify;
    private boolean isWhitelistable;
    private boolean isWildPerm;
    private Object defaultWildPerm;

    FlagType(Class<?> type, Object defaultValue, Object[] allowedValues, boolean wildPerm, Object defaultWildPerm, Property property, boolean isWhitelistable) {
        this.type = type;
        this.property = property;
        this.allowedValues = allowedValues;
        this.isWhitelistable = isWhitelistable;
        this.canTownsModify = true;
        this.defaultValue = defaultValue;
        this.isWildPerm = wildPerm;
        this.defaultWildPerm = defaultWildPerm;
    }

    FlagType(Class<?> type, Object defaultValue, Object[] allowedValues, boolean wildPerm, Object defaultWildPerm, Property property) {
        this(type, defaultValue, allowedValues, wildPerm, defaultWildPerm, property, false);
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
        return property == Property.townOnly;
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

    public boolean isValueallowed(Object value) {
        if (allowedValues == null)
            return true;
        else {
            for (Object s : allowedValues) {
                if (s.equals(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public enum Property {
        all,
        townOnly,
        plotOnly
    }

}
