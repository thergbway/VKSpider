package VKSDK.Exceptions;

public class AccessToPostCommentsDeniedVKException extends VKException {
    public AccessToPostCommentsDeniedVKException() {
        super("Access to post comments denied");
    }

    public AccessToPostCommentsDeniedVKException(String message) {
        super(message);
    }

    public AccessToPostCommentsDeniedVKException(String message, Throwable cause) {
        super(message, cause);
    }

    public AccessToPostCommentsDeniedVKException(Throwable cause) {
        super(cause);
    }
}
