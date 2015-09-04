package mytown.protection.segment.getter;

import bsh.EvalError;
import bsh.Interpreter;

import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableMap;

import mytown.MyTown;
import mytown.util.exceptions.GetterException;
import net.minecraft.entity.Entity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;

import org.apache.commons.lang3.exception.ExceptionUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Stores all the getters from a Segment
 */
public class Getters {
    
    private final Map<String, List<Caller>> callersMap;
    private final Map<String, Object> constantsMap;
    
    private String segmentName;
    
    public Getters() {
        callersMap = new HashMap<String, List<Caller>>();
        constantsMap = new HashMap<String, Object>();
    }

    public void setName(String segmentName) {
        this.segmentName = segmentName;
    }
    
    public void addCallers(String name, List<Caller> callers) {
        callersMap.put(name, callers);
    }
    
    public void addConstant(String name, Object value) {
        constantsMap.put(name, value);   
    }

    public void removeGetter(String name) {
        if(callersMap.containsKey(name))
            callersMap.remove(name);
        else if(constantsMap.containsKey(name))
            constantsMap.remove(name);
    }

    public Map<String, List<Caller>> getCallersMap() { return ImmutableMap.copyOf(callersMap); }
    public Map<String, Object> getConstantsMap() { return ImmutableMap.copyOf(constantsMap); }

    public boolean hasValue(String key) {
        return callersMap.containsKey(key) || constantsMap.containsKey(key);
    }

    /**
     * From a list of Callers it tries to get a value of type with all its getters calling on the instance.
     * OR It simply gets a constant available in the constantsMap.
     * If method call fails with no parameters it's gonna try to add the parameter for the call.
     */
    public Object getValue(String callerName, Class<?> returnType, Object instance, Object parameter) {

        // If it is a constant then simply return it

        Object constant = constantsMap.get(callerName);
        if(constant != null) {
            return constant;
        }

        // If it is a dynamic Caller list then try to cascade through them and get the value needed

        List<Caller> callerList = callersMap.get(callerName);
        if(callerList == null) {
            throw new GetterException("[Segment:"+ segmentName +"] Getter with name \"" + callerName + "\" could not be found.");
        }

        Object lastInstance = instance;

        try {
            forLoop: for (Caller caller : callerList) {
                if (lastInstance == null) {
                    return null;
                }
                switch (caller.getCallerType()) {
                    case FIELD:
                        try {
                            Field fieldObject = lastInstance.getClass().getField(caller.getElement());
                            lastInstance = fieldObject.get(lastInstance);
                        } catch (NoSuchFieldException ex) {
                            Field fieldObject = lastInstance.getClass().getDeclaredField(caller.getElement());
                            fieldObject.setAccessible(true);
                            lastInstance = fieldObject.get(lastInstance);
                        }
                        break;
                    case METHOD:
                        try {
                            Method methodObject = lastInstance.getClass().getMethod(caller.getElement());
                            try {
                                lastInstance = methodObject.invoke(lastInstance);
                            } catch (IllegalArgumentException ex) {
                                try {
                                    lastInstance = methodObject.invoke(lastInstance, parameter);
                                } catch (IllegalArgumentException ex2) {
                                    // Throwing the original exception.
                                    throw ex;
                                }
                            }
                        } catch (NoSuchMethodException ex) {
                            Method methodObject = lastInstance.getClass().getDeclaredMethod(caller.getElement());
                            methodObject.setAccessible(true);
                            try {
                                lastInstance = methodObject.invoke(lastInstance);
                            } catch (IllegalArgumentException ex1) {
                                try {
                                    lastInstance = methodObject.invoke(lastInstance, parameter);
                                } catch (IllegalArgumentException ex2) {
                                    // Throwing the original exception.
                                    throw ex1;
                                }
                            }
                        }
                        break;
                    case FORMULA:
                        // Return instantly since it can only be a number
                        lastInstance = getInfoFromFormula(caller.getElement(), instance, parameter);
                        break forLoop;
                    case NBT:
                        if(lastInstance instanceof Entity) {
                            NBTTagCompound nbt = ((Entity) lastInstance).getEntityData();
                            lastInstance = nbt.getTag(caller.getElement());
                        } else if(lastInstance instanceof TileEntity) {
                            NBTTagCompound nbt = new NBTTagCompound();
                            ((TileEntity) lastInstance).writeToNBT(nbt);
                            lastInstance = nbt.getTag(caller.getElement());
                        } else if(lastInstance instanceof Item) {
                            lastInstance = ((ItemStack)parameter).getTagCompound().getTag(caller.getElement());
                        } else if(lastInstance instanceof NBTTagCompound) {
                            lastInstance = ((NBTTagCompound) lastInstance).getTag(caller.getElement());

                            if(lastInstance instanceof NBTTagDouble) {
                                lastInstance = ((NBTTagDouble) lastInstance).func_150286_g();
                            } else if(lastInstance instanceof NBTTagFloat) {
                                lastInstance = ((NBTTagFloat) lastInstance).func_150288_h();
                            } else if(lastInstance instanceof NBTTagInt) {
                                lastInstance = ((NBTTagInt) lastInstance).func_150287_d();
                            } else if(lastInstance instanceof NBTTagString) {
                                lastInstance = ((NBTTagString) lastInstance).func_150285_a_();
                            }
                        } else if(lastInstance instanceof NBTTagList) {

                            // Getting the id of the list
                            int id = -1;
                            try {
                                id = Integer.parseInt(caller.getElement());
                            } catch (NumberFormatException ex) {
                                // TODO: Generalise this to be put in the list down below
                                throw new GetterException("[Segment:"+ segmentName +"] Cannot parse element to Integer for NBTTagList id in getter: " + callerName, ex);
                            }

                            // Checking if out of bounds
                            if(id < 0 || id >= ((NBTTagList) lastInstance).tagCount())
                                // TODO: Generalise this to be put in the list down below
                                throw new GetterException("[Segment:"+ segmentName +"] ID for NBTTagList is out of bounds ( < 0 or >= " + ((NBTTagList) lastInstance).tagCount() + " ) in getter: " + callerName);

                            lastInstance = ((NBTTagList) lastInstance).getCompoundTagAt(id);
                        }
                        break;
                }
            }
            if(returnType == Integer.class)
                lastInstance = tryConvert(lastInstance);

            if(lastInstance == null)
                throw new GetterException("[Segment:"+ segmentName +"] Failed to get " + returnType.getSimpleName() + " type in getter: " + callerName + ". Value returned null.");

            if(returnType != Object.class &&
                    (!returnType.isAssignableFrom(lastInstance.getClass()) ||
                            callerList.get(callerList.size() - 1).getValueType() != null &&
                                    !callerList.get(callerList.size() - 1).getValueType().isAssignableFrom(lastInstance.getClass())))
                throw new GetterException("[Segment:"+ segmentName +"] Failed to get " + returnType.getSimpleName() + " type in getter: " + callerName);
            return lastInstance;
        } catch(NoSuchFieldException nfex) {
            throw new GetterException("[Segment:"+ segmentName +"] Failed to get a field in getter: " + callerName, nfex);
        } catch (IllegalAccessException iaex) {
            throw new GetterException("[Segment:"+ segmentName +"] Failed to access a field/method in getter: " + callerName, iaex);
        } catch (NoSuchMethodException nmex) {
            throw new GetterException("[Segment:"+ segmentName +"] Failed to get a method in getter: " + callerName, nmex);
        } catch (InvocationTargetException itex) {
            throw new GetterException("[Segment:"+ segmentName +"] Failed to get " + returnType.getSimpleName() + " type in getter: " + callerName, itex);
        }
    }

    /**
     * Converts an Object that is Float, Double, Short or Long to an Integer type.
     */
    private Object tryConvert(Object obj) {
        if(obj instanceof Float) {
            return ((Float) obj).intValue();
        } else if(obj instanceof Double) {
            return ((Double) obj).intValue();
        } else if(obj instanceof Long) {
            return ((Long) obj).intValue();
        } else if(obj instanceof Short)
            return ((Short) obj).intValue();
        return obj;
    }

    /**
     * Converts a formula that contains Getter identifiers to an (integer) value
     */
    private Object getInfoFromFormula(String formula, Object instance, Object parameter) {
        Object result = null;

        String[] elements = formula.split(" ");

        // Replace all the getters with proper numbers, assume getters that are invalid as being numbers
        for(int i = 0 ; i < elements.length; i++) {
            if(!"+".equals(elements[i]) && !"-".equals(elements[i]) && !"*".equals(elements[i]) && !"/".equals(elements[i]) && !"^".equals(elements[i]) && hasValue(elements[i])) {

                Object info = getValue(elements[i], Object.class, instance, parameter);
                // Replace all occurrences with the value that it got.
                // Spaces are needed to not replace parts of other getters.
                elements[i] = info.toString();
            }
        }

        formula = Joiner.on(' ').join(elements);

        Interpreter interpreter = new Interpreter();
        try {
            interpreter.eval("result = " + formula);
            result = interpreter.get("result");
        } catch (EvalError ex) {
            MyTown.instance.LOG.error(ExceptionUtils.getStackTrace(ex));
        }

        return result;
    }
}
