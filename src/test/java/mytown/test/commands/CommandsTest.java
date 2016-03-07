package mytown.test.commands;

import junit.framework.Assert;
import mypermissions.command.api.CommandResponse;
import mytown.commands.CommandsAdmin;
import mytown.test.MyTownTest;
import org.junit.Test;

import java.util.ArrayList;

public class CommandsTest extends MyTownTest {

    @Test
    public void testTownAdminCommand() {

        CommandResponse response = CommandsAdmin.townAdminCommand(player, null);
        Assert.assertEquals("The command response was not SEND_HELP_MESSAGE", CommandResponse.SEND_HELP_MESSAGE, response);

    }

    @Test
    public void testTownConfigCommand() {

        CommandResponse response = CommandsAdmin.configCommand(player, null);
        Assert.assertEquals("The command response was not SEND_HELP_MESSAGE", CommandResponse.SEND_HELP_MESSAGE, response);

    }

}
