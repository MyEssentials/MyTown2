package mytown.protection.segment.getter;

/**
 * Created by AfterWind on 1/1/2015.
 * A caller is the information needed to get an object/value
 */
public class Caller {
    public String element;
    public CallerType type;
    public Class<?> valueType;

    public Caller(String element, CallerType type, Class<?> valueType) {
        this.element = element;
        this.type = type;
        this.valueType = valueType;
    }

    public Class<?> getValueType() {
        return valueType == null ? Integer.class : valueType;
    }


    public enum CallerType {
        method,
        field,
        formula,
        nbt
    }
}