package org.example.serversafeinfo;

import jakarta.servlet.annotation.*;
import org.json.JSONObject;

@WebServlet(name = "getPubKeyServlet", value = "/api/getPubKey")
public class GetPubKeyServlet extends BaseServlet {

    @Override
    protected JSONObject processRequest(JSONObject jsonObject) {
        String token = jsonObject.optString("token", "defaultToken");
        String otherUser = jsonObject.optString("otherUser", "defaultUser");

        String statusMessage;
        String pubKey = null;

        if(!otherUser.matches("[a-zA-Z0-9_]+")) {
            statusMessage = "Invalid username characters";
        }else {
            if (token.equals("defaultToken")) {
                statusMessage = "Token not specified";
            } else if (otherUser.equals("defaultUser")) {
                statusMessage = "Other user not specified";
            } else {
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
                    statusMessage = myDatabase.getPubKey(otherUser);
                    if (!statusMessage.equals("Failed to get pubKey from database") && !statusMessage.equals("Database error")) {
                        pubKey = statusMessage;
                        statusMessage = "success";
                    }
                }
            }
        }

        JSONObject jsonResponse = new JSONObject();
        if (pubKey != null) {
            jsonResponse.put("status", "success");
            jsonResponse.put("pubKey", pubKey);
        } else {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", statusMessage);
        }
        return jsonResponse;
    }
}
