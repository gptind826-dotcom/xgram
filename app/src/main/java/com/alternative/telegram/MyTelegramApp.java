/*
 * ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ᴀᴘᴘʟɪᴄᴀᴛɪᴏɴ ᴄʟᴀꜱꜱ
 * ɪɴɪᴛɪᴀʟɪᴢᴇꜱ ɢʟɪᴅᴇ, ɴᴇᴛᴡᴏʀᴋ ᴄᴏɴꜰɪɢ, ᴀɴᴅ ɢʟᴏʙᴀʟ ꜱᴛᴀᴛᴇ
 */

package com.alternative.telegram;

import android.app.Application;
import android.util.Log;

import com.bumptech.glide.Glide;

public class MyTelegramApp extends Application {

    private static final String TAG = "MyTelegramApp";
    private static MyTelegramApp instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        Log.i(TAG, "MyTelegramApp initialized — Liquid Glass Edition");
    }

    public static MyTelegramApp getInstance() {
        return instance;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        Glide.get(this).clearMemory();
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        Glide.get(this).trimMemory(level);
    }
}
