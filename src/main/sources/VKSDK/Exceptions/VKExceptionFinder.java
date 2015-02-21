package VKSDK.Exceptions;

import com.google.gson.JsonObject;

public class VKExceptionFinder {
    public static VKException findInJsonObject(JsonObject jsonObject) {
        if (jsonObject.get("error") == null)
            return null;

        int errorCode = jsonObject.get("error").getAsJsonObject().get("error_code").getAsInt();
        switch (errorCode) {
            case 212:
                return new AccessToPostCommentsDeniedVKException(jsonObject.get("error").toString());
            case 18:
                return new UserInactiveVKException(jsonObject.get("error").toString());
            case 6:
                return new TooManeRequestsVKException(jsonObject.get("error").toString());
            default:
                return new VKException(jsonObject.get("error").toString());
        }
    }
}
