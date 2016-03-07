package mytown.test;


import metest.api.BaseTest;
import metest.api.TestPlayer;
import mytown.entities.Resident;
import mytown.new_datasource.MyTownUniverse;
import org.junit.Before;

public class MyTownTest extends BaseTest {

    protected TestPlayer player = new TestPlayer(this.server, "Test Resident");
    protected Resident resident = new Resident(player);

    @Before
    public void init() {
        MyTownUniverse.instance.addResident(resident);
    }

}
