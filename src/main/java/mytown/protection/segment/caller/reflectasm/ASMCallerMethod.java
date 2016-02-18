package mytown.protection.segment.caller.reflectasm;

import com.esotericsoftware.reflectasm.MethodAccess;
import mytown.MyTown;
import mytown.protection.segment.caller.reflect.ReflectCallerMethod;

public class ASMCallerMethod extends ReflectCallerMethod {
    private MethodAccess access;
    private int index;

    @Override
    public Object invoke(Object instance, Object... parameters) throws Exception {
        // Fallback to reflection in-case ReflectASM can't get to the method for whatever reason
        if (index == -1) {
            return super.invoke(instance, parameters);
        }

        return access.invoke(instance, index, parameters);
    }

    @Override
    public Class<?> nextClass() throws Exception {
        if (index != -1) {
            return access.getReturnTypes()[index];
        }

        return super.nextClass();
    }

    @Override
    public void setClass(Class<?> clazz) {
        super.setClass(clazz);

        // Setup ReflectASM stuff
        try {
            access = MethodAccess.get(clazz);
            index = access.getIndex(name);
        } catch (Exception ex) {
            MyTown.instance.LOG.debug(name + " - Falling back to reflection based method caller", ex);
            index = -1;
        }
    }
}
