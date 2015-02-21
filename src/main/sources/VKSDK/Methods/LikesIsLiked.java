package VKSDK.Methods;

import VKSDK.Exceptions.VKException;
import VKSDK.Exceptions.VKExceptionFinder;
import com.google.gson.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;

public class LikesIsLiked implements VKMethod {
    @Override
    public Boolean invoke() throws VKException {
        JsonObject jsonObject = null;
        try {
            jsonObject = VKHttpURLLoaderWithDelay.load(new URL("https://api.vk.com/method/likes.isLiked?" +
                    "access_token=" + accessToken +
                    "&user_id=" + userId +
                    "&type=" + type.toString() +
                    "&owner_id=" + ownerId +
                    "&item_id=" + itemId +
                    "&v=5.28"
            ));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        VKException vkException = VKExceptionFinder.findInJsonObject(jsonObject);
        if (vkException != null)
            throw vkException;

        long isLiked = jsonObject.get("response").getAsJsonObject().get("liked").getAsLong();
        return isLiked == 1L ? true : false;
    }

    public LikesIsLiked(Long ownerId, Long userId, Long itemId, Type type, String accessToken) {
        this.itemId = itemId;
        this.ownerId = ownerId;
        this.type = type;
        this.userId = userId;
        this.accessToken = accessToken;
    }

    private final String accessToken;
    private final Long userId;
    private final Type type;
    private final Long ownerId;
    private final Long itemId;

    public enum Type {
        POST, COMMENT, PHOTO, AUDIO, VIDEO, NOTE,
        PHOTO_COMMENT, VIDEO_COMMENT, TOPIC_COMMENT,
        SITEPAGE;

        @Override
        public String toString() {
            switch (this) {
                case POST:
                    return "post";
                case COMMENT:
                    return "comment";
                case PHOTO:
                    return "photo";
                case AUDIO:
                    return "audio";
                case VIDEO:
                    return "video";
                case NOTE:
                    return "note";
                case PHOTO_COMMENT:
                    return "photo_comment";
                case VIDEO_COMMENT:
                    return "video_comment";
                case TOPIC_COMMENT:
                    return "topic_comment";
                case SITEPAGE:
                    return "sitepage";
                default:
                    throw new RuntimeException("No mapping for " + super.toString());
            }
        }
    }
}
