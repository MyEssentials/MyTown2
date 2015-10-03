package mytown.api.container;

import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import mytown.entities.Rank;
import myessentials.utils.ColorUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RanksContainer extends ArrayList<Rank> {

    public boolean contains(String rankName) {
        for (Rank r : this) {
            if (r.getName().equals(rankName))
                return true;
        }
        return false;
    }

    public Rank get(String rankName) {
        for (Rank r : this) {
            if (r.getName().equals(rankName))
                return r;
        }
        return null;
    }

    public Rank get(Rank.Type type) {
        if(!type.unique) {
            throw new RuntimeException("The rank you are trying to get is not unique!");
        }

        for(Rank rank : this) {
            if(rank.getType() == type) {
                return rank;
            }
        }
        return null;
    }

    public Rank getMayorRank() {
        for(Rank rank : this) {
            if(rank.getType() == Rank.Type.MAYOR) {
                return rank;
            }
        }
        return null;
    }

    public Rank getDefaultRank() {
        for(Rank rank : this) {
            if(rank.getType() == Rank.Type.DEFAULT) {
                return rank;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        String res = null;
        for (Rank rank : this) {
            if (res == null) {
                res = rank.toString();
            } else {
                res += ColorUtils.colorComma + ", " + rank.toString();
            }
        }

        if (isEmpty()) {
            res = ColorUtils.colorEmpty + "NONE";
        }
        return res;
    }

    public static class Serializer extends TypeAdapter<RanksContainer> {

        @Override
        public void write(JsonWriter out, RanksContainer ranks) throws IOException {
            out.beginArray();
            for(Rank rank : ranks) {
                out.beginObject();
                out.name("name").value(rank.getName());
                out.name("type").value(rank.getType().toString());
                out.name("permissions").beginArray();
                for(String perm : rank.permissionsContainer) {
                    out.value(perm);
                }
                out.endArray();
                out.endObject();
            }
            out.endArray();
        }

        @Override
        public RanksContainer read(JsonReader in) throws IOException {
            RanksContainer ranks = new RanksContainer();

            in.beginArray();
            String nextName;
            while(in.peek() != JsonToken.END_ARRAY) {
                List<String> permissionsContainer = new ArrayList<String>();
                String name = null;
                Rank.Type type = null;

                in.beginObject();
                while(in.peek() != JsonToken.END_OBJECT) {
                    nextName = in.nextName();

                    if ("name".equals(nextName)) {
                        name = in.nextString();
                        continue;
                    }

                    if ("type".equals(nextName)) {
                        type = Rank.Type.valueOf(in.nextString().toUpperCase());
                        continue;
                    }

                    if ("permissions".equals(nextName)) {
                        in.beginArray();
                        while (in.peek() != JsonToken.END_ARRAY) {
                            permissionsContainer.add(in.nextString());
                        }
                        in.endArray();
                    }
                }
                in.endObject();

                if(name == null) {
                    throw new IOException("Rank name cannot be null!");
                }
                if(type == null) {
                    throw new IOException("Rank type cannot be null!");
                }

                Rank rank = new Rank(name, null, type);
                rank.permissionsContainer.addAll(permissionsContainer);
                ranks.add(rank);
            }
            in.endArray();

            return ranks;
        }
    }
}
