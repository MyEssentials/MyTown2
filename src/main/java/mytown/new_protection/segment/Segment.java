package mytown.new_protection.segment;

import java.util.List;
import java.util.Map;

/**
 * Created by AfterWind on 1/1/2015.
 * A part of the protection that protects against a specific thing.
 */
public class Segment {
    public Class<?> theClass;
    public Map<String, List<Getter>> extraGettersMap;

    public Segment(Class<?> theClass, Map<String, List<Getter>> extraGettersMap) {
        this.theClass = theClass;
        this.extraGettersMap = extraGettersMap;
    }
}
