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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.PublicKey;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static Service.User.getPublicKeyFromString;

public class NewChat {
    public static void handleNewChat(String otherUser, Consumer<String> onFailure){

        if(!otherUser.matches("[a-zA-Z0-9_]+")) {
            onFailure.accept("Invalid username characters");
            return;
        }

        // get the chat id
        try(CloseableHttpClient client = HttpClients.createDefault()){
            HttpPost request = new HttpPost(API.getChatId);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            JSONObject json = new JSONObject();
            json.put("otherUser", otherUser);
            json.put("token", UserGlobals.geSession());

            StringEntity entity = new StringEntity(json.toString());
            request.setEntity(entity);

            HttpResponse response = client.execute(request);
            String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining("\n"));

            JSONObject jsonResponse =  new JSONObject(responseBody);
            if(jsonResponse.has("status")){
                String status = jsonResponse.getString("status");
                if(status.equals("success")){

                    if(jsonResponse.has("chatId")) {
                        UserGlobals.setCurrentChatId(jsonResponse.getString("chatId"));
                        UserGlobals.setChatWith(otherUser);
                        getPublicKeyServer(otherUser,onFailure);

                        storeChatId(onFailure);
                    }
                }else{
                    String statusMessage = jsonResponse.getString("message");
                    onFailure.accept(statusMessage);
                }
            }

        }catch (Exception e){
            Logging.logError(e);
            onFailure.accept("Failed to send 'newChatId' request to server");
        }
    }

    private static void getPublicKeyServer(String otherUser,Consumer<String> onFailure) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API.getPubKey);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            JSONObject json = new JSONObject();
            json.put("token", UserGlobals.geSession());
            json.put("otherUser", otherUser);

            StringEntity entity = new StringEntity(json.toString());
            request.setEntity(entity);

            HttpResponse response = client.execute(request);

            String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining("\n"));

            JSONObject jsonResponse = new JSONObject(responseBody);
            if(jsonResponse.has("status")) {
                String status = jsonResponse.getString("status");
                if(status.equals("success")) {
                    if (jsonResponse.has("pubKey")) {
                        String publicKey = jsonResponse.getString("pubKey");
                        PublicKey pubKey = getPublicKeyFromString(publicKey);
                        UserGlobals.setPublicKey(pubKey);
                    } else {
                        onFailure.accept("Failed to retrieve Public Key from server.");
                    }
                }else{
                    String statusMessage = jsonResponse.getString("message");
                    onFailure.accept(statusMessage);
                }
            }

        } catch (Exception e) {
            Logging.logError(e);
            onFailure.accept("Failed to retrieve Public Key from server.");
        }
    }

    private static void storeChatId(Consumer<String> onFailure) {
        // Define the file path
        String filePath = FILE.chatHistory + UserGlobals.getUsername() + ".txt";

        // Initialize the root JSON object (this will hold all chat data)
        JSONObject chatHistory;

        // Try to open and read the file if it exists
        try {
            File file = new File(filePath);
            if (file.exists()) {
                // read the file content and parse it into a JSONObject
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                chatHistory = new JSONObject(content);  // Existing chat data
            } else {
                // if the file doesn't exist, initialize an empty JSONObject
                chatHistory = new JSONObject();
            }
        } catch (Exception e) {
            Logging.logError(e);
            onFailure.accept("Error reading chat history from file");
            chatHistory = new JSONObject();  // In case of error, start with an empty object
        }

        boolean isChatFoundinFile = false;
        // check if current chat id already exists in chat history
        for (String chatId : chatHistory.keySet()) {
            if (chatId.equals(UserGlobals.getCurrentChatId())) {
                isChatFoundinFile = true;
                break;
            }
        }

        // only append the chat id if it's new
        if (!isChatFoundinFile) {

            // prepare chat data for current user
            JSONArray myMessagesArray = new JSONArray();

            // prepare chat data for the other user
            JSONArray otherMessagesArray = new JSONArray();  // Empty array for messages (add actual messages here)

            // prepare the chat object
            JSONObject chat = new JSONObject();
            chat.put(UserGlobals.getUsername(),myMessagesArray);
            chat.put(UserGlobals.getChatWith(),otherMessagesArray);

            // add/update the current chat in the chat history
            chatHistory.put(UserGlobals.getCurrentChatId(), chat);

            // write the updated chat history back to the file
            try (PrintWriter writer = new PrintWriter(filePath)) {
                writer.write(chatHistory.toString());
            } catch (Exception e) {
                Logging.logError(e);
                onFailure.accept("Error storing chat id in chat history");
            }
        }
    }
}
