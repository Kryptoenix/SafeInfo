package org.example.serversafeinfo;


import jakarta.servlet.annotation.*;
import org.json.JSONObject;

@WebServlet(name = "getChatIdServlet", value = "/api/getChatId")
public class GetChatIdServlet extends BaseServlet {

    @Override
    protected JSONObject processRequest(JSONObject jsonObject) {
        String token = jsonObject.optString("token", "defaultToken");
        String otherUser = jsonObject.optString("otherUser", "defaultUser");

        String resultDb;
        String chatId = null;
        if (token.equals("defaultToken")) {
            resultDb = "Token not specified";
        } else if (otherUser.equals("defaultUser")) {
            resultDb = "User not specified";
        } else {
            String username = ChatServlet.extractUsernameFromToken(token);

            boolean isTokenValid = false;
            try {
                String tokenResult = SessionManager.validateToken(token);
                if (tokenResult.equals("success")) {
                    isTokenValid = true;
                    resultDb = "success";
                } else {
                    resultDb = tokenResult;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (isTokenValid) {
                if (!username.equals(otherUser)) {
                    resultDb = myDatabase.getChatId(username, otherUser);
                    if (!resultDb.equals("Failed to get chat id from database") && !resultDb.equals("Database error")) {
                        chatId = resultDb;
                        resultDb = "success";
                    }
                } else {
                    resultDb = "Username can't be the same as the other user";
                }
            }
        }

        JSONObject jsonResponse = new JSONObject();
        if (chatId != null) {
            jsonResponse.put("status", resultDb);
            jsonResponse.put("chatId", chatId);
        } else {
            jsonResponse.put("status", resultDb);
        }
        return jsonResponse;
    }
}
