package mytown.util.exceptions;

import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;

/**
 * @author Joe Goett
 */
public class MyTownCommandException extends CommandException {
    public MyTownCommandException(String key, Object... args) {
        super(LocalizationProxy.getLocalization().getLocalization(key, args));
    }
}
