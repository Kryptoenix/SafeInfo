package org.example.serversafeinfo;
import java.io.*;
import java.nio.file.*;
import java.time.Instant;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONArray;
import org.json.JSONObject;


@WebServlet(name = "chatServlet", value = "/chat/*")
public class ChatServlet extends HttpServlet {

    private MyDatabase myDatabase = new MyDatabase();

    // handle GET requests (used to retrieve messages)
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // set response type to JSON
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // init the JSON response object
        JSONObject jsonResponse = new JSONObject();

        // get the token from query parameters and validate it
        String token = request.getParameter("token");
        if (!validateToken(token, jsonResponse, response)) {
            return;
        }

        // extract the chatId from the request URL
        String chatId = extractChatId(request, jsonResponse, response);
        if (chatId == null) {
            return;
        }

        // extract the username from the validated token
        String username = extractUsernameFromToken(token);

        // retrieve the previous request timestamp for the user in the chat
        Instant prevRequestTimestamp = getPreviousRequestTimestamp(username, chatId, jsonResponse, response);
        if (prevRequestTimestamp == null) {
            return;
        }

        // retrieve the other user in the chat
        String otherUser = getOtherUser(username, chatId, jsonResponse, response);
        if (otherUser == null) {
            return;
        }

        // retrieve and filter chat messages
        JSONArray newMessages = getNewMessages(chatId, otherUser, prevRequestTimestamp);

        // update the user's last request timestamp in the database
        if (!updateUserTimestamp(username, chatId, jsonResponse, response)) {
            return;
        }

        // build a success response with the new messages
        jsonResponse.put("status", "success");
        jsonResponse.put("messages", newMessages);
        sendResponse(response, jsonResponse);
    }

    private boolean validateToken(String token, JSONObject jsonResponse, HttpServletResponse response) throws IOException {
        if (token == null) {
            jsonResponse.put("status", "No token specified");
            sendResponse(response, jsonResponse);
            return false;
        }

        try {
            String tokenResult = SessionManager.validateToken(token);
            if (!"success".equals(tokenResult)) {
                jsonResponse.put("status", tokenResult);
                sendResponse(response, jsonResponse);
                return false;
            }
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private String extractChatId(HttpServletRequest request, JSONObject jsonResponse, HttpServletResponse response) throws IOException {
        String pathInfo = request.getPathInfo();
        if (pathInfo != null && pathInfo.length() > 1) {
            return pathInfo.substring(1);  // Extract chatId from URL
        } else {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "No chatId provided in the URL.");
            sendResponse(response, jsonResponse);
            return null;
        }
    }

    private Instant getPreviousRequestTimestamp(String username, String chatId, JSONObject jsonResponse, HttpServletResponse response) throws IOException {
        String prevRequestTimestampStr = myDatabase.getPrevReqTs(username, chatId);

        if ("Failed to retrieve the last request timestamp".equals(prevRequestTimestampStr) ||
                "Failed to find chat id".equals(prevRequestTimestampStr) ||
                "Database Error".equals(prevRequestTimestampStr)) {
            jsonResponse.put("status", prevRequestTimestampStr);
            sendResponse(response, jsonResponse);
            return null;
        }

        return Instant.parse(prevRequestTimestampStr);
    }

    private String getOtherUser(String username, String chatId, JSONObject jsonResponse, HttpServletResponse response) throws IOException {
        String otherUser = myDatabase.getOtherUser(username, chatId);
        if ("Failed to retrieve the other user".equals(otherUser) || "Database error".equals(otherUser)) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", "Failed to retrieve the other user for chatId: " + chatId);
            sendResponse(response, jsonResponse);
            return null;
        }
        return otherUser;
    }

    private JSONArray getNewMessages(String chatId, String otherUser, Instant prevRequestTimestamp) throws IOException {
        File chatFile = new File("chat_" + chatId + ".txt");
        JSONArray newMessages = new JSONArray();

        if (chatFile.exists()) {
            // read the chat history from the file
            String chatHistory = Files.readString(Paths.get(chatFile.getAbsolutePath()));
            JSONObject chatJson = new JSONObject(chatHistory);
            JSONArray messages = chatJson.optJSONArray(otherUser);

            // filter messages based on the previous request timestamp
            if (messages != null) {
                for (int i = 0; i < messages.length(); i++) {
                    JSONObject message = messages.getJSONObject(i);
                    String messageTimestampStr = message.getString("t");
                    Instant messageTimestamp = Instant.parse(messageTimestampStr);

                    // add only messages after the previous request timestamp
                    if (messageTimestamp.isAfter(prevRequestTimestamp)) {
                        newMessages.put(message);
                    }
                }
            }
        }
        return newMessages;
    }

    private boolean updateUserTimestamp(String username, String chatId, JSONObject jsonResponse, HttpServletResponse response) throws IOException {
        Instant newPrevRequestTimestamp = Instant.now();
        String statusMessage = myDatabase.updateUserTs(username, chatId, newPrevRequestTimestamp.toString());

        if (!"success".equals(statusMessage)) {
            jsonResponse.put("status", statusMessage);
            sendResponse(response, jsonResponse);
            return false;
        }
        return true;
    }

    // Helper method to send the JSON response
    private void sendResponse(HttpServletResponse response, JSONObject jsonResponse) throws IOException {
        PrintWriter out = response.getWriter();
        out.print(jsonResponse.toString());
        out.flush();
    }



    // handle POST requests (used to send messages)
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // parse URL -> extract chat UUID
        String pathInfo = request.getPathInfo();
        String chatId;
        JSONObject jsonResponse = new JSONObject();

        if (pathInfo != null && pathInfo.length() > 1) {
            chatId = pathInfo.substring(1);

            // read JSON data from the request body
            StringBuilder jsonBuffer = new StringBuilder();
            try (BufferedReader reader = request.getReader()) {
                String line;
                while ((line = reader.readLine()) != null) {
                    jsonBuffer.append(line);
                }
            }

            // parse the JSON object
            String jsonData = jsonBuffer.toString();
            JSONObject jsonObject = new JSONObject(jsonData);
            JSONObject jsonMessage = jsonObject.getJSONObject("message");

            // ensure "m", "t", and "token" are present
            if (jsonMessage.has("m") && jsonMessage.has("t") && jsonObject.has("token")) {
                String message = jsonMessage.getString("m");
                String timestamp = jsonMessage.getString("t");
                String token = jsonObject.getString("token");


                // validate token
                boolean isValidToken = false;
                try {
                    String tokenResult = SessionManager.validateToken(token);
                    if (tokenResult.equals("success")) {
                        isValidToken = true;
                    } else {
                        jsonResponse.put("status", "error");
                        jsonResponse.put("message", tokenResult);
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }


                if (isValidToken) {
                    // file where chat messages will be stored
                    File chatFile = new File("chat_" + chatId + ".txt");

                    JSONObject existingChatData = new JSONObject();

                    if (chatFile.exists()) {
                        // read the existing chat data
                        String existingChatContent = Files.readString(Paths.get(chatFile.getAbsolutePath()));
                        if (!existingChatContent.isEmpty()) {
                            existingChatData = new JSONObject(existingChatContent);
                        }
                    }

                    // get the sender's username from the token (assuming the token includes the username)
                    String sender = extractUsernameFromToken(token);

                    // add new message to the chat file under the sender's name
                    JSONArray senderMessages = existingChatData.optJSONArray(sender);
                    if (senderMessages == null) {
                        senderMessages = new JSONArray();
                    }

                    // create message object with message and timestamp
                    JSONObject messageObject = new JSONObject();
                    messageObject.put("m", message);
                    messageObject.put("t", timestamp);

                    // add the new message to the array
                    senderMessages.put(messageObject);

                    // update the chat data with the new message
                    existingChatData.put(sender, senderMessages);

                    // write the updated chat data back to the file
                    Files.write(Paths.get(chatFile.getAbsolutePath()), existingChatData.toString().getBytes());

                    jsonResponse.put("status", "success");
                    jsonResponse.put("message", "Message sent successfully.");
                }
                } else {
                    jsonResponse.put("status", "error");
                    jsonResponse.put("message", "Invalid request. 'message', 'timestamp', and 'token' are required.");
                }
            } else {
                jsonResponse.put("status", "error");
                jsonResponse.put("message", "No chatId provided in the URL.");
            }
        PrintWriter out = response.getWriter();
        out.print(jsonResponse);
        out.flush();
    }

    // extract the username from the JWT token
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

    public void destroy() {
        // Clean up resources
    }
}
