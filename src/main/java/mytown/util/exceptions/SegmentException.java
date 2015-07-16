package mytown.util.exceptions;

/**
 * Exception thrown when parsing the data in a Segment.
 */
public class SegmentException extends RuntimeException{

    public SegmentException(String message) {
        super(message);
    }

    public SegmentException(String message, Throwable cause) {
        super(message, cause);
    }
}
