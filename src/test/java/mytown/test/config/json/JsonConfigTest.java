package mytown.test.config.json;

import myessentials.json.api.JsonConfig;
import mytown.config.json.FlagsConfig;
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

    private List<JsonConfig> jsonConfigs;

    @Before
    public void shouldInitConfigs() {

        TestMain.main();

        jsonConfigs = new ArrayList<JsonConfig>();
        //jsonConfigs.add(new RanksConfig(path + "/DefaultRanks.json"));
        jsonConfigs.add(new WildPermsConfig(TestMain.path + "/WildPerms.json"));
        jsonConfigs.add(new FlagsConfig(TestMain.path + "/DefaultFlags.json"));
    }

    @Test
    public void shouldLoadConfigs() {
        for (JsonConfig jsonConfig : jsonConfigs) {
            jsonConfig.init();
        }
    }

    @Test
    public void shouldWildHaveProperValues() {
        for(Flag flag : Wild.instance.flagsContainer) {
            Assert.assertEquals(flag.value, flag.flagType.defaultWildValue);
        }
    }
}
