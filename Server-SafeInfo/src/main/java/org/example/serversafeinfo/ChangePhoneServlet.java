package org.example.serversafeinfo;


import jakarta.servlet.annotation.*;
import org.json.JSONObject;


@WebServlet(name = "changePhoneServlet", value = "/api/changePhone")
public class ChangePhoneServlet extends BaseServlet {

    @Override
    protected JSONObject processRequest(JSONObject jsonObject) {
        String token = jsonObject.optString("token", "defaultToken");
        String phone = jsonObject.optString("phone", "defaultPhone");

        String statusMessage;
        if (token.equals("defaultToken")) {
            statusMessage = "Token not specified";
        } else if (phone.equals("defaultPhone")) {
            statusMessage = "New phone not specified";
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
                statusMessage = myDatabase.changePhone(username, phone);
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
