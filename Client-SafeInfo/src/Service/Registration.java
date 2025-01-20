package Service;

import GUI.Main;
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

public class Registration {
    static void handleRegistrationRequest(String username, String password, String passwordConfirm, String phoneNumber, Consumer<String> onFailure){

        // sanitize input
        if(!username.matches("[a-zA-Z0-9_]+") || !password.matches("[a-zA-Z0-9_]+")) {
            onFailure.accept("Invalid username or password characters");
            return;
        }

        if(!phoneNumber.matches("^\\+?[1-9]\\d{1,14}$")){
            onFailure.accept("Invalid phone number");
            return;
        }

        if(!password.equals(passwordConfirm)){
            onFailure.accept("Passwords do not match");
        }
        else {
            String hashedPassword = CryptoProvider.SHA2.generateHash(password);

            // initiate an HTTP connection
            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost request = new HttpPost(API.registration);

                JSONObject json = new JSONObject();
                json.put("username", username);
                json.put("passwordHash", hashedPassword);
                json.put("phone", phoneNumber);

                StringEntity entity = new StringEntity(json.toString());
                request.setEntity(entity);

                HttpResponse response = client.execute(request);
                String requestBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining("\n"));
                JSONObject responseJson = new JSONObject(requestBody);

                if (!responseJson.isEmpty()) {
                    if (responseJson.has("status")) {
                        String status = responseJson.getString("status");
                        if (status.equals("success")) {
                            Main.switchPanel("Home");
                        } else {
                            onFailure.accept(status);
                        }
                    }
                } else {
                    onFailure.accept("No JSON received");
                }
            } catch (Exception ex) {
                Logging.logError(ex);
                onFailure.accept("Failed to register user");
            }
        }
    }
}
