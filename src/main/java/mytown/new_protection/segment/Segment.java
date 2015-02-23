package mytown.new_protection.segment;

import bsh.EvalError;
import bsh.Interpreter;
import mytown.MyTown;
import mytown.entities.flag.FlagType;
import mytown.new_protection.ProtectionUtils;
import mytown.util.MyTownUtils;
import mytown.util.exceptions.ConditionException;
import mytown.util.exceptions.GetterException;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.tileentity.TileEntity;
import net.minecraftforge.common.util.Constants;
import org.apache.commons.lang3.StringUtils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 1/1/2015.
 * A part of the protection that protects against a specific thing.
 */
public class Segment {
    public Class<?> theClass;
    public FlagType flag;
    public Map<String, List<Getter>> extraGettersMap;
    public String[] conditionString;

    public Segment(Class<?> theClass, Map<String, List<Getter>> extraGettersMap, String conditionString) {
        this.theClass = theClass;
        this.extraGettersMap = extraGettersMap;
        if(conditionString != null)
            this.conditionString = conditionString.split(" ");
    }

    public boolean checkCondition(Object object) {

        if(conditionString == null)
            return true;

        MyTown.instance.log.info("Checking condition: " + StringUtils.join(conditionString, " "));
        boolean current;

        /*
            This is very important when checking as the ItemStack that is passed doesn't
            have the methods and fields needed only the Item itself does.

            Block, TileEntity and Entity should be left unchanged as their classes should be mod defined
         */

        Object instance;
        if(object instanceof ItemStack) {
            instance = ((ItemStack) object).getItem();
        } else {
            instance = object;
        }
        for(int i = 0; i < conditionString.length; i += 4) {

            // Get the boolean value of each part of the condition.
            if(MyTownUtils.tryParseBoolean(conditionString[i + 2])) {
                boolean value = (Boolean) getInfoFromGetters(conditionString[i], Boolean.class, instance, object);
                if (conditionString[i + 1].equals("==")) {
                    current = value == Boolean.parseBoolean(conditionString[i + 2]);
                } else if(conditionString[i + 1].equals("!=")) {
                    current = value != Boolean.parseBoolean(conditionString[i + 2]);
                } else {
                    throw new ConditionException("[Segment: " + this.theClass.getName() + "] The element number " + (i / 4) + 1 + " has an invalid condition!");
                }
            } else if(MyTownUtils.tryParseInt(conditionString[i + 2])) {
                int value = (Integer) getInfoFromGetters(conditionString[i], Integer.class, instance, object);
                if(conditionString[i+1].equals("==")) {
                    current = value == Integer.parseInt(conditionString[i + 2]);
                } else if(conditionString[i + 1].equals("!=")) {
                    current = value != Integer.parseInt(conditionString[i + 2]);
                } else if(conditionString[i+1].equals("<")) {
                    current = value < Integer.parseInt(conditionString[i + 2]);
                } else if(conditionString[i+1].equals(">")) {
                    current = value > Integer.parseInt(conditionString[i + 2]);
                } else {
                    throw new ConditionException("[Segment: "+ this.theClass.getName() +"] The element number " + (i / 4) + 1 + " has an invalid condition!");
                }
            } else if(MyTownUtils.tryParseFloat(conditionString[i + 2])) {
                float value = (Integer) getInfoFromGetters(conditionString[i], Integer.class, instance, object);
                if(conditionString[i+1].equals("==")) {
                    current = value == Float.parseFloat(conditionString[i + 2]);
                } else if(conditionString[i + 1].equals("!=")) {
                    current = value != Float.parseFloat(conditionString[i + 2]);
                } else if(conditionString[i+1].equals("<")) {
                    current = value < Float.parseFloat(conditionString[i + 2]);
                } else if(conditionString[i+1].equals(">")) {
                    current = value > Float.parseFloat(conditionString[i + 2]);
                } else {
                    throw new ConditionException("[Segment: "+ this.theClass.getName() +"] The element number " + ((i/4)+1) + " has an invalid condition!");
                }
            } else {
                throw new ConditionException("[Segment: "+ this.theClass.getName() +"] The element with number " + ((i/4)+1) + " has an invalid type to be checked against!");
            }

            if(conditionString.length <= i + 3 || current && conditionString[i + 3].equals("OR") || !current && conditionString[i + 3].equals("AND"))
                return current;

            if(!conditionString[i + 3].equals("OR") && !conditionString[i + 3].equals("AND"))
                throw new ConditionException("[Segment: "+ this.theClass.getName()  +"] Invalid condition element: " + conditionString[i + 3]);
        }
        return false;
    }

    /**
     * From a list of Getters it tries to get a value of type with all its getters calling on the instance.
     * If method call fails with no parameters it's gonna try to add the parameter for the call.
     *
     * @return
     */
    public Object getInfoFromGetters(String getterName, Class<?> returnType, Object instance, Object parameter) {
        Object lastInstance = instance;
        List<Getter> getterList = extraGettersMap.get(getterName);

        if(getterList == null) {
            throw new GetterException("[Segment:"+ theClass.getName() +"] Getter with name \"" + getterName + "\" could not be found.");
        }

        try {
            forLoop: for (Getter getter : getterList) {
                switch (getter.type) {
                    case field:
                        Field fieldObject = lastInstance.getClass().getField(getter.element);
                        fieldObject.setAccessible(true);
                        lastInstance = fieldObject.get(lastInstance);
                        break;
                    case method:
                        Method methodObject = lastInstance.getClass().getDeclaredMethod(getter.element);
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
                        lastInstance = getInfoFromFormula(getter.element, instance, parameter);
                        break forLoop;
                    case value:
                        // Return instantly since it can only be a constant
                        lastInstance = Integer.parseInt(getter.element);
                        break forLoop;
                    case nbt:
                        if(lastInstance instanceof TileEntity) {
                            NBTTagCompound nbt = new NBTTagCompound();
                            ((TileEntity) lastInstance).writeToNBT(nbt);
                            lastInstance = nbt.getTag(getter.element);
                            MyTown.instance.log.info("Got tag with name: " + getter.element);
                        } else if(lastInstance instanceof Item) {
                            lastInstance = ((ItemStack)parameter).getTagCompound().getTag(getter.element);
                        } else if(lastInstance instanceof NBTTagCompound) {
                            lastInstance = ((NBTTagCompound) lastInstance).getTag(getter.element);

                            if(lastInstance instanceof NBTTagDouble) {
                                lastInstance = ((NBTTagDouble) lastInstance).func_150286_g();
                            } else if(lastInstance instanceof NBTTagFloat) {
                                lastInstance = ((NBTTagFloat) lastInstance).func_150288_h();
                            } else if(lastInstance instanceof NBTTagInt) {
                                lastInstance = ((NBTTagInt) lastInstance).func_150287_d();
                            } else if(lastInstance instanceof NBTTagString) {
                                lastInstance = ((NBTTagString) lastInstance).func_150285_a_();
                            }

                            MyTown.instance.log.info("Got tag with name: " + getter.element + " and value " + lastInstance.toString());
                        } else if(lastInstance instanceof NBTTagList) {

                            // Getting the id of the list
                            int id = -1;
                            try {
                                id = Integer.parseInt(getter.element);
                            } catch (NumberFormatException ex) {
                                // TODO: Generalise this to be put in the list down below
                                throw new GetterException("[Segment:"+ theClass.getName() +"] Cannot parse element to Integer for NBTTagList id in getter: " + getterName);
                            }

                            // Checking if out of bounds
                            if(id < 0 || id >= ((NBTTagList) lastInstance).tagCount())
                                // TODO: Generalise this to be put in the list down below
                                throw new GetterException("[Segment:"+ theClass.getName() +"] ID for NBTTagList is out of bounds ( < 0 or >= " + ((NBTTagList) lastInstance).tagCount() + " ) in getter: " + getterName);

                            lastInstance = ((NBTTagList) lastInstance).getCompoundTagAt(id);
                        }
                        break;
                }
            }
            if(!returnType.isAssignableFrom(lastInstance.getClass()))
                throw new GetterException("[Segment:"+ theClass.getName() +"] Failed to get " + returnType.getSimpleName() + " type in getter: " + getterName);
            return lastInstance;
        } catch(NoSuchFieldException nfex) {
            throw new GetterException("[Segment:"+ theClass.getName() +"] Failed to get a field in getter: " + getterName, nfex);
        } catch (IllegalAccessException iaex) {
            throw new GetterException("[Segment:"+ theClass.getName() +"] Failed to access a field/method in getter: " + getterName, iaex);
        } catch (NoSuchMethodException nmex) {
            throw new GetterException("[Segment:"+ theClass.getName() +"] Failed to get a method in getter: " + getterName, nmex);
        } catch (InvocationTargetException itex) {
            throw new GetterException("[Segment:"+ theClass.getName() +"] Failed to get " + returnType.getSimpleName() + " type in getter: " + getterName, itex);
        }
    }

    /**
     * Converts a formula that contains Getter identifiers to an (integer) value
     *
     * @param formula
     * @param instance
     * @param parameter
     * @return
     */
    public int getInfoFromFormula(String formula, Object instance, Object parameter) {
        int result = -1;

        String[] elements = formula.split(" ");

        // Replace all the getters with proper numbers, assume getters that are invalid as being numbers
        for(String element : elements) {
            if(!element.equals("+") && !element.equals("-") && !element.equals("*") && !element.equals("/") && !element.equals("^")) {
                if(extraGettersMap.get(element) != null) {
                    int info = (Integer) getInfoFromGetters(element, Integer.class, instance, parameter);
                    // Replace all occurrences with the value that it got.
                    // Spaces are needed to not replace parts of other getters.
                    formula = formula.replace(" " + element + " ", " " + String.valueOf(info) + " ");
                }
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
