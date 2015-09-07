package mytown.util.exceptions;

import mytown.MyTown;
import net.minecraft.command.WrongUsageException;

public class MyTownWrongUsageException extends WrongUsageException {
    public MyTownWrongUsageException(String key, Object... args) {
        super(MyTown.instance.LOCAL.getLocalization(key, args));
    }
}
