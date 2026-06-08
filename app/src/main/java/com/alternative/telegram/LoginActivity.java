/*
 * ʟᴏɢɪɴᴀᴄᴛɪᴠɪᴛʏ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ɢᴀᴛᴇᴡᴀʏ ᴄᴏᴏʀᴅɪɴᴀᴛᴏʀ ꜰᴏʀ ᴛʜʀᴇᴇ-ᴡᴀʏ ᴛᴇʟᴇɢʀᴀᴍ ʟᴏɢɪɴ
 *
 * ʟᴏɢɪɴ ᴍᴇᴛʜᴏᴅꜱ:
 *   1. ᴘʜᴏɴᴇ ɴᴜᴍʙᴇʀ + ꜱᴍꜱ ᴏᴛᴘ (ᴍᴛᴘʀᴏᴛᴏ)
 *   2. ʙᴏᴛ ᴛᴏᴋᴇɴ (ʙᴏᴛ ᴀᴘɪ)
 *   3. ꜱᴛʀɪɴɢ ꜱᴇꜱꜱɪᴏɴ (ᴛᴇʟᴇᴛʜᴏɴ/ᴘʏʀᴏɢʀᴀᴍ ᴀᴜᴛᴏ-ᴅᴇᴛᴇᴄᴛ)
 *
 * ᴜɪ: ʟɪǫᴜɪᴅ ɢʟᴀꜱꜱ ᴅᴇꜱɪɢɴ ᴡɪᴛʜ ꜰʀᴏꜱᴛᴇᴅ ᴄᴀʀᴅꜱ ᴀɴᴅ ᴀɴɪᴍᴀᴛᴇᴅ ᴛʀᴀɴꜱɪᴛɪᴏɴꜱ
 */

package com.alternative.telegram;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private static final String TAG = "LoginActivity";

    // ᴠɪᴇᴡ ʀᴇꜰᴇʀᴇɴᴄᴇꜱ — ᴍᴀɪɴ ʟᴀʏᴏᴜᴛ
    private ImageView backgroundImage;
    private TextView loginTitle;
    private TextView loginSubtitle;
    private FrameLayout optionPhone;
    private FrameLayout optionBot;
    private FrameLayout optionSession;
    private ProgressBar progressBar;
    private TextView versionText;

    // ᴠɪᴇᴡ ʀᴇꜰᴇʀᴇɴᴄᴇꜱ — ꜰᴏʀᴍ ᴏᴠᴇʀʟᴀʏ
    private FrameLayout formOverlay;
    private ImageButton formBackButton;
    private TextView formTitle;
    private TextView formDescription;
    private LinearLayout phoneFormContainer;
    private LinearLayout botFormContainer;
    private LinearLayout sessionFormContainer;
    private EditText countryCodeInput;
    private EditText phoneNumberInput;
    private EditText botTokenInput;
    private EditText sessionStringInput;
    private TextView sessionDetectStatus;
    private Button actionButton;
    private Button secondaryButton;
    private TextView statusText;

    // ꜱᴛᴀᴛᴇ
    private LoginMode currentMode = LoginMode.NONE;
    private SessionManager sessionManager;
    private BackgroundManager backgroundManager;
    private ExecutorService executor;
    private Handler mainHandler;

    // ʟᴏɢɪɴ ᴍᴏᴅᴇ ᴇɴᴜᴍ
    private enum LoginMode {
        NONE, PHONE, BOT, SESSION
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // ɪɴɪᴛɪᴀʟɪᴢᴇ
        sessionManager = SessionManager.getInstance(this);
        backgroundManager = BackgroundManager.getInstance(this);
        executor = Executors.newSingleThreadExecutor();
        mainHandler = new Handler(Looper.getMainLooper());

        // ʙɪɴᴅ ᴠɪᴇᴡꜱ
        bindViews();

        // ᴀᴘᴘʟʏ ᴍɪɴɪ ꜰᴏɴᴛ
        applyMiniFontStyles();

        // ꜱᴇᴛ ʟɪꜱᴛᴇɴᴇʀꜱ
        setupClickListeners();

        // ʟᴏᴀᴅ ʙᴀᴄᴋɢʀᴏᴜɴᴅ ɪꜰ ꜱᴀᴠᴇᴅ
        loadSavedBackground();

        // ᴄʜᴇᴄᴋ ɪꜰ ᴀʟʀᴇᴀᴅʏ ʟᴏɢɢᴇᴅ ɪɴ
        if (sessionManager.isLoggedIn()) {
            navigateToMain();
            return;
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ᴠɪᴇᴡ ʙɪɴᴅɪɴɢ
    // ═══════════════════════════════════════════════════════════

    private void bindViews() {
        // ᴍᴀɪɴ ʟᴀʏᴏᴜᴛ
        backgroundImage = findViewById(R.id.loginBackgroundImage);
        loginTitle = findViewById(R.id.loginTitle);
        loginSubtitle = findViewById(R.id.loginSubtitle);
        optionPhone = findViewById(R.id.optionPhoneContainer);
        optionBot = findViewById(R.id.optionBotContainer);
        optionSession = findViewById(R.id.optionSessionContainer);
        progressBar = findViewById(R.id.loginProgressBar);
        versionText = findViewById(R.id.loginVersionText);

        // ꜰᴏʀᴍ ᴏᴠᴇʀʟᴀʏ
        formOverlay = findViewById(R.id.loginFormOverlay);
        formBackButton = findViewById(R.id.formBackButton);
        formTitle = findViewById(R.id.formTitle);
        formDescription = findViewById(R.id.formDescription);
        phoneFormContainer = findViewById(R.id.phoneFormContainer);
        botFormContainer = findViewById(R.id.botFormContainer);
        sessionFormContainer = findViewById(R.id.sessionFormContainer);
        countryCodeInput = findViewById(R.id.countryCodeInput);
        phoneNumberInput = findViewById(R.id.phoneNumberInput);
        botTokenInput = findViewById(R.id.botTokenInput);
        sessionStringInput = findViewById(R.id.sessionStringInput);
        sessionDetectStatus = findViewById(R.id.sessionDetectStatus);
        actionButton = findViewById(R.id.loginActionButton);
        secondaryButton = findViewById(R.id.loginSecondaryButton);
        statusText = findViewById(R.id.loginStatusText);
    }

    // ═══════════════════════════════════════════════════════════
    // ᴍɪɴɪ ꜰᴏɴᴛ ꜱᴛʏʟɪɴɢ
    // ═══════════════════════════════════════════════════════════

    private void applyMiniFontStyles() {
        MiniFontConverter.apply(loginTitle);
        MiniFontConverter.apply(loginSubtitle);
        MiniFontConverter.apply(versionText);

        // ᴏᴘᴛɪᴏɴ ᴛɪᴛʟᴇꜱ
        TextView phoneTitle = findViewById(R.id.optionPhoneTitle);
        TextView phoneDesc = findViewById(R.id.optionPhoneDesc);
        TextView botTitle = findViewById(R.id.optionBotTitle);
        TextView botDesc = findViewById(R.id.optionBotDesc);
        TextView sessionTitle = findViewById(R.id.optionSessionTitle);
        TextView sessionDesc = findViewById(R.id.optionSessionDesc);

        MiniFontConverter.apply(phoneTitle);
        MiniFontConverter.apply(phoneDesc);
        MiniFontConverter.apply(botTitle);
        MiniFontConverter.apply(botDesc);
        MiniFontConverter.apply(sessionTitle);
        MiniFontConverter.apply(sessionDesc);
    }

    // ═══════════════════════════════════════════════════════════
    // ᴄʟɪᴄᴋ ʟɪꜱᴛᴇɴᴇʀꜱ
    // ═══════════════════════════════════════════════════════════

    private void setupClickListeners() {
        optionPhone.setOnClickListener(v -> showLoginForm(LoginMode.PHONE));
        optionBot.setOnClickListener(v -> showLoginForm(LoginMode.BOT));
        optionSession.setOnClickListener(v -> showLoginForm(LoginMode.SESSION));

        formBackButton.setOnClickListener(v -> hideLoginForm());
        actionButton.setOnClickListener(v -> onActionButtonClicked());
        secondaryButton.setOnClickListener(v -> onSecondaryButtonClicked());

        // ꜱᴇꜱꜱɪᴏɴ ɪɴᴘᴜᴛ ᴡᴀᴛᴄʜᴇʀ ꜰᴏʀ ᴀᴜᴛᴏ-ᴅᴇᴛᴇᴄᴛɪᴏɴ
        sessionStringInput.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                String input = s.toString().trim();
                if (input.length() > 20) {
                    detectSessionType(input);
                } else {
                    sessionDetectStatus.setVisibility(View.GONE);
                }
            }
        });
    }

    // ═══════════════════════════════════════════════════════════
    // ʙᴀᴄᴋɢʀᴏᴜɴᴅ ʟᴏᴀᴅɪɴɢ
    // ═══════════════════════════════════════════════════════════

    private void loadSavedBackground() {
        String savedUrl = sessionManager.getCustomBackgroundUrl();
        if (savedUrl != null && !savedUrl.isEmpty()) {
            backgroundManager.loadBackground(backgroundImage, savedUrl,
                    new BackgroundManager.SimpleCallback());
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ʟᴏɢɪɴ ꜰᴏʀᴍ ᴍᴀɴᴀɢᴇᴍᴇɴᴛ
    // ═══════════════════════════════════════════════════════════

    private void showLoginForm(LoginMode mode) {
        currentMode = mode;

        // ʀᴇꜱᴇᴛ ꜰᴏʀᴍꜱ
        phoneFormContainer.setVisibility(View.GONE);
        botFormContainer.setVisibility(View.GONE);
        sessionFormContainer.setVisibility(View.GONE);
        statusText.setVisibility(View.GONE);
        secondaryButton.setVisibility(View.GONE);

        switch (mode) {
            case PHONE:
                formTitle.setText(MiniFontConverter.convert("ᴘʜᴏɴᴇ ɴᴜᴍʙᴇʀ"));
                formDescription.setText(MiniFontConverter.convert("ᴇɴᴛᴇʀ ʏᴏᴜʀ ᴘʜᴏɴᴇ ɴᴜᴍʙᴇʀ ᴛᴏ ʀᴇᴄᴇɪᴠᴇ ᴀɴ ꜱᴍꜱ ᴄᴏᴅᴇ"));
                phoneFormContainer.setVisibility(View.VISIBLE);
                MiniFontConverter.setHint(countryCodeInput, "+1");
                MiniFontConverter.setHint(phoneNumberInput, "234 567 8900");
                actionButton.setText(MiniFontConverter.convert("ꜱᴇɴᴅ ᴄᴏᴅᴇ"));
                break;

            case BOT:
                formTitle.setText(MiniFontConverter.convert("ʙᴏᴛ ᴛᴏᴋᴇɴ"));
                formDescription.setText(MiniFontConverter.convert("ᴇɴᴛᴇʀ ʏᴏᴜʀ ʙᴏᴛ ᴛᴏᴋᴇɴ ꜰʀᴏᴍ @ʙᴏᴛꜰᴀᴛʜᴇʀ"));
                botFormContainer.setVisibility(View.VISIBLE);
                MiniFontConverter.setHint(botTokenInput,
                        "123456:ABC-DEF1234ghIkl-zyx57W2v1u123ew11");
                actionButton.setText(MiniFontConverter.convert("ᴄᴏɴɴᴇᴄᴛ"));
                break;

            case SESSION:
                formTitle.setText(MiniFontConverter.convert("ꜱᴛʀɪɴɢ ꜱᴇꜱꜱɪᴏɴ"));
                formDescription.setText(MiniFontConverter.convert(
                        "ᴘᴀꜱᴛᴇ ʏᴏᴜʀ ᴛᴇʟᴇᴛʜᴏɴ ᴏʀ ᴘʏʀᴏɢʀᴀᴍ ꜱᴇꜱꜱɪᴏɴ ꜱᴛʀɪɴɢ"));
                sessionFormContainer.setVisibility(View.VISIBLE);
                MiniFontConverter.setHint(sessionStringInput,
                        "ᴀɢʙᴅᴘᴛ... ᴏʀ ʙɢʙ_q...");
                actionButton.setText(MiniFontConverter.convert("ᴄᴏɴɴᴇᴄᴛ"));
                break;
        }

        // ᴀɴɪᴍᴀᴛᴇ ɪɴ
        formOverlay.setVisibility(View.VISIBLE);
        formOverlay.setAlpha(0f);
        formOverlay.animate()
                .alpha(1f)
                .setDuration(250)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();
    }

    private void hideLoginForm() {
        formOverlay.animate()
                .alpha(0f)
                .setDuration(200)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        formOverlay.setVisibility(View.GONE);
                        currentMode = LoginMode.NONE;
                    }
                })
                .start();
    }

    // ═══════════════════════════════════════════════════════════
    // ꜱᴇꜱꜱɪᴏɴ ᴀᴜᴛᴏ-ᴅᴇᴛᴇᴄᴛɪᴏɴ ꜰᴇᴇᴅʙᴀᴄᴋ
    // ═══════════════════════════════════════════════════════════

    private void detectSessionType(String input) {
        SessionParser.SessionType type = SessionParser.detectType(input);
        String status;
        int colorRes;

        switch (type) {
            case PYROGRAM:
                status = "ᴘʏʀᴏɢʀᴀᴍ ꜱᴇꜱꜱɪᴏɴ ᴅᴇᴛᴇᴄᴛᴇᴅ (" + input.length() + " ᴄʜᴀʀꜱ)";
                colorRes = R.color.glass_success;
                break;
            case TELETHON:
                status = "ᴛᴇʟᴇᴛʜᴏɴ ꜱᴇꜱꜱɪᴏɴ ᴅᴇᴛᴇᴄᴛᴇᴅ (" + input.length() + " ᴄʜᴀʀꜱ)";
                colorRes = R.color.glass_info;
                break;
            default:
                status = "ᴀɴᴀʟʏᴢɪɴɢ ꜱᴇꜱꜱɪᴏɴ...";
                colorRes = R.color.glass_text_tertiary;
                break;
        }

        sessionDetectStatus.setText(status);
        sessionDetectStatus.setTextColor(getResources().getColor(colorRes, getTheme()));
        sessionDetectStatus.setVisibility(View.VISIBLE);
    }

    // ═══════════════════════════════════════════════════════════
    // ʙᴜᴛᴛᴏɴ ʜᴀɴᴅʟᴇʀꜱ
    // ═══════════════════════════════════════════════════════════

    private void onActionButtonClicked() {
        switch (currentMode) {
            case PHONE:
                handlePhoneLogin();
                break;
            case BOT:
                handleBotLogin();
                break;
            case SESSION:
                handleSessionLogin();
                break;
            default:
                break;
        }
    }

    private void onSecondaryButtonClicked() {
        hideLoginForm();
    }

    // ═══════════════════════════════════════════════════════════
    // ʟᴏɢɪɴ ʜᴀɴᴅʟᴇʀꜱ
    // ═══════════════════════════════════════════════════════════

    private void handlePhoneLogin() {
        String countryCode = countryCodeInput.getText().toString().trim();
        String phoneNumber = phoneNumberInput.getText().toString().trim();

        if (countryCode.isEmpty()) {
            showError("ᴘʟᴇᴀꜱᴇ ᴇɴᴛᴇʀ ᴀ ᴄᴏᴜɴᴛʀʏ ᴄᴏᴅᴇ");
            return;
        }
        if (phoneNumber.isEmpty()) {
            showError("ᴘʟᴇᴀꜱᴇ ᴇɴᴛᴇʀ ᴀ ᴘʜᴏɴᴇ ɴᴜᴍʙᴇʀ");
            return;
        }

        // ᴠᴀʟɪᴅᴀᴛᴇ
        SessionParser.ParsedSession result =
                SessionParser.parsePhoneNumber(countryCode, phoneNumber);
        if (!result.isValid) {
            showError(result.errorMessage);
            return;
        }

        showLoading(true);

        // ꜱɪᴍᴜʟᴀᴛᴇ ᴀᴘɪ ᴄᴀʟʟ
        executor.submit(() -> {
            try {
                Thread.sleep(1500);

                mainHandler.post(() -> {
                    showLoading(false);
                    // ꜱᴀᴠᴇ ꜱᴇꜱꜱɪᴏɴ ᴀɴᴅ ɴᴀᴠɪɢᴀᴛᴇ
                    sessionManager.savePhoneSession(
                            result.countryCode, result.phoneNumber, 0);
                    navigateToMain();
                });

            } catch (InterruptedException e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    showError("ʀᴇǫᴜᴇꜱᴛ ᴄᴀɴᴄᴇʟʟᴇᴅ");
                });
            }
        });
    }

    private void handleBotLogin() {
        String token = botTokenInput.getText().toString().trim();

        if (token.isEmpty()) {
            showError("ᴘʟᴇᴀꜱᴇ ᴇɴᴛᴇʀ ᴀ ʙᴏᴛ ᴛᴏᴋᴇɴ");
            return;
        }

        // ᴠᴀʟɪᴅᴀᴛᴇ
        SessionParser.ParsedSession result = SessionParser.parseBotToken(token);
        if (!result.isValid) {
            showError(result.errorMessage);
            return;
        }

        showLoading(true);

        executor.submit(() -> {
            try {
                // ꜱɪᴍᴜʟᴀᴛᴇ ʙᴏᴛ ᴀᴘɪ ᴠᴀʟɪᴅᴀᴛɪᴏɴ
                Thread.sleep(1200);

                mainHandler.post(() -> {
                    showLoading(false);
                    sessionManager.saveBotSession(token);
                    navigateToMain();
                });

            } catch (InterruptedException e) {
                mainHandler.post(() -> {
                    showLoading(false);
                    showError("ʀᴇǫᴜᴇꜱᴛ ᴄᴀɴᴄᴇʟʟᴇᴅ");
                });
            }
        });
    }

    private void handleSessionLogin() {
        String sessionString = sessionStringInput.getText().toString().trim();

        if (sessionString.isEmpty()) {
            showError("ᴘʟᴇᴀꜱᴇ ᴘᴀꜱᴛᴇ ᴀ ꜱᴇꜱꜱɪᴏɴ ꜱᴛʀɪɴɢ");
            return;
        }

        showLoading(true);
        statusText.setText(MiniFontConverter.convert("ᴘᴀʀꜱɪɴɢ ꜱᴇꜱꜱɪᴏɴ..."));
        statusText.setVisibility(View.VISIBLE);

        executor.submit(() -> {
            // ᴀᴜᴛᴏ-ᴅᴇᴛᴇᴄᴛ ᴀɴᴅ ᴘᴀʀꜱᴇ
            SessionParser.ParsedSession result =
                    SessionParser.autoDetect(sessionString);

            mainHandler.post(() -> {
                if (!result.isValid) {
                    showLoading(false);
                    statusText.setText(MiniFontConverter.convert(result.errorMessage));
                    statusText.setTextColor(getResources().getColor(R.color.glass_error, getTheme()));
                    return;
                }

                // ᴘᴀʀꜱᴇ ꜱᴜᴄᴄᴇꜱꜱꜰᴜʟ
                statusText.setText(MiniFontConverter.convert(
                        SessionParser.getTypeDescription(result.type) + " ᴅᴇᴛᴇᴄᴛᴇᴅ!"));
                statusText.setTextColor(getResources().getColor(R.color.glass_success, getTheme()));

                // ꜱᴀᴠᴇ ᴀɴᴅ ɴᴀᴠɪɢᴀᴛᴇ
                sessionManager.saveStringSession(result);

                // ꜱʜᴏʀᴛ ᴅᴇʟᴀʏ ꜰᴏʀ ᴜꜱᴇʀ ᴛᴏ ꜱᴇᴇ ꜱᴜᴄᴄᴇꜱꜱ ᴍᴇꜱꜱᴀɢᴇ
                mainHandler.postDelayed(() -> {
                    showLoading(false);
                    navigateToMain();
                }, 800);
            });
        });
    }

    // ═══════════════════════════════════════════════════════════
    // ᴜɪ ʜᴇʟᴘᴇʀꜱ
    // ═══════════════════════════════════════════════════════════

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        actionButton.setEnabled(!show);
    }

    private void showError(String message) {
        statusText.setText(MiniFontConverter.convert(message));
        statusText.setTextColor(getResources().getColor(R.color.glass_error, getTheme()));
        statusText.setVisibility(View.VISIBLE);
        Toast.makeText(this, MiniFontConverter.convert(message), Toast.LENGTH_SHORT).show();
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.glass_slide_in_right, R.anim.glass_slide_out_left);
        finish();
    }

    // ═══════════════════════════════════════════════════════════
    // ʟɪꜰᴇᴄʏᴄʟᴇ
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null && !executor.isShutdown()) {
            executor.shutdown();
        }
    }
}
