/*
 * MyTelegramApp.java - XGram
 * Application class with safe initialization
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
        try {
            instance = this;
            Log.i(TAG, "MyTelegramApp initialized - Liquid Glass Edition");
        } catch (Exception e) {
            Log.e(TAG, "Error during app initialization", e);
        }
    }

    public static MyTelegramApp getInstance() {
        return instance;
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        try {
            Glide.get(this).clearMemory();
        } catch (Exception e) {
            Log.e(TAG, "Error clearing Glide memory", e);
        }
    }

    @Override
    public void onTrimMemory(int level) {
        super.onTrimMemory(level);
        try {
            Glide.get(this).trimMemory(level);
        } catch (Exception e) {
            Log.e(TAG, "Error trimming Glide memory", e);
        }
    }
}
