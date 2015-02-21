package VKSDK.Methods;

import VKSDK.DataTypes.Comment;
import VKSDK.Exceptions.VKException;
import VKSDK.Exceptions.VKExceptionFinder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class WallGetComments implements VKMethod {
    @Override
    public LinkedList<Comment> invoke() throws VKException {
        Long commentCountToGet = null;
        JsonObject jsonObject = null;
        try {
            jsonObject = VKHttpURLLoaderWithDelay.load(new URL("https://api.vk.com/method/wall.getComments?" +
                    "owner_id=" + ownerId + "&post_id=" + postId + "&need_likes=" + 0 +
                    "&count=" + 1 + "&preview_length=" + 1 + "&access_token=" + accessToken + "&v=5.28"
            ));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        VKException vkException = VKExceptionFinder.findInJsonObject(jsonObject);
        if (vkException != null)
            throw vkException;

        long totalCommentsCount = jsonObject.get("response").getAsJsonObject().get("count").getAsLong();
        if (count == null)
            commentCountToGet = totalCommentsCount;
        else if (count <= totalCommentsCount)
            commentCountToGet = count;
        else
            commentCountToGet = totalCommentsCount;

        LinkedList<Comment> commentsList = new LinkedList<>();
        while (commentsList.size() < commentCountToGet) {
            try {
                jsonObject = VKHttpURLLoaderWithDelay.load(new URL("https://api.vk.com/method/wall.getComments?" +
                        "access_token=" + accessToken + "&owner_id=" + ownerId + "&post_id=" + postId +
                        "&sort=" + sort + "&preview_length=" + previewLength +
                        "&offset=" + commentsList.size() + "&count=" + (commentCountToGet - commentsList.size() + "&v=5.28")
                ));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }

            JsonArray commentsJsonArray = jsonObject.get("response").getAsJsonObject().get("items").getAsJsonArray();
            for (JsonElement commentAsJsonElement : commentsJsonArray) {
                JsonObject commentAsJsonObject = commentAsJsonElement.getAsJsonObject();
                Comment comment = new Comment();
                comment.setId(commentAsJsonObject.get("id").getAsLong());
                comment.setOwnerId(ownerId);
                comment.setPostId(postId);
                comment.setFromId(commentAsJsonObject.get("from_id").getAsLong());
                comment.setDate(commentAsJsonObject.get("date").getAsLong());
                comment.setText(commentAsJsonObject.get("text").getAsString());

                commentsList.add(comment);
            }
        }

        return commentsList;
    }

    private final String accessToken;
    private final Long ownerId;
    private final Long postId;
    private Long count = null;
    private final String sort = "desc";
    private Long previewLength = 0L;

    public WallGetComments(String accessToken, Long ownerId, Long postId) {
        this.accessToken = accessToken;
        this.ownerId = ownerId;
        this.postId = postId;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getCount() {
        return count;
    }

    public void setCount(Long count) {
        this.count = count;
    }

    public String getSort() {
        return sort;
    }

    public Long getPreviewLength() {
        return previewLength;
    }

    public void setPreviewLength(Long previewLength) {
        this.previewLength = previewLength;
    }

}
