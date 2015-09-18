package mytown.api.container;

import mytown.protection.segment.Segment;
import mytown.protection.segment.enums.Priority;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class SegmentsContainer<T extends Segment> extends ArrayList<T> {
    public List<T> get(Class<?> clazz) {
        List<T> usableSegments = new ArrayList<T>();
        for(Segment segment : this) {
            if(!segment.isDisabled() && segment.shouldCheckType(clazz)) {
                usableSegments.add((T)segment);
            }
        }
        if(usableSegments.size() > 1) {
            Priority highestPriority = Priority.LOWEST;
            for(Segment segment : usableSegments) {
                if(highestPriority.ordinal() < segment.getPriority().ordinal()) {
                    highestPriority = segment.getPriority();
                }
            }

            for(Iterator<T> it = usableSegments.iterator(); it.hasNext();) {
                Segment segment = it.next();
                if(segment.getPriority() != highestPriority) {
                    it.remove();
                }
            }
        }
        return usableSegments;
    }
}
