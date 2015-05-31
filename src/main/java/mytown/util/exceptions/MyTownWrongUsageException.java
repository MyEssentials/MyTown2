package mytown.util.exceptions;

import mytown.proxies.LocalizationProxy;
import net.minecraft.command.WrongUsageException;

public class MyTownWrongUsageException extends WrongUsageException {
    public MyTownWrongUsageException(String key, Object... args) {
        super(LocalizationProxy.getLocalization().getLocalization(key, args));
    }
}
