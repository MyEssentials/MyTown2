package mytown.config.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import com.google.gson.TypeAdapter;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import myessentials.json.JSONConfig;
import mytown.MyTown;
import mytown.entities.Rank;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class RanksConfig extends JSONConfig<Rank> {

    public RanksConfig(String path) {
        super(path, "DefaultTownRanks");
        this.gsonType = new TypeToken<List<Rank>>() {}.getType();
        this.gson = new GsonBuilder().registerTypeAdapter(gsonType, new RankTypeAdapter()).setPrettyPrinting().create();
    }

    @Override
    public void create(List<Rank> items) {
        Rank.initDefaultRanks();
        items.addAll(Rank.defaultRanks);
        super.create(items);
    }

    @Override
    public List<Rank> read() {
        List<Rank> ranks = super.read();

        Rank.defaultRanks.clear();
        Rank.defaultRanks.addAll(ranks);

        return ranks;
    }

    @Override
    public boolean validate(List<Rank> items) {
        boolean isValid = true;
        for(Rank.Type type : Rank.Type.values()) {
            if(type.unique) {
                List<Rank> rankOfType = new ArrayList<Rank>();
                for(Rank rank : items) {
                    if(rank.getType() == type) {
                        rankOfType.add(rank);
                    }
                }

                if(rankOfType.size() == 0) {
                    isValid = false;
                    MyTown.instance.LOG.error("Unique type of Rank was not found in " + name);
                    items.add(Rank.defaultRanks.get(type));
                } else if(rankOfType.size() > 1) {
                    isValid = false;
                    MyTown.instance.LOG.error("Unique type of Rank was found multiple times in " + name + ". Setting all aside from the first to type regular.");
                    for(int i = 1; i < rankOfType.size(); i++) {
                        rankOfType.get(i).setType(Rank.Type.REGULAR);
                    }
                }
            }
        }

        return isValid;
    }

    public class RankTypeAdapter extends TypeAdapter<List<Rank>>{

        @Override
        public void write(JsonWriter out, List<Rank> ranks) throws IOException {
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
        public List<Rank> read(JsonReader in) throws IOException {
            List<Rank> ranks = new ArrayList<Rank>();

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
