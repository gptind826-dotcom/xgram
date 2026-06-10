/*
 * SessionManager.java - XGram
 * Secure session persistence manager
 */

package com.alternative.telegram;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Base64;
import android.util.Log;

import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;

public class SessionManager {

    private static final String TAG = "SessionManager";

    private static final String PREFS_SECURE = "telegram_session_secure";
    private static final String PREFS_GENERAL = "telegram_session_general";
    private static final String PREFS_FALLBACK = "telegram_session_fallback";

    private static final String KEY_SESSION_TYPE = "session_type";
    private static final String KEY_AUTH_KEY = "auth_key_b64";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_API_ID = "api_id";
    private static final String KEY_DC_ID = "dc_id";
    private static final String KEY_SERVER_ADDRESS = "server_address";
    private static final String KEY_PORT = "port";
    private static final String KEY_BOT_TOKEN = "bot_token";
    private static final String KEY_PHONE_NUMBER = "phone_number";
    private static final String KEY_COUNTRY_CODE = "country_code";
    private static final String KEY_SESSION_STRING = "session_string";

    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LOGIN_METHOD = "login_method";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_BIO = "bio";
    private static final String KEY_PROFILE_PHOTO_URL = "profile_photo_url";
    private static final String KEY_CUSTOM_BG_URL = "custom_background_url";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_LAST_ACTIVE = "last_active_timestamp";

    public static final String LOGIN_METHOD_PHONE = "phone";
    public static final String LOGIN_METHOD_BOT = "bot";
    public static final String LOGIN_METHOD_SESSION = "session";

    private static SessionManager instance;

    private final SharedPreferences securePrefs;
    private final SharedPreferences generalPrefs;
    private final Context appContext;
    private final boolean usingEncryption;

    private SessionManager(Context context) {
        this.appContext = context.getApplicationContext();
        SharedPreferences encrypted = null;
        boolean encryptionOk = false;
        try {
            MasterKey masterKey = new MasterKey.Builder(appContext)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();
            encrypted = EncryptedSharedPreferences.create(
                    appContext,
                    PREFS_SECURE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
            encryptionOk = true;
            Log.i(TAG, "EncryptedSharedPreferences initialized successfully");
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to create EncryptedSharedPreferences, using fallback", e);
        }
        this.usingEncryption = encryptionOk;
        if (encrypted != null) {
            this.securePrefs = encrypted;
        } else {
            this.securePrefs = appContext.getSharedPreferences(PREFS_FALLBACK, Context.MODE_PRIVATE);
            Log.w(TAG, "Using unencrypted fallback preferences");
        }
        this.generalPrefs = appContext.getSharedPreferences(PREFS_GENERAL, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    public boolean isUsingEncryption() {
        return usingEncryption;
    }

    public void savePhoneSession(String countryCode, String phoneNumber, long userId) {
        SharedPreferences.Editor secureEdit = securePrefs.edit();
        secureEdit.putString(KEY_COUNTRY_CODE, countryCode);
        secureEdit.putString(KEY_PHONE_NUMBER, phoneNumber);
        secureEdit.putLong(KEY_USER_ID, userId);
        secureEdit.apply();

        SharedPreferences.Editor generalEdit = generalPrefs.edit();
        generalEdit.putString(KEY_LOGIN_METHOD, LOGIN_METHOD_PHONE);
        generalEdit.putLong(KEY_USER_ID, userId);
        generalEdit.putBoolean(KEY_IS_LOGGED_IN, true);
        generalEdit.putString(KEY_SESSION_ID, generateSessionId());
        generalEdit.putLong(KEY_LAST_ACTIVE, System.currentTimeMillis());
        generalEdit.apply();

        Log.i(TAG, "Phone session saved for user: " + userId);
    }

    public void saveBotSession(String botToken) {
        SharedPreferences.Editor secureEdit = securePrefs.edit();
        secureEdit.putString(KEY_BOT_TOKEN, botToken);
        secureEdit.apply();

        long botId = extractBotId(botToken);

        SharedPreferences.Editor generalEdit = generalPrefs.edit();
        generalEdit.putString(KEY_LOGIN_METHOD, LOGIN_METHOD_BOT);
        generalEdit.putLong(KEY_USER_ID, botId);
        generalEdit.putBoolean(KEY_IS_LOGGED_IN, true);
        generalEdit.putString(KEY_SESSION_ID, generateSessionId());
        generalEdit.putLong(KEY_LAST_ACTIVE, System.currentTimeMillis());
        generalEdit.apply();

        Log.i(TAG, "Bot session saved for bot ID: " + botId);
    }

    public void saveStringSession(SessionParser.ParsedSession session) {
        SharedPreferences.Editor secureEdit = securePrefs.edit();
        secureEdit.putString(KEY_SESSION_TYPE, session.type.name());
        secureEdit.putLong(KEY_USER_ID, session.userId);
        secureEdit.putInt(KEY_API_ID, session.apiId);
        secureEdit.putInt(KEY_DC_ID, session.dataCenterId);
        secureEdit.putString(KEY_SERVER_ADDRESS, session.serverAddress);
        secureEdit.putInt(KEY_PORT, session.port);
        secureEdit.putString(KEY_SESSION_STRING, session.rawInput);

        if (session.authKey != null) {
            String authKeyB64 = Base64.encodeToString(session.authKey, Base64.NO_WRAP);
            secureEdit.putString(KEY_AUTH_KEY, authKeyB64);
        }

        secureEdit.apply();

        SharedPreferences.Editor generalEdit = generalPrefs.edit();
        generalEdit.putString(KEY_LOGIN_METHOD, LOGIN_METHOD_SESSION);
        generalEdit.putLong(KEY_USER_ID, session.userId);
        generalEdit.putBoolean(KEY_IS_LOGGED_IN, true);
        generalEdit.putString(KEY_SESSION_ID, generateSessionId());
        generalEdit.putLong(KEY_LAST_ACTIVE, System.currentTimeMillis());
        generalEdit.apply();

        Log.i(TAG, "String session saved: type=" + session.type
                + ", userId=" + session.userId
                + ", dcId=" + session.dataCenterId);
    }

    public String getLoginMethod() {
        return generalPrefs.getString(KEY_LOGIN_METHOD, "");
    }

    public boolean isBotLogin() {
        return LOGIN_METHOD_BOT.equals(getLoginMethod());
    }

    public boolean isPhoneLogin() {
        return LOGIN_METHOD_PHONE.equals(getLoginMethod());
    }

    public boolean isSessionLogin() {
        return LOGIN_METHOD_SESSION.equals(getLoginMethod());
    }

    public String getBotToken() {
        return securePrefs.getString(KEY_BOT_TOKEN, null);
    }

    public byte[] getAuthKey() {
        String authKeyB64 = securePrefs.getString(KEY_AUTH_KEY, null);
        if (authKeyB64 != null) {
            return Base64.decode(authKeyB64, Base64.DEFAULT);
        }
        return null;
    }

    public long getUserId() {
        return generalPrefs.getLong(KEY_USER_ID, 0);
    }

    public int getDcId() {
        return securePrefs.getInt(KEY_DC_ID, 1);
    }

    public int getApiId() {
        return securePrefs.getInt(KEY_API_ID, 0);
    }

    public String getServerAddress() {
        return securePrefs.getString(KEY_SERVER_ADDRESS, null);
    }

    public int getPort() {
        return securePrefs.getInt(KEY_PORT, 443);
    }

    public String getSessionString() {
        return securePrefs.getString(KEY_SESSION_STRING, null);
    }

    public String getPhoneNumber() {
        return securePrefs.getString(KEY_PHONE_NUMBER, null);
    }

    public String getCountryCode() {
        return securePrefs.getString(KEY_COUNTRY_CODE, null);
    }

    public void setUsername(String username) {
        generalPrefs.edit().putString(KEY_USERNAME, username).apply();
    }

    public String getUsername() {
        return generalPrefs.getString(KEY_USERNAME, "");
    }

    public void setDisplayName(String name) {
        generalPrefs.edit().putString(KEY_DISPLAY_NAME, name).apply();
    }

    public String getDisplayName() {
        return generalPrefs.getString(KEY_DISPLAY_NAME, "");
    }

    public void setBio(String bio) {
        generalPrefs.edit().putString(KEY_BIO, bio).apply();
    }

    public String getBio() {
        return generalPrefs.getString(KEY_BIO, "");
    }

    public void setProfilePhotoUrl(String url) {
        generalPrefs.edit().putString(KEY_PROFILE_PHOTO_URL, url).apply();
    }

    public String getProfilePhotoUrl() {
        return generalPrefs.getString(KEY_PROFILE_PHOTO_URL, null);
    }

    public void setCustomBackgroundUrl(String url) {
        generalPrefs.edit().putString(KEY_CUSTOM_BG_URL, url).apply();
    }

    public String getCustomBackgroundUrl() {
        return generalPrefs.getString(KEY_CUSTOM_BG_URL, null);
    }

    public boolean isLoggedIn() {
        return generalPrefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    public void updateLastActive() {
        generalPrefs.edit().putLong(KEY_LAST_ACTIVE, System.currentTimeMillis()).apply();
    }

    public long getLastActive() {
        return generalPrefs.getLong(KEY_LAST_ACTIVE, 0);
    }

    public String getSessionId() {
        return generalPrefs.getString(KEY_SESSION_ID, "");
    }

    public void clearSession() {
        securePrefs.edit().clear().apply();

        generalPrefs.edit()
                .remove(KEY_IS_LOGGED_IN)
                .remove(KEY_LOGIN_METHOD)
                .remove(KEY_USER_ID)
                .remove(KEY_USERNAME)
                .remove(KEY_DISPLAY_NAME)
                .remove(KEY_BIO)
                .remove(KEY_PROFILE_PHOTO_URL)
                .remove(KEY_SESSION_ID)
                .remove(KEY_LAST_ACTIVE)
                .remove(KEY_BOT_TOKEN)
                .remove(KEY_PHONE_NUMBER)
                .remove(KEY_COUNTRY_CODE)
                .apply();

        Log.i(TAG, "Session cleared - user logged out");
    }

    public JSONObject exportSessionInfo() {
        JSONObject info = new JSONObject();
        try {
            info.put("login_method", getLoginMethod());
            info.put("user_id", getUserId());
            info.put("is_logged_in", isLoggedIn());
            info.put("session_type",
                    isBotLogin() ? "Bot"
                            : isPhoneLogin() ? "Phone"
                            : isSessionLogin() ? "String Session"
                            : "Unknown");
            info.put("username", getUsername());
            info.put("display_name", getDisplayName());
            info.put("dc_id", getDcId());
            info.put("last_active", getLastActive());
        } catch (JSONException e) {
            Log.e(TAG, "Failed to export session info", e);
        }
        return info;
    }

    private String generateSessionId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    private long extractBotId(String botToken) {
        try {
            String idPart = botToken.substring(0, botToken.indexOf(':'));
            return Long.parseLong(idPart);
        } catch (Exception e) {
            return 0;
        }
    }

    public void forceReinitialize(Context context) {
        instance = new SessionManager(context);
        Log.i(TAG, "Session manager force-reinitialized");
    }
}
