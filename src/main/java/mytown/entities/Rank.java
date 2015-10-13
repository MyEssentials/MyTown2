package mytown.entities;

import com.google.common.collect.ImmutableList;
import com.google.gson.*;
import mypermissions.api.container.PermissionsContainer;
import mytown.api.container.RanksContainer;
import net.minecraft.util.EnumChatFormatting;

import java.lang.reflect.Type;

public class Rank {

    /**
     * All the default ranks that are added to each town on creation (except AdminTowns)
     */
    public static final RanksContainer defaultRanks = new RanksContainer();

    public static void initDefaultRanks() {

        Rank mayorRank = new Rank("Mayor", null, Type.MAYOR);
        Rank assistantRank = new Rank("Assistant", null, Type.REGULAR);
        Rank residentRank = new Rank("Resident", null, Type.DEFAULT);

        mayorRank.permissionsContainer.add("mytown.cmd");
        mayorRank.permissionsContainer.add("mytown.bypass");

        assistantRank.permissionsContainer.add("mytown.cmd");
        assistantRank.permissionsContainer.add("-mytown.cmd.mayor");
        assistantRank.permissionsContainer.add("mytown.bypass.plot");
        assistantRank.permissionsContainer.add("mytown.bypass.flag");

        residentRank.permissionsContainer.add("mytown.cmd.everyone");
        residentRank.permissionsContainer.add("mytown.cmd.outsider");
        residentRank.permissionsContainer.add("mytown.bypass.flag");
        residentRank.permissionsContainer.add("-mytown.bypass.flag.restrictions");

        Rank.defaultRanks.clear();
        Rank.defaultRanks.add(mayorRank);
        Rank.defaultRanks.add(assistantRank);
        Rank.defaultRanks.add(residentRank);
    }

    private String name, newName = null;
    private Town town;
    private Type type;

    public final PermissionsContainer permissionsContainer = new PermissionsContainer();

    public Rank(String name, Town town, Type type) {
        this.name = name;
        this.town = town;
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void rename(String newName) {
        this.newName = newName;
    }

    public void resetNewName() {
        this.name = newName;
        this.newName = null;
    }

    public String getNewName() {
        return this.newName;
    }

    public Town getTown() {
        return town;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return type.color + getName();
    }

    public enum Type {
        /**
         * Rank that can do anything
         */
        MAYOR(EnumChatFormatting.RED.toString(), true),

        /**
         * Rank that is assigned to players on joining the town
         */
        DEFAULT(EnumChatFormatting.GREEN.toString(), true),

        /**
         * Nothing special to this rank
         */
        REGULAR(EnumChatFormatting.WHITE.toString(), false);

        public final String color;
        public final boolean unique;

        Type(String color, boolean unique) {
            this.color = color;
            this.unique = unique;
        }
    }

    public static class Serializer implements JsonSerializer<Rank>, JsonDeserializer<Rank> {

        @Override
        public Rank deserialize(JsonElement json, java.lang.reflect.Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            JsonObject jsonObject = json.getAsJsonObject();

            String name = jsonObject.get("name").getAsString();
            Rank.Type rankType = Type.valueOf(jsonObject.get("type").getAsString());
            Rank rank = new Rank(name, null, rankType);
            if (jsonObject.has("permissions")) {
                rank.permissionsContainer.addAll(ImmutableList.copyOf(context.<String[]>deserialize(jsonObject.get("permissions"), String[].class)));
            }
            return rank;
        }

        @Override
        public JsonElement serialize(Rank rank, java.lang.reflect.Type typeOfSrc, JsonSerializationContext context) {
            JsonObject json = new JsonObject();

            json.addProperty("name", rank.name);
            json.addProperty("type", rank.type.toString());
            json.add("permissions", context.serialize(rank.permissionsContainer, String[].class));

            return json;
        }
    }
}
