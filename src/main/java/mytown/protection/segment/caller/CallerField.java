package mytown.protection.segment.caller;

import java.lang.reflect.Field;

public class CallerField extends Caller {
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
