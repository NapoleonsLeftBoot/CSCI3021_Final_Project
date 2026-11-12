package com.mycompany.holidaily.agent;

import com.google.gson.*;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;

public class GeminiClient {

    // Change GEMINI_MODEL in your environment if needed.
    private static final String DEFAULT_MODEL =
            "gemini-3.1-flash-lite";

    public JsonObject requestTool(String goal) throws IOException {

        String apiKey = System.getenv("GEMINI_API_KEY");

        if (apiKey == null || apiKey.trim().isEmpty()) {
            throw new IOException("GEMINI_API_KEY is missing");
        }

        String model = System.getenv("GEMINI_MODEL");
        if (model == null || model.trim().isEmpty()) {
            model = DEFAULT_MODEL;
        }

        // Build the request for Gemini.
        JsonObject request = new JsonObject();

        JsonObject userPart = new JsonObject();
        userPart.addProperty("text",
                "You are the HoliDaily puzzle agent. "
                + "Choose one tool to handle this request. "
                + "Only use words explicitly provided by the user. "
                + "Do not invent missing words. "
                + "User request: " + goal);

        JsonArray parts = new JsonArray();
        parts.add(userPart);

        JsonObject content = new JsonObject();
        content.addProperty("role", "user");
        content.add("parts", parts);

        JsonArray contents = new JsonArray();
        contents.add(content);
        request.add("contents", contents);

        // Tell Gemini which tools are available.
        JsonArray declarations = new JsonArray();

        declarations.add(createTool(
                "dictionaryCheck",
                "Check whether two five-letter words "
                + "exist in the HoliDaily dictionary. "
                + "Use this for dictionary validation."));

        declarations.add(createTool(
                "profanityCheck",
                "Check whether either submitted word "
                + "appears in the blocked-words table. "
                + "Use this for profanity validation."));

        JsonObject tools = new JsonObject();
        tools.add("functionDeclarations", declarations);

        JsonArray toolArray = new JsonArray();
        toolArray.add(tools);
        request.add("tools", toolArray);

        // Require a function call for this milestone.
        JsonObject functionCalling = new JsonObject();
        functionCalling.addProperty("mode", "ANY");

        JsonArray allowed = new JsonArray();
        allowed.add("dictionaryCheck");
        allowed.add("profanityCheck");
        functionCalling.add("allowedFunctionNames", allowed);

        JsonObject toolConfig = new JsonObject();
        toolConfig.add("functionCallingConfig", functionCalling);
        request.add("toolConfig", toolConfig);

        String endpoint =
                "https://generativelanguage.googleapis.com/v1beta/"
                + "models/" + model + ":generateContent";

        HttpURLConnection conn = (HttpURLConnection)
                new URL(endpoint).openConnection();

        try {
            conn.setRequestMethod("POST");
            conn.setRequestProperty(
                    "Content-Type", "application/json");
            conn.setRequestProperty("x-goog-api-key", apiKey);
            conn.setConnectTimeout(10000);
            conn.setReadTimeout(30000);
            conn.setDoOutput(true);

            byte[] body = request.toString().getBytes(
                    StandardCharsets.UTF_8);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(body);
            }

            int status = conn.getResponseCode();

            if (status < 200 || status >= 300) {
                // Do not include raw provider responses
                // or API credentials in user-facing errors.
                throw new IOException(
                        "Gemini API returned HTTP " + status);
            }

            try (Reader reader = new InputStreamReader(
                    conn.getInputStream(),
                    StandardCharsets.UTF_8)) {

                JsonObject response =
                        JsonParser.parseReader(reader).getAsJsonObject();

                JsonArray candidates =
                        response.getAsJsonArray("candidates");

                if (candidates == null || candidates.size() == 0) {
                    throw new IOException("Gemini returned no candidates");
                }

                JsonObject candidate =
                        candidates.get(0).getAsJsonObject();

                JsonObject responseContent =
                        candidate.getAsJsonObject("content");

                if (responseContent == null) {
                    throw new IOException("Gemini returned no content");
                }

                JsonArray responseParts =
                        responseContent.getAsJsonArray("parts");

                if (responseParts == null) {
                    throw new IOException("Gemini returned no parts");
                }

                // Find the function call requested by Gemini.
                for (JsonElement element : responseParts) {
                    JsonObject part = element.getAsJsonObject();

                    if (part.has("functionCall")) {
                        return part.getAsJsonObject("functionCall");
                    }
                }

                throw new IOException(
                        "Gemini did not request a tool");
            }
        } finally {
            conn.disconnect();
        }
    }

    private JsonObject createTool(
            String name, String description) {

        JsonObject tool = new JsonObject();
        tool.addProperty("name", name);
        tool.addProperty("description", description);

        JsonObject parameters = new JsonObject();
        parameters.addProperty("type", "OBJECT");

        JsonObject properties = new JsonObject();

        JsonObject start = new JsonObject();
        start.addProperty("type", "STRING");
        start.addProperty("description",
                "The first five-letter word");

        JsonObject goal = new JsonObject();
        goal.addProperty("type", "STRING");
        goal.addProperty("description",
                "The second five-letter word");

        properties.add("start", start);
        properties.add("goal", goal);
        parameters.add("properties", properties);

        JsonArray required = new JsonArray();
        required.add("start");
        required.add("goal");
        parameters.add("required", required);

        tool.add("parameters", parameters);

        return tool;
    }
}
