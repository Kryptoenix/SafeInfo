package org.example.serversafeinfo;

import java.io.*;
import java.util.*;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.http.*;
import jakarta.servlet.annotation.*;
import org.json.JSONObject;


@WebServlet(name = "getSessionServlet", value = "/api/getSession")
public class GetSessionServlet extends HttpServlet {

    private final MyDatabase myDatabase = new MyDatabase();

    // Handle GET requests
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html");

    }

    // Handle POST requests
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        // Set response type
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        // Read JSON data from the request body
        StringBuilder jsonBuffer = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        // Parse the JSON object
        String jsonData = jsonBuffer.toString();
        JSONObject jsonObject = new JSONObject(jsonData);

        // Extract information from the JSON
        String username = jsonObject.optString("username", "defaultUser");
        String password = jsonObject.optString("passwordHash", "defaultPass");

        String resultDb;
        String token="";
        if("defaultUser".equals(username) || "defaultPass".equals(password)) {
            resultDb = "Username or Password not specified";
        }
        else {
            resultDb = myDatabase.validateCreds(username, password);
            if(resultDb.equals("success")) {
                try {
                    token = SessionManager.generateToken(username);
                    resultDb = "success";
                }catch (Exception e) {
                    e.printStackTrace();
                    resultDb = "Failed to create token";
                }
            }
        }

        // Respond back to the client with a success message
        PrintWriter out = response.getWriter();
        JSONObject jsonResponse = new JSONObject();

        if(token.isEmpty()){
            jsonResponse.put("status", resultDb);
        }
        else {
            jsonResponse.put("status", resultDb);
            jsonResponse.put("token", token);
        }

        out.print(jsonResponse);
        out.flush();
    }
    public void destroy() {
        // Clean up resources
    }
}


class SessionManager {
    private static final byte[] keyHMAC = {
            (byte) 0x1F, (byte) 0xA2, (byte) 0xB7, (byte) 0x5C,
            (byte) 0x9D, (byte) 0x3E, (byte) 0x78, (byte) 0x14,
            (byte) 0xC9, (byte) 0x4F, (byte) 0x22, (byte) 0x85,
            (byte) 0x6A, (byte) 0x10, (byte) 0xBB, (byte) 0x33,
            (byte) 0xD8, (byte) 0x7F, (byte) 0x94, (byte) 0x55,
            (byte) 0xE3, (byte) 0x12, (byte) 0x4A, (byte) 0x69,
            (byte) 0x82, (byte) 0x0B, (byte) 0xF6, (byte) 0xCA,
            (byte) 0x5E, (byte) 0x37, (byte) 0x6D, (byte) 0xA8
    };

    public static String generateToken(String username) {
        // Create a 30-minute expiration date
        Calendar calendar = Calendar.getInstance();
        Date now = calendar.getTime(); // Current time
        calendar.add(Calendar.MINUTE, 30); // Token validity for 30 minutes
        Date expiryDate = calendar.getTime();

        // Prepare header claims for JWT
        Map<String, Object> headerClaims = new HashMap<>();
        headerClaims.put("typ", "JWT");
        headerClaims.put("alg", "HS256");

        // Generate the JWT token
        String token = JWT.create()
                .withSubject(username)
                .withExpiresAt(expiryDate)
                .withIssuer("SafeInfo")
                .withIssuedAt(now)
                .withNotBefore(now)
                .withHeader(headerClaims)
                .sign(Algorithm.HMAC256(SessionManager.keyHMAC)); // Ensure keyHMAC is a byte[] (HMAC secret key)

        // Now the token is generated
        System.out.println("Generated JWT token: " + token);

        return token;
    }


    public static String validateToken(String token) {
        // Create a verification context for the token
        JWTVerifier verifier = JWT.require(Algorithm.HMAC256(SessionManager.keyHMAC))
                .withIssuer("IssuerID")
                .build(); // Reusable verifier instance for token validation

        // Verify the token, if the verification fails, an exception is thrown
        try {
            DecodedJWT decodedToken = verifier.verify(token);
            System.out.println("Token verification successful. User: " + decodedToken.getSubject());
        }catch (TokenExpiredException ex) {
            System.out.println("JWT token expired");
        }catch (JWTVerificationException ex) {
            System.out.println("JWT token verification failed");
            return "Invalid token";
        }

        return "success";
    }


}