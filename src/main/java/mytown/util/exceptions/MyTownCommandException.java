package mytown.util.exceptions;

import mytown.MyTown;
import net.minecraft.command.CommandException;

public class MyTownCommandException extends CommandException {
    public MyTownCommandException(String key, Object... args) {
        super(MyTown.instance.LOCAL.getLocalization(key, args));
    }

    public MyTownCommandException(String key, Throwable cause, Object... args) {
        this(key, args);
        initCause(cause);
    }
}
