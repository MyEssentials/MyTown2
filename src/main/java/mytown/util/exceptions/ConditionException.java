package mytown.util.exceptions;

/**
 * Created by AfterWind on 2/15/2015.
 * Exception thrown when verifying a condition.
 */
public class ConditionException extends RuntimeException {
    public ConditionException(String message) {
        super(message);
    }
}
