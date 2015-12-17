package mytown.protection.segment.caller;

import java.lang.reflect.Method;

public class CallerMethod extends Caller {
    private Method method;

    @Override
    public Object invoke(Object instance, Object... parameters) throws Exception {
        try {
            return getMethod().invoke(instance);
        } catch (IllegalArgumentException ex) {
            try {
                return getMethod().invoke(instance, parameters);
            } catch (IllegalArgumentException ex2) {
                throw ex;
            }
        }
    }

    private Method getMethod() throws Exception {
        // Lazy loading ftw!
        if (method == null) {
            try {
                method = checkClass.getMethod(name);
            } catch (NoSuchMethodException ex) {
                method = checkClass.getDeclaredMethod(name);
                method.setAccessible(true);
            }
        }

        return method;
    }
}
