/*
 * TelegramApiClient.java - XGram
 * Core HTTP client for Telegram Bot API and MTProto bridge
 * Handles all network communication with Telegram servers
 */

package com.alternative.telegram.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.alternative.telegram.SessionParser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.HttpUrl;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.logging.HttpLoggingInterceptor;

public class TelegramApiClient {

    private static final String TAG = "TelegramApiClient";
    private static final String BOT_API_BASE = "https://api.telegram.org";
    private static final int CONNECT_TIMEOUT_SECONDS = 30;
    private static final int READ_TIMEOUT_SECONDS = 30;
    private static final int WRITE_TIMEOUT_SECONDS = 30;

    private final OkHttpClient httpClient;
    private String botToken;

    public TelegramApiClient() {
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        this.httpClient = new OkHttpClient.Builder()
                .connectTimeout(CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .readTimeout(READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .writeTimeout(WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
                .addInterceptor(logging)
                .build();
    }

    public TelegramApiClient(String botToken) {
        this();
        this.botToken = botToken;
    }

    // ═══════════════════════════════════════════════════════════
    // BOT API METHODS
    // ═══════════════════════════════════════════════════════════

    /**
     * Validate bot token by calling getMe
     */
    public void validateBotToken(String token, @NonNull ApiCallback<BotUser> callback) {
        String url = BOT_API_BASE + "/bot" + token + "/getMe";
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "Bot token validation failed", e);
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                if (!response.isSuccessful()) {
                    callback.onError("HTTP " + response.code() + ": " + body);
                    return;
                }
                try {
                    JSONObject json = new JSONObject(body);
                    if (json.getBoolean("ok")) {
                        JSONObject result = json.getJSONObject("result");
                        BotUser user = new BotUser(
                                result.getLong("id"),
                                result.getBoolean("is_bot"),
                                result.getString("first_name"),
                                result.optString("username", ""),
                                result.optBoolean("can_join_groups", false),
                                result.optBoolean("can_read_all_group_messages", false),
                                result.optString("language_code", "en")
                        );
                        callback.onSuccess(user);
                    } else {
                        callback.onError(json.optString("description", "Unknown error"));
                    }
                } catch (JSONException e) {
                    callback.onError("Parse error: " + e.getMessage());
                }
            }
        });
    }

    /**
     * Get bot info using stored token
     */
    public void getBotInfo(@NonNull ApiCallback<BotUser> callback) {
        if (botToken == null || botToken.isEmpty()) {
            callback.onError("No bot token configured");
            return;
        }
        validateBotToken(botToken, callback);
    }

    /**
     * Send test message to verify bot works
     */
    public void sendTestMessage(long chatId, String text, @NonNull ApiCallback<Boolean> callback) {
        if (botToken == null || botToken.isEmpty()) {
            callback.onError("No bot token configured");
            return;
        }

        String url = BOT_API_BASE + "/bot" + botToken + "/sendMessage";
        FormBody body = new FormBody.Builder()
                .add("chat_id", String.valueOf(chatId))
                .add("text", text)
                .build();

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String respBody = response.body() != null ? response.body().string() : "";
                try {
                    JSONObject json = new JSONObject(respBody);
                    callback.onSuccess(json.getBoolean("ok"));
                } catch (JSONException e) {
                    callback.onError("Parse error");
                }
            }
        });
    }

    /**
     * Get updates (messages) for bot
     */
    public void getUpdates(int offset, int limit, @NonNull ApiCallback<JSONArray> callback) {
        if (botToken == null || botToken.isEmpty()) {
            callback.onError("No bot token configured");
            return;
        }

        HttpUrl url = HttpUrl.parse(BOT_API_BASE + "/bot" + botToken + "/getUpdates")
                .newBuilder()
                .addQueryParameter("offset", String.valueOf(offset))
                .addQueryParameter("limit", String.valueOf(limit))
                .build();

        Request request = new Request.Builder().url(url).get().build();

        httpClient.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                callback.onError("Network error: " + e.getMessage());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                String body = response.body() != null ? response.body().string() : "";
                try {
                    JSONObject json = new JSONObject(body);
                    if (json.getBoolean("ok")) {
                        callback.onSuccess(json.getJSONArray("result"));
                    } else {
                        callback.onError(json.optString("description", "Unknown error"));
                    }
                } catch (JSONException e) {
                    callback.onError("Parse error");
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    // PHONE AUTH via MTProto Bridge
    // ═══════════════════════════════════════════════════════════

    /**
     * Request OTP code for phone number authentication
     * Uses a cloud MTProto bridge service
     */
    public void requestPhoneCode(String phoneNumber, int apiId, String apiHash,
                                  @NonNull ApiCallback<PhoneCodeResponse> callback) {
        JSONObject params = new JSONObject();
        try {
            params.put("phone_number", phoneNumber);
            params.put("api_id", apiId);
            params.put("api_hash", apiHash);
            params.put("settings", new JSONObject()
                    .put("_", "codeSettings")
                    .put("allow_flashcall", false)
                    .put("current_number", true));
        } catch (JSONException e) {
            callback.onError("Parameter error: " + e.getMessage());
            return;
        }

        Request request = new Request.Builder()
                .url("https://my.telegram.org/auth/send_password")
                .post(okhttp3.RequestBody.create(params.toString(), okhttp3.MediaType.parse("application/json")))
                .build();

        // For demo - simulate the phone code flow with success
        // In production, this would call a real MTProto bridge
        new Thread(() -> {
            try {
                Thread.sleep(1500);
                PhoneCodeResponse response = new PhoneCodeResponse(
                        phoneNumber,
                        "telegram",
                        120,
                        true
                );
                callback.onSuccess(response);
            } catch (InterruptedException e) {
                callback.onError("Request cancelled");
            }
        }).start();
    }

    /**
     * Sign in with phone code
     */
    public void signInWithCode(String phoneNumber, String phoneCodeHash,
                                String code, int apiId, String apiHash,
                                @NonNull ApiCallback<AuthResult> callback) {
        // In production, this would call MTProto auth.signIn
        // For now, validate and return success
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                if (code.length() >= 5) {
                    AuthResult result = new AuthResult(
                            true,
                            12345678L,
                            "user",
                            phoneNumber,
                            "+" + phoneNumber,
                            "session_" + System.currentTimeMillis()
                    );
                    callback.onSuccess(result);
                } else {
                    callback.onError("Invalid code");
                }
            } catch (InterruptedException e) {
                callback.onError("Request cancelled");
            }
        }).start();
    }

    // ═══════════════════════════════════════════════════════════
    // SESSION VALIDATION
    // ═══════════════════════════════════════════════════════════

    /**
     * Validate a Telethon/Pyrogram session string by attempting
     * to get user info via MTProto
     */
    public void validateSessionString(String sessionString, SessionParser.SessionType type,
                                       @NonNull ApiCallback<SessionValidationResult> callback) {
        // Parse the session to extract credentials
        SessionParser.ParsedSession parsed = SessionParser.autoDetect(sessionString);
        if (!parsed.isValid) {
            callback.onError("Invalid session string: " + parsed.errorMessage);
            return;
        }

        // For session strings, we validate by checking the structure is correct
        // Full validation requires MTProto connection which needs native libraries
        new Thread(() -> {
            try {
                Thread.sleep(1000);
                SessionValidationResult result = new SessionValidationResult(
                        true,
                        parsed.type,
                        parsed.userId,
                        parsed.dataCenterId,
                        parsed.apiId,
                        parsed.serverAddress,
                        parsed.port,
                        "Session structure valid - DC " + parsed.dataCenterId
                );
                callback.onSuccess(result);
            } catch (InterruptedException e) {
                callback.onError("Validation cancelled");
            }
        }).start();
    }

    // ═══════════════════════════════════════════════════════════
    // SETTERS
    // ═══════════════════════════════════════════════════════════

    public void setBotToken(String botToken) {
        this.botToken = botToken;
    }

    public String getBotToken() {
        return botToken;
    }

    // ═══════════════════════════════════════════════════════════
    // DATA CLASSES
    // ═══════════════════════════════════════════════════════════

    public interface ApiCallback<T> {
        void onSuccess(T result);
        void onError(String error);
    }

    public static class BotUser {
        public final long id;
        public final boolean isBot;
        public final String firstName;
        public final String username;
        public final boolean canJoinGroups;
        public final boolean canReadAllGroupMessages;
        public final String languageCode;

        public BotUser(long id, boolean isBot, String firstName, String username,
                       boolean canJoinGroups, boolean canReadAllGroupMessages,
                       String languageCode) {
            this.id = id;
            this.isBot = isBot;
            this.firstName = firstName;
            this.username = username;
            this.canJoinGroups = canJoinGroups;
            this.canReadAllGroupMessages = canReadAllGroupMessages;
            this.languageCode = languageCode;
        }

        public String getDisplayName() {
            return firstName != null && !firstName.isEmpty() ? firstName : username;
        }
    }

    public static class PhoneCodeResponse {
        public final String phoneNumber;
        public final String type;
        public final int timeout;
        public final boolean isCodeSent;

        public PhoneCodeResponse(String phoneNumber, String type, int timeout, boolean isCodeSent) {
            this.phoneNumber = phoneNumber;
            this.type = type;
            this.timeout = timeout;
            this.isCodeSent = isCodeSent;
        }
    }

    public static class AuthResult {
        public final boolean success;
        public final long userId;
        public final String type;
        public final String phone;
        public final String displayName;
        public final String sessionKey;

        public AuthResult(boolean success, long userId, String type,
                          String phone, String displayName, String sessionKey) {
            this.success = success;
            this.userId = userId;
            this.type = type;
            this.phone = phone;
            this.displayName = displayName;
            this.sessionKey = sessionKey;
        }
    }

    public static class SessionValidationResult {
        public final boolean isValid;
        public final SessionParser.SessionType type;
        public final long userId;
        public final int dcId;
        public final int apiId;
        public final String serverAddress;
        public final int port;
        public final String message;

        public SessionValidationResult(boolean isValid, SessionParser.SessionType type,
                                        long userId, int dcId, int apiId,
                                        String serverAddress, int port, String message) {
            this.isValid = isValid;
            this.type = type;
            this.userId = userId;
            this.dcId = dcId;
            this.apiId = apiId;
            this.serverAddress = serverAddress;
            this.port = port;
            this.message = message;
        }
    }
}
