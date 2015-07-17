package mytown.test.datasource;

import mytown.entities.Town;
import mytown.entities.TownBlock;
import mytown.proxies.DatasourceProxy;
import mytown.test.TestMain;
import net.minecraftforge.common.config.Configuration;
import org.junit.Before;
import org.junit.Test;

import java.io.File;

/**
 * TODO: Have this be tested when we have a way to run a full minecraft server during the test
 */
public class DatasourceTest {

    //@Before
    public void shouldLoadDatasource() {
        TestMain.main();
        Configuration config = new Configuration(new File(TestMain.path, "MyTown.cfg"));
        DatasourceProxy.start(config);
    }

    //@Test
    public void shouldSaveTown() {
        Town town = new Town("TestTown");
        DatasourceProxy.getDatasource().saveTown(town);
        DatasourceProxy.getDatasource().saveBlock(new TownBlock(0, 3, 3, false, 0, town));

    }

}
