package mytown.protection.segment.caller.reflect;

import mytown.protection.segment.caller.Caller;

import java.lang.reflect.Field;

public class ReflectCallerField extends Caller {
    private Field field;

    @Override
    public Object invoke(Object instance, Object... parameters) throws Exception {
        return getField().get(instance);
    }

    private Field getField() throws Exception {
        // Lazy loading ftw!
        if (field == null) {
            try {
                field = checkClass.getField(name);
            } catch (NoSuchFieldException ex) {
                field = checkClass.getDeclaredField(name);
                field.setAccessible(true);
            }
        }

        return field;
    }
}
