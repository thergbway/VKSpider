package VKSDK.Exceptions;

public class TooManeRequestsVKException extends VKException {
    public TooManeRequestsVKException() {
        super("Too many request per second");
    }

    public TooManeRequestsVKException(String message) {
        super(message);
    }

    public TooManeRequestsVKException(String message, Throwable cause) {
        super(message, cause);
    }

    public TooManeRequestsVKException(Throwable cause) {
        super(cause);
    }
}
