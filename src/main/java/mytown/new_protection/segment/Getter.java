package mytown.new_protection.segment;

/**
 * Created by AfterWind on 1/1/2015.
 * An enum that is used to know what how to get a certain type of information.
 */
public class Getter {
    public String element;
    public GetterType type;

    public Getter(String element, GetterType type) {
        this.element = element;
        this.type = type;
    }

    public enum GetterType {
        method,
        field
    }
}