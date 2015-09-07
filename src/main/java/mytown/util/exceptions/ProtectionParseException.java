package mytown.util.exceptions;

import com.google.gson.JsonParseException;

public class ProtectionParseException extends JsonParseException {

    public ProtectionParseException(String message) {
        super(message);
    }

    public ProtectionParseException(String message, Throwable cause) {
        super(message, cause);
    }

    public ProtectionParseException(Throwable cause) {
        super(cause);
    }
}
