package VKSDK.Methods;

import VKSDK.DataTypes.Post;
import VKSDK.Exceptions.VKException;
import VKSDK.Exceptions.VKExceptionFinder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class WallGet implements VKMethod {
    @Override
    public LinkedList<Post> invoke() throws VKException {
        Long postsCountToGet = null;
        JsonObject jsonObject = null;
        try {
            jsonObject = VKHttpURLLoaderWithDelay.load(new URL("https://api.vk.com/method/wall.get?owner_id="
                    + ownerId + "&count=1" + "&filter=all" + "&access_token=" + accessToken + "&v=5.28"));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        VKException vkException = VKExceptionFinder.findInJsonObject(jsonObject);
        if (vkException != null)
            throw vkException;

        long totalWallPostCount = jsonObject.get("response").getAsJsonObject().get("count").getAsLong();
        if (count == null) {
            postsCountToGet = totalWallPostCount;
        } else if (totalWallPostCount < count)
            postsCountToGet = totalWallPostCount;
        else postsCountToGet = count;

        LinkedList<Post> postsList = new LinkedList<>();
        while (postsList.size() < postsCountToGet) {
            try {
                jsonObject = VKHttpURLLoaderWithDelay.load(new URL("https://api.vk.com/method/wall.get?owner_id="
                        + ownerId + "&offset=" + postsList.size() + "&count=" + postsCountToGet + "&filter=all" +
                        "&access_token=" + accessToken + "&v=5.28"
                ));
            } catch (MalformedURLException e) {
                throw new RuntimeException(e);
            }
            vkException = VKExceptionFinder.findInJsonObject(jsonObject);
            if (vkException != null) {
                throw vkException;
            }

            JsonArray postsJsonArray = jsonObject.get("response").getAsJsonObject().get("items").getAsJsonArray();
            for (int i = 0; i < postsJsonArray.size(); i++) {
                JsonElement postElement = postsJsonArray.get(i);
                JsonObject postObject = postElement.getAsJsonObject();
                Post post = new Post();
                post.setId(postObject.get("id").getAsLong());
                post.setFromId(postObject.get("from_id").getAsLong());
                post.setOwnerId(ownerId);
                post.setDate(postObject.get("date").getAsLong());
                post.setText(postObject.get("text").getAsString());
                postsList.add(post);
            }
        }

        return postsList;
    }

    private final Long ownerId;
    private final String accessToken;
    private Long count;

    public WallGet(Long ownerId, String accessToken) {
        this.ownerId = ownerId;
        this.accessToken = accessToken;
    }

    public void setCount(Long count) {
        this.count = count;
    }
}
