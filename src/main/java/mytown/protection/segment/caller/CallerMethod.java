package mytown.protection.segment.caller;

import java.lang.reflect.Method;

public class CallerMethod extends Caller {
    public CallerMethod(String element, Class<?> valueType) {
        super(element, valueType);
    }

    @Override
    public Object invoke(Object instance, Object... parameters) throws Exception {
        try {
            Method methodObject = instance.getClass().getMethod(name);
            try {
                return methodObject.invoke(instance);
            } catch (IllegalArgumentException ex) {
                try {
                    return methodObject.invoke(instance, parameters);
                } catch (IllegalArgumentException ex2) {
                    throw ex;
                }
            }
        } catch (NoSuchMethodException ex) {
            Method methodObject = instance.getClass().getDeclaredMethod(name);
            methodObject.setAccessible(true);
            try {
                return methodObject.invoke(instance);
            } catch (IllegalArgumentException ex1) {
                try {
                    return methodObject.invoke(instance, parameters);
                } catch (IllegalArgumentException ex2) {
                    // Throwing the original exception.
                    throw ex1;
                }
            }
        }
    }

    @Override
    public String getCallerTypeString() {
        return "METHOD";
    }
}
