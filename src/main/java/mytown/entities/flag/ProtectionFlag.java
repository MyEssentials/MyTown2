package mytown.entities.flag;

import com.google.common.reflect.TypeToken;

public class ProtectionFlag extends Flag<Boolean> {

    public final ProtectionFlagType flagType;

    public ProtectionFlag(ProtectionFlagType flagType, Boolean defaultValue) {
        super(flagType.toString(), defaultValue);
        this.flagType = flagType;
        gsonType = new TypeToken<Boolean>() {}.getType();
    }

    @Override
    public boolean setValue(String str) {
        if(str.equalsIgnoreCase("false") || str.equalsIgnoreCase("true")) {
            this.value = Boolean.valueOf(str);
            return true;
        }
        return false;
    }
}
