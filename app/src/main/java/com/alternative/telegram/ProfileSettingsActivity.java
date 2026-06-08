/*
 * ᴘʀᴏꜰɪʟᴇꜱᴇᴛᴛɪɴɢꜱᴀᴄᴛɪᴠɪᴛʏ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ᴜꜱᴇʀ ᴘʀᴏꜰɪʟᴇ ᴇᴅɪᴛɪɴɢ
 * ᴇᴅɪᴛ ᴜꜱᴇʀɴᴀᴍᴇ, ʙɪᴏ, ꜱᴛᴀᴛᴜꜱ
 */

package com.alternative.telegram;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class ProfileSettingsActivity extends AppCompatActivity {

    private TextView titleText;
    private ImageButton backButton;
    private EditText usernameInput;
    private EditText displayNameInput;
    private EditText bioInput;
    private Button saveButton;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_settings);

        sessionManager = SessionManager.getInstance(this);

        titleText = findViewById(R.id.profileSettingsTitle);
        backButton = findViewById(R.id.profileBackButton);
        usernameInput = findViewById(R.id.usernameInput);
        displayNameInput = findViewById(R.id.displayNameInput);
        bioInput = findViewById(R.id.bioInput);
        saveButton = findViewById(R.id.saveProfileButton);

        // ᴀᴘᴘʟʏ ᴍɪɴɪ ꜰᴏɴᴛ
        MiniFontConverter.apply(titleText);
        MiniFontConverter.setHint(usernameInput, "ᴜꜱᴇʀɴᴀᴍᴇ");
        MiniFontConverter.setHint(displayNameInput, "ᴅɪꜱᴘʟᴀʏ ɴᴀᴍᴇ");
        MiniFontConverter.setHint(bioInput, "ᴡʀɪᴛᴇ ᴀ ꜱʜᴏʀᴛ ʙɪᴏ...");
        MiniFontConverter.setText(saveButton, "ꜱᴀᴠᴇ ᴄʜᴀɴɢᴇꜱ");

        // ʟᴏᴀᴅ ᴇхɪꜱᴛɪɴɢ ᴠᴀʟᴜᴇꜱ
        usernameInput.setText(sessionManager.getUsername());
        displayNameInput.setText(sessionManager.getDisplayName());
        bioInput.setText(sessionManager.getBio());

        backButton.setOnClickListener(v -> finish());
        saveButton.setOnClickListener(v -> saveProfile());
    }

    private void saveProfile() {
        String username = usernameInput.getText().toString().trim();
        String displayName = displayNameInput.getText().toString().trim();
        String bio = bioInput.getText().toString().trim();

        sessionManager.setUsername(username);
        sessionManager.setDisplayName(displayName);
        sessionManager.setBio(bio);

        MiniFontConverter.showToast(this, "ᴘʀᴏꜰɪʟᴇ ᴜᴘᴅᴀᴛᴇᴅ", Toast.LENGTH_SHORT);
        finish();
    }

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.glass_slide_in_left, R.anim.glass_slide_out_right);
    }
}
