package mytown.util.exceptions;

import mytown.proxies.LocalizationProxy;
import net.minecraft.command.CommandException;

public class MyTownCommandException extends CommandException {
    public MyTownCommandException(String key, Object... args) {
        super(LocalizationProxy.getLocalization().getLocalization(key, args));
    }

    public MyTownCommandException(String key, Throwable cause, Object... args) {
        this(key, args);
        initCause(cause);
    }
}
