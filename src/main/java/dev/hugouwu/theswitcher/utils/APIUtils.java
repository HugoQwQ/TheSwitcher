package dev.hugouwu.theswitcher.utils;

import com.google.gson.JsonObject;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import org.jetbrains.annotations.NotNull;

public class APIUtils {
    private static final String MINECRAFT_API_BASE = "https://api.minecraftservices.com";

    public static int changeName(@NotNull String newName, @NotNull String token) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(
                MINECRAFT_API_BASE + "/minecraft/profile/name/" + newName).openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestMethod("PUT");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        int statusCode = conn.getResponseCode();
        conn.disconnect();
        return statusCode;
    }

    public static int changeSkin(@NotNull String skinUrl, @NotNull String token) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(
                MINECRAFT_API_BASE + "/minecraft/profile/skins").openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setRequestProperty("Content-Type", "application/json");
        conn.setRequestMethod("POST");
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        conn.setDoOutput(true);
        String jsonBody = String.format("{\"variant\": \"classic\", \"url\": \"%s\"}", skinUrl);
        try (OutputStream out = conn.getOutputStream()) {
            out.write(jsonBody.getBytes(StandardCharsets.UTF_8));
        }
        int statusCode = conn.getResponseCode();
        conn.disconnect();
        return statusCode;
    }

    public static String @NotNull [] getProfileInfo(@NotNull String token) throws Exception {
        HttpURLConnection conn = (HttpURLConnection) new URL(
                MINECRAFT_API_BASE + "/minecraft/profile").openConnection();
        conn.setRequestProperty("Authorization", "Bearer " + token);
        conn.setConnectTimeout(15000);
        conn.setReadTimeout(15000);
        if (conn.getResponseCode() < 200 || conn.getResponseCode() > 299) {
            try (BufferedReader err = new BufferedReader(
                    new InputStreamReader(conn.getErrorStream(), StandardCharsets.UTF_8))) {
                throw new IllegalArgumentException("getProfileInfo response: " + conn.getResponseCode()
                        + ", data: " + err.lines().collect(java.util.stream.Collectors.joining("\n")));
            }
        }
        try (BufferedReader in = new BufferedReader(
                new InputStreamReader(conn.getInputStream(), StandardCharsets.UTF_8))) {
            JsonObject resp = Shared.GSON.fromJson(in.lines().collect(java.util.stream.Collectors.joining("\n")),
                    JsonObject.class);
            String name = resp.get("name").getAsString();
            String uuid = resp.get("id").getAsString();
            return new String[]{name, uuid};
        } finally {
            conn.disconnect();
        }
    }

    public static boolean validateSession(@NotNull String token) {
        try {
            getProfileInfo(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
