package DesktopClient;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.geometry.Insets;
import javafx.scene.layout.Border;
import javafx.scene.layout.BorderStroke;
import javafx.scene.layout.BorderStrokeStyle;
import javafx.scene.layout.Region;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebEvent;
import javafx.scene.web.WebView;

public class VKLoginPage extends Region {
    private final WebView browser = new WebView();
    private final WebEngine webEngine = browser.getEngine();

    public VKLoginPage() {
        setBorder(new Border(new BorderStroke(null, BorderStrokeStyle.SOLID,null,null, new Insets(-5.0))));

        webEngine.load("https://oauth.vk.com/authorize?client_id=4425181&redirect_uri=" +
                "https://oauth.vk.com/blank.html&scope=friends,wall,offline&display=popup&response_type=token");
        webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
            @Override
            public void handle(WebEvent<String> event) {
                String location = webEngine.getLocation();
                if (location.matches(".*access_token=.*&.*")) {
                    String accessToken = location.substring(45, 130);
                    webEngine.setOnStatusChanged(new EventHandler<WebEvent<String>>() {
                        @Override
                        public void handle(WebEvent<String> event) {
                            //setting off the event handler
                        }
                    });
                    fireEvent(new VKLoginSuccessEvent(accessToken));
                }
            }
        });
        getChildren().add(browser);
    }

    public static class VKLoginSuccessEvent extends Event {
        public static final EventType<VKLoginSuccessEvent> VK_LOGIN_SUCCESS = new EventType<>(ANY, "VK_LOGIN_SUCCESS");
        private final String accessToken;

        public VKLoginSuccessEvent(String accessToken) {
            super(VK_LOGIN_SUCCESS);
            this.accessToken = accessToken;
        }

        public String getAccessToken() {
            return accessToken;
        }
    }
}
