package mytown.protection.segment.caller;

import java.lang.reflect.Field;

public class CallerField extends Caller {

    public CallerField(String element, Class<?> valueType) {
        super(element, valueType);
    }

    @Override
    public Object invoke(Object instance, Object... parameters) throws Exception {
        try {
            Field fieldObject = instance.getClass().getField(name);
            return fieldObject.get(instance);
        } catch (NoSuchFieldException ex) {
            Field fieldObject = instance.getClass().getDeclaredField(name);
            fieldObject.setAccessible(true);
            return fieldObject.get(instance);
        }
    }

    @Override
    public String getCallerTypeString() {
        return "FIELD";
    }
}
