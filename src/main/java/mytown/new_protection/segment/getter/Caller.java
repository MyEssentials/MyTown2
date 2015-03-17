package mytown.new_protection.segment.getter;

/**
 * Created by AfterWind on 1/1/2015.
 * A caller is the information needed to get an object/value
 */
public class Caller {
    public String element;
    public CallerType type;

    public Caller(String element, CallerType type) {
        this.element = element;
        this.type = type;
    }

    public enum CallerType {
        method,
        field,
        formula,
        nbt
    }
}