package mytown.protection.segment.getter;

/**
 * A caller is the information needed to get an object/value
 */
public class Caller {
    private final String element;
    private final CallerType callerType;
    private final Class<?> valueType;

    public Caller(String element, CallerType callerType, Class<?> valueType) {
        this.element = element;
        this.callerType = callerType;
        this.valueType = valueType;
    }

    public Class<?> getValueType() {
        //return valueType == null ? Integer.class : valueType;
        return valueType;
    }

    public CallerType getCallerType() {
        return callerType;
    }

    public String getElement() {
        return element;
    }

    public enum CallerType {
        METHOD,
        FIELD,
        FORMULA,
        NBT
    }
}