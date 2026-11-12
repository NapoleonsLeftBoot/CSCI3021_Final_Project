package com.mycompany.holidaily.agent;

import com.google.gson.JsonObject;
import com.mycompany.holidaily.dao.WordDAO;
import java.sql.SQLException;
import java.util.Locale;

public class AgentToolExecutor {

    private final WordDAO wordDAO = new WordDAO();

    public JsonObject execute(
            String toolName, JsonObject args)
            throws SQLException {

        // Only allow the tools that we implemented.
        if (!"dictionaryCheck".equals(toolName)
                && !"profanityCheck".equals(toolName)) {
            throw new IllegalArgumentException(
                    "Unsupported tool: " + toolName);
        }

        if (args == null
                || !args.has("start")
                || !args.has("goal")
                || !args.get("start").isJsonPrimitive()
                || !args.get("goal").isJsonPrimitive()) {
            throw new IllegalArgumentException(
                    "Missing start or goal argument");
        }

        String start = args.get("start").getAsString()
                .trim().toUpperCase(Locale.ROOT);

        String goal = args.get("goal").getAsString()
                .trim().toUpperCase(Locale.ROOT);

        if (!start.matches("[A-Z]{5}")
                || !goal.matches("[A-Z]{5}")) {
            throw new IllegalArgumentException(
                    "Both words must contain exactly five letters");
        }

        JsonObject result = new JsonObject();
        result.addProperty("start", start);
        result.addProperty("goal", goal);

        if ("dictionaryCheck".equals(toolName)) {

            boolean startValid = wordDAO.isValidWord(start);
            boolean goalValid = wordDAO.isValidWord(goal);

            result.addProperty("startValid", startValid);
            result.addProperty("goalValid", goalValid);
            result.addProperty("result",
                    startValid && goalValid);

        } else if ("profanityCheck".equals(toolName)) {

            boolean startBlocked = wordDAO.isBlockedWord(start);
            boolean goalBlocked = wordDAO.isBlockedWord(goal);

            result.addProperty("startBlocked", startBlocked);
            result.addProperty("goalBlocked", goalBlocked);
            result.addProperty("result",
                    !startBlocked && !goalBlocked);
        }

        return result;
    }
}
