package org.example.serversafeinfo;

import jakarta.servlet.annotation.*;
import org.json.JSONObject;


@WebServlet(name = "registrationServlet", value = "/api/registration")
public class RegistrationServlet extends BaseServlet {
    @Override
    protected JSONObject processRequest(JSONObject jsonObject) {
        String username = jsonObject.optString("username", "defaultUser");
        String password = jsonObject.optString("passwordHash", "defaultPass");
        String phone = jsonObject.optString("phone", "defaultPhone");

        String statusMessage;

        if(!username.matches("[a-zA-Z0-9_]+") || !password.matches("[a-zA-Z0-9_]+") || !phone.matches("^\\+?[1-9]\\d{1,14}$")) {
            statusMessage = "Invalid username/password/phone number";
        }else {
            if (username.equals("defaultUser") || password.equals("defaultPass")) {
                statusMessage = "Username or Password not specified";
            } else if (phone.equals("defaultPhone")) {
                statusMessage = "Phone number not specified";
            } else {
                statusMessage = myDatabase.register(username, password, phone);
            }
        }

        JSONObject jsonResponse = new JSONObject();
        if(statusMessage.equals("success")) {
            jsonResponse.put("status", "success");
        } else {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", statusMessage);
        }
        return jsonResponse;
    }
}