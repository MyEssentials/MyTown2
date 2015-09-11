package mytown.entities.flag;

import mytown.MyTown;

/**
 * Flags enumeration. Enumerating all flags here
 */
public enum ProtectionFlagType {
    // Allows entering the area
    ENTER(true, Property.IN_TOWN, Property.IN_PLOT),

    // Allows opening GUIs and right-clicking TileEntities
    ACCESS(false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD, Property.WHITELISTABLE),

    // Allows pickup of items.
    PICKUP(true, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD, Property.WHITELISTABLE),

    // Allows pvp
    PVP(false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD),

    // Allows mobs to spawn
    MOBS(false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD),

    // Allows living entities to spawn
    ENTITIES(true, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD),

    // Allows outsiders to hurt passive and other types of entities.
    PVE(false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD),

    // Allows the use of some items such as: Bucket, Spawn Eggs etc.
    USAGE(false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD),

    // Allows to activate blocks such as: Buttons, Doors etc.
    ACTIVATE(false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD, Property.WHITELISTABLE),

    // Allows fake players to bypass some of the flags (MODIFY, USAGE, PVE)
    FAKERS(true, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD),

    // Allows modifying blocks.
    MODIFY(false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD),

    // Allows explosions.
    EXPLOSIONS(false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD),

    // Allows normal residents to have permission outside their claimed plots.
    RESTRICTIONS(false, Property.IN_TOWN),

    // Allows other nearby towns to be created nearby
    NEARBY(false, Property.IN_TOWN);

    public boolean defaultValue = false;
    public boolean isWhitelistable = false;
    public boolean isWildPerm = false;
    public boolean defaultWildValue = false;
    public boolean isPlotPerm = false;
    public boolean isTownPerm = false;

    ProtectionFlagType(boolean defaultValue, boolean defaultWildValue, Property... properties) {
        for(Property property : properties) {
            switch (property) {
                case IN_PLOT:
                    this.isPlotPerm = true;
                    this.defaultValue = defaultValue;
                    break;
                case IN_TOWN:
                    this.isTownPerm = true;
                    this.defaultValue = defaultValue;
                    break;
                case IN_WILD:
                    this.isWildPerm = true;
                    this.defaultWildValue = defaultWildValue;
                    break;
                case WHITELISTABLE:
                    this.isWhitelistable = true;
                    break;
            }
        }
    }

    ProtectionFlagType(boolean defaultValue, Property... properties) {
        this(defaultValue, false, properties);
    }

    public String getLocalizedDescription() {
        return MyTown.instance.LOCAL.getLocalization("mytown.flag." + this.toString());
    }

    public String getLocalizedProtectionDenial() {
        return MyTown.instance.LOCAL.getLocalization("mytown.protection." + this.toString());
    }

    public String getLocalizedTownNotification() {
        return MyTown.instance.LOCAL.getLocalization("mytown.protection.notify." + this.toString());
    }

    public String getBypassPermission() {
        return "mytown.bypass.flag." + this.toString().toLowerCase();
    }

    private enum Property {
        IN_TOWN,
        IN_PLOT,
        IN_WILD,
        WHITELISTABLE


    }
}

