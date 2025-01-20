package Service;

import UserConfig.UserGlobals;
import Utils.API;
import Utils.FILE;
import Utils.Logging;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class NewPassword {
    public static void handleNewPasswordRequest(String newPassword, Consumer<String> onFailure){

        if(!newPassword.matches("[a-zA-Z0-9_]+")) {
            onFailure.accept("Invalid password characters");
            return;
        }

        // hash password for server
        String hashedPassword = CryptoProvider.SHA2.generateHash(newPassword);


        // tell the server to update the database with new password for our user
        try(CloseableHttpClient client = HttpClients.createDefault()){
            HttpPost request = new HttpPost(API.changePassword);

            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            JSONObject json = new JSONObject();
            json.put("token", UserGlobals.geSession());
            json.put("newPassword",hashedPassword);

            StringEntity entity = new StringEntity(json.toString());
            request.setEntity(entity);

            HttpResponse response = client.execute(request);
            String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining("\n"));

            JSONObject jsonResponse = new JSONObject(responseBody);
            if(jsonResponse.has("status")){
                String status = jsonResponse.getString("status");
                if(status.equals("success")){

                    System.out.println("Password has been changed successfully");
                    UserGlobals.setPassword(newPassword);
                    UserGlobals.setHashedPassword(hashedPassword);

                    setNewDataKey(onFailure);
                }else{
                    String statusMessage = jsonResponse.getString("message");
                    onFailure.accept(statusMessage);
                }
            } else{
                onFailure.accept("Failed to parse json");
            }
        }catch (Exception e){
            Logging.logError(e);
            onFailure.accept("Failed to send 'changePassword' request to server");
        }
    }

    private static void setNewDataKey(Consumer<String> onFailure) {
        String keyFile = FILE.keys + UserGlobals.getUsername() + ".txt";

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(keyFile))) {
            String encPrivateKey = bufferedReader.readLine();
            String iv = bufferedReader.readLine();
            String encMasterKeyB64 = bufferedReader.readLine();

            if (encPrivateKey == null || iv == null || encMasterKeyB64 == null) {
                return;  // exit if any required key data is missing
            }

            // re-encrypt the master key using the new password-derived key
            byte[] masterKeyBytes = UserGlobals.getSecretKey().getEncoded();
            String masterKeyBase64 = Base64.getEncoder().encodeToString(masterKeyBytes);
            String encMasterKey = CryptoProvider.Misc.encryptMasterKey(masterKeyBase64);  // Encrypt with new key
            String encMasterKeyBase64 = Base64.getEncoder().encodeToString(encMasterKey.getBytes());

            // store updated keys back to the file
            String storeData = encPrivateKey + "\n" + iv + "\n" + encMasterKeyBase64;

            try {
                Files.write(Path.of(keyFile), storeData.getBytes());
            } catch (IOException e) {
                Logging.logError(e);
                onFailure.accept("Failed to write to key file");
            }

        } catch (IOException ex) {
            Logging.logError(ex);
            onFailure.accept("Failed to read key file");
        }
    }
}
