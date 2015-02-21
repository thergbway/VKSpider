package VKSDK.Exceptions;

public class VKException extends Exception {
    public VKException() {
        super();
    }

    public VKException(String message) {
        super(message);
    }

    public VKException(String message, Throwable cause) {
        super(message, cause);
    }

    public VKException(Throwable cause) {
        super(cause);
    }

    protected VKException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
