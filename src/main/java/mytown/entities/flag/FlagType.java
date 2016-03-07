package mytown.entities.flag;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import myessentials.json.api.SerializerTemplate;
import mytown.MyTown;
import net.minecraft.util.IChatComponent;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class FlagType<T> implements Comparable<FlagType<T>>{
    public static final FlagType<Boolean> ENTER, ACCESS, PICKUP, PVP, MOBS, ENTITIES,
            PVE, USAGE, ACTIVATE, FAKERS, MODIFY, EXPLOSIONS, RESTRICTIONS, NEARBY;


    private static final List<FlagType<?>> values = new ArrayList<FlagType<?>>();
    public static ImmutableList<FlagType<?>> values() {
        return ImmutableList.copyOf(values);
    }

    public static List<FlagType<?>> getWhitelistable() {
        List<FlagType<?>> whitelistableFlags = new ArrayList<FlagType<?>>();
        for(FlagType flagType : FlagType.values()) {
            if(flagType.isWhitelistable) {
                whitelistableFlags.add(flagType);
            }
        }
        return whitelistableFlags;
    }

    static {
        // Allows entering the area
        ENTER = new FlagType<Boolean>("ENTER", true, Property.IN_TOWN, Property.IN_PLOT);

        // Allows opening GUIs and right-clicking TileEntities
        ACCESS = new FlagType<Boolean>("ACCESS", false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD, Property.WHITELISTABLE);

        // Allows pickup of items.
        PICKUP = new FlagType<Boolean>("PICKUP", true, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD, Property.WHITELISTABLE);

        // Allows PVP
        PVP = new FlagType<Boolean>("PVP", false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD);

        // Allows hostile mobs to spawn
        MOBS = new FlagType<Boolean>("MOBS", false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD);

        // Allows living entities to spawn
        ENTITIES = new FlagType<Boolean>("ENTITIES", true, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD);

        // Allows PVE
        PVE = new FlagType<Boolean>("PVE", false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD);

        // Allows the use of items such as: Buckets, Spawn Eggs etc.
        USAGE = new FlagType<Boolean>("USAGE", false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD);

        // Allows the right-clicking of certain blocks, such as: Doors, Buttons, Levers etc.
        ACTIVATE = new FlagType<Boolean>("ACTIVATE", false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD, Property.WHITELISTABLE);

        // Allows actions to be performed by fake players
        FAKERS = new FlagType<Boolean>("FAKERS", true, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD);

        // Allows breaking or placing blocks
        MODIFY = new FlagType<Boolean>("MODIFY", false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD);

        // Allows explosions
        EXPLOSIONS = new FlagType<Boolean>("EXPLOSIONS", false, true, Property.IN_TOWN, Property.IN_PLOT, Property.IN_WILD);

        // Allows normal residents to have permission outside their plots
        RESTRICTIONS = new FlagType<Boolean>("RESTRICTIONS", false, Property.IN_TOWN);

        // Allows other players to create towns nearby
        NEARBY = new FlagType<Boolean>("NEARBY", false, Property.IN_TOWN);

        values.add(ENTER);
        values.add(ACCESS);
        values.add(PICKUP);
        values.add(PVP);
        values.add(MOBS);
        values.add(ENTITIES);
        values.add(PVE);
        values.add(USAGE);
        values.add(ACTIVATE);
        values.add(FAKERS);
        values.add(MODIFY);
        values.add(EXPLOSIONS);
        values.add(RESTRICTIONS);
        values.add(NEARBY);
    }

    public static FlagType valueOf(String name) {
        for(FlagType flagType : values) {
            if(flagType.name.equals(name)) {
                return flagType;
            }
        }
        throw new IllegalArgumentException(name + " flag type does not exist");
    }

    private final Gson gson = new GsonBuilder().create();

    public final String name;
    public final Class<T> type;
    public T defaultValue;
    public T defaultWildValue;
    public boolean isWhitelistable = false;
    public boolean isWildPerm = false;
    public boolean isPlotPerm = false;
    public boolean isTownPerm = false;
    public boolean configurable = true;

    @SuppressWarnings("unchecked")
    private FlagType(String name, T defaultValue, T defaultWildValue, Property... properties) {
        this.name = name;
        this.type = (Class<T>)defaultValue.getClass();
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

    private FlagType(String name, T defaultValue, Property... properties) {
        this(name, defaultValue, null, properties);
    }

    @Override
    public int compareTo(FlagType<T> other) {
        return name.compareTo(other.name);
    }

    public String getDescriptionKey() {
        return "mytown.flag." + name;
    }

    public String getDenialKey() {
        return "mytown.protection." + name;
    }

    public String getTownNotificationKey() {
        return "mytown.protection.notify." + name;
    }

    public String getBypassPermission() {
        return "mytown.bypass.flag." + name.toLowerCase();
    }

    public String serializeValue(T value) {
        return gson.toJson(value, type);
    }

    @Override
    public String toString()
    {
        return name;
    }

    private enum Property {
        IN_TOWN,
        IN_PLOT,
        IN_WILD,
        WHITELISTABLE
    }

    public static class Serializer extends SerializerTemplate<FlagType> {

        @Override
        public void register(GsonBuilder builder) {
            builder.registerTypeAdapter(FlagType.class, this);
        }

        @Override
        public JsonElement serialize(FlagType flagType, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(flagType.name);
        }

        @Override
        public FlagType deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            return valueOf(json.getAsString());
        }
    }
}
