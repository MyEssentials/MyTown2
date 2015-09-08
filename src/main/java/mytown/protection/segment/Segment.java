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
    public Condition condition;

    public final GettersContainer getters = new GettersContainer();

    public void setConditionString(String conditionString) {
        this.condition = new Condition(conditionString);
    }

    public void setFlag(FlagType flag) {
        this.flag = flag;
    }

    public void setDenialValue(Object denialValue) {
        this.denialValue = denialValue;
    }

    public void setCheckClass(Class<?> checkClass) {
        this.checkClass = checkClass;
    }

    public Class<?> getCheckClass() {
        return checkClass;
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
