package mytown.test.entities;

import myessentials.teleport.Teleport;
import mytown.entities.*;
import mytown.entities.flag.Flag;
import mytown.entities.flag.FlagType;
import mytown.test.TestMain;
import org.junit.Assert;

import java.util.UUID;

/**
 * TODO: Have this be tested when we have a way to run a full minecraft server during the test
 */
public class EntitiesTest {

    private Town town;
    private Resident resident;
    private Plot plot;


    //@Before
    public void shouldLoadSomeEntities() {
        TestMain.main();

        Rank onCreationDefaultRank = null;

        town = new Town("TestTown");

        // Setting spawn before saving
        town.setSpawn(new Teleport(0, 0, 0, 0, 0, 0));

        //Claiming first block
        TownBlock block = new TownBlock(0, 0, 0, false, 0, town);
        town.townBlocksContainer.add(block);
        // Saving and adding all flags to the database
        for (FlagType type : FlagType.values()) {
            if (type.canTownsModify()) {
                town.flagsContainer.add(new Flag(type, type.getDefaultValue()));
            }
        }

        Rank rank = new Rank("Test", town, Rank.Type.REGULAR);
        town.ranksContainer.add(rank);
        town.bank.setAmount(100);

        resident = new Resident(UUID.randomUUID(), "TestPlayer");
        town.residentsMap.put(resident, rank);

        plot = new Plot("TestPlot", town, 0, 0, 0, 0, 14, 90, 14);
        town.plotsContainer.add(plot);
    }

    //@Test
    public void shouldCheckPermissionsProperly() {
        Assert.assertTrue(town.hasPermission(resident, FlagType.ACCESS, false));
        // Inside of TestPlot
        Assert.assertTrue(town.hasPermission(resident, FlagType.MODIFY, false, 0, 1, 1, 1));
    }

    //@Test
    public void shouldCheckPermissionsWithRestrictionsFlagProperly() {
        // Outside of plot, inside town with Restrictions flag false
        Assert.assertTrue(town.hasPermission(resident, FlagType.PICKUP, false, 0, 15, 0, 15));
        town.flagsContainer.get(FlagType.RESTRICTIONS).setValueFromString("true");
        Assert.assertFalse(town.hasPermission(resident, FlagType.PICKUP, false, 0, 15, 0, 15));
    }

}
