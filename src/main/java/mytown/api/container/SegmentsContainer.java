package mytown.api.container;

import mytown.protection.segment.Segment;

import java.util.ArrayList;
import java.util.List;

public class SegmentsContainer<T extends Segment> extends ArrayList<T> {

    public List<T> get(Class<?> clazz) {
        List<T> usableSegments = new ArrayList<T>();
        for(Segment segment : this) {
            if(!segment.isDisabled() && segment.shouldCheck(clazz)) {
                usableSegments.add((T)segment);
            }
        }
        return usableSegments;
    }
}
