package mytown.test.datasource;

import mytown.entities.Town;

/**
 * TODO: Have this be tested when we have a way to run a full minecraft server during the test
 */
public class DatasourceTest {

    //@Before
    public void shouldLoadDatasource() {
        //TestMain.main();
        //Configuration config = new Configuration(new File(TestMain.path, "MyTown.cfg"));
    }

    //@Test
    public void shouldSaveTown() {
        Town town = new Town("TestTown");

    }

}
