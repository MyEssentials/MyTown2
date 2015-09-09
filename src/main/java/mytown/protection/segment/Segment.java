package mytown.protection.segment;

import mytown.api.container.GettersContainer;
import mytown.entities.flag.FlagType;

/**
 * A part of the protection that protects against a specific thing.
 */
public class Segment {
    protected Class<?> checkClass;
    protected FlagType flag;
    protected Object denialValue;
    protected Condition condition;

    public final GettersContainer getters = new GettersContainer();

    public void setCheckClass(Class<?> checkClass) {
        this.checkClass = checkClass;
    }

    public void setConditionString(String conditionString) {
        if(conditionString != null) {
            this.condition = new Condition(conditionString);
        }
    }

    public void setFlag(FlagType flag) {
        this.flag = flag;
    }

    public void setDenialValue(Object denialValue) {
        this.denialValue = denialValue;
    }

    public Class<?> getCheckClass() {
        return checkClass;
    }

    public Condition getCondition() {
        return condition;
    }

    public FlagType getFlag() {
        return flag;
    }

    public Object getDenialValue() {
        return denialValue;
    }

    public int getRange(Object object) {
        return getters.contains("range") ? (Integer) getters.get("range").invoke(Integer.class, object, object) : 0;
    }
}
