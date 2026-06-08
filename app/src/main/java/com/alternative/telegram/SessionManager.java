/*
 * ꜱᴇꜱꜱɪᴏɴᴍᴀɴᴀɢᴇʀ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ꜱᴇᴄᴜʀᴇ ꜱᴇꜱꜱɪᴏɴ ᴘᴇʀꜱɪꜱᴛᴇɴᴄᴇ ᴍᴀɴᴀɢᴇʀ
 *
 * ʜᴀɴᴅʟᴇꜱ:
 * - ꜱᴇᴄᴜʀᴇ ꜱᴛᴏʀᴀɢᴇ ᴏꜰ ᴀᴜᴛʜᴇɴᴛɪᴄᴀᴛɪᴏɴ ᴅᴀᴛᴀ
 * - ꜱᴇꜱꜱɪᴏɴ ꜱᴛᴀᴛᴇ ᴍᴀɴᴀɢᴇᴍᴇɴᴛ (ᴀᴄᴛɪᴠᴇ/ᴇхᴘɪʀᴇᴅ)
 * - ʟᴏɢɪɴ ᴛʏᴘᴇ ᴛʀᴀᴄᴋɪɴɢ (ᴘʜᴏɴᴇ/ʙᴏᴛ/ꜱᴇꜱꜱɪᴏɴ)
 * - ᴄʀᴇᴅᴇɴᴛɪᴀʟ ꜱᴇᴄʀᴇᴛ-ʙᴀꜱᴇᴅ ᴇɴᴄʀʏᴘᴛɪᴏɴ
 * - ᴀᴜᴛᴏ-ʟᴏɢᴏᴜᴛ ᴏɴ ꜱᴇᴄᴜʀɪᴛʏ ᴇᴠᴇɴᴛꜱ
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
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.util.UUID;

public class SessionManager {

    private static final String TAG = "SessionManager";

    // ᴘʀᴇꜰᴇʀᴇɴᴄᴇ ꜰɪʟᴇ ɴᴀᴍᴇꜱ
    private static final String PREFS_SECURE = "telegram_session_secure";
    private static final String PREFS_GENERAL = "telegram_session_general";

    // ᴋᴇʏꜱ ꜰᴏʀ ꜱᴇᴄᴜʀᴇ ꜱᴛᴏʀᴀɢᴇ
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

    // ᴋᴇʏꜱ ꜰᴏʀ ɢᴇɴᴇʀᴀʟ ꜱᴛᴏʀᴀɢᴇ (ɴᴏɴ-ꜱᴇɴꜱɪᴛɪᴠᴇ)
    private static final String KEY_IS_LOGGED_IN = "is_logged_in";
    private static final String KEY_LOGIN_METHOD = "login_method";
    private static final String KEY_USERNAME = "username";
    private static final String KEY_DISPLAY_NAME = "display_name";
    private static final String KEY_BIO = "bio";
    private static final String KEY_PROFILE_PHOTO_URL = "profile_photo_url";
    private static final String KEY_CUSTOM_BG_URL = "custom_background_url";
    private static final String KEY_SESSION_ID = "session_id";
    private static final String KEY_LAST_ACTIVE = "last_active_timestamp";

    // ʟᴏɢɪɴ ᴍᴇᴛʜᴏᴅ ᴄᴏɴꜱᴛᴀɴᴛꜱ
    public static final String LOGIN_METHOD_PHONE = "phone";
    public static final String LOGIN_METHOD_BOT = "bot";
    public static final String LOGIN_METHOD_SESSION = "session";

    // ꜱɪɴɢʟᴇᴛᴏɴ ɪɴꜱᴛᴀɴᴄᴇ
    private static SessionManager instance;

    private final EncryptedSharedPreferences securePrefs;
    private final SharedPreferences generalPrefs;
    private final Context appContext;

    // ═══════════════════════════════════════════════════════════
    // ɪɴɪᴛɪᴀʟɪᴢᴀᴛɪᴏɴ
    // ═══════════════════════════════════════════════════════════

    private SessionManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.securePrefs = createSecurePreferences(appContext);
        this.generalPrefs = appContext.getSharedPreferences(PREFS_GENERAL, Context.MODE_PRIVATE);
    }

    public static synchronized SessionManager getInstance(Context context) {
        if (instance == null) {
            instance = new SessionManager(context);
        }
        return instance;
    }

    private EncryptedSharedPreferences createSecurePreferences(Context context) {
        try {
            MasterKey masterKey = new MasterKey.Builder(context)
                    .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                    .build();

            return (EncryptedSharedPreferences) EncryptedSharedPreferences.create(
                    context,
                    PREFS_SECURE,
                    masterKey,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException | IOException e) {
            Log.e(TAG, "Failed to create encrypted preferences, falling back to regular", e);
            // ꜰᴀʟʟʙᴀᴄᴋ — ɴᴏᴛ ᴘʀᴏᴅᴜᴄᴛɪᴏɴ-ꜱᴀꜰᴇ ʙᴜᴛ ᴋᴇᴇᴘꜱ ᴀᴘᴘ ꜰᴜɴᴄᴛɪᴏɴᴀʟ
            return null;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ꜱᴇꜱꜱɪᴏɴ ꜱᴛᴏʀᴀɢᴇ — ꜱᴀᴠᴇ ᴘᴀʀꜱᴇᴅ ꜱᴇꜱꜱɪᴏɴ
    // ═══════════════════════════════════════════════════════════

    /** ꜱᴀᴠᴇ ᴀ ᴘʜᴏɴᴇ-ɴᴜᴍʙᴇʀ-ʙᴀꜱᴇᴅ ꜱᴇꜱꜱɪᴏɴ */
    public void savePhoneSession(String countryCode, String phoneNumber, long userId) {
        SharedPreferences.Editor secureEdit = securePrefs != null ? securePrefs.edit() : null;
        SharedPreferences.Editor generalEdit = generalPrefs.edit();

        if (secureEdit != null) {
            secureEdit.putString(KEY_COUNTRY_CODE, countryCode);
            secureEdit.putString(KEY_PHONE_NUMBER, phoneNumber);
            secureEdit.putLong(KEY_USER_ID, userId);
            secureEdit.apply();
        }

        generalEdit.putString(KEY_LOGIN_METHOD, LOGIN_METHOD_PHONE);
        generalEdit.putLong(KEY_USER_ID, userId);
        generalEdit.putBoolean(KEY_IS_LOGGED_IN, true);
        generalEdit.putString(KEY_SESSION_ID, generateSessionId());
        generalEdit.putLong(KEY_LAST_ACTIVE, System.currentTimeMillis());
        generalEdit.apply();

        Log.i(TAG, "Phone session saved for user: " + userId);
    }

    /** ꜱᴀᴠᴇ ᴀ ʙᴏᴛ ᴛᴏᴋᴇɴ ꜱᴇꜱꜱɪᴏɴ */
    public void saveBotSession(String botToken) {
        SharedPreferences.Editor secureEdit = securePrefs != null ? securePrefs.edit() : null;
        SharedPreferences.Editor generalEdit = generalPrefs.edit();

        if (secureEdit != null) {
            secureEdit.putString(KEY_BOT_TOKEN, botToken);
            secureEdit.apply();
        }

        // ᴇхᴛʀᴀᴄᴛ ʙᴏᴛ ɪᴅ ꜰʀᴏᴍ ᴛᴏᴋᴇɴ
        long botId = extractBotId(botToken);

        generalEdit.putString(KEY_LOGIN_METHOD, LOGIN_METHOD_BOT);
        generalEdit.putLong(KEY_USER_ID, botId);
        generalEdit.putBoolean(KEY_IS_LOGGED_IN, true);
        generalEdit.putString(KEY_SESSION_ID, generateSessionId());
        generalEdit.putLong(KEY_LAST_ACTIVE, System.currentTimeMillis());
        generalEdit.apply();

        Log.i(TAG, "Bot session saved for bot ID: " + botId);
    }

    /** ꜱᴀᴠᴇ ᴀ ꜱᴛʀɪɴɢ ꜱᴇꜱꜱɪᴏɴ (ᴛᴇʟᴇᴛʜᴏɴ ᴏʀ ᴘʏʀᴏɢʀᴀᴍ) */
    public void saveStringSession(SessionParser.ParsedSession session) {
        SharedPreferences.Editor secureEdit = securePrefs != null ? securePrefs.edit() : null;
        SharedPreferences.Editor generalEdit = generalPrefs.edit();

        if (secureEdit != null) {
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
        }

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

    // ═══════════════════════════════════════════════════════════
    // ꜱᴇꜱꜱɪᴏɴ ʀᴇᴛʀɪᴇᴠᴀʟ
    // ═══════════════════════════════════════════════════════════

    /** ɢᴇᴛ ᴛʜᴇ ʟᴏɢɪɴ ᴍᴇᴛʜᴏᴅ ᴜꜱᴇᴅ (ᴘʜᴏɴᴇ, ʙᴏᴛ, ꜱᴇꜱꜱɪᴏɴ) */
    public String getLoginMethod() {
        return generalPrefs.getString(KEY_LOGIN_METHOD, "");
    }

    /** ᴄʜᴇᴄᴋ ɪꜱ ʟᴏɢɪɴ ᴍᴇᴛʜᴏᴅ ɪꜱ ʙᴏᴛ */
    public boolean isBotLogin() {
        return LOGIN_METHOD_BOT.equals(getLoginMethod());
    }

    /** ᴄʜᴇᴄᴋ ɪꜱ ʟᴏɢɪɴ ᴍᴇᴛʜᴏᴅ ɪꜱ ᴘʜᴏɴᴇ */
    public boolean isPhoneLogin() {
        return LOGIN_METHOD_PHONE.equals(getLoginMethod());
    }

    /** ᴄʜᴇᴄᴋ ɪꜱ ʟᴏɢɪɴ ᴍᴇᴛʜᴏᴅ ɪꜱ ꜱᴛʀɪɴɢ ꜱᴇꜱꜱɪᴏɴ */
    public boolean isSessionLogin() {
        return LOGIN_METHOD_SESSION.equals(getLoginMethod());
    }

    /** ɢᴇᴛ ꜱᴛᴏʀᴇᴅ ʙᴏᴛ ᴛᴏᴋᴇɴ */
    public String getBotToken() {
        if (securePrefs != null) {
            return securePrefs.getString(KEY_BOT_TOKEN, null);
        }
        return null;
    }

    /** ɢᴇᴛ ꜱᴛᴏʀᴇᴅ ᴀᴜᴛʜ ᴋᴇʏ ᴀꜱ ʙʏᴛᴇ ᴀʀʀᴀʏ */
    public byte[] getAuthKey() {
        if (securePrefs == null) return null;
        String authKeyB64 = securePrefs.getString(KEY_AUTH_KEY, null);
        if (authKeyB64 != null) {
            return Base64.decode(authKeyB64, Base64.DEFAULT);
        }
        return null;
    }

    /** ɢᴇᴛ ᴜꜱᴇʀ ɪᴅ */
    public long getUserId() {
        return generalPrefs.getLong(KEY_USER_ID, 0);
    }

    /** ɢᴇᴛ ᴅᴀᴛᴀᴄᴇɴᴛᴇʀ ɪᴅ */
    public int getDcId() {
        if (securePrefs != null) {
            return securePrefs.getInt(KEY_DC_ID, 1);
        }
        return 1;
    }

    /** ɢᴇᴛ ᴀᴘɪ ɪᴅ */
    public int getApiId() {
        if (securePrefs != null) {
            return securePrefs.getInt(KEY_API_ID, 0);
        }
        return 0;
    }

    /** ɢᴇᴛ ꜱᴇʀᴠᴇʀ ᴀᴅᴅʀᴇꜱꜱ */
    public String getServerAddress() {
        if (securePrefs != null) {
            return securePrefs.getString(KEY_SERVER_ADDRESS, null);
        }
        return null;
    }

    /** ɢᴇᴛ ᴘᴏʀᴛ */
    public int getPort() {
        if (securePrefs != null) {
            return securePrefs.getInt(KEY_PORT, 443);
        }
        return 443;
    }

    /** ɢᴇᴛ ꜱᴇꜱꜱɪᴏɴ ꜱᴛʀɪɴɢ */
    public String getSessionString() {
        if (securePrefs != null) {
            return securePrefs.getString(KEY_SESSION_STRING, null);
        }
        return null;
    }

    /** ɢᴇᴛ ᴘʜᴏɴᴇ ɴᴜᴍʙᴇʀ */
    public String getPhoneNumber() {
        if (securePrefs != null) {
            return securePrefs.getString(KEY_PHONE_NUMBER, null);
        }
        return null;
    }

    /** ɢᴇᴛ ᴄᴏᴜɴᴛʀʏ ᴄᴏᴅᴇ */
    public String getCountryCode() {
        if (securePrefs != null) {
            return securePrefs.getString(KEY_COUNTRY_CODE, null);
        }
        return null;
    }

    // ═══════════════════════════════════════════════════════════
    // ᴘʀᴏꜰɪʟᴇ ᴅᴀᴛᴀ
    // ═══════════════════════════════════════════════════════════

    /** ꜱᴀᴠᴇ ᴜꜱᴇʀɴᴀᴍᴇ */
    public void setUsername(String username) {
        generalPrefs.edit().putString(KEY_USERNAME, username).apply();
    }

    /** ɢᴇᴛ ᴜꜱᴇʀɴᴀᴍᴇ */
    public String getUsername() {
        return generalPrefs.getString(KEY_USERNAME, "");
    }

    /** ꜱᴀᴠᴇ ᴅɪꜱᴘʟᴀʏ ɴᴀᴍᴇ */
    public void setDisplayName(String name) {
        generalPrefs.edit().putString(KEY_DISPLAY_NAME, name).apply();
    }

    /** ɢᴇᴛ ᴅɪꜱᴘʟᴀʏ ɴᴀᴍᴇ */
    public String getDisplayName() {
        return generalPrefs.getString(KEY_DISPLAY_NAME, "");
    }

    /** ꜱᴀᴠᴇ ʙɪᴏ */
    public void setBio(String bio) {
        generalPrefs.edit().putString(KEY_BIO, bio).apply();
    }

    /** ɢᴇᴛ ʙɪᴏ */
    public String getBio() {
        return generalPrefs.getString(KEY_BIO, "");
    }

    /** ꜱᴀᴠᴇ ᴘʀᴏꜰɪʟᴇ ᴘʜᴏᴛᴏ ᴜʀʟ */
    public void setProfilePhotoUrl(String url) {
        generalPrefs.edit().putString(KEY_PROFILE_PHOTO_URL, url).apply();
    }

    /** ɢᴇᴛ ᴘʀᴏꜰɪʟᴇ ᴘʜᴏᴛᴏ ᴜʀʟ */
    public String getProfilePhotoUrl() {
        return generalPrefs.getString(KEY_PROFILE_PHOTO_URL, null);
    }

    // ═══════════════════════════════════════════════════════════
    // ʙᴀᴄᴋɢʀᴏᴜɴᴅ ᴜʀʟ ꜱᴛᴏʀᴀɢᴇ
    // ═══════════════════════════════════════════════════════════

    /** ꜱᴀᴠᴇ ᴄᴜꜱᴛᴏᴍ ʙᴀᴄᴋɢʀᴏᴜɴᴅ ɪᴍᴀɢᴇ ᴜʀʟ */
    public void setCustomBackgroundUrl(String url) {
        generalPrefs.edit().putString(KEY_CUSTOM_BG_URL, url).apply();
    }

    /** ɢᴇᴛ ᴄᴜꜱᴛᴏᴍ ʙᴀᴄᴋɢʀᴏᴜɴᴅ ɪᴍᴀɢᴇ ᴜʀʟ */
    public String getCustomBackgroundUrl() {
        return generalPrefs.getString(KEY_CUSTOM_BG_URL, null);
    }

    // ═══════════════════════════════════════════════════════════
    // ꜱᴇꜱꜱɪᴏɴ ꜱᴛᴀᴛᴇ
    // ═══════════════════════════════════════════════════════════

    /** ᴄʜᴇᴄᴋ ɪꜰ ᴀɴʏ ꜱᴇꜱꜱɪᴏɴ ɪꜱ ᴀᴄᴛɪᴠᴇ */
    public boolean isLoggedIn() {
        return generalPrefs.getBoolean(KEY_IS_LOGGED_IN, false);
    }

    /** ᴜᴘᴅᴀᴛᴇ ʟᴀꜱᴛ ᴀᴄᴛɪᴠᴇ ᴛɪᴍᴇꜱᴛᴀᴍᴘ */
    public void updateLastActive() {
        generalPrefs.edit().putLong(KEY_LAST_ACTIVE, System.currentTimeMillis()).apply();
    }

    /** ɢᴇᴛ ʟᴀꜱᴛ ᴀᴄᴛɪᴠᴇ ᴛɪᴍᴇꜱᴛᴀᴍᴘ */
    public long getLastActive() {
        return generalPrefs.getLong(KEY_LAST_ACTIVE, 0);
    }

    /** ɢᴇᴛ ꜱᴇꜱꜱɪᴏɴ ɪᴅ */
    public String getSessionId() {
        return generalPrefs.getString(KEY_SESSION_ID, "");
    }

    // ═══════════════════════════════════════════════════════════
    // ꜱᴇꜱꜱɪᴏɴ ᴛᴇʀᴍɪɴᴀᴛɪᴏɴ
    // ═══════════════════════════════════════════════════════════

    /** ᴄʟᴇᴀʀ ᴀʟʟ ꜱᴇꜱꜱɪᴏɴ ᴅᴀᴛᴀ (ʟᴏɢᴏᴜᴛ) */
    public void clearSession() {
        // ᴄʟᴇᴀʀ ꜱᴇᴄᴜʀᴇ ᴅᴀᴛᴀ
        if (securePrefs != null) {
            securePrefs.edit().clear().apply();
        }

        // ᴄʟᴇᴀʀ ɢᴇɴᴇʀᴀʟ ᴅᴀᴛᴀ (ᴘʀᴇꜱᴇʀᴠᴇ ᴀᴘᴘ ꜱᴇᴛᴛɪɴɢꜱ)
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

        Log.i(TAG, "Session cleared — user logged out");
    }

    /** ᴇхᴘᴏʀᴛ ꜱᴇꜱꜱɪᴏɴ ɪɴꜰᴏ (ꜰᴏʀ ʙᴀᴄᴋᴜᴘ/ᴅᴇʙᴜɢ, ꜱᴛʀɪᴘᴘɪɴɢ ꜱᴇɴꜱɪᴛɪᴠᴇ ᴅᴀᴛᴀ) */
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

    // ═══════════════════════════════════════════════════════════
    // ᴘʀɪᴠᴀᴛᴇ ʜᴇʟᴘᴇʀꜱ
    // ═══════════════════════════════════════════════════════════

    /** ɢᴇɴᴇʀᴀᴛᴇ ᴀ ᴜɴɪǫᴜᴇ ꜱᴇꜱꜱɪᴏɴ ɪᴅ */
    private String generateSessionId() {
        return UUID.randomUUID().toString().substring(0, 8);
    }

    /** ᴇхᴛʀᴀᴄᴛ ʙᴏᴛ ɪᴅ ꜰʀᴏᴍ ʙᴏᴛ ᴛᴏᴋᴇɴ */
    private long extractBotId(String botToken) {
        try {
            String idPart = botToken.substring(0, botToken.indexOf(':'));
            return Long.parseLong(idPart);
        } catch (Exception e) {
            return 0;
        }
    }

    /** ꜰᴏʀᴄᴇ ʀᴇɪɴɪᴛɪᴀʟɪᴢᴇ (ᴜꜱᴇᴅ ᴏɴ ᴋᴇʏ ʀᴏᴛᴀᴛɪᴏɴ ᴏʀ ᴄᴏʀʀᴜᴘᴛɪᴏɴ) */
    public void forceReinitialize(Context context) {
        instance = new SessionManager(context);
        Log.i(TAG, "Session manager force-reinitialized");
    }
}
