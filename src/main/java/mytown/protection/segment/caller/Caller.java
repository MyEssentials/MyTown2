package mytown.protection.segment.caller;

/**
 * A caller is the information needed to get an object/value
 */
public abstract class Caller {
    protected final String name;
    protected final Class<?> valueType;

    public Caller(String name, Class<?> valueType) {
        this.name = name;
        this.valueType = valueType;
    }

    public abstract Object invoke(Object instance, Object... parameters) throws Exception;

    public Class<?> getValueType() {
        return valueType;
    }

    public String getName() {
        return name;
    }
}