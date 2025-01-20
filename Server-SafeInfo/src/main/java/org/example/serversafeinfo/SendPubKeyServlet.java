package org.example.serversafeinfo;


import jakarta.servlet.annotation.*;
import org.json.JSONObject;


@WebServlet(name = "sendPubKeyServlet", value = "/api/sendPubKey")
public class SendPubKeyServlet extends BaseServlet {

    @Override
    protected JSONObject processRequest(JSONObject jsonObject) {
        String pubKey = jsonObject.optString("pubKey", "defaultPubKey");
        String token = jsonObject.optString("token", "defaultToken");

        String statusMessage;
        if (pubKey.equals("defaultPubKey")) {
            statusMessage = "Public Key not specified";
        } else if (token.equals("defaultToken")) {
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
                statusMessage = myDatabase.setPubKey(username, pubKey);
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