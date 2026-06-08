/*
 * ɴᴇᴛᴡᴏʀᴋꜱᴛᴀᴛᴇʀᴇᴄᴇɪᴠᴇʀ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ᴍᴏɴɪᴛᴏʀꜱ ɴᴇᴛᴡᴏʀᴋ ᴄᴏɴɴᴇᴄᴛɪᴠɪᴛʏ ᴄʜᴀɴɢᴇꜱ
 */

package com.alternative.telegram.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkStateReceiver extends BroadcastReceiver {

    private static final String TAG = "NetworkState";

    @Override
    public void onReceive(Context context, Intent intent) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnected();
        Log.d(TAG, "Network state changed: " + (isConnected ? "CONNECTED" : "DISCONNECTED"));
    }

    public static boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        return activeNetwork != null && activeNetwork.isConnected();
    }
}
