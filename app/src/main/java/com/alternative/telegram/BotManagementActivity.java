/*
 * ʙᴏᴛᴍᴀɴᴀɢᴇᴍᴇɴᴛᴀᴄᴛɪᴠɪᴛʏ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ʙᴏᴛ ᴄᴏɴꜰɪɢᴜʀᴀᴛɪᴏɴ ᴀɴᴅ ᴄᴏᴍᴍᴀɴᴅ ɪɴꜱᴘᴇᴄᴛᴏʀ
 * ᴍᴀɴᴀɢᴇ ʙᴏᴛ ɴᴀᴍᴇ, ᴅᴇꜱᴄʀɪᴘᴛɪᴏɴ, ᴀʙᴏᴜᴛ
 * ɪɴꜱᴘᴇᴄᴛ ɪɴᴄᴏᴍɪɴɢ ᴄᴏᴍᴍᴀɴᴅ ᴘᴀʏʟᴏᴀᴅꜱ
 */

package com.alternative.telegram;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class BotManagementActivity extends AppCompatActivity {

    private TextView titleText;
    private ImageButton backButton;
    private EditText botNameInput;
    private EditText botDescriptionInput;
    private EditText botAboutInput;
    private Button saveBotButton;
    private LinearLayout inspectorContainer;
    private TextView inspectorEmptyText;
    private ScrollView inspectorScrollView;
    private Button clearLogButton;

    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bot_management);

        sessionManager = SessionManager.getInstance(this);

        titleText = findViewById(R.id.botManagementTitle);
        backButton = findViewById(R.id.botBackButton);
        botNameInput = findViewById(R.id.botNameInput);
        botDescriptionInput = findViewById(R.id.botDescriptionInput);
        botAboutInput = findViewById(R.id.botAboutInput);
        saveBotButton = findViewById(R.id.saveBotButton);
        inspectorContainer = findViewById(R.id.inspectorContainer);
        inspectorEmptyText = findViewById(R.id.inspectorEmptyText);
        inspectorScrollView = findViewById(R.id.inspectorScrollView);
        clearLogButton = findViewById(R.id.clearLogButton);

        // ᴀᴘᴘʟʏ ᴍɪɴɪ ꜰᴏɴᴛ
        MiniFontConverter.apply(titleText);
        MiniFontConverter.setHint(botNameInput, "ʙᴏᴛ ɴᴀᴍᴇ");
        MiniFontConverter.setHint(botDescriptionInput, "ʙᴏᴛ ᴅᴇꜱᴄʀɪᴘᴛɪᴏɴ");
        MiniFontConverter.setHint(botAboutInput, "ᴀʙᴏᴜᴛ ᴛᴇхᴛ");
        MiniFontConverter.setText(saveBotButton, "ꜱᴀᴠᴇ ʙᴏᴛ ꜱᴇᴛᴛɪɴɢꜱ");
        MiniFontConverter.apply(inspectorEmptyText);

        backButton.setOnClickListener(v -> finish());
        saveBotButton.setOnClickListener(v -> saveBotSettings());
        clearLogButton.setOnClickListener(v -> clearCommandLog());

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                finish();
            }
        });

        // ᴄʜᴇᴄᴋ ɪꜰ ɪɴꜱᴘᴇᴄᴛᴏʀ ᴛᴀʙ ᴡᴀꜱ ʀᴇǫᴜᴇꜱᴛᴇᴅ
        String requestedTab = getIntent().getStringExtra("tab");
        if ("inspector".equals(requestedTab)) {
            inspectorScrollView.setVisibility(View.VISIBLE);
            // ʜɪᴅᴇ ᴇᴅɪᴛ ꜰᴏʀᴍꜱ
            botNameInput.setVisibility(View.GONE);
            botDescriptionInput.setVisibility(View.GONE);
            botAboutInput.setVisibility(View.GONE);
            saveBotButton.setVisibility(View.GONE);
            titleText.setText(MiniFontConverter.convert("ᴄᴏᴍᴍᴀɴᴅ ɪɴꜱᴘᴇᴄᴛᴏʀ"));
        }

        // ʟᴏᴀᴅ ꜱᴀᴍᴘʟᴇ ᴄᴏᴍᴍᴀɴᴅꜱ
        loadSampleCommands();
    }

    private void saveBotSettings() {
        String name = botNameInput.getText().toString().trim();
        String description = botDescriptionInput.getText().toString().trim();
        String about = botAboutInput.getText().toString().trim();

        MiniFontConverter.showToast(this, "ʙᴏᴛ ꜱᴇᴛᴛɪɴɢꜱ ꜱᴀᴠᴇᴅ", Toast.LENGTH_SHORT);
    }

    private void clearCommandLog() {
        inspectorContainer.removeAllViews();
        inspectorEmptyText.setVisibility(View.VISIBLE);
        MiniFontConverter.showToast(this, "ᴄᴏᴍᴍᴀɴᴅ ʟᴏɢ ᴄʟᴇᴀʀᴇᴅ", Toast.LENGTH_SHORT);
    }

    private void loadSampleCommands() {
        // ɪɴ ᴀ ʀᴇᴀʟ ᴀᴘᴘ, ᴛʜɪꜱ ᴡᴏᴜʟᴅ ʟᴏᴀᴅ ꜰʀᴏᴍ ᴀ ʟᴏᴄᴀʟ ᴅᴀᴛᴀʙᴀꜱᴇ
        // ꜰᴏʀ ɴᴏᴡ, ᴡᴇ ꜱʜᴏᴡ ᴛʜᴇ ᴇᴍᴘᴛʏ ꜱᴛᴀᴛᴇ
        inspectorEmptyText.setVisibility(View.VISIBLE);
    }

    private void addCommandEntry(String command, String user, String chat, String timestamp) {
        inspectorEmptyText.setVisibility(View.GONE);

        View entryView = getLayoutInflater().inflate(R.layout.item_command_log, inspectorContainer, false);

        TextView cmdText = entryView.findViewById(R.id.commandText);
        TextView userText = entryView.findViewById(R.id.commandUser);
        TextView timeText = entryView.findViewById(R.id.commandTime);

        cmdText.setText(MiniFontConverter.convert("/" + command));
        userText.setText(MiniFontConverter.convert(user));
        timeText.setText(MiniFontConverter.convert(timestamp));

        inspectorContainer.addView(entryView, 0);
    }

}
