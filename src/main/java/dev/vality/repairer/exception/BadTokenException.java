package dev.vality.repairer.exception;

public class BadTokenException extends IllegalArgumentException {

    public BadTokenException() {
    }

    public BadTokenException(String s) {
        super(s);
    }

    public BadTokenException(String message, Throwable cause) {
        super(message, cause);
    }

    public BadTokenException(Throwable cause) {
        super(cause);
    }
}
