package mytown.util.exceptions;

/**
 * Created by AfterWind on 2/26/2015.
 * Exception thrown when parsing the data in a Segment.
 */
public class SegmentException extends RuntimeException{

    public SegmentException(String message) {
        super(message);
    }
}
