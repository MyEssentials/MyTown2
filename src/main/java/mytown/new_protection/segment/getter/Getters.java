package mytown.new_protection.segment.getter;

import bsh.EvalError;
import bsh.Interpreter;
import com.google.common.collect.ImmutableMap;
import mytown.util.exceptions.GetterException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 3/6/2015.
 * Stores all the getters from a Segment
 */
public class Getters {
    
    private Map<String, List<Caller>> callersMap;
    private Map<String, Object> constantsMap;
    
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
                switch (caller.type) {
                    case field:
                        Field fieldObject = lastInstance.getClass().getField(caller.element);
                        fieldObject.setAccessible(true);
                        lastInstance = fieldObject.get(lastInstance);
                        break;
                    case method:
                        Method methodObject = lastInstance.getClass().getDeclaredMethod(caller.element);
                        methodObject.setAccessible(true);
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
                        break;
                    case formula:
                        // Return instantly since it can only be a number
                        lastInstance = getInfoFromFormula(caller.element, instance, parameter);
                        break forLoop;
                    case value:
                        // Return instantly since it can only be a constant
                        lastInstance = Integer.parseInt(caller.element);
                        break forLoop;
                    case nbt:
                        if(lastInstance instanceof TileEntity) {
                            NBTTagCompound nbt = new NBTTagCompound();
                            ((TileEntity) lastInstance).writeToNBT(nbt);
                            lastInstance = nbt.getTag(caller.element);
                            //MyTown.instance.log.info("Got tag with name: " + getter.element);
                        } else if(lastInstance instanceof Item) {
                            lastInstance = ((ItemStack)parameter).getTagCompound().getTag(caller.element);
                        } else if(lastInstance instanceof NBTTagCompound) {
                            lastInstance = ((NBTTagCompound) lastInstance).getTag(caller.element);

                            if(lastInstance instanceof NBTTagDouble) {
                                lastInstance = ((NBTTagDouble) lastInstance).func_150286_g();
                            } else if(lastInstance instanceof NBTTagFloat) {
                                lastInstance = ((NBTTagFloat) lastInstance).func_150288_h();
                            } else if(lastInstance instanceof NBTTagInt) {
                                lastInstance = ((NBTTagInt) lastInstance).func_150287_d();
                            } else if(lastInstance instanceof NBTTagString) {
                                lastInstance = ((NBTTagString) lastInstance).func_150285_a_();
                            }

                            //MyTown.instance.log.info("Got tag with name: " + getter.element + " and value " + lastInstance.toString());
                        } else if(lastInstance instanceof NBTTagList) {

                            // Getting the id of the list
                            int id = -1;
                            try {
                                id = Integer.parseInt(caller.element);
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
            if(!returnType.isAssignableFrom(lastInstance.getClass()))
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
     * Converts a formula that contains Getter identifiers to an (integer) value
     */
    public int getInfoFromFormula(String formula, Object instance, Object parameter) {
        int result = -1;

        String[] elements = formula.split(" ");

        // Replace all the getters with proper numbers, assume getters that are invalid as being numbers
        for(String element : elements) {
            if(!element.equals("+") && !element.equals("-") && !element.equals("*") && !element.equals("/") && !element.equals("^")) {
                int info = (Integer) getValue(element, Integer.class, instance, parameter);
                // Replace all occurrences with the value that it got.
                // Spaces are needed to not replace parts of other getters.
                formula = formula.replace(" " + element + " ", " " + String.valueOf(info) + " ");
            }
        }

        //MyTown.instance.log.info("Got formula at the end: " + formula);
        //MyTown.instance.log.info("Trying to parse it.");

        Interpreter interpreter = new Interpreter();
        try {
            interpreter.eval("result = " + formula);
            result = (Integer)interpreter.get("result");
        } catch (EvalError ex) {
            ex.printStackTrace();
        }
        //MyTown.instance.log.info("Returning: " + result);
        return result;
    }
}
