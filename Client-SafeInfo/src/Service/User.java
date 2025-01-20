package Service;

import GUI.Main;
import UserConfig.UserGlobals;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.KeyFactory;
import java.security.PublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import Utils.*;

import javax.crypto.SecretKey;
import javax.crypto.spec.IvParameterSpec;

import static GUI.User.addUserChat;
import static GUI.User.panelChatIds;

public class User {

    public static void getSession(Consumer<String> onFailure){
        // check if session is stored locally
        Path filePath = Paths.get(FILE.session);
        try{
            String sessionFromFile = Files.readString(filePath);

            // check if caches session matches the current user
            String sessionUser = extractUsernameFromToken(sessionFromFile);
            if(!sessionUser.equals(UserGlobals.getUsername())) {
                // if they don't match, delete the caches session
                destroyCachedSession(onFailure);
                UserGlobals.setSession(null);
            }else{
                // the caches session might be valid
                UserGlobals.setSession(sessionFromFile);
            }
            System.out.println("Session read locally");
        }
        catch (IOException e){
            UserGlobals.setSession(null);
        }

        // otherwise, ask server for it
        if (UserGlobals.geSession() == null) {

            try (CloseableHttpClient client = HttpClients.createDefault()) {
                HttpPost request = new HttpPost(API.getSession);
                request.setHeader("Content-type", "application/json");
                request.setHeader("Accept", "application/json");

                JSONObject json = new JSONObject();
                json.put("username", UserGlobals.getUsername());
                json.put("passwordHash", UserGlobals.getHashedPassword());


                StringEntity entity = new StringEntity(json.toString());
                request.setEntity(entity);

                HttpResponse response = client.execute(request);
                String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining("\n"));

                JSONObject responseJson = new JSONObject(responseBody);
                if (responseJson.has("status")) {
                    String status = responseJson.getString("status");
                    if (status.equals("success")) {
                        if (responseJson.has("token")) {
                            String token = responseJson.getString("token");
                            UserGlobals.setSession(token);

                            // write session to file
                            filePath = Paths.get(FILE.session);
                            try {
                                Files.write(filePath, token.getBytes());
                            } catch (IOException e) {
                                Logging.logError(e);
                                onFailure.accept("Failed to access session file");
                            }
                        } else {
                            onFailure.accept("Token not received");
                        }
                    }else{
                        String statusMessage = responseJson.getString("message");
                        onFailure.accept(statusMessage);
                    }
                }
            } catch (Exception e) {
                Logging.logError(e);
                onFailure.accept("Failed to read session");
            }
        }
    }

    public static String extractUsernameFromToken(String token) {
        if (token != null && !token.isEmpty()) {
            String[] splitToken = token.split("\\.");
            if (splitToken.length == 3) {
                String payload = new String(java.util.Base64.getDecoder().decode(splitToken[1]));
                JSONObject payloadJson = new JSONObject(payload);
                return payloadJson.optString("sub", "unknown"); // Assuming the token has a "sub" (subject) field
            }
        }
        return "unknown";
    }

    // destroy locally stored session
    public static void destroyCachedSession(Consumer<String> onFailure){

        UserGlobals.resetUserConfig(); // for logout
        Path filePath = Paths.get(FILE.session);
        try {
            // delete the file if it exists
            Files.delete(filePath);
        } catch (IOException e) {
            Logging.logError(e);
            onFailure.accept("Failed to delete session file");
        }
    }

    public static void handleReadUserKeys(Consumer<String> onFailure){
        String keyFile = FILE.keys + UserGlobals.getUsername() + ".txt";

        try (BufferedReader bufferedReader = new BufferedReader(new FileReader(keyFile))) {
            String encPrivateKey = bufferedReader.readLine();
            String iv = bufferedReader.readLine();
            String encMasterKeyB64 = bufferedReader.readLine();

            if (encPrivateKey == null || iv == null || encMasterKeyB64 == null) {
                regenerateKeys(onFailure);  // if any data is missing, regenerate keys
                return;
            }

            // decode and decrypt the master key
            byte[] encMasterKeyBytes = Base64.getDecoder().decode(encMasterKeyB64);
            String encMasterKey = new String(encMasterKeyBytes);
            String masterKeyB64 = CryptoProvider.Misc.decryptMasterKey(encMasterKey);
            SecretKey masterKey = CryptoProvider.Misc.stringToSecretKey(masterKeyB64, "AES");
            UserGlobals.setSecretKey(masterKey);  // set the decrypted master key globally

            // decode and set IV
            byte[] ivBytes = Base64.getDecoder().decode(iv);
            IvParameterSpec ivSpec = new IvParameterSpec(ivBytes);
            UserGlobals.setSecretIv(ivSpec);

            // decrypt the private key
            String decryptedPrivateKey = CryptoProvider.AES.decryptMessageFromHistory(encPrivateKey);
            if (decryptedPrivateKey != null) {
                CryptoProvider.Misc.setPrivateKey(decryptedPrivateKey);
            } else {
                onFailure.accept( "Failed to decrypt the private key.");
            }

        } catch (IOException ex) {
            regenerateKeys(onFailure);
        }
    }

    private static void regenerateKeys(Consumer<String> onFailure) {
        CryptoProvider.RSA.generateKeyPair();
        String publicKey = CryptoProvider.RSA.getBase64PublicKey();
        String privateKey = CryptoProvider.RSA.getBase64PrivateKey();

        // generate IV and master key
        Map<String, Object> result = CryptoProvider.AES.generateIV();
        byte[] ivBytes = (byte[]) result.get("ivBytes");
        IvParameterSpec ivSpec = (IvParameterSpec) result.get("ivSpec");
        UserGlobals.setSecretIv(ivSpec);

        String encodedIv = Base64.getEncoder().encodeToString(ivBytes);

        // generate a random master key and encrypt it with the data key
        byte[] masterKeyBytes = Objects.requireNonNull(CryptoProvider.AES.generateKey(256)).getEncoded();  // 256-bit master key
        String masterKey =Base64.getEncoder().encodeToString(masterKeyBytes);
        String encMasterKey = CryptoProvider.Misc.encryptMasterKey(masterKey);
        String encMasterKeyBase64 = Base64.getEncoder().encodeToString(encMasterKey.getBytes());
        UserGlobals.setSecretKey(CryptoProvider.Misc.stringToSecretKey(masterKey,"AES"));

        // save encrypted private key, IV, and encrypted master key to the file
        String encryptedPrivateKey = CryptoProvider.AES.encryptMessageFromHistory(privateKey);
        String storeData = encryptedPrivateKey + "\n" + encodedIv + "\n" + encMasterKeyBase64;
        String keyFile = FILE.keys + UserGlobals.getUsername() + ".txt";

        try {
            Files.write(Path.of(keyFile), storeData.getBytes());
        } catch (IOException e) {
            Logging.logError(e);
            onFailure.accept("Failed to write the keys.");
        }

        // send the public key to the server
        sendPublicKeyServer(publicKey,onFailure);
    }

    private static void sendPublicKeyServer(String pubKey,Consumer<String> onFailure) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API.sendPubKey);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            JSONObject json = new JSONObject();
            json.put("pubKey", pubKey);
            json.put("token", UserGlobals.geSession());

            StringEntity entity = new StringEntity(json.toString());
            request.setEntity(entity);
            HttpResponse response = client.execute(request);

            String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining("\n"));
            JSONObject jsonResponse = new JSONObject(responseBody);

            if (jsonResponse.has("status")) {
                String status = jsonResponse.getString("status");
                if (status.equals("success")) {
                    System.out.println("Successfully set public key");
                } else {
                    String statusMessage = jsonResponse.getString("message");
                    onFailure.accept(statusMessage);
                }
            } else {
                onFailure.accept("Failed to retrieve Public Key");
            }
        } catch (IOException e) {
            Logging.logError(e);
            onFailure.accept("Failed to send Public Key to server.");
        }
    }

    static void handleLoadChats(Consumer<String> onFailure) {
        String filePath = FILE.chatHistory + UserGlobals.getUsername() + ".txt";
        JSONObject chatHistory;

        try {
            File file = new File(filePath);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                chatHistory = new JSONObject(content);
            } else {
                // no chat history to load
                return;
            }
        } catch (IOException ex) {
            Logging.logError(ex);
            onFailure.accept("Failed to load chat history.");
            return;
        }

        // iterate through each chat in the chat history
        Iterator<String> keys = chatHistory.keys();
        while (keys.hasNext()) {
            String chatId = keys.next();

            // get users associated with the chat id
            JSONObject chatUsers = chatHistory.optJSONObject(chatId);
            if (chatUsers == null) {
                continue; // if there's no valid chat object, skip
            }

            String otherUser = null;

            // ensure valid otherUser is found in the chat
            for (String user : chatUsers.keySet()) {
                if (!user.equals(UserGlobals.getUsername())) {
                    otherUser = user;
                    break;
                }
            }

            if (otherUser != null) {
                // set the chat context AFTER finding a valid other user
                UserGlobals.setChatWith(otherUser);
                addUserChat(chatId);
            }
        }
    }


    static void handleNewChatIds(Consumer<String> onFailure) {
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API.newChatIds);
            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            JSONObject json = new JSONObject();
            json.put("token", UserGlobals.geSession());

            StringEntity entity = new StringEntity(json.toString());
            request.setEntity(entity);

            HttpResponse response = client.execute(request);
            String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                    .lines()
                    .collect(Collectors.joining("\n"));

            // only parse if new chat ids were found
            if (!responseBody.isEmpty()) {

                JSONObject jsonResponse = new JSONObject(responseBody);
                System.out.println(jsonResponse);
                // max chat ids allowed on the panel
                int maxChatIds = 7;

                if (jsonResponse.has("status")) {
                    String status = jsonResponse.getString("status");
                    if (status.equals("success")) {
                        if (jsonResponse.has("newChatIds")) {
                            String newChatIds = jsonResponse.getString("newChatIds");
                            if (!newChatIds.isEmpty()) {

                                JSONArray newChatIdsArray = new JSONArray(newChatIds);

                                // Loop over the new chat IDs in the JSONArray
                                for (int j = 0; j < newChatIdsArray.length(); j++) {
                                    JSONObject chatObject = newChatIdsArray.getJSONObject(j); // each entry is a JSON obj
                                    String currentChatId = chatObject.getString("chat_id");
                                    String otherUser = chatObject.getString("otherUser");
                                    UserGlobals.setChatWith(otherUser);

                                    // check if the currentChatId is already present in panelChatIds
                                    if (!Arrays.asList(panelChatIds).contains(currentChatId)) {
                                        // loop over panelChatIds to find an empty space
                                        for (int i = 0; i < maxChatIds; i++) {
                                            if (panelChatIds[i] == null || panelChatIds[i].isEmpty()) {
                                                // Add the new chat button to the panel
                                                addUserChat(currentChatId);
                                                // store the currentChatId in the first empty space
                                                panelChatIds[i] = currentChatId;
                                                break; // exit the loop once the ID is added
                                            }
                                        }
                                    }
                                }
                            }
                            storeNewMessages(jsonResponse,onFailure);
                        }
                    }else {
                        String statusMessage = jsonResponse.getString("message");
                        ShowErrorMessage error = new ShowErrorMessage(Main.mainFrame, statusMessage);
                        error.setVisible(true);
                    }
                }
            }


        } catch (Exception e) {
            Logging.logError(e);
            ShowErrorMessage error = new ShowErrorMessage(Main.mainFrame,"Failed to handle new chat ids from server.");
            error.setVisible(true);
        }
    }

    private static void storeNewMessages(JSONObject jsonResponse,Consumer<String> onFailure) {
        String filePath = FILE.chatHistory + UserGlobals.getUsername() + ".txt";
        JSONObject chatHistory;

        try {
            File file = new File(filePath);
            if (file.exists()) {
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                chatHistory = new JSONObject(content);
            } else {
                // init an empty JSON object if no file exists
                chatHistory = new JSONObject();
            }
        } catch (IOException ex) {
            Logging.logError(ex);
            onFailure.accept("Failed to open chat history.");
            chatHistory = new JSONObject(); // in case of error, start with an empty object
        }

        // parse new chat IDs from response
        String newChatIdsStr = jsonResponse.getString("newChatIds");
        JSONArray chatIds = new JSONArray(newChatIdsStr);

        // process each chat object in the array
        for (int i = 0; i < chatIds.length(); i++) {
            JSONObject chatObj = chatIds.getJSONObject(i);
            String otherUser = chatObj.getString("otherUser");
            String chatId = chatObj.getString("chat_id");

            // ensure chatId and otherUser are valid before storing
            if (chatId == null || otherUser == null || otherUser.isEmpty()) {
                continue; // skip invalid chats
            }

            // retrieve or init the chat JSON object for the current chatId
            JSONObject chatJson = chatHistory.optJSONObject(chatId);
            if (chatJson == null) {
                chatJson = new JSONObject();
                chatHistory.put(chatId, chatJson);
            }

            // ensure chat arrays for other user and current user are initialized
            String currentUser = UserGlobals.getUsername();
            JSONArray otherUserMessages = chatJson.optJSONArray(otherUser);
            JSONArray userMessages = chatJson.optJSONArray(currentUser);

            if (otherUserMessages == null) {
                otherUserMessages = new JSONArray();
                chatJson.put(otherUser, otherUserMessages);
            }

            if (userMessages == null) {
                userMessages = new JSONArray();
                chatJson.put(currentUser, userMessages);
            }

            // set the chat context (other user)
            UserGlobals.setChatWith(otherUser);
        }

        // save the updated chat history back to the file
        try {
            String updatedHistory = chatHistory.toString();
            Files.write(Paths.get(filePath), updatedHistory.getBytes());
        } catch (IOException ex) {
            Logging.logError(ex);
            onFailure.accept("Failed to update chat history.");
        }
    }


    public static PublicKey getPublicKeyFromString(String pubKeyString) throws Exception {
        // decode the Base64-encoded public key string into a byte array
        byte[] publicBytes = Base64.getDecoder().decode(pubKeyString);

        // create an X509EncodedKeySpec using the byte array
        X509EncodedKeySpec keySpec = new X509EncodedKeySpec(publicBytes);

        //  generate a PublicKey object
        KeyFactory keyFactory = KeyFactory.getInstance("RSA"); // You can change "RSA" to another algorithm if needed

        return keyFactory.generatePublic(keySpec);
    }

    static void handlePublicKeyServer(String otherUser, Consumer<String> onFailure) {
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
}
