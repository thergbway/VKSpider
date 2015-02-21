package VKSDK.Methods;

import VKSDK.DataTypes.User;
import VKSDK.Exceptions.VKException;
import VKSDK.Exceptions.VKExceptionFinder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class FriendsGet implements VKMethod {
    @Override
    public LinkedList<User> invoke() throws VKException {
        JsonObject jsonObject = null;
        try {
            jsonObject = VKHttpURLLoaderWithDelay.load(new URL(constructURLAsString()));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }
        VKException vkException = VKExceptionFinder.findInJsonObject(jsonObject);
        if (vkException != null)
            throw vkException;

        LinkedList<User> usersList = new LinkedList<>();
        JsonArray userItems = jsonObject.get("response").getAsJsonObject().get("items").getAsJsonArray();
        for (JsonElement userItemElement : userItems) {
            JsonObject userItemObject = (JsonObject) userItemElement;
            User user = new User();
            user.setId(userItemObject.get("id").getAsLong());
            user.setFirstName(userItemObject.get("first_name").getAsString());
            user.setLastName(userItemObject.get("last_name").getAsString());
            usersList.add(user);
        }
        return usersList;
    }

    private Long userId = null;
    private String accessToken = null;

    public FriendsGet(Long userId, String accessToken) {
        this.userId = userId;
        this.accessToken = accessToken;
    }

    public FriendsGet(String accessToken) {
        this.accessToken = accessToken;
    }

    private String constructURLAsString() {
        StringBuilder sb = new StringBuilder();
        sb.append("https://api.vk.com/method/friends.get?");
        sb.append("fields=nickname&");
        if (userId == null && accessToken == null)
            throw new RuntimeException("Object FriendGet was initialized wrong");
        if (userId != null)
            sb.append("user_id=" + userId + "&");
        if (accessToken != null)
            sb.append("access_token=" + accessToken + "&");
        sb.append("v=5.28");
        sb.append("&lang=en");

        return sb.toString();
    }
}
