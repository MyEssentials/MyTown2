package mytown.util.exceptions;

/**
 * Exception thrown when checking the config file.
 */
public class ConfigException extends RuntimeException {

    public ConfigException(String s) {
        super(s);
    }
}
