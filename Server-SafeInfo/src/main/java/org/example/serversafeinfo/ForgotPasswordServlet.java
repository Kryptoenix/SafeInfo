package org.example.serversafeinfo;


import jakarta.servlet.annotation.*;
import org.json.JSONObject;

@WebServlet(name = "forgotPasswordServlet", value = "/api/forgotPassword")
public class ForgotPasswordServlet extends BaseServlet {

    @Override
    protected JSONObject processRequest(JSONObject jsonObject) {
        String username = jsonObject.optString("username", "defaultUser");

        String statusMessage;
        if (username.equals("defaultUser")) {
            statusMessage = "Username not specified";
        } else {
            statusMessage = myDatabase.forgotPassword(username);
        }

        JSONObject jsonResponse = new JSONObject();
        if (statusMessage.equals("success")) {
            jsonResponse.put("status", "success");
            jsonResponse.put("phoneNumber", statusMessage);
        } else {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", statusMessage);
        }
        return jsonResponse;
    }
}
