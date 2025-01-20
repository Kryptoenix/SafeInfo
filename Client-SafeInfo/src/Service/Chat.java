package Service;


import Design.ChatBubble;
import Design.ChatBubbleText;
import UserConfig.UserGlobals;
import Utils.*;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.json.JSONArray;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Iterator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static GUI.Chat.*;

public class Chat {

    static void handleMessage(String message, Instant timestamp, Consumer<String> onFailure) {

        String filePath = FILE.chatHistory + UserGlobals.getUsername() + ".txt";
        JSONObject chatHistory;
        String currentChatId = UserGlobals.getCurrentChatId();
        String senderUser = UserGlobals.getUsername(); // current user

        // encrypt with AES for chat history
        String encAESMessage = CryptoProvider.AES.encryptMessageFromHistory(message);

        // encrypt with RSA for server (public key)
        String encRSAMessage = CryptoProvider.RSA.encryptMessageFromServer(message);

        // try opening and reading the existing chat history
        FileLock.lock();
        try {
            File file = new File(filePath);
            if (file.exists()) {
                // read the file content and parse it as a JSONObject
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                chatHistory = new JSONObject(content);
            } else {
                // if the file doesn't exist, initialize an empty chat history
                chatHistory = new JSONObject();
            }
        } catch (IOException ex) {
            Logging.logError(ex);
            onFailure.accept("Failed to access chat history (send message)");
            return;
        }

        try {
            // get the chat users for the current chat ID
            JSONObject chatUsers = chatHistory.optJSONObject(currentChatId);
            if (chatUsers == null) {
                chatUsers = new JSONObject();
                chatHistory.put(currentChatId, chatUsers); // add it to the overall chat history
            }

            // get the message array for the current user (sender)
            JSONArray senderMessages = chatUsers.optJSONArray(senderUser);
            if (senderMessages == null) {
                senderMessages = new JSONArray();
                chatUsers.put(senderUser, senderMessages); // add it to the chat object for the current chat
            }


            // create a new message JSON object for the sender
            JSONObject newMessage = new JSONObject();
            newMessage.put("m", encAESMessage);
            newMessage.put("t", timestamp.toString());

            senderMessages.put(newMessage);

            // write the updated chat history back to the file
            Files.write(Paths.get(filePath), chatHistory.toString().getBytes());
        } catch (IOException ex) {
            Logging.logError(ex);
            onFailure.accept("Failed to access chat history");
        }finally {
            FileLock.unlock();
        }



        // inform server about the message sent
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpPost request = new HttpPost(API.chat + UserGlobals.getCurrentChatId());

            request.setHeader("Content-Type", "application/json");
            request.setHeader("Accept", "application/json");

            // {{m:"m",t:"t"},{m:"m",t:"t"},..}
            JSONObject json = new JSONObject();
            json.put("m", encRSAMessage);
            json.put("t", timestamp);


            // wrap the messages array into a parent json object
            JSONObject newMessage = new JSONObject();
            newMessage.put("message", json);
            newMessage.put("token", UserGlobals.geSession());


            StringEntity entity = new StringEntity(newMessage.toString());
            request.setEntity(entity);

            HttpResponse response = client.execute(request);
            String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent())).lines().collect(Collectors.joining("\n"));

            JSONObject jsonResponse = new JSONObject(responseBody);
            if(jsonResponse.has("success")){
                String status = jsonResponse.getString("success");
                if(!status.equals("success")){
                    String statusMessage = jsonResponse.getString("message");
                    onFailure.accept(statusMessage);
                }
            }
        } catch (Exception ex) {
            Logging.logError(ex);
            onFailure.accept("Failed to send chat message to server");
        }
    }


    static void handleNewMessages(Consumer<String> onFailure) {
        try {
            Controller.setServiceControlFlag(Controller.ServiceID.GetNewMessages, true);
            while (Controller.getServiceControlFlag(Controller.ServiceID.GetNewMessages)){

                try (CloseableHttpClient client = HttpClients.createDefault()) {
                    // Build the URI with query parameters
                    URIBuilder uriBuilder = new URIBuilder(API.chat + UserGlobals.getCurrentChatId());
                    uriBuilder.setParameter("token", UserGlobals.geSession());

                    HttpGet request = new HttpGet(uriBuilder.build());
                    request.setHeader("Accept", "application/json");

                    // Send the request
                    HttpResponse response = client.execute(request);
                    String responseBody = new BufferedReader(new InputStreamReader(response.getEntity().getContent()))
                            .lines()
                            .collect(Collectors.joining("\n"));

                    JSONObject jsonResponse = new JSONObject(responseBody);

                    if (jsonResponse.has("status") && "success".equals(jsonResponse.getString("status"))) {
                        processIncomingMessages(jsonResponse);
                    } else {
                        String statusMessage = jsonResponse.optString("message", "Unknown error");
                        onFailure.accept(statusMessage);
                    }
                } catch (Exception ex) {
                    Logging.logError(ex);
                    onFailure.accept("Failed to get messages from server: " + ex.getMessage());
                }

                // Sleep for 3 seconds before making the next request
                Thread.sleep(3000);
            }
        } catch (InterruptedException e) {
            Logging.logError(e);
            onFailure.accept("Message polling interrupted: " + e.getMessage());
            Thread.currentThread().interrupt();
        }
    }


    private static void processIncomingMessages(JSONObject jsonResponse) {
        String currentChatId = UserGlobals.getCurrentChatId();
        String otherUser = UserGlobals.getChatWith();
        String filePath = Paths.get(FILE.chatHistory + UserGlobals.getUsername() + ".txt").toString();
        JSONObject chatHistory;

        FileLock.lock();
        try {
            if (Files.exists(Paths.get(filePath))) {
                String content = Files.readString(Paths.get(filePath));
                chatHistory = new JSONObject(content);
            } else {
                chatHistory = new JSONObject();
            }

            JSONObject currentChat = chatHistory.optJSONObject(currentChatId);
            if (currentChat == null) {
                currentChat = new JSONObject();
                chatHistory.put(currentChatId, currentChat);
            }

            JSONArray otherUserMessages = currentChat.optJSONArray(otherUser);
            if (otherUserMessages == null) {
                otherUserMessages = new JSONArray();
                currentChat.put(otherUser, otherUserMessages);
            }

            JSONArray senderMessages = currentChat.optJSONArray(UserGlobals.getUsername());
            if (senderMessages == null) {
                senderMessages = new JSONArray();
                currentChat.put(UserGlobals.getUsername(), senderMessages);
            }

            if (jsonResponse.has("messages")) {
                JSONArray messagesArray = jsonResponse.getJSONArray("messages");
                for (int i = 0; i < messagesArray.length(); i++) {
                    JSONObject jsonMessage = messagesArray.getJSONObject(i);
                    if (jsonMessage.has("m") && jsonMessage.has("t")) {
                        String message = jsonMessage.getString("m");
                        String timestamp = jsonMessage.getString("t");

                        Instant ts = Instant.parse(timestamp);
                        String time = insToTimeBubble(ts);

                        String decMessage = CryptoProvider.RSA.decryptMessageFromServer(message);
                        String encMessage = CryptoProvider.AES.encryptMessageFromHistory(decMessage);

                        JSONObject newMessage = new JSONObject();
                        newMessage.put("m", encMessage);
                        newMessage.put("t", timestamp);
                        otherUserMessages.put(newMessage);

                        // Update the UI using a callback or directly
                        SwingUtilities.invokeLater(() -> updateChatUI(decMessage, time));
                    }
                }
            }

            Files.write(Paths.get(filePath), chatHistory.toString().getBytes());
        } catch (IOException ex) {
            Logging.logError(ex);
            throw new RuntimeException("Failed to access chat history", ex);
        } finally {
            FileLock.unlock();
        }
    }

    private static void updateChatUI(String message, String time) {
        // Example UI update logic
        boolean isSender = false;

        chatBubbleWrapper.setLayout(new BoxLayout(chatBubbleWrapper, BoxLayout.Y_AXIS));
        chatBubbleWrapper.add(Box.createRigidArea(new Dimension(0, 10)));  // 10px vertical spacing
        // check if the message is an image
        String[] messageParts = message.split(" ");
        if (messageParts.length == 1 && message.length() > 100) {
            // it means we have an image (base64 encoded)
            byte[] imageBytes = Base64.getDecoder().decode(message);
                ChatBubble chatBubble = new ChatBubble(imageBytes, time, isSender);
            chatBubbleWrapper.add(chatBubble);
        } else {
            // otherwise, it's a simple text message
            ChatBubbleText chatBubble = new ChatBubbleText(message, time, isSender);
            chatBubbleWrapper.add(chatBubble);
        }
        // refresh the UI
        chatBubbleWrapper.revalidate();
        chatBubbleWrapper.repaint();
        centerPanel.add(chatBubbleWrapper);
        centerPanel.revalidate();
        centerPanel.repaint();
    }


    static void handleChatHistory(Consumer<String> onFailure) {
        // parse chat history file
        String filePath = FILE.chatHistory + UserGlobals.getUsername() + ".txt";
        JSONObject chatHistory;

        // try opening and reading the existing chat history
        try {
            File file = new File(filePath);
            if (file.exists()) {
                // read the file content and parse it as a JSONObject
                String content = new String(Files.readAllBytes(Paths.get(filePath)));
                chatHistory = new JSONObject(content);
            } else {
                // nothing to show
                return;
            }
        } catch (IOException ex) {
            Logging.logError(ex);
            onFailure.accept("Failed to load chat history");
            return;
        }

        boolean isSender;


        Iterator<String> keys = chatHistory.keys();
        // for each chat ID from chat history
        while (keys.hasNext()) {
            String chatId = keys.next();

            if(chatId.equals(UserGlobals.getCurrentChatId())) {

                // get users associated with the chat
                JSONObject chatUsers = chatHistory.getJSONObject(chatId);
                Iterator<String> keyUsers = chatUsers.keys();

                // this chat has only 2 users, so extract them (both)
                String user = keyUsers.next();
                String otherUser = keyUsers.next();

                // get messages from both users
                JSONArray userMessages = chatUsers.getJSONArray(user);
                JSONArray otherUserMessages = chatUsers.getJSONArray(otherUser);

                // create a list to hold all messages from both users
                List<JSONObject> allMessages = new ArrayList<>();

                // add user messages to the list, with a sender identifier
                for (int i = 0; i < userMessages.length(); i++) {
                    JSONObject userMessage = userMessages.getJSONObject(i);
                    if (!userMessage.isEmpty()) {
                        // attach sender info manually
                        userMessage.put("sender", user);
                        allMessages.add(userMessage);
                    }
                }

                // add other user messages to the list, with a sender identifier
                for (int i = 0; i < otherUserMessages.length(); i++) {
                    JSONObject otherUserMessage = otherUserMessages.getJSONObject(i);
                    if (!otherUserMessage.isEmpty()) {
                        // attach sender info manually
                        otherUserMessage.put("sender", otherUser);
                        allMessages.add(otherUserMessage);
                    }
                }

                // sort messages by timestamp
                allMessages.sort((msg1, msg2) -> {
                    Instant ts1 = Instant.parse(msg1.getString("t"));  // "t" = timestamp
                    Instant ts2 = Instant.parse(msg2.getString("t"));
                    return ts1.compareTo(ts2);  // sort in ascending order
                });

                //  display messages in chronological order
                for (JSONObject messageObj : allMessages) {
                    String sender = messageObj.getString("sender");
                    isSender = sender.equals(UserGlobals.getUsername());

                    if (!messageObj.has("m") || !messageObj.has("t")) {
                        continue;
                    }

                    // decrypt the message
                    String encMessage = messageObj.getString("m");
                    String message = CryptoProvider.AES.decryptMessageFromHistory(encMessage);

                    // get the timestamp of the message
                    Instant ts = Instant.parse(messageObj.getString("t"));
                    String time = insToTimeBubble(ts); // local date time based on system config


                    chatBubbleWrapper.setLayout(new BoxLayout(chatBubbleWrapper, BoxLayout.Y_AXIS));
                    chatBubbleWrapper.add(Box.createRigidArea(new Dimension(0, 10)));  // 10px vertical spacing

                    // check if the message is an image
                    assert message != null;
                    String[] messageParts = message.split(" ");
                    if (messageParts.length == 1 && message.length() > 100) {
                        // it means we have an image (base64 encoded)
                        byte[] imageBytes = Base64.getDecoder().decode(message);
                        ChatBubble chatBubble = new ChatBubble(imageBytes, time, isSender);
                        chatBubbleWrapper.add(chatBubble);
                    } else {
                        // otherwise, it's a simple text message
                        ChatBubbleText chatBubble = new ChatBubbleText(message, time, isSender);
                        chatBubbleWrapper.add(chatBubble);
                    }

                    // refresh the UI
                    chatBubbleWrapper.revalidate();
                    chatBubbleWrapper.repaint();
                    centerPanel.add(chatBubbleWrapper);
                    centerPanel.revalidate();
                    centerPanel.repaint();
                }
            }
        }

    }


    public static String insToTimeBubble(Instant ts){
        // convert instant into local date time (use system config to find it)
        LocalDateTime localDateTime = LocalDateTime.ofInstant(ts, ZoneId.systemDefault());

        // format the local time into "hour:min"
        return String.format("%s:%s",localDateTime.getHour(),localDateTime.getMinute());
    }
}
