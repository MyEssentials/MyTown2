package mytown.util.exceptions;

/**
 * Created by AfterWind on 2/15/2015.
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
