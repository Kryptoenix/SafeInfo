package org.example.serversafeinfo;

import jakarta.servlet.annotation.*;
import org.json.JSONObject;

@WebServlet(name = "twoFactorOTPServlet", value = "/api/verifyOTP")
public class TwoFactorOTPServlet extends BaseServlet {

    @Override
    protected JSONObject processRequest(JSONObject jsonObject) {
        String otp = jsonObject.optString("otp", "defaultOtp");
        String username = jsonObject.optString("username", "defaultUser");

        String statusMessage;
        String passwordHash;
        if (otp.equals("defaultOtp")) {
            statusMessage = "Otp not provided";
        } else if (username.equals("defaultUser")) {
            statusMessage = "Username not provided";
        } else {
            statusMessage = myDatabase.twoFactorAuthVerifyOTP(username, otp);
        }

        if (statusMessage.equals("success")) {
            statusMessage = myDatabase.getPasswordHash(username); // returns the password hash if no errors
        }

        JSONObject jsonResponse = new JSONObject();
        if (statusMessage != null && !statusMessage.equals("Failed to get password hash from database") && !statusMessage.equals("Database Error")) {
            passwordHash = statusMessage;
            jsonResponse.put("passwordHash", passwordHash);
            jsonResponse.put("status", "success");
        } else {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", statusMessage);
        }
        return jsonResponse;
    }
}
