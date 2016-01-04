package mytown.protection.segment.caller.reflectasm;

import com.esotericsoftware.reflectasm.FieldAccess;
import mytown.MyTown;
import mytown.protection.segment.caller.reflect.ReflectCallerField;

public class ASMCallerField extends ReflectCallerField {
    private FieldAccess access;
    private int index;

    @Override
    public Object invoke(Object instance, Object... parameters) throws Exception {
        // Fallback to reflection in-case ReflectASM can't get to the field for whatever reason
        if (index == -1) {
            return super.invoke(instance, parameters);
        }

        return access.get(instance, index);
    }

    @Override
    public Class<?> nextClass() throws Exception {
        if (index != -1) {
            return access.getFieldTypes()[index];
        }

        return super.nextClass();
    }

    @Override
    public void setClass(Class<?> clazz) {
        super.setClass(clazz);

        // Setup ReflectASM stuff
        try {
            access = FieldAccess.get(clazz);
            index = access.getIndex(name);
        } catch(Exception ex) {
            MyTown.instance.LOG.debug(name + " - Falling back to reflection based field caller", ex);
            index = -1;
        }
    }
}
