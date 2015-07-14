package mytown.util.exceptions;

/**
 * Exception thrown when getting information from a getter
 */
public class GetterException extends RuntimeException {
    public GetterException(String message) {
        super(message);
    }

    public GetterException(String message, Exception ex) {
        super(message, ex);
    }
}
