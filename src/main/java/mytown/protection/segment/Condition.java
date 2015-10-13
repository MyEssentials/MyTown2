package mytown.protection.segment;

import com.google.common.base.Joiner;
import myessentials.utils.StringUtils;
import mytown.protection.segment.getter.Getter;
import mytown.util.exceptions.ConditionException;
import mytown.util.exceptions.GetterException;

public class Condition {

    private String[] conditionString;

    public Condition(String conditionString) {
        this.conditionString = conditionString.split(" ");
    }

    public boolean execute(Object object, Getter.Container getters) throws ConditionException, GetterException {
        if(conditionString == null) {
            return true;
        }

        //MyTown.instance.log.info("Checking condition: " + StringUtils.join(conditionString, " "));
        boolean current;

        Object instance;
        instance = object;

        for(int i = 0; i < conditionString.length; i += 4) {

            // Get the boolean value of each part of the condition.
            if(StringUtils.tryParseBoolean(conditionString[i + 2])) {
                boolean value = (Boolean) getters.get(conditionString[i]).invoke(Boolean.class, instance, object);
                if ("==".equals(conditionString[i + 1])) {
                    current = value == Boolean.parseBoolean(conditionString[i + 2]);
                } else if("!=".equals(conditionString[i + 1])) {
                    current = value != Boolean.parseBoolean(conditionString[i + 2]);
                } else {
                    throw new ConditionException("The element number " + (i / 4) + 1 + " has an invalid condition!");
                }
            } else if(StringUtils.tryParseInt(conditionString[i + 2])) {
                int value = (Integer) getters.get(conditionString[i]).invoke(Integer.class, instance, object);
                if("==".equals(conditionString[i + 1])) {
                    current = value == Integer.parseInt(conditionString[i + 2]);
                } else if("!=".equals(conditionString[i + 1])) {
                    current = value != Integer.parseInt(conditionString[i + 2]);
                } else if("<".equals(conditionString[i + 1])) {
                    current = value < Integer.parseInt(conditionString[i + 2]);
                } else if(">".equals(conditionString[i + 1])) {
                    current = value > Integer.parseInt(conditionString[i + 2]);
                } else {
                    throw new ConditionException("The element number " + (i / 4) + 1 + " has an invalid condition!");
                }
            } else if(StringUtils.tryParseFloat(conditionString[i + 2])) {
                float value = (Integer) getters.get(conditionString[i]).invoke(Integer.class, instance, object);
                if("==".equals(conditionString[i + 1])) {
                    current = value == Float.parseFloat(conditionString[i + 2]);
                } else if("!=".equals(conditionString[i + 1])) {
                    current = value != Float.parseFloat(conditionString[i + 2]);
                } else if("<".equals(conditionString[i + 1])) {
                    current = value < Float.parseFloat(conditionString[i + 2]);
                } else if(">".equals(conditionString[i + 1])) {
                    current = value > Float.parseFloat(conditionString[i + 2]);
                } else {
                    throw new ConditionException("The element number " + ((i/4)+1) + " has an invalid condition!");
                }
            } else if(conditionString[i + 2].startsWith("'") && conditionString[i+2].endsWith("'")){
                String value = (String) getters.get(conditionString[i]).invoke(String.class, instance, object);
                if("==".equals(conditionString[i + 1])) {
                    current = value.equals(conditionString[i+2].substring(1, conditionString[i+2].length() - 1));
                } else if("!=".equals(conditionString[i + 1])) {
                    current = !value.equals(conditionString[i+2].substring(1, conditionString[i+2].length() - 1));
                } else {
                    throw new ConditionException("The element number " + ((i/4)+1) + " has an invalid condition!");
                }
            } else {
                throw new ConditionException("The element with number " + ((i/4)+1) + " has an invalid type to be checked against!");
            }

            if(conditionString.length <= i + 3 || current && "OR".equals(conditionString[i + 3]) || !current && "AND".equals(conditionString[i + 3]))
                return current;

            if(!"OR".equals(conditionString[i + 3]) && !"AND".equals(conditionString[i + 3]))
                throw new ConditionException("Invalid condition element: " + conditionString[i + 3]);
        }
        return false;
    }

    @Override
    public String toString() {
        return Joiner.on(' ').join(conditionString);
    }
}
