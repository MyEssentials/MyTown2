package mytown.util.exceptions;

import mypermissions.command.core.exception.CommandException;


public class MyTownCommandException extends CommandException {
    public MyTownCommandException(String key, Object... args) {
        super(key, args);
    }

    public MyTownCommandException(String key, Throwable cause, Object... args) {
        this(key, args);
        initCause(cause);
    }
}
