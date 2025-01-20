package org.example.serversafeinfo;


import jakarta.servlet.annotation.*;
import org.json.JSONObject;

@WebServlet(name = "changePasswordServlet", value = "/api/changePassword")
public class ChangePasswordServlet extends BaseServlet {

    @Override
    protected JSONObject processRequest(JSONObject jsonObject) {
        String token = jsonObject.optString("token", "defaultToken");
        String password = jsonObject.optString("newPassword", "defaultPassword");

        String statusMessage;

        if (token.equals("defaultToken")) {
            statusMessage = "Token not specified";
        } else if (password.equals("defaultPassword")) {
            statusMessage = "New password not specified";
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
                statusMessage = myDatabase.changePassword(username, password);
            }
        }

        JSONObject jsonResponse = new JSONObject();
        if (statusMessage.equals("success")) {
            jsonResponse.put("status", "success");
        } else {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", statusMessage);
        }
        return jsonResponse;
    }
}
