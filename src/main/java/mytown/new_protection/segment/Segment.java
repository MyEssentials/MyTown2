package mytown.new_protection.segment;

import mytown.MyTown;
import mytown.entities.flag.FlagType;
import mytown.new_protection.ProtectionUtils;
import mytown.util.MyTownUtils;
import net.minecraft.item.ItemStack;
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
                boolean value = (Boolean) ProtectionUtils.getInfoFromGetters(extraGettersMap.get(conditionString[i]), instance, Boolean.class, this.theClass.getName(), object);
                if (conditionString[i + 1].equals("==")) {
                    current = value == Boolean.parseBoolean(conditionString[i + 2]);
                } else if(conditionString[i + 1].equals("!=")) {
                    current = value != Boolean.parseBoolean(conditionString[i + 2]);
                } else {
                    throw new RuntimeException("[Segment: " + this.theClass.getName() + "] The element number " + (i / 4) + 1 + " has an invalid condition!");
                }
            } else if(MyTownUtils.tryParseInt(conditionString[i+2])) {
                int value = (Integer)ProtectionUtils.getInfoFromGetters(extraGettersMap.get(conditionString[i]), instance, Integer.class, this.theClass.getName(), object);
                if(conditionString[i+1].equals("==")) {
                    current = value == Integer.parseInt(conditionString[i+2]);
                } else if(conditionString[i + 1].equals("!=")) {
                    current = value != Integer.parseInt(conditionString[i + 2]);
                } else if(conditionString[i+1].equals("<")) {
                    current = value < Integer.parseInt(conditionString[i+2]);
                } else if(conditionString[i+1].equals(">")) {
                    current = value > Integer.parseInt(conditionString[i+2]);
                } else {
                    throw new RuntimeException("[Segment: "+ this.theClass.getName() +"] The element number " + (i/4)+1 + " has an invalid condition!");
                }
            } else if(MyTownUtils.tryParseFloat(conditionString[i+2])) {
                float value = (Integer)ProtectionUtils.getInfoFromGetters(extraGettersMap.get(conditionString[i]), instance, Float.class, this.theClass.getName(), object);
                if(conditionString[i+1].equals("==")) {
                    current = value == Float.parseFloat(conditionString[i+2]);
                } else if(conditionString[i + 1].equals("!=")) {
                    current = value != Float.parseFloat(conditionString[i + 2]);
                } else if(conditionString[i+1].equals("<")) {
                    current = value < Float.parseFloat(conditionString[i+2]);
                } else if(conditionString[i+1].equals(">")) {
                    current = value > Float.parseFloat(conditionString[i+2]);
                } else {
                    throw new RuntimeException("[Segment: "+ this.theClass.getName() +"] The element number " + ((i/4)+1) + " has an invalid condition!");
                }
            } else {
                throw new RuntimeException("[Segment: "+ this.theClass.getName() +"] The element with number " + ((i/4)+1) + " has an invalid type to be checked against!");
            }

            if(conditionString.length <= i+3 || current && conditionString[i+3].equals("OR") || !current && conditionString[i+3].equals("AND"))
                return current;
        }


        return false;
    }

}
