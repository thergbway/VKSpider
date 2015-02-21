package VKSDK.Exceptions;

public class UserInactiveVKException extends VKException {
    public UserInactiveVKException() {
        super("User was deleted or banned");
    }

    public UserInactiveVKException(String message) {
        super(message);
    }

    public UserInactiveVKException(String message, Throwable cause) {
        super(message, cause);
    }

    public UserInactiveVKException(Throwable cause) {
        super(cause);
    }
}
