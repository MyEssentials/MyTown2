package mytown.test.config.json;

import mytown.config.json.FlagsConfig;
import myessentials.json.JSONConfig;
import mytown.config.json.WildPermsConfig;
import mytown.entities.Wild;
import mytown.entities.flag.Flag;
import mytown.test.TestMain;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class JsonConfigTest {

    private List<JSONConfig> jsonConfigs;

    @Before
    public void shouldInitConfigs() {

        TestMain.main();

        jsonConfigs = new ArrayList<JSONConfig>();
        //jsonConfigs.add(new RanksConfig(path + "/DefaultRanks.json"));
        jsonConfigs.add(new WildPermsConfig(TestMain.path + "/WildPerms.json"));
        jsonConfigs.add(new FlagsConfig(TestMain.path + "/DefaultFlags.json"));
    }

    @Test
    public void shouldLoadConfigs() {
        for (JSONConfig jsonConfig : jsonConfigs) {
            jsonConfig.init();
        }
    }

    @Test
    public void shouldWildHaveProperValues() {
        for(Flag flag : Wild.instance.flagsContainer) {
            Assert.assertEquals(flag.getValue(), flag.getFlagType().getDefaultWildPerm());
        }
    }
}
