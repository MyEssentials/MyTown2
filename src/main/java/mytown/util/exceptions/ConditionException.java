package mytown.util.exceptions;

/**
 * Exception thrown when verifying a condition.
 */
public class ConditionException extends RuntimeException {
    public ConditionException(String message) {
        super(message);
    }
}
