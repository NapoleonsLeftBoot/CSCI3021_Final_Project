package com.mycompany.holidaily.servlet;

import com.google.gson.*;
import com.mycompany.holidaily.agent.AgentToolExecutor;
import com.mycompany.holidaily.agent.GeminiClient;
import com.mycompany.holidaily.dao.AgentLogDAO;

import java.io.IOException;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@WebServlet(name = "AgentServlet", urlPatterns = {"/api/agent"})
public class AgentServlet extends HttpServlet {

    private final GeminiClient gemini = new GeminiClient();
    private final AgentToolExecutor executor =
            new AgentToolExecutor();
    private final AgentLogDAO logDAO = new AgentLogDAO();

    @Override
    protected void doPost(
            HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        response.setContentType("application/json;charset=UTF-8");
        response.setHeader("Cache-Control", "no-store");

        // Step 1: Check that the user has a session.
        HttpSession session = request.getSession(false);

        if (session == null
                || session.getAttribute("userId") == null) {
            sendError(response, 401, "Not logged in");
            return;
        }

        int userId = (Integer) session.getAttribute("userId");

        // Step 2: Accept the goal from a form or JSON.
        String goal;

        try {
            String contentType = request.getContentType();

            if (contentType != null
                    && contentType.toLowerCase()
                            .startsWith("application/json")) {

                JsonObject body = JsonParser.parseReader(
                        request.getReader()).getAsJsonObject();

                goal = body.has("goal")
                        ? body.get("goal").getAsString()
                        : null;

            } else {
                goal = request.getParameter("goal");
            }

        } catch (Exception e) {
            sendError(response, 400, "Invalid request body");
            return;
        }

        if (goal == null || goal.trim().isEmpty()
                || goal.length() > 500) {
            sendError(response, 400,
                    "Goal must contain 1 to 500 characters");
            return;
        }

        // Step 3: Ask Gemini to choose a tool.
        JsonObject functionCall;

        try {
            functionCall = gemini.requestTool(goal);
        } catch (IOException | IllegalStateException e) {
            log("Gemini request failed", e);
            sendError(response, 502,
                    "Gemini request failed. Check server logs.");
            return;
        }

        if (!functionCall.has("name")
                || !functionCall.has("args")) {
            sendError(response, 502,
                    "Gemini returned an incomplete tool call");
            return;
        }

        String toolName;
        JsonObject args;

        try {
            toolName = functionCall.get("name").getAsString();
            args = functionCall.getAsJsonObject("args");
        } catch (Exception e) {
            sendError(response, 502,
                    "Gemini returned invalid tool arguments");
            return;
        }

        // Step 4: Execute the selected tool.
        JsonObject observation = new JsonObject();
        boolean success = true;
        int status = 200;

        try {
            observation = executor.execute(toolName, args);

        } catch (IllegalArgumentException e) {
            success = false;
            status = 400;
            observation.addProperty("error", e.getMessage());

        } catch (SQLException e) {
            success = false;
            status = 500;
            observation.addProperty("error", "Database error");
            log("Agent tool database error", e);
        }

        // Step 5: Log the actual call and its result.
        try {
            logDAO.logToolCall(
                    userId,
                    toolName,
                    args.toString(),
                    observation.toString());

        } catch (SQLException e) {
            log("Could not write agent log", e);
            sendError(response, 500,
                    "Agent tool ran, but logging failed");
            return;
        }

        // Step 6: Return the observation to the user.
        JsonObject result = new JsonObject();
        result.addProperty("success", success);
        result.addProperty("goal", goal);
        result.addProperty("toolCalled", toolName);
        result.add("arguments", args);
        result.add("observation", observation);
        result.addProperty("logged", true);

        response.setStatus(status);
        response.getWriter().print(result.toString());
    }

    private void sendError(
            HttpServletResponse response,
            int status,
            String message) throws IOException {

        JsonObject result = new JsonObject();
        result.addProperty("success", false);
        result.addProperty("error", message);

        response.setStatus(status);
        response.getWriter().print(result.toString());
    }
}
