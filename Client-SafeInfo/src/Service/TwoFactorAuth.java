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
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class TwoFactorAuth {
    static void handleTwoFactorPhoneRequest(Consumer<String> onFailure) throws IOException {
        // initiate an HTTP connection
        try (CloseableHttpClient client = HttpClients.createDefault()){

            HttpPost request = new HttpPost(API.verifyPhone);
            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            JSONObject json = new JSONObject();
            json.put("username", UserGlobals.getUsername());
            json.put("hashedPassword", UserGlobals.getHashedPassword());

            StringEntity entity = new StringEntity(json.toString());
            request.setEntity(entity);

            HttpResponse responseCode = client.execute(request);
            String requestBody = new BufferedReader(new InputStreamReader(responseCode.getEntity().getContent())).lines().collect(Collectors.joining("\n"));
            JSONObject responseJson = new JSONObject(requestBody);

            if(!responseJson.isEmpty()) {
                if (responseJson.has("status")) {
                    String status = responseJson.getString("status");
                    if (status.equals("success")) {
                        UserGlobals.setPhoneNumber(responseJson.getString("phoneNumber"));
                    } else {
                        String statusMessage = responseJson.getString("message");
                        onFailure.accept(statusMessage);
                    }

                }
            }else{
                onFailure.accept( "No JSON received");
            }

        }
    }

    static void handleTwoFactorOTP(String otp, Consumer<String> onFailure) {
        // initiate an HTTP connection
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API.verifyOTP);

            request.addHeader("Content-Type", "application/json");
            request.addHeader("Accept", "application/json");

            JSONObject json = new JSONObject();
            json.put("otp", otp);
            json.put("username", UserGlobals.getUsername());

            StringEntity entity = new StringEntity(json.toString());
            request.setEntity(entity);

            HttpResponse responseCode = client.execute(request);
            String requestBody = new BufferedReader(new InputStreamReader(responseCode.getEntity().getContent())).lines().collect(Collectors.joining("\n"));

            JSONObject responseJson = new JSONObject(requestBody);
            if(!responseJson.isEmpty()) {
                if (responseJson.has("status")) {

                    String value = responseJson.getString("status");
                    if (value.equals("success")) {

                        UserGlobals.setHashedPassword(responseJson.getString("passwordHash"));
                        UserGlobals.setUseSecret(false);

                        // where to redirect user
                        if (TFA.getSwitchTo().equals("User")) {
                            User.getSession(onFailure);
                            Main.switchPanel("User");
                        } else if (TFA.getSwitchTo().equals("Settings")) {
                            Main.switchPanel("NewPassword");
                        }
                    } else {
                        String statusMessage = responseJson.getString("message");
                        onFailure.accept(statusMessage);
                    }
                }
            }else{
                onFailure.accept("No JSON received");
            }
        } catch (Exception ex) {
            Logging.logError(ex);
            onFailure.accept("Failed to verify OTP");
        }
    }
}
