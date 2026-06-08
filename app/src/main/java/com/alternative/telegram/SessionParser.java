/*
 * ꜱᴇꜱꜱɪᴏɴᴘᴀʀꜱᴇʀ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ᴅᴜᴀʟ ᴀᴜᴛᴏ-ᴅᴇᴛᴇᴄᴛɪᴏɴ ᴇɴɢɪɴᴇ ꜰᴏʀ ᴛᴇʟᴇɢʀᴀᴍ ꜱᴇꜱꜱɪᴏɴ ꜱᴛʀɪɴɢꜱ
 *
 * ꜱᴜᴘᴘᴏʀᴛꜱ ᴛᴡᴏ ꜰᴏʀᴍᴀᴛꜱ:
 *   1. ᴛᴇʟᴇᴛʜᴏɴ: ꜱʜᴏʀᴛᴇʀ, ᴀʟᴘʜᴀɴᴜᴍᴇʀɪᴄ-ʙᴀꜱᴇ64 ꜱᴛʀɪɴɢꜱ
 *   2. ᴘʏʀᴏɢʀᴀᴍ: ʟᴏɴɢᴇʀ ʙᴀꜱᴇ64 (351+ ᴄʜᴀʀꜱ) ᴡɪᴛʜ ᴜꜱᴇʀ ɪᴅ, ᴀᴘɪ ɪᴅ, ᴅᴄ ɪᴅ, ᴀᴜᴛʜ ᴋᴇʏ
 *
 * ᴅᴇᴛᴇᴄᴛɪᴏɴ ʟᴏɢɪᴄ:
 *   - ʟᴇɴɢᴛʜ < 200: ᴛᴇʟᴇᴛʜᴏɴ
 *   - ʟᴇɴɢᴛʜ >= 351: ᴘʏʀᴏɢʀᴀᴍ
 *   - ʙᴀꜱᴇ64 ᴅᴇᴄᴏᴅɪɴɢ ᴠᴀʟɪᴅᴀᴛɪᴏɴ
 *   - ꜱᴛʀᴜᴄᴛᴜʀᴀʟ ᴘᴀᴛᴛᴇʀɴ ᴍᴀᴛᴄʜɪɴɢ
 */

package com.alternative.telegram;

import android.util.Base64;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.security.MessageDigest;
import java.util.Arrays;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SessionParser {

    private static final String TAG = "SessionParser";

    // ᴛᴇʟᴇɢʀᴀᴍ ᴅᴀᴛᴀᴄᴇɴᴛᴇʀ ɪᴘ ᴀᴅᴅʀᴇꜱꜱᴇꜱ
    private static final String[] DC_ADDRESSES = {
        "149.154.167.50",   // ᴅᴄ 1
        "149.154.167.51",   // ᴅᴄ 2
        "149.154.175.100",  // ᴅᴄ 3
        "149.154.167.91",   // ᴅᴄ 4
        "91.108.56.100",    // ᴅᴄ 5
    };

    // ᴘʏʀᴏɢʀᴀᴍ ꜱᴇꜱꜱɪᴏɴ ꜱɪɢɴᴀᴛᴜʀᴇ: "BQ" ᴘʀᴇꜰɪx ɪɴ ʙᴀꜱᴇ64
    private static final byte[] PYROGRAM_SIGNATURE = {0x05, 0x01};

    // ᴛᴇʟᴇᴛʜᴏɴ ꜱᴇꜱꜱɪᴏɴ ᴘᴀᴛᴛᴇʀɴ: 1ꜱᴛ ʙʏᴛᴇ ɪꜱ ᴠᴇʀꜱɪᴏɴ ɪᴅᴇɴᴛɪꜰɪᴇʀ
    private static final byte[] TELETHON_SIGNATURE_V1 = {0x01};

    // ᴘᴀᴛᴛᴇʀɴ ꜰᴏʀ ʙᴏᴛ ᴛᴏᴋᴇɴ ᴠᴀʟɪᴅᴀᴛɪᴏɴ
    private static final Pattern BOT_TOKEN_PATTERN = Pattern.compile(
            "^\\d+:[A-Za-z0-9_-]{35,}$"
    );

    // ᴘᴀᴛᴛᴇʀɴ ꜰᴏʀ ᴘʜᴏɴᴇ ɴᴜᴍʙᴇʀ ᴠᴀʟɪᴅᴀᴛɪᴏɴ (ᴇ.164 ꜰᴏʀᴍᴀᴛ)
    private static final Pattern PHONE_E164_PATTERN = Pattern.compile(
            "^\\+?[1-9]\\d{1,14}$"
    );

    // ═══════════════════════════════════════════════════════════
    // ꜱᴇꜱꜱɪᴏɴ ᴛʏᴘᴇ ᴇɴᴜᴍ
    // ═══════════════════════════════════════════════════════════

    public enum SessionType {
        UNKNOWN,
        TELETHON,       // ᴛᴇʟᴇᴛʜᴏɴ ꜱᴛʀɪɴɢ ꜱᴇꜱꜱɪᴏɴ
        PYROGRAM,       // ᴘʏʀᴏɢʀᴀᴍ ꜱᴛʀɪɴɢ ꜱᴇꜱꜱɪᴏɴ
        BOT_TOKEN,      // ʙᴏᴛ ᴀᴘɪ ᴛᴏᴋᴇɴ
        PHONE_NUMBER    // ᴘʜᴏɴᴇ ɴᴜᴍʙᴇʀ ʟᴏɢɪɴ
    }

    // ═══════════════════════════════════════════════════════════
    // ᴘᴀʀꜱᴇᴅ ꜱᴇꜱꜱɪᴏɴ ᴅᴀᴛᴀ ʜᴏʟᴅᴇʀ
    // ═══════════════════════════════════════════════════════════

    public static class ParsedSession {
        public final SessionType type;
        public final String rawInput;
        public final int dataCenterId;
        public final long userId;
        public final int apiId;
        public final byte[] authKey;
        public final String serverAddress;
        public final int port;
        public final boolean isValid;
        public final String errorMessage;
        public final String botToken;     // ꜰᴏʀ ʙᴏᴛ ᴛᴏᴋᴇɴ ʟᴏɢɪɴ
        public final String phoneNumber;  // ꜰᴏʀ ᴘʜᴏɴᴇ ʟᴏɢɪɴ
        public final String countryCode;  // ꜰᴏʀ ᴘʜᴏɴᴇ ʟᴏɢɪɴ

        // ᴘʏʀᴏɢʀᴀᴍ/ᴛᴇʟᴇᴛʜᴏɴ ᴄᴏɴꜱᴛʀᴜᴄᴛᴏʀ
        public ParsedSession(SessionType type, String rawInput, int dataCenterId,
                             long userId, int apiId, byte[] authKey,
                             String serverAddress, int port) {
            this.type = type;
            this.rawInput = rawInput;
            this.dataCenterId = dataCenterId;
            this.userId = userId;
            this.apiId = apiId;
            this.authKey = authKey;
            this.serverAddress = serverAddress;
            this.port = port;
            this.isValid = true;
            this.errorMessage = null;
            this.botToken = null;
            this.phoneNumber = null;
            this.countryCode = null;
        }

        // ʙᴏᴛ ᴛᴏᴋᴇɴ ᴄᴏɴꜱᴛʀᴜᴄᴛᴏʀ
        public ParsedSession(String botToken) {
            this.type = SessionType.BOT_TOKEN;
            this.rawInput = botToken;
            this.botToken = botToken;
            this.isValid = true;
            this.errorMessage = null;
            this.dataCenterId = 0;
            this.userId = 0;
            this.apiId = 0;
            this.authKey = null;
            this.serverAddress = null;
            this.port = 0;
            this.phoneNumber = null;
            this.countryCode = null;
        }

        // ᴘʜᴏɴᴇ ɴᴜᴍʙᴇʀ ᴄᴏɴꜱᴛʀᴜᴄᴛᴏʀ
        public ParsedSession(String countryCode, String phoneNumber) {
            this.type = SessionType.PHONE_NUMBER;
            this.rawInput = countryCode + phoneNumber;
            this.countryCode = countryCode;
            this.phoneNumber = phoneNumber;
            this.isValid = true;
            this.errorMessage = null;
            this.dataCenterId = 0;
            this.userId = 0;
            this.apiId = 0;
            this.authKey = null;
            this.serverAddress = null;
            this.port = 0;
            this.botToken = null;
        }

        // ᴇʀʀᴏʀ ᴄᴏɴꜱᴛʀᴜᴄᴛᴏʀ
        public ParsedSession(String rawInput, String errorMessage) {
            this.type = SessionType.UNKNOWN;
            this.rawInput = rawInput;
            this.errorMessage = errorMessage;
            this.isValid = false;
            this.dataCenterId = 0;
            this.userId = 0;
            this.apiId = 0;
            this.authKey = null;
            this.serverAddress = null;
            this.port = 0;
            this.botToken = null;
            this.phoneNumber = null;
            this.countryCode = null;
        }

        @Override
        public String toString() {
            if (!isValid) {
                return "ParsedSession{invalid, error='" + errorMessage + "'}";
            }
            return "ParsedSession{type=" + type
                    + ", dc=" + dataCenterId
                    + ", userId=" + userId
                    + ", apiId=" + apiId
                    + ", server=" + serverAddress
                    + ", port=" + port
                    + ", botToken=" + (botToken != null ? "***" : "null")
                    + ", phone=" + (phoneNumber != null ? "***" : "null")
                    + "}";
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ᴍᴀɪɴ ᴀᴜᴛᴏ-ᴅᴇᴛᴇᴄᴛɪᴏɴ ᴇɴᴛʀʏ ᴘᴏɪɴᴛ
    // ═══════════════════════════════════════════════════════════

    /**
     * ᴀᴜᴛᴏᴍᴀᴛɪᴄᴀʟʟʏ ᴅᴇᴛᴇᴄᴛ ᴀɴᴅ ᴘᴀʀꜱᴇ ᴀɴʏ ꜱᴇꜱꜱɪᴏɴ ꜱᴛʀɪɴɢ ᴛʏᴘᴇ
     * @param input ʀᴀᴡ ꜱᴇꜱꜱɪᴏɴ ꜱᴛʀɪɴɢ ꜰʀᴏᴍ ᴜꜱᴇʀ ɪɴᴘᴜᴛ
     * @return ᴘᴀʀꜱᴇᴅꜱᴇꜱꜱɪᴏɴ ᴡɪᴛʜ ᴀʟʟ ᴇхᴛʀᴀᴄᴛᴇᴅ ꜰɪᴇʟᴅꜱ
     */
    public static ParsedSession autoDetect(String input) {
        if (input == null || input.trim().isEmpty()) {
            return new ParsedSession(input, "Empty session string");
        }

        String trimmed = input.trim();
        Log.d(TAG, "Auto-detecting session type for input length: " + trimmed.length());

        // ʟᴇɴɢᴛʜ-ʙᴀꜱᴇᴅ ʜᴇᴜʀɪꜱᴛɪᴄ
        if (trimmed.length() >= 351) {
            // ʟᴏɴɢ ꜱᴛʀɪɴɢ: ᴍᴏꜱᴛ ʟɪᴋᴇʟʏ ᴘʏʀᴏɢʀᴀᴍ
            Log.d(TAG, "Length >= 351, trying Pyrogram format...");
            ParsedSession pyrogramResult = parsePyrogram(trimmed);
            if (pyrogramResult.isValid) {
                return pyrogramResult;
            }
            // ꜰᴀʟʟʙᴀᴄᴋ: ᴛʀʏ ᴛᴇʟᴇᴛʜᴏɴ ᴡɪᴛʜ ꜰᴜʟʟ ꜱᴛʀɪɴɢ
            Log.d(TAG, "Pyrogram parse failed, trying Telethon fallback...");
            return parseTelethon(trimmed);
        } else if (trimmed.length() > 50 && trimmed.length() < 351) {
            // ᴍᴇᴅɪᴜᴍ ʟᴇɴɢᴛʜ: ᴛʀʏ ᴛᴇʟᴇᴛʜᴏɴ ꜰɪʀꜱᴛ
            Log.d(TAG, "Length 50-350, trying Telethon format...");
            ParsedSession telethonResult = parseTelethon(trimmed);
            if (telethonResult.isValid) {
                return telethonResult;
            }
            // ꜰᴀʟʟʙᴀᴄᴋ: ᴛʀʏ ᴘʏʀᴏɢʀᴀᴍ
            Log.d(TAG, "Telethon parse failed, trying Pyrogram fallback...");
            return parsePyrogram(trimmed);
        } else {
            // ꜱʜᴏʀᴛ ꜱᴛʀɪɴɢ: ᴛᴇʟᴇᴛʜᴏɴ ᴏɴʟʏ
            Log.d(TAG, "Length < 50, trying Telethon format...");
            return parseTelethon(trimmed);
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ᴛᴇʟᴇᴛʜᴏɴ ꜱᴇꜱꜱɪᴏɴ ᴘᴀʀꜱɪɴɢ
    // ═══════════════════════════════════════════════════════════

    /**
     * ᴘᴀʀꜱᴇ ᴛᴇʟᴇᴛʜᴏɴ ꜱᴛʀɪɴɢ ꜱᴇꜱꜱɪᴏɴ ꜰᴏʀᴍᴀᴛ
     * ꜱᴛʀᴜᴄᴛᴜʀᴇ: ʙᴀꜱᴇ64(ᴠᴇʀꜱɪᴏɴ + ᴅᴄ_ɪᴅ + ᴀᴘɪ_ɪᴅ + ᴀᴜᴛʜ_ᴋᴇʏ)
     */
    public static ParsedSession parseTelethon(String input) {
        try {
            String trimmed = input.trim();

            // ᴅᴇᴄᴏᴅᴇ ʙᴀꜱᴇ64
            byte[] decoded;
            try {
                decoded = Base64.decode(trimmed, Base64.DEFAULT);
            } catch (IllegalArgumentException e) {
                return new ParsedSession(input, "Invalid Base64 encoding");
            }

            if (decoded.length < 25) {
                return new ParsedSession(input, "Telethon session too short (need 25+ bytes)");
            }

            // ᴘᴀʀꜱᴇ ʙʏᴛᴇꜱ
            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            buffer.order(ByteOrder.LITTLE_ENDIAN);

            // ʙʏᴛᴇ 0: ᴠᴇʀꜱɪᴏɴ (ᴍᴜꜱᴛ ʙᴇ 0x01)
            int version = buffer.get() & 0xFF;
            if (version != 1) {
                return new ParsedSession(input, "Unknown Telethon version: " + version);
            }

            // ʙʏᴛᴇ 1: ᴅᴀᴛᴀᴄᴇɴᴛᴇʀ ɪᴅ
            int dcId = buffer.get() & 0xFF;

            // ʙʏᴛᴇꜱ 2-5: ᴀᴘɪ ɪᴅ (ɪɴᴛ32 ʟᴇ)
            int apiId = buffer.getInt();

            // ʙʏᴛᴇꜱ 6-9: ᴛᴇꜱᴛ ꜱᴇʀᴠᴇʀ (ʙᴏᴏʟᴇᴀɴ ᴀꜱ ɪɴᴛ32)
            int testMode = buffer.getInt();

            // ʙʏᴛᴇꜱ 10-13: ᴀᴜᴛʜ ᴋᴇʏ ʟᴇɴɢᴛʜ (ꜱʜᴏᴜʟᴅ ʙᴇ 256)
            int authKeyLen = buffer.getInt();
            if (authKeyLen != 256) {
                return new ParsedSession(input, "Invalid auth key length: " + authKeyLen);
            }

            // ʀᴇᴍᴀɪɴɪɴɢ ʙʏᴛᴇꜱ: ᴀᴜᴛʜ ᴋᴇʏ
            byte[] authKey = new byte[authKeyLen];
            buffer.get(authKey);

            // ʙʏᴛᴇꜱ ᴀꜰᴛᴇʀ ᴀᴜᴛʜ ᴋᴇʏ: ᴜꜱᴇʀ ɪᴅ (ɪɴᴛ64)
            long userId = 0;
            if (buffer.remaining() >= 8) {
                userId = buffer.getLong();
            }

            // ʀᴇꜱᴏʟᴠᴇ ꜱᴇʀᴠᴇʀ ᴀᴅᴅʀᴇꜱꜱ
            String serverAddress = getDcAddress(dcId);
            int port = (testMode != 0) ? 443 : 443;

            Log.i(TAG, "Telethon session parsed: dc=" + dcId + ", apiId=" + apiId + ", userId=" + userId);

            return new ParsedSession(SessionType.TELETHON, trimmed, dcId, userId, apiId,
                    authKey, serverAddress, port);

        } catch (Exception e) {
            Log.e(TAG, "Telethon parse error", e);
            return new ParsedSession(input, "Telethon parse error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ᴘʏʀᴏɢʀᴀᴍ ꜱᴇꜱꜱɪᴏɴ ᴘᴀʀꜱɪɴɢ
    // ═══════════════════════════════════════════════════════════

    /**
     * ᴘᴀʀꜱᴇ ᴘʏʀᴏɢʀᴀᴍ ꜱᴛʀɪɴɢ ꜱᴇꜱꜱɪᴏɴ ꜰᴏʀᴍᴀᴛ (351+ ᴄʜᴀʀᴀᴄᴛᴇʀꜱ)
     * ꜱᴛʀᴜᴄᴛᴜʀᴇ: ʙᴀꜱᴇ64(ꜱɪɢɴᴀᴛᴜʀᴇ + ᴅᴄ_ɪᴅ + ᴀᴘɪ_ɪᴅ + ᴛᴇꜱᴛ_ᴍᴏᴅᴇ + ᴜꜱᴇʀ_ɪᴅ + ɪꜱ_ʙᴏᴛ + ᴅᴄ_ɪᴘ + ᴅᴄ_ᴘᴏʀᴛ + ᴀᴜᴛʜ_ᴋᴇʏ)
     */
    public static ParsedSession parsePyrogram(String input) {
        try {
            String trimmed = input.trim();

            // ᴅᴇᴄᴏᴅᴇ ʙᴀꜱᴇ64
            byte[] decoded;
            try {
                decoded = Base64.decode(trimmed, Base64.URL_SAFE | Base64.DEFAULT);
            } catch (IllegalArgumentException e) {
                // ᴛʀʏ ꜱᴛᴀɴᴅᴀʀᴅ ʙᴀꜱᴇ64
                try {
                    decoded = Base64.decode(trimmed, Base64.DEFAULT);
                } catch (IllegalArgumentException e2) {
                    return new ParsedSession(input, "Invalid Base64 encoding");
                }
            }

            if (decoded.length < 253) {
                return new ParsedSession(input, "Pyrogram session too short (need 253+ bytes, got " + decoded.length + ")");
            }

            ByteBuffer buffer = ByteBuffer.wrap(decoded);
            buffer.order(ByteOrder.BIG_ENDIAN);

            // ᴠᴀʟɪᴅᴀᴛᴇ ꜱɪɢɴᴀᴛᴜʀᴇ (ꜰɪʀꜱᴛ 2 ʙʏᴛᴇꜱ: 0x05 0x01)
            byte sig1 = buffer.get();
            byte sig2 = buffer.get();
            if (sig1 != 0x05 || sig2 != 0x01) {
                // ᴛʀʏ ʟɪᴛᴛʟᴇ-ᴇɴᴅɪᴀɴ
                buffer.order(ByteOrder.LITTLE_ENDIAN);
                buffer.position(0);
                sig1 = buffer.get();
                sig2 = buffer.get();
                if (sig1 != 0x05 || sig2 != 0x01) {
                    return new ParsedSession(input, "Invalid Pyrogram signature");
                }
            }

            // ᴅᴀᴛᴀᴄᴇɴᴛᴇʀ ɪᴅ (1 ʙʏᴛᴇ)
            int dcId = buffer.get() & 0xFF;

            // ᴀᴘɪ ɪᴅ (4 ʙʏᴛᴇꜱ)
            int apiId = buffer.getInt();

            // ᴛᴇꜱᴛ ᴍᴏᴅᴇ (1 ʙʏᴛᴇ)
            int testMode = buffer.get() & 0xFF;

            // ᴜꜱᴇʀ ɪᴅ (8 ʙʏᴛᴇꜱ, ꜱɪɢɴᴇᴅ)
            long userId = buffer.getLong();

            // ɪꜱ ʙᴏᴛ (1 ʙʏᴛᴇ)
            int isBot = buffer.get() & 0xFF;

            // ᴅᴄ ɪᴘ (4 ʙʏᴛᴇꜱ — ɪɴᴛ32 ʙɪɢ-ᴇɴᴅɪᴀɴ)
            byte[] ipBytes = new byte[4];
            buffer.get(ipBytes);
            String ipAddress = (ipBytes[0] & 0xFF) + "." +
                    (ipBytes[1] & 0xFF) + "." +
                    (ipBytes[2] & 0xFF) + "." +
                    (ipBytes[3] & 0xFF);

            // ᴅᴄ ᴘᴏʀᴛ (2 ʙʏᴛᴇꜱ)
            int port = buffer.getShort() & 0xFFFF;

            // ᴀᴜᴛʜ ᴋᴇʏ (256 ʙʏᴛᴇꜱ)
            byte[] authKey = new byte[256];
            buffer.get(authKey);

            Log.i(TAG, "Pyrogram session parsed: dc=" + dcId + ", apiId=" + apiId
                    + ", userId=" + userId + ", isBot=" + isBot
                    + ", ip=" + ipAddress + ", port=" + port);

            return new ParsedSession(SessionType.PYROGRAM, trimmed, dcId, userId, apiId,
                    authKey, ipAddress, port);

        } catch (Exception e) {
            Log.e(TAG, "Pyrogram parse error", e);
            return new ParsedSession(input, "Pyrogram parse error: " + e.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ʙᴏᴛ ᴛᴏᴋᴇɴ ᴠᴀʟɪᴅᴀᴛɪᴏɴ
    // ═══════════════════════════════════════════════════════════

    /**
     * ᴠᴀʟɪᴅᴀᴛᴇ ᴀɴᴅ ᴘᴀʀꜱᴇ ʙᴏᴛ ᴛᴏᴋᴇɴ ꜰᴏʀᴍᴀᴛ: ɴᴜᴍʙᴇʀꜱ:ᴀʟᴘʜᴀɴᴜᴍᴇʀɪᴄ
     */
    public static ParsedSession parseBotToken(String token) {
        if (token == null || token.trim().isEmpty()) {
            return new ParsedSession(token, "Empty bot token");
        }

        String trimmed = token.trim();
        Matcher matcher = BOT_TOKEN_PATTERN.matcher(trimmed);
        if (!matcher.matches()) {
            return new ParsedSession(token, "Invalid bot token format. Expected: numbers:alphanumeric");
        }

        // ᴇхᴛʀᴀᴄᴛ ʙᴏᴛ ɪᴅ ꜰʀᴏᴍ ᴛᴏᴋᴇɴ (ᴘᴀʀᴛ ʙᴇꜰᴏʀᴇ ᴄᴏʟᴏɴ)
        try {
            String botIdStr = trimmed.substring(0, trimmed.indexOf(':'));
            long botId = Long.parseLong(botIdStr);
            Log.i(TAG, "Bot token validated for bot ID: " + botId);
        } catch (NumberFormatException e) {
            return new ParsedSession(token, "Invalid bot ID in token");
        }

        return new ParsedSession(trimmed);
    }

    /**
     * ᴄʜᴇᴄᴋ ɪꜰ ɪɴᴘᴜᴛ ʟᴏᴏᴋꜱ ʟɪᴋᴇ ᴀ ʙᴏᴛ ᴛᴏᴋᴇɴ
     */
    public static boolean isBotTokenFormat(String input) {
        if (input == null) return false;
        return BOT_TOKEN_PATTERN.matcher(input.trim()).matches();
    }

    // ═══════════════════════════════════════════════════════════
    // ᴘʜᴏɴᴇ ɴᴜᴍʙᴇʀ ᴠᴀʟɪᴅᴀᴛɪᴏɴ
    // ═══════════════════════════════════════════════════════════

    /**
     * ᴠᴀʟɪᴅᴀᴛᴇ ᴀɴᴅ ᴘᴀʀꜱᴇ ᴘʜᴏɴᴇ ɴᴜᴍʙᴇʀ ɪɴ ᴇ.164 ꜰᴏʀᴍᴀᴛ
     */
    public static ParsedSession parsePhoneNumber(String countryCode, String phoneNumber) {
        if (countryCode == null || countryCode.trim().isEmpty()) {
            return new ParsedSession("", "Country code is required");
        }
        if (phoneNumber == null || phoneNumber.trim().isEmpty()) {
            return new ParsedSession("", "Phone number is required");
        }

        // ᴄʟᴇᴀɴ ᴄᴏᴜɴᴛʀʏ ᴄᴏᴅᴇ
        String cc = countryCode.trim();
        if (!cc.startsWith("+")) {
            cc = "+" + cc;
        }
        // ʀᴇᴍᴏᴠᴇ ɴᴏɴ-ᴅɪɢɪᴛꜱ ᴇхᴄᴇᴘᴛ +
        cc = "+" + cc.substring(1).replaceAll("[^0-9]", "");

        // ᴄʟᴇᴀɴ ᴘʜᴏɴᴇ ɴᴜᴍʙᴇʀ
        String phone = phoneNumber.trim().replaceAll("[^0-9]", "");

        // ᴠᴀʟɪᴅᴀᴛᴇ
        String fullNumber = cc + phone;
        Matcher matcher = PHONE_E164_PATTERN.matcher(fullNumber);
        if (!matcher.matches()) {
            return new ParsedSession(fullNumber, "Invalid phone number format");
        }

        Log.i(TAG, "Phone number parsed: " + cc + " " + maskPhone(phone));
        return new ParsedSession(cc, phone);
    }

    /**
     * ᴍᴀꜱᴋ ᴘʜᴏɴᴇ ɴᴜᴍʙᴇʀ ꜰᴏʀ ʟᴏɢɢɪɴɢ (ᴘʀɪᴠᴀᴄʏ)
     */
    private static String maskPhone(String phone) {
        if (phone.length() < 4) return "***";
        return phone.substring(0, 2) + "***" + phone.substring(phone.length() - 2);
    }

    // ═══════════════════════════════════════════════════════════
    // ᴜᴛɪʟɪᴛʏ ᴍᴇᴛʜᴏᴅꜱ
    // ═══════════════════════════════════════════════════════════

    /**
     * ɢᴇᴛ ᴛᴇʟᴇɢʀᴀᴍ ᴅᴄ ɪᴘ ᴀᴅᴅʀᴇꜱꜱ ʙʏ ɪᴅ
     */
    private static String getDcAddress(int dcId) {
        if (dcId >= 1 && dcId <= DC_ADDRESSES.length) {
            return DC_ADDRESSES[dcId - 1];
        }
        return DC_ADDRESSES[0]; // ꜰᴀʟʟʙᴀᴄᴋ ᴛᴏ ᴅᴄ 1
    }

    /**
     * ᴄᴀʟᴄᴜʟᴀᴛᴇ ꜱᴇꜱꜱɪᴏɴ ꜰɪɴɢᴇʀᴘʀɪɴᴛ ꜰᴏʀ ᴠᴇʀɪꜰɪᴄᴀᴛɪᴏɴ
     */
    public static String calculateFingerprint(byte[] authKey) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(authKey);
            // ʀᴇᴛᴜʀɴ ꜰɪʀꜱᴛ 8 ʙʏᴛᴇꜱ ᴀꜱ ʜᴇх ꜰᴏʀ ᴅɪꜱᴘʟᴀʏ
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < 8 && i < hash.length; i++) {
                sb.append(String.format("%02X", hash[i]));
            }
            return sb.toString();
        } catch (Exception e) {
            return "UNKNOWN";
        }
    }

    /**
     * ǫᴜɪᴄᴋ ꜱᴇꜱꜱɪᴏɴ ᴛʏᴘᴇ ᴅᴇᴛᴇᴄᴛɪᴏɴ ᴡɪᴛʜᴏᴜᴛ ꜰᴜʟʟ ᴘᴀʀꜱɪɴɢ
     */
    public static SessionType detectType(String input) {
        if (input == null || input.trim().isEmpty()) {
            return SessionType.UNKNOWN;
        }
        String trimmed = input.trim();
        if (trimmed.length() >= 351) {
            return SessionType.PYROGRAM;
        } else if (isBotTokenFormat(trimmed)) {
            return SessionType.BOT_TOKEN;
        } else if (trimmed.length() > 20) {
            return SessionType.TELETHON;
        }
        return SessionType.UNKNOWN;
    }

    /**
     * ᴠᴀʟɪᴅᴀᴛᴇ ꜱᴇꜱꜱɪᴏɴ ᴡɪᴛʜᴏᴜᴛ ꜰᴜʟʟ ᴘᴀʀꜱᴇ
     */
    public static boolean isValidSessionString(String input) {
        ParsedSession result = autoDetect(input);
        return result.isValid;
    }

    /**
     * ɢᴇᴛ ʜᴜᴍᴀɴ-ʀᴇᴀᴅᴀʙʟᴇ ᴅᴇꜱᴄʀɪᴘᴛɪᴏɴ ᴏꜰ ᴅᴇᴛᴇᴄᴛᴇᴅ ꜱᴇꜱꜱɪᴏɴ ᴛʏᴘᴇ
     */
    public static String getTypeDescription(SessionType type) {
        switch (type) {
            case TELETHON:
                return "Telethon Session";
            case PYROGRAM:
                return "Pyrogram Session";
            case BOT_TOKEN:
                return "Bot Token";
            case PHONE_NUMBER:
                return "Phone Number";
            default:
                return "Unknown";
        }
    }
}
