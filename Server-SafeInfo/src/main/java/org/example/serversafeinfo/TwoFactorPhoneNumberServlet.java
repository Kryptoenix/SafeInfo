package org.example.serversafeinfo;

import com.twilio.rest.verify.v2.service.Verification;
import jakarta.servlet.annotation.*;
import org.json.JSONObject;
import com.twilio.*;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;


@WebServlet(name = "twoFactorPhoneNumberServlet", value = "/api/verifyPhoneNumber")
public class TwoFactorPhoneNumberServlet extends BaseServlet {

    private static final Properties properties = new Properties();

    static {
        try (FileInputStream fis = new FileInputStream("$HOME/config.properties")) {
            properties.load(fis);
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to load configuration file", e);
        }
    }

    public static final String ACCOUNT_SID = properties.getProperty("account.sid");
    public static final String AUTH_TOKEN = properties.getProperty("auth.token");

    @Override
    protected JSONObject processRequest(JSONObject jsonObject) {
        String username = jsonObject.optString("username", "defaultUser");
        String hashedPassword = jsonObject.optString("hashedPassword", "defaultPassword");

        String statusMessage;
        String phoneNumber = "";
        if (username.equals("defaultUser") || hashedPassword.equals("defaultPassword")) {
            statusMessage = "Credentials not provided";
        } else {
            statusMessage = myDatabase.login(username, hashedPassword);
            if (statusMessage.equals("invalid")) {
                statusMessage = "Invalid credentials";
            } else {
                statusMessage = myDatabase.twoFactorAuth(username);
                if (!statusMessage.equals("Phone not found") && !statusMessage.equals("Database Error")) {
                    phoneNumber = statusMessage;

                    Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
                    Verification verification = Verification.creator("VA65ac97e306fdc87a371c29c36709b9b7", phoneNumber, "sms").create();

                    System.out.println("Verification SID: " + verification.getSid());
                    System.out.println("OTP sent to " + phoneNumber);


                    statusMessage = "success";
                }
            }
        }

        JSONObject jsonResponse = new JSONObject();
        if (phoneNumber.isEmpty()) {
            jsonResponse.put("status", "error");
            jsonResponse.put("message", statusMessage);
        } else {
            jsonResponse.put("status", "success");
            jsonResponse.put("phoneNumber", phoneNumber);
        }
        return jsonResponse;
    }

}
