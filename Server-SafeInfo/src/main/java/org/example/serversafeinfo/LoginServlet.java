package org.example.serversafeinfo;


import jakarta.servlet.annotation.*;
import org.json.JSONObject;

@WebServlet(name = "loginServlet", value = "/api/login")
public class LoginServlet extends BaseServlet {
    @Override
    protected JSONObject processRequest(JSONObject jsonObject) {
        String username = jsonObject.optString("username", "defaultUser");
        String password = jsonObject.optString("passwordHash", "defaultPass");


        String statusMessage;
        String phoneNumber = "";

        if(!username.matches("[a-zA-Z0-9_]+") || !password.matches("[a-zA-Z0-9_]+")) {
            statusMessage = "Invalid username or password characters";
        }else {

            if (username.equals("defaultUser") || password.equals("defaultPass")) {
                statusMessage = "Username or Password not specified";
            } else {
                statusMessage = myDatabase.login(username, password);
                if (!statusMessage.equals("User not found") && !statusMessage.equals("Wrong username or password") && !statusMessage.equals("Database error")) {
                    phoneNumber = statusMessage;
                    statusMessage = "success";
                }
            }
        }

        JSONObject jsonResponse = new JSONObject();
        if(phoneNumber.isEmpty()) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", statusMessage);
        } else {
            jsonResponse.put("status", "success");
            jsonResponse.put("phoneNumber", phoneNumber);
        }
        return jsonResponse;
    }
}
