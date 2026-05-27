/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.network;

import com.google.gson.Gson;
import meteordevelopment.meteorclient.MeteorClient;

import java.net.http.HttpResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChestApiClient {
    private static final Gson GSON = new Gson();

    private final String apiUrl;
    private final String apiKey;

    public ChestApiClient(String apiUrl, String apiKey) {
        this.apiUrl = apiUrl;
        this.apiKey = apiKey;
    }

    public void uploadChestContents(String location, List<ItemData> items) throws Exception {
        Map<String, Object> payload = new HashMap<>();
        payload.put("location", location);
        payload.put("items", items);

        HttpResponse<String> response = Http.post(apiUrl)
            .header("x-api-key", apiKey)
            .bodyJson(payload)
            .sendStringResponse();

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            String errorBody = response.body() != null ? response.body() : "Unknown error";
            throw new Exception("Upload failed: " + response.statusCode() + " - " + errorBody);
        }

        MeteorClient.LOG.info("Successfully uploaded chest contents. Response: " + response.body());
    }
}
