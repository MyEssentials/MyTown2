package mytown.protection.segment.getter;

import mytown.protection.segment.caller.Caller;
import mytown.util.exceptions.GetterException;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

/**
 * From a list of Callers it tries to get a value of type with all its getters calling on the instance.
 * OR It simply gets a constant available in the constants.
 * If method call fails with no parameters it's gonna try to add the parameter for the call.
 */
public class GetterDynamic extends Getter {

    public final List<Caller> callers = new ArrayList<Caller>();

    public GetterDynamic(List<Caller> callers) {
        if(callers != null) {
            this.callers.addAll(callers);
        }
    }

    @Override
    public void setClass(Class<?> clazz) {
        Class<?> currClass = clazz;
        for (Caller caller : this.callers) {
            caller.setClass(currClass);
            try {
                currClass = caller.nextClass();
            } catch(Exception ex) {
            }
        }
    }

    @Override
    public Object invoke(Class<?> returnType, Object instance, Object... parameters) throws GetterException {
        if(instance == null) {
            return null;
        }

        Object lastInstance = instance;

        for (Caller caller : callers) {
            try {

                lastInstance = caller.invoke(lastInstance, parameters);

            } catch(NoSuchFieldException nfex) {
                throw new GetterException("Failed to get field " + caller.getName() + " in getter: " + name, nfex);
            } catch (IllegalAccessException iaex) {
                throw new GetterException("Failed to access field/method " + caller.getName() +" in getter: " + name, iaex);
            } catch (NoSuchMethodException nmex) {
                throw new GetterException("Failed to get method " + caller.getName() + " in getter: " + name, nmex);
            } catch (InvocationTargetException itex) {
                throw new GetterException("Failed to get " + returnType.getSimpleName() + " type in getter: " + name, itex);
            } catch (NumberFormatException numex) {
                throw new GetterException("Failed to get Integer type in getter: " + name + ". Found " + caller.getName(), numex);
            } catch (IndexOutOfBoundsException iobex) {
                throw new GetterException("Integer out of bounds in getter: " + name + ". Found " + caller.getName(), iobex);
            } catch (Exception ex) {
                throw new GetterException("Unknown exception has been thrown in getter: " + name, ex);
            }
            if (lastInstance == null) {
                throw new GetterException("Failed to get " + returnType.getSimpleName() + " type in getter: " + name + ". Value returned null.");
            }
        }

        if(!returnType.isAssignableFrom(lastInstance.getClass())) {
            throw new GetterException("Failed to get " + returnType.getSimpleName() + " type in getter: " + name);
        }

        Caller lastCaller = callers.get(callers.size() - 1);
        if(lastCaller.getValueType() != null && !lastCaller.getValueType().isAssignableFrom(lastInstance.getClass())) {
            throw new GetterException("Failed to get " + lastCaller.getValueType().getSimpleName() + " type in getter: " + name);
        }

        return lastInstance;
    }
}
