package org.example.serversafeinfo;

import java.io.*;

import jakarta.inject.Inject;
import jakarta.servlet.http.*;
import org.json.JSONObject;


public abstract class BaseServlet extends HttpServlet {

    protected MyDatabase myDatabase = new MyDatabase();

    @Override
    public void doGet(HttpServletRequest request, HttpServletResponse response) {
        response.setContentType("text/html");
    }

    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        StringBuilder jsonBuffer = new StringBuilder();
        try (BufferedReader reader = request.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                jsonBuffer.append(line);
            }
        }

        String jsonData = jsonBuffer.toString();
        JSONObject jsonObject = new JSONObject(jsonData);

        JSONObject jsonResponse = processRequest(jsonObject);

        PrintWriter out = response.getWriter();
        out.print(jsonResponse);
        out.flush();
    }

    protected abstract JSONObject processRequest(JSONObject jsonObject);

    @Override
    public void destroy() {
        // Clean up resources
    }
}
