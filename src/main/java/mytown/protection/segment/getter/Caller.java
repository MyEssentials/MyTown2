package mytown.protection.segment.getter;

/**
 * A caller is the information needed to get an object/value
 */
public class Caller {
    public final String element;
    public final CallerType type;
    public final Class<?> valueType;

    public Caller(String element, CallerType type, Class<?> valueType) {
        this.element = element;
        this.type = type;
        this.valueType = valueType;
    }

    public Class<?> getValueType() {
        return valueType == null ? Integer.class : valueType;
    }


    public enum CallerType {
        METHOD,
        FIELD,
        FORMULA,
        NBT
    }
}