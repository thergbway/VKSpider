package VKSDK.Methods;

import VKSDK.Exceptions.TooManeRequestsVKException;
import VKSDK.Exceptions.VKExceptionFinder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class VKHttpURLLoaderWithDelay {
    public static JsonObject load(URL url) {
        try {
            Thread.sleep(300L);
        } catch (InterruptedException e) {}
        while (true) {
            String response;
            try {
                response = HttpURLLoader.load(url);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            JsonObject responseJson = parser.parse(response).getAsJsonObject();
            if (VKExceptionFinder.findInJsonObject(responseJson) instanceof TooManeRequestsVKException) {
                try {
                    Thread.sleep(650L);
                } catch (InterruptedException e) {}
                continue;
            }
            return responseJson;
        }
    }

    private static JsonParser parser = new JsonParser();

    private VKHttpURLLoaderWithDelay() {
    }

    private static class HttpURLLoader {
        private static String load(URL url) throws IOException {
            HttpURLConnection connection = ((HttpURLConnection) url.openConnection());
            return convertInputStreamToString(connection.getInputStream());
        }

        private static String convertInputStreamToString(InputStream is) {
            Scanner s = new Scanner(is).useDelimiter("\\A");
            return s.hasNext() ? s.next() : "";
        }

        private HttpURLLoader() {
        }
    }
}
