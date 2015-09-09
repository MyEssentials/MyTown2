package mytown.api.container;

import mytown.protection.segment.Segment;

import java.util.ArrayList;
import java.util.List;

public class SegmentContainer extends ArrayList<Segment> {

    public List<Segment> get(Class<?> clazz) {
        List<Segment> usableSegments = new ArrayList<Segment>();
        for(Segment segment : this) {
            if(segment.shouldCheck(clazz)) {
                usableSegments.add(segment);
            }
        }
        return usableSegments;
    }
}
