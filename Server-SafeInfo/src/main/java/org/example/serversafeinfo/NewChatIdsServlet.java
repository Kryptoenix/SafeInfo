package org.example.serversafeinfo;

import jakarta.servlet.annotation.*;
import org.json.JSONObject;

@WebServlet(name = "newChatIdsServlet", value = "/api/newChatIds")
public class NewChatIdsServlet extends BaseServlet {

    @Override
    protected JSONObject processRequest(JSONObject jsonObject) {
        String token = jsonObject.optString("token", "defaultToken");
        String newChatIds = null;
        String statusMessage;

        if (token.equals("defaultToken")) {
            statusMessage = "Token not specified";
        } else {
            String username = ChatServlet.extractUsernameFromToken(token);

            boolean isValidToken = false;
            try {
                String tokenResult = SessionManager.validateToken(token);
                if (tokenResult.equals("success")) {
                    isValidToken = true;
                    statusMessage = "success";
                } else {
                    statusMessage = tokenResult;
                }
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

            if (isValidToken) {
                statusMessage = myDatabase.getNewChatIdsHandler(username);
                if (!statusMessage.isEmpty() && !statusMessage.equals("Failed to find new chat ids in database") && !statusMessage.equals("Database error")) {
                    newChatIds = statusMessage;
                    statusMessage = "success";
                }
                if (statusMessage.equals("Failed to find new chat ids in database")) {
                    statusMessage = "success";
                }
            }
        }

        JSONObject jsonResponse = new JSONObject();
        if (newChatIds != null) {
            jsonResponse.put("newChatIds", newChatIds);
            jsonResponse.put("status", "success");
        } else if (statusMessage.equals("success") || statusMessage.isEmpty()) {
            jsonResponse.put("status", "success");
            jsonResponse.put("message", "No new chat ids found");
        } else{
            jsonResponse.put("status", "error");
            jsonResponse.put("message", statusMessage);
        }
        return jsonResponse;
    }
}
