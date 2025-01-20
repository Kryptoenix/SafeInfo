package Service;

import GUI.Main;
import UserConfig.UserGlobals;
import Utils.API;
import Utils.Logging;
import Utils.TFA;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ForgotPassword {

    public static void handleForgotPasswordRequest(String username, Consumer<String> onFailure) {

        if(!username.matches("[a-zA-Z0-9_]+")) {
            onFailure.accept("Invalid username characters");
            return;
        }

        UserGlobals.setUsername(username);

        // initiate an HTTP connection
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API.forgotPassword);
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            JSONObject json = new JSONObject();
            json.put("username", UserGlobals.getUsername());

            StringEntity entity = new StringEntity(json.toString());
            request.setEntity(entity);

            HttpResponse response = client.execute(request);
            String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining("\n"));

            JSONObject jsonResponse = new JSONObject(responseBody);
            if (!jsonResponse.isEmpty()) {
                if (jsonResponse.has("status")) {
                    String status = jsonResponse.getString("status");
                    if (!status.equals("error")) {
                        String phone =jsonResponse.getString("phoneNumber");
                        TFA.setSwitchTo("User");
                        UserGlobals.setUsername(username);
                        UserGlobals.setPhoneNumber(phone);
                        Main.switchPanel("TwoFactorAuth");
                    } else {
                        String statusMessage = jsonResponse.getString("message");
                        onFailure.accept( statusMessage);
                    }
                }
            } else {
                onFailure.accept("No JSON received");
            }
        } catch (Exception ex) {
            Logging.logError(ex);
            onFailure.accept("Failed to send 'forgotPassword' request to server");
        }
    }
}
