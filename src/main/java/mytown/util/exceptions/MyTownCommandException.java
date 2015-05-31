package mytown.util.exceptions;

import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;

public class MyTownCommandException extends CommandException {
    public MyTownCommandException(String key, Object... args) {
        super(LocalizationProxy.getLocalization().getLocalization(key, args));
    }
}
