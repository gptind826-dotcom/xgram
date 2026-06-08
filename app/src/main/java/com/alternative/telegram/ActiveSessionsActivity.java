/*
 * ᴀᴄᴛɪᴠᴇꜱᴇꜱꜱɪᴏɴꜱᴀᴄᴛɪᴠɪᴛʏ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ᴅɪꜱᴘʟᴀʏ ᴀᴄᴛɪᴠᴇ ᴛᴇʟᴇɢʀᴀᴍ ꜱᴇꜱꜱɪᴏɴꜱ
 * ᴀʟʟᴏᴡ ᴛᴇʀᴍɪɴᴀᴛɪɴɢ ꜱᴘᴇᴄɪꜰɪᴄ ꜱᴇꜱꜱɪᴏɴꜱ
 */

package com.alternative.telegram;

import android.os.Bundle;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ActiveSessionsActivity extends AppCompatActivity {

    private TextView titleText;
    private ImageButton backButton;
    private LinearLayout sessionsContainer;
    private TextView emptyText;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_active_sessions);

        titleText = findViewById(R.id.sessionsTitle);
        backButton = findViewById(R.id.sessionsBackButton);
        sessionsContainer = findViewById(R.id.sessionsContainer);
        emptyText = findViewById(R.id.sessionsEmptyText);

        MiniFontConverter.apply(titleText);
        MiniFontConverter.apply(emptyText);

        backButton.setOnClickListener(v -> finish());

        loadActiveSessions();
    }

    private void loadActiveSessions() {
        // ɪɴ ᴀ ʀᴇᴀʟ ᴀᴘᴘ, ꜰᴇᴛᴄʜ ꜰʀᴏᴍ ᴛᴇʟᴇɢʀᴀᴍ ᴀᴘɪ
        // ꜱʜᴏᴡ ᴄᴜʀʀᴇɴᴛ ᴀᴘᴘ ꜱᴇꜱꜱɪᴏɴ
        SessionManager sm = SessionManager.getInstance(this);

        View sessionView = getLayoutInflater().inflate(R.layout.item_session, sessionsContainer, false);

        TextView deviceText = sessionView.findViewById(R.id.sessionDevice);
        TextView locationText = sessionView.findViewById(R.id.sessionLocation);
        TextView statusText = sessionView.findViewById(R.id.sessionStatus);

        deviceText.setText(MiniFontConverter.convert("ᴛʜɪꜱ ᴅᴇᴠɪᴄᴇ — ᴀɴᴅʀᴏɪᴅ"));
        locationText.setText(MiniFontConverter.convert("ᴄᴜʀʀᴇɴᴛ ꜱᴇꜱꜱɪᴏɴ"));
        statusText.setText(MiniFontConverter.convert("ᴏɴʟɪɴᴇ"));
        statusText.setTextColor(getResources().getColor(R.color.glass_success, getTheme()));

        sessionsContainer.addView(sessionView);
        emptyText.setVisibility(View.GONE);
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.glass_slide_in_left, R.anim.glass_slide_out_right);
    }
}
