/*
 * ꜱᴍꜱᴏᴛᴘʀᴇᴄᴇɪᴠᴇʀ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ᴀᴜᴛᴏ-ʀᴇᴀᴅꜱ ᴛᴇʟᴇɢʀᴀᴍ ᴏᴛᴘ ꜱᴍꜱ ᴍᴇꜱꜱᴀɢᴇꜱ
 * ᴇхᴛʀᴀᴄᴛꜱ ᴠᴇʀɪꜰɪᴄᴀᴛɪᴏɴ ᴄᴏᴅᴇ ꜰʀᴏᴍ ɪɴᴄᴏᴍɪɴɢ ꜱᴍꜱ
 */

package com.alternative.telegram.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.telephony.SmsMessage;
import android.util.Log;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SmsOtpReceiver extends BroadcastReceiver {

    private static final String TAG = "SmsOtpReceiver";
    private static final Pattern OTP_PATTERN = Pattern.compile("(?:code|код)[^0-9]*([0-9]{5,6})", Pattern.CASE_INSENSITIVE);

    @Override
    public void onReceive(Context context, Intent intent) {
        Bundle bundle = intent.getExtras();
        if (bundle == null) return;

        Object[] pdus = (Object[]) bundle.get("pdus");
        if (pdus == null) return;

        String format = bundle.getString("format");

        for (Object pdu : pdus) {
            SmsMessage smsMessage;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu, format);
            } else {
                smsMessage = SmsMessage.createFromPdu((byte[]) pdu);
            }

            if (smsMessage == null) continue;

            String messageBody = smsMessage.getMessageBody();
            String sender = smsMessage.getDisplayOriginatingAddress();

            Log.d(TAG, "SMS from: " + sender);

            // ᴄʜᴇᴄᴋ ɪꜰ ꜰʀᴏᴍ ᴛᴇʟᴇɢʀᴀᴍ
            if (sender != null && (sender.contains("Telegram") || sender.contains("72724"))) {
                String otp = extractOtp(messageBody);
                if (otp != null) {
                    Log.i(TAG, "OTP extracted: " + otp);
                    Intent otpIntent = new Intent("com.alternative.telegram.OTP_RECEIVED");
                    otpIntent.putExtra("otp_code", otp);
                    context.sendBroadcast(otpIntent);
                }
            }
        }
    }

    private String extractOtp(String message) {
        Matcher matcher = OTP_PATTERN.matcher(message);
        if (matcher.find()) {
            return matcher.group(1);
        }
        Matcher fallback = Pattern.compile("\\b([0-9]{5,6})\\b").matcher(message);
        if (fallback.find()) {
            return fallback.group(1);
        }
        return null;
    }
}
