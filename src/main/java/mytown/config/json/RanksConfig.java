package mytown.config.json;

import com.google.common.reflect.TypeToken;
import com.google.gson.GsonBuilder;
import myessentials.json.JsonConfig;
import mytown.MyTown;
import mytown.api.container.RanksContainer;
import mytown.entities.Rank;

import java.util.ArrayList;
import java.util.List;

public class RanksConfig extends JsonConfig<Rank, RanksContainer> {

    public RanksConfig(String path) {
        super(path, "DefaultTownRanks");
        this.gsonType = new TypeToken<RanksContainer>() {}.getType();
        this.gson = new GsonBuilder().registerTypeAdapter(gsonType, new RanksContainer.Serializer()).setPrettyPrinting().create();
    }

    @Override
    protected RanksContainer newList() {
        return new RanksContainer();
    }

    @Override
    public void create(RanksContainer items) {
        Rank.initDefaultRanks();
        items.addAll(Rank.defaultRanks);
        super.create(items);
    }

    @Override
    public RanksContainer read() {
        RanksContainer ranks = super.read();

        Rank.defaultRanks.clear();
        Rank.defaultRanks.addAll(ranks);

        return ranks;
    }

    @Override
    public boolean validate(RanksContainer items) {
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
}
