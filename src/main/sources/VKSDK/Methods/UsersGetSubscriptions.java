package VKSDK.Methods;

import VKSDK.DataTypes.Group;
import VKSDK.DataTypes.User;
import VKSDK.DataTypes.VKDataType;
import VKSDK.Exceptions.VKException;
import VKSDK.Exceptions.VKExceptionFinder;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.LinkedList;

public class UsersGetSubscriptions implements VKMethod {

    @Override
    public LinkedList<VKDataType> invoke() throws VKException {
        JsonObject jsonObject = null;
        try {
            jsonObject = VKHttpURLLoaderWithDelay.load(new URL("https://api.vk.com/method/users.getSubscriptions?" +
                    "user_id=" + userId + "&access_token=" + accessToken + "&extended=" + extended + "&v=5.28" +
                    "&count=200&lang=en"
            ));
        } catch (MalformedURLException e) {
            throw new RuntimeException(e);
        }

        VKException vkException = VKExceptionFinder.findInJsonObject(jsonObject);
        if (vkException != null)
            throw vkException;

        LinkedList<VKDataType> subscriptionsList = new LinkedList<>();

        JsonArray itemsArray = jsonObject.get("response").getAsJsonObject().get("items").getAsJsonArray();
        for (JsonElement itemAsElement : itemsArray) {
            JsonObject itemAsObject = (JsonObject) itemAsElement;
            if (itemAsObject.get("type").getAsString().equals("page") ||
                    itemAsObject.get("type").getAsString().equals("group") ||
                            itemAsObject.get("type").getAsString().equals("event")) {
                Group group = new Group();
                group.setId(itemAsObject.get("id").getAsLong());
                group.setName(itemAsObject.get("name").getAsString());

                subscriptionsList.add(group);
                continue;
            }
            if (itemAsObject.get("type").getAsString().equals("profile")) {
                User user = new User();
                user.setId(itemAsObject.get("id").getAsLong());
                user.setFirstName(itemAsObject.get("first_name").getAsString());
                user.setLastName(itemAsObject.get("last_name").getAsString());

                subscriptionsList.add(user);
                continue;
            }
            throw new RuntimeException("Unexpected item type = " + itemAsObject.get("type").getAsString());
        }
        return subscriptionsList;
    }

    public UsersGetSubscriptions(Long userId, String accessToken) {
        this.userId = userId;
        this.accessToken = accessToken;
    }

    private final Long userId;
    private final String accessToken;
    private final Long extended = 1L;

}
