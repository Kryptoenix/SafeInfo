package Service;

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

public class NewPhoneNumber {
    public static void handleNewPhoneRequest(String password, String newPhone, Consumer<String> onFailure) {


        // check if the provided password is correct
        if(password.equals(UserGlobals.getPassword())){

            // tell the server to update the database with new password for our user
            try(CloseableHttpClient client = HttpClients.createDefault()){
                HttpPost request = new HttpPost(API.changePhone);

                request.setHeader("Content-Type", "application/json");
                request.setHeader("Accept", "application/json");

                JSONObject json = new JSONObject();
                json.put("token", UserGlobals.geSession());
                json.put("phone",newPhone);

                StringEntity entity = new StringEntity(json.toString());
                request.setEntity(entity);

                HttpResponse response = client.execute(request);
                String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining("\n"));

                JSONObject jsonResponse = new JSONObject(responseBody);
                if(jsonResponse.has("status")){
                    String status = jsonResponse.getString("status");
                    if(!status.equals("success")){
                        String statusMessage = jsonResponse.getString("message");
                        onFailure.accept(statusMessage);
                    }else{
                        onFailure.accept("success");
                    }
                }
                else{
                    onFailure.accept("Failed to parse json");
                }
            }catch (Exception e){
                Logging.logError(e);
                onFailure.accept("Failed to send 'changePhone' request to server");
            }

        }
        else{
            onFailure.accept("Password does not match");
        }
    }
}
