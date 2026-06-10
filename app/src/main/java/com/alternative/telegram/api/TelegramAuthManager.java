/*
 * TelegramAuthManager.java - XGram
 * Central authentication coordinator for all Telegram login methods
 * Manages state transitions and session persistence
 */

package com.alternative.telegram.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.alternative.telegram.SessionManager;
import com.alternative.telegram.SessionParser;

import org.json.JSONArray;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TelegramAuthManager {

    private static final String TAG = "TelegramAuthManager";

    private static TelegramAuthManager instance;

    private final TelegramApiClient apiClient;
    private final SessionManager sessionManager;
    private final ExecutorService executor;
    private final Handler mainHandler;

    // Auth state
    private volatile String pendingPhoneNumber;
    private volatile String pendingPhoneCodeHash;
    private volatile boolean isAuthenticating = false;

    private TelegramAuthManager(Context context) {
        this.apiClient = new TelegramApiClient();
        this.sessionManager = SessionManager.getInstance(context);
        this.executor = Executors.newSingleThreadExecutor();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized TelegramAuthManager getInstance(Context context) {
        if (instance == null) {
            instance = new TelegramAuthManager(context);
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // BOT TOKEN AUTH
    // ═══════════════════════════════════════════════════════════

    /**
     * Authenticate with bot token - validates against Telegram Bot API
     */
    public void authenticateWithBotToken(String botToken, AuthCallback callback) {
        if (isAuthenticating) {
            callback.onError("Authentication already in progress");
            return;
        }
        isAuthenticating = true;

        apiClient.validateBotToken(botToken, new TelegramApiClient.ApiCallback<TelegramApiClient.BotUser>() {
            @Override
            public void onSuccess(TelegramApiClient.BotUser botUser) {
                isAuthenticating = false;

                // Save session
                sessionManager.saveBotSession(botToken);
                sessionManager.setUsername(botUser.username);
                sessionManager.setDisplayName(botUser.getDisplayName());

                Log.i(TAG, "Bot authentication successful: " + botUser.username);

                mainHandler.post(() -> callback.onSuccess(
                        AuthMethod.BOT_TOKEN,
                        botUser.id,
                        botUser.getDisplayName(),
                        botUser.username
                ));
            }

            @Override
            public void onError(String error) {
                isAuthenticating = false;
                Log.e(TAG, "Bot authentication failed: " + error);
                mainHandler.post(() -> callback.onError(error));
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    // PHONE NUMBER AUTH
    // ═══════════════════════════════════════════════════════════

    /**
     * Step 1: Request OTP code for phone number
     */
    public void requestPhoneCode(String countryCode, String phoneNumber,
                                  int apiId, String apiHash,
                                  PhoneCodeCallback callback) {
        if (isAuthenticating) {
            callback.onError("Authentication already in progress");
            return;
        }
        isAuthenticating = true;

        String fullNumber = countryCode + phoneNumber;
        pendingPhoneNumber = fullNumber;

        apiClient.requestPhoneCode(fullNumber, apiId, apiHash,
                new TelegramApiClient.ApiCallback<TelegramApiClient.PhoneCodeResponse>() {
                    @Override
                    public void onSuccess(TelegramApiClient.PhoneCodeResponse response) {
                        pendingPhoneCodeHash = "hash_" + System.currentTimeMillis();
                        Log.i(TAG, "Phone code requested for: " + fullNumber);
                        mainHandler.post(() -> callback.onCodeSent(
                                response.type, response.timeout));
                    }

                    @Override
                    public void onError(String error) {
                        isAuthenticating = false;
                        mainHandler.post(() -> callback.onError(error));
                    }
                });
    }

    /**
     * Step 2: Verify OTP code and complete phone authentication
     */
    public void verifyPhoneCode(String code, int apiId, String apiHash,
                                 AuthCallback callback) {
        if (pendingPhoneNumber == null || pendingPhoneCodeHash == null) {
            callback.onError("No pending phone authentication");
            return;
        }

        apiClient.signInWithCode(pendingPhoneNumber, pendingPhoneCodeHash, code,
                apiId, apiHash,
                new TelegramApiClient.ApiCallback<TelegramApiClient.AuthResult>() {
                    @Override
                    public void onSuccess(TelegramApiClient.AuthResult result) {
                        isAuthenticating = false;

                        // Extract country code and phone
                        String countryCode = "";
                        String phoneNumber = pendingPhoneNumber;
                        if (pendingPhoneNumber.startsWith("+")) {
                            phoneNumber = pendingPhoneNumber.substring(1);
                        }

                        sessionManager.savePhoneSession(countryCode, phoneNumber, result.userId);
                        sessionManager.setDisplayName(result.displayName);

                        Log.i(TAG, "Phone authentication successful for user: " + result.userId);

                        mainHandler.post(() -> callback.onSuccess(
                                AuthMethod.PHONE,
                                result.userId,
                                result.displayName,
                                result.phone
                        ));

                        clearPendingPhoneAuth();
                    }

                    @Override
                    public void onError(String error) {
                        isAuthenticating = false;
                        mainHandler.post(() -> callback.onError(error));
                    }
                });
    }

    /**
     * Cancel pending phone authentication
     */
    public void cancelPhoneAuth() {
        clearPendingPhoneAuth();
        isAuthenticating = false;
    }

    private void clearPendingPhoneAuth() {
        pendingPhoneNumber = null;
        pendingPhoneCodeHash = null;
    }

    // ═══════════════════════════════════════════════════════════
    // SESSION STRING AUTH
    // ═══════════════════════════════════════════════════════════

    /**
     * Authenticate with Telethon/Pyrogram session string
     */
    public void authenticateWithSessionString(String sessionString, AuthCallback callback) {
        if (isAuthenticating) {
            callback.onError("Authentication already in progress");
            return;
        }
        isAuthenticating = true;

        // First parse the session
        SessionParser.ParsedSession parsed = SessionParser.autoDetect(sessionString);
        if (!parsed.isValid) {
            isAuthenticating = false;
            callback.onError("Invalid session: " + parsed.errorMessage);
            return;
        }

        // Validate via API
        apiClient.validateSessionString(sessionString, parsed.type,
                new TelegramApiClient.ApiCallback<TelegramApiClient.SessionValidationResult>() {
                    @Override
                    public void onSuccess(TelegramApiClient.SessionValidationResult result) {
                        isAuthenticating = false;

                        // Save session
                        sessionManager.saveStringSession(parsed);

                        String displayName = "Session User (DC" + result.dcId + ")";
                        String username = "user_dc" + result.dcId;

                        if (result.userId != 0) {
                            sessionManager.setUsername("user_" + result.userId);
                        }
                        sessionManager.setDisplayName(displayName);

                        Log.i(TAG, "Session authentication successful: " + result.message);

                        mainHandler.post(() -> callback.onSuccess(
                                AuthMethod.SESSION_STRING,
                                result.userId != 0 ? result.userId : parsed.userId,
                                displayName,
                                username
                        ));
                    }

                    @Override
                    public void onError(String error) {
                        isAuthenticating = false;
                        mainHandler.post(() -> callback.onError(error));
                    }
                });
    }

    // ═══════════════════════════════════════════════════════════
    // BOT OPERATIONS
    // ═══════════════════════════════════════════════════════════

    /**
     * Get bot info if logged in as bot
     */
    public void getBotInfo(BotInfoCallback callback) {
        String token = sessionManager.getBotToken();
        if (token == null || token.isEmpty()) {
            callback.onError("Not logged in as bot");
            return;
        }

        apiClient.setBotToken(token);
        apiClient.getBotInfo(new TelegramApiClient.ApiCallback<TelegramApiClient.BotUser>() {
            @Override
            public void onSuccess(TelegramApiClient.BotUser botUser) {
                mainHandler.post(() -> callback.onInfoLoaded(botUser));
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> callback.onError(error));
            }
        });
    }

    /**
     * Get bot updates
     */
    public void getBotUpdates(int offset, UpdatesCallback callback) {
        String token = sessionManager.getBotToken();
        if (token == null || token.isEmpty()) {
            callback.onError("Not logged in as bot");
            return;
        }

        apiClient.setBotToken(token);
        apiClient.getUpdates(offset, 50, new TelegramApiClient.ApiCallback<JSONArray>() {
            @Override
            public void onSuccess(JSONArray result) {
                mainHandler.post(() -> callback.onUpdatesReceived(result));
            }

            @Override
            public void onError(String error) {
                mainHandler.post(() -> callback.onError(error));
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    // SESSION MANAGEMENT
    // ═══════════════════════════════════════════════════════════

    public boolean isLoggedIn() {
        return sessionManager.isLoggedIn();
    }

    public void logout(LogoutCallback callback) {
        executor.submit(() -> {
            try {
                sessionManager.clearSession();
                pendingPhoneNumber = null;
                pendingPhoneCodeHash = null;
                isAuthenticating = false;

                mainHandler.post(() -> {
                    if (callback != null) callback.onLoggedOut();
                });
            } catch (Exception e) {
                Log.e(TAG, "Logout error", e);
                mainHandler.post(() -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
            }
        });
    }

    public String getPendingPhoneNumber() {
        return pendingPhoneNumber;
    }

    public boolean isAuthenticating() {
        return isAuthenticating;
    }

    // ═══════════════════════════════════════════════════════════
    // CALLBACK INTERFACES
    // ═══════════════════════════════════════════════════════════

    public interface AuthCallback {
        void onSuccess(AuthMethod method, long userId, String displayName, String username);
        void onError(String error);
    }

    public interface PhoneCodeCallback {
        void onCodeSent(String type, int timeout);
        void onError(String error);
    }

    public interface BotInfoCallback {
        void onInfoLoaded(TelegramApiClient.BotUser botUser);
        void onError(String error);
    }

    public interface UpdatesCallback {
        void onUpdatesReceived(JSONArray updates);
        void onError(String error);
    }

    public interface LogoutCallback {
        void onLoggedOut();
        void onError(String error);
    }

    public enum AuthMethod {
        BOT_TOKEN,
        PHONE,
        SESSION_STRING
    }
}
