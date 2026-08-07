/*
 * SessionManager.java — XGram
 * Secure session persistence manager with fallback
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
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error creating EncryptedSharedPreferences", e);
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
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error saving phone session", e);
        }
    }

    public void saveBotSession(String botToken) {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error saving bot session", e);
        }
    }

    public void saveStringSession(SessionParser.ParsedSession session) {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error saving string session", e);
        }
    }

    public String getLoginMethod() {
        return getGeneralString(KEY_LOGIN_METHOD, "");
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
        return getSecureString(KEY_BOT_TOKEN, null);
    }

    public byte[] getAuthKey() {
        String authKeyB64 = getSecureString(KEY_AUTH_KEY, null);
        if (authKeyB64 != null) {
            try {
                return Base64.decode(authKeyB64, Base64.DEFAULT);
            } catch (Exception e) {
                Log.e(TAG, "Error decoding auth key", e);
                invalidateUnreadableSession();
            }
        }
        return null;
    }

    public long getUserId() {
        return getGeneralLong(KEY_USER_ID, 0);
    }

    public int getDcId() {
        return getSecureInt(KEY_DC_ID, 1);
    }

    public int getApiId() {
        return getSecureInt(KEY_API_ID, 0);
    }

    public String getServerAddress() {
        return getSecureString(KEY_SERVER_ADDRESS, null);
    }

    public int getPort() {
        return getSecureInt(KEY_PORT, 443);
    }

    public String getSessionString() {
        return getSecureString(KEY_SESSION_STRING, null);
    }

    public String getPhoneNumber() {
        return getSecureString(KEY_PHONE_NUMBER, null);
    }

    public String getCountryCode() {
        return getSecureString(KEY_COUNTRY_CODE, null);
    }

    public void setUsername(String username) {
        generalPrefs.edit().putString(KEY_USERNAME, username != null ? username : "").apply();
    }

    public String getUsername() {
        return getGeneralString(KEY_USERNAME, "");
    }

    public void setDisplayName(String name) {
        generalPrefs.edit().putString(KEY_DISPLAY_NAME, name != null ? name : "").apply();
    }

    public String getDisplayName() {
        return getGeneralString(KEY_DISPLAY_NAME, "");
    }

    public void setBio(String bio) {
        generalPrefs.edit().putString(KEY_BIO, bio != null ? bio : "").apply();
    }

    public String getBio() {
        return getGeneralString(KEY_BIO, "");
    }

    public void setProfilePhotoUrl(String url) {
        generalPrefs.edit().putString(KEY_PROFILE_PHOTO_URL, url).apply();
    }

    public String getProfilePhotoUrl() {
        return getGeneralString(KEY_PROFILE_PHOTO_URL, null);
    }

    public void setCustomBackgroundUrl(String url) {
        generalPrefs.edit().putString(KEY_CUSTOM_BG_URL, url != null ? url : "").apply();
    }

    public String getCustomBackgroundUrl() {
        return getGeneralString(KEY_CUSTOM_BG_URL, null);
    }

    public boolean isLoggedIn() {
        if (!getGeneralBoolean(KEY_IS_LOGGED_IN, false)) {
            return false;
        }

        String method = getLoginMethod();
        boolean hasCredentials;
        if (LOGIN_METHOD_BOT.equals(method)) {
            hasCredentials = hasValue(getBotToken());
        } else if (LOGIN_METHOD_PHONE.equals(method)) {
            hasCredentials = hasValue(getPhoneNumber());
        } else if (LOGIN_METHOD_SESSION.equals(method)) {
            hasCredentials = hasValue(getSessionString());
        } else {
            hasCredentials = false;
        }

        if (!hasCredentials) {
            invalidateUnreadableSession();
        }
        return hasCredentials;
    }

    public void updateLastActive() {
        generalPrefs.edit().putLong(KEY_LAST_ACTIVE, System.currentTimeMillis()).apply();
    }

    public long getLastActive() {
        return getGeneralLong(KEY_LAST_ACTIVE, 0);
    }

    public String getSessionId() {
        return getGeneralString(KEY_SESSION_ID, "");
    }

    private String getSecureString(String key, String defaultValue) {
        try {
            return securePrefs.getString(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Secure session data is unreadable", e);
            invalidateUnreadableSession();
            return defaultValue;
        }
    }

    private int getSecureInt(String key, int defaultValue) {
        try {
            return securePrefs.getInt(key, defaultValue);
        } catch (Exception e) {
            Log.e(TAG, "Secure session data is unreadable", e);
            invalidateUnreadableSession();
            return defaultValue;
        }
    }

    private String getGeneralString(String key, String defaultValue) {
        try {
            return generalPrefs.getString(key, defaultValue);
        } catch (Exception e) {
            removeInvalidGeneralPreference(key, e);
            return defaultValue;
        }
    }

    private long getGeneralLong(String key, long defaultValue) {
        try {
            return generalPrefs.getLong(key, defaultValue);
        } catch (Exception e) {
            removeInvalidGeneralPreference(key, e);
            return defaultValue;
        }
    }

    private boolean getGeneralBoolean(String key, boolean defaultValue) {
        try {
            return generalPrefs.getBoolean(key, defaultValue);
        } catch (Exception e) {
            removeInvalidGeneralPreference(key, e);
            return defaultValue;
        }
    }

    private void removeInvalidGeneralPreference(String key, Exception cause) {
        Log.w(TAG, "Removing unreadable general preference: " + key, cause);
        try {
            generalPrefs.edit().remove(key).apply();
        } catch (Exception cleanupError) {
            Log.e(TAG, "Failed to remove unreadable general preference: " + key,
                    cleanupError);
        }
    }

    private boolean hasValue(String value) {
        return value != null && !value.isEmpty();
    }

    private void invalidateUnreadableSession() {
        try {
            generalPrefs.edit()
                    .putBoolean(KEY_IS_LOGGED_IN, false)
                    .remove(KEY_LOGIN_METHOD)
                    .remove(KEY_USER_ID)
                    .apply();
        } catch (Exception e) {
            Log.e(TAG, "Failed to invalidate unreadable session", e);
        }
    }

    public void clearSession() {
        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Error clearing session", e);
        }
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
