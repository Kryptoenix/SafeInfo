package Service;

import GUI.Main;
import UserConfig.UserGlobals;
import Utils.API;
import Utils.Logging;
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

public class Login {

    static void handleLoginRequest(String username, String password, Consumer<String> onFailure) {

        // sanitize input
        if(!username.matches("[a-zA-Z0-9_]+") || !password.matches("[a-zA-Z0-9_]+")) {
            onFailure.accept("Invalid username or password characters");
            return;
        }

        String passwordHash = CryptoProvider.SHA2.generateHash(password);

        // Handle user login request
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            // create the POST request
            HttpPost request = new HttpPost(API.login);
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            // {"username":<username>,"password":<password>}
            JSONObject json = new JSONObject();
            json.put("username", username);
            json.put("passwordHash", passwordHash);

            // set the request entity
            StringEntity entity = new StringEntity(json.toString());
            request.setEntity(entity);

            // send the request and process the response
            HttpResponse response = client.execute(request);

            String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                    .lines().collect(Collectors.joining("\n"));

            JSONObject jsonResponse = new JSONObject(responseBody);
            if(!jsonResponse.isEmpty()) {
                if (jsonResponse.has("status")) {
                    String status = jsonResponse.getString("status");
                    if (status.equals("success")) {
                        UserGlobals.setPhoneNumber(jsonResponse.getString("phoneNumber"));
                        UserGlobals.setUsername(username);
                        UserGlobals.setPassword(password);
                        UserGlobals.setHashedPassword(passwordHash);
                        User.getSession(onFailure);
                        Main.switchPanel("User");
                    } else {
                        onFailure.accept(jsonResponse.getString("message"));
                    }
                }
            }else{
                onFailure.accept( "No JSON received");
            }

        } catch (Exception ex) {
            Logging.logError(ex);
            onFailure.accept("An error occurred during login");
        }
    }
}
