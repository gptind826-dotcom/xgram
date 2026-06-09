/*
 * ᴍᴀɪɴᴀᴄᴛɪᴠɪᴛʏ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ᴘᴏꜱᴛ-ᴀᴜᴛʜᴇɴᴛɪᴄᴀᴛɪᴏɴ ᴅᴀꜱʜʙᴏᴀʀᴅ
 *
 * ꜰᴇᴀᴛᴜʀᴇꜱ:
 * - ʟɪǫᴜɪᴅ ɢʟᴀꜱꜱ ɴᴀᴠɪɢᴀᴛɪᴏɴ (ꜰʀᴏꜱᴛᴇᴅ ꜱᴇᴀʀᴄʜ ʙᴀʀ, ᴄʜᴀᴛ ʟɪꜱᴛ, ᴛᴀʙꜱ)
 * - ʙᴀᴄᴋɢʀᴏᴜɴᴅ ᴜʀʟ ꜱᴜᴘᴘᴏʀᴛ (ᴄᴜꜱᴛᴏᴍ ɪᴍɢʙʙ ʙᴀᴄᴋᴅʀᴏᴘ)
 * - ᴄᴏɴᴅɪᴛɪᴏɴᴀʟ ꜱᴇᴛᴛɪɴɢꜱ:
 *     ᴄᴏɴᴛᴇхᴛ ᴀ: ᴜꜱᴇʀ ᴀᴄᴄᴏᴜɴᴛ (2ꜰᴀ, ꜱᴇꜱꜱɪᴏɴꜱ, ᴘʀɪᴠᴀᴄʏ)
 *     ᴄᴏɴᴛᴇхᴛ ʙ: ʙᴏᴛ ᴛᴏᴋᴇɴ (ʙᴏᴛ ᴘʀᴏꜰɪʟᴇ, ᴄᴏᴍᴍᴀɴᴅ ɪɴꜱᴘᴇᴄᴛᴏʀ)
 * - ᴍɪɴɪ-ᴜɴɪᴄᴏᴅᴇ ꜰᴏɴᴛ ᴇɴꜰᴏʀᴄᴇᴍᴇɴᴛ
 */

package com.alternative.telegram;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    // ᴠɪᴇᴡ ʀᴇꜰᴇʀᴇɴᴄᴇꜱ
    private ImageView backgroundImage;
    private TextView mainTitle;
    private EditText searchInput;
    private ImageButton settingsButton;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private BottomNavigationView bottomNav;
    private FloatingActionButton fabNewChat;
    private FrameLayout settingsPanel;
    private View dimOverlay;

    // ꜱᴇᴛᴛɪɴɢꜱ ᴠɪᴇᴡꜱ
    private ImageButton settingsCloseButton;
    private TextView settingsPanelTitle;
    private TextView settingsProfileName;
    private TextView settingsProfileStatus;
    private TextView settingsAvatarInitial;
    private EditText backgroundUrlInput;
    private Button backgroundApplyButton;
    private Button backgroundDefaultButton;
    private LinearLayout userSettingsContainer;
    private LinearLayout botSettingsContainer;
    private Switch notificationsSwitch;
    private Button logoutButton;

    // ꜱᴇᴛᴛɪɴɢꜱ ʀᴏᴡ ᴄʟɪᴄᴋᴀʙʟᴇꜱ
    private LinearLayout editProfileRow;
    private LinearLayout twoStepRow;
    private LinearLayout activeSessionsRow;
    private LinearLayout privacyRow;
    private LinearLayout botNameRow;
    private LinearLayout botDescriptionRow;
    private LinearLayout commandInspectorRow;
    private LinearLayout botWebhookRow;

    // ᴍᴀɴᴀɢᴇʀꜱ
    private SessionManager sessionManager;
    private BackgroundManager backgroundManager;
    private Handler mainHandler;

    // ᴀᴅᴀᴘᴛᴇʀ
    private MainPagerAdapter pagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ɪɴɪᴛɪᴀʟɪᴢᴇ
        sessionManager = SessionManager.getInstance(this);
        backgroundManager = BackgroundManager.getInstance(this);
        mainHandler = new Handler(Looper.getMainLooper());

        // ᴠᴇʀɪꜰʏ ʟᴏɢɪɴ ꜱᴛᴀᴛᴇ
        if (!sessionManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        bindViews();
        applyMiniFontStyles();
        setupViewPager();
        setupBottomNavigation();
        setupClickListeners();
        setupSettingsPanel();
        loadBackground();
    }

    // ═══════════════════════════════════════════════════════════
    // ᴠɪᴇᴡ ʙɪɴᴅɪɴɢ
    // ═══════════════════════════════════════════════════════════

    private void bindViews() {
        // ᴍᴀɪɴ ᴄᴏɴᴛᴇɴᴛ
        backgroundImage = findViewById(R.id.mainBackgroundImage);
        mainTitle = findViewById(R.id.mainTitle);
        searchInput = findViewById(R.id.searchInput);
        settingsButton = findViewById(R.id.settingsButton);
        tabLayout = findViewById(R.id.mainTabLayout);
        viewPager = findViewById(R.id.mainViewPager);
        bottomNav = findViewById(R.id.mainBottomNav);
        fabNewChat = findViewById(R.id.fabNewChat);
        settingsPanel = findViewById(R.id.settingsPanel);
        dimOverlay = findViewById(R.id.mainDimOverlay);

        // ꜱᴇᴛᴛɪɴɢꜱ ᴘᴀɴᴇʟ
        settingsCloseButton = findViewById(R.id.settingsCloseButton);
        settingsPanelTitle = findViewById(R.id.settingsPanelTitle);
        settingsProfileName = findViewById(R.id.settingsProfileName);
        settingsProfileStatus = findViewById(R.id.settingsProfileStatus);
        settingsAvatarInitial = findViewById(R.id.settingsAvatarInitial);
        backgroundUrlInput = findViewById(R.id.backgroundUrlInput);
        backgroundApplyButton = findViewById(R.id.backgroundApplyButton);
        backgroundDefaultButton = findViewById(R.id.backgroundDefaultButton);
        userSettingsContainer = findViewById(R.id.userSettingsContainer);
        botSettingsContainer = findViewById(R.id.botSettingsContainer);
        notificationsSwitch = findViewById(R.id.notificationsSwitch);
        logoutButton = findViewById(R.id.logoutButton);

        // ꜱᴇᴛᴛɪɴɢꜱ ʀᴏᴡꜱ
        editProfileRow = findViewById(R.id.editProfileRow);
        twoStepRow = findViewById(R.id.twoStepRow);
        activeSessionsRow = findViewById(R.id.activeSessionsRow);
        privacyRow = findViewById(R.id.privacyRow);
        botNameRow = findViewById(R.id.botNameRow);
        botDescriptionRow = findViewById(R.id.botDescriptionRow);
        commandInspectorRow = findViewById(R.id.commandInspectorRow);
        botWebhookRow = findViewById(R.id.botWebhookRow);
    }

    // ═══════════════════════════════════════════════════════════
    // ᴍɪɴɪ ꜰᴏɴᴛ ꜱᴛʏʟɪɴɢ
    // ═══════════════════════════════════════════════════════════

    private void applyMiniFontStyles() {
        MiniFontConverter.apply(mainTitle);
        MiniFontConverter.setHint(searchInput, "ꜱᴇᴀʀᴄʜ ᴄʜᴀᴛꜱ...");
    }

    // ═══════════════════════════════════════════════════════════
    // ᴠɪᴇᴡ ᴘᴀɢᴇʀ ꜱᴇᴛᴜᴘ
    // ═══════════════════════════════════════════════════════════

    private void setupViewPager() {
        pagerAdapter = new MainPagerAdapter(this);
        viewPager.setAdapter(pagerAdapter);

        // ᴛᴀʙ ʟᴀʏᴏᴜᴛ ᴍᴇᴅɪᴀᴛᴏʀ
        String[] tabTitles = {"ᴄʜᴀᴛꜱ", "ᴄʜᴀɴɴᴇʟꜱ", "ɢʀᴏᴜᴘꜱ", "ᴄᴏɴᴛᴀᴄᴛꜱ"};
        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            tab.setText(tabTitles[position]);
        }).attach();
    }

    // ═══════════════════════════════════════════════════════════
    // ʙᴏᴛᴛᴏᴍ ɴᴀᴠɪɢᴀᴛɪᴏɴ
    // ═══════════════════════════════════════════════════════════

    private void setupBottomNavigation() {
        bottomNav.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.nav_chats) {
                viewPager.setCurrentItem(0, true);
                return true;
            } else if (itemId == R.id.nav_channels) {
                viewPager.setCurrentItem(1, true);
                return true;
            } else if (itemId == R.id.nav_groups) {
                viewPager.setCurrentItem(2, true);
                return true;
            } else if (itemId == R.id.nav_contacts) {
                viewPager.setCurrentItem(3, true);
                return true;
            }
            return false;
        });
    }

    // ═══════════════════════════════════════════════════════════
    // ᴄʟɪᴄᴋ ʟɪꜱᴛᴇɴᴇʀꜱ
    // ═══════════════════════════════════════════════════════════

    private void setupClickListeners() {
        settingsButton.setOnClickListener(v -> openSettingsPanel());
        settingsCloseButton.setOnClickListener(v -> closeSettingsPanel());
        fabNewChat.setOnClickListener(v -> onNewChatClicked());

        // ʙᴀᴄᴋɢʀᴏᴜɴᴅ ᴜʀʟ
        backgroundApplyButton.setOnClickListener(v -> applyCustomBackground());
        backgroundDefaultButton.setOnClickListener(v -> resetBackground());

        // ꜱᴇᴛᴛɪɴɢꜱ ʀᴏᴡꜱ
        editProfileRow.setOnClickListener(v -> openProfileEditor());
        twoStepRow.setOnClickListener(v -> openTwoStepSettings());
        activeSessionsRow.setOnClickListener(v -> openActiveSessions());
        privacyRow.setOnClickListener(v -> openPrivacySettings());
        botNameRow.setOnClickListener(v -> openBotNameEditor());
        botDescriptionRow.setOnClickListener(v -> openBotDescriptionEditor());
        commandInspectorRow.setOnClickListener(v -> openCommandInspector());
        botWebhookRow.setOnClickListener(v -> openWebhookSettings());

        // ʟᴏɢᴏᴜᴛ
        logoutButton.setOnClickListener(v -> showLogoutConfirmDialog());
    }

    // ═══════════════════════════════════════════════════════════
    // ꜱᴇᴛᴛɪɴɢꜱ ᴘᴀɴᴇʟ — ᴄᴏɴᴅɪᴛɪᴏɴᴀʟ ʟᴀʏᴏᴜᴛ
    // ═══════════════════════════════════════════════════════════

    private void setupSettingsPanel() {
        String displayName = sessionManager.getDisplayName();
        if (displayName.isEmpty()) {
            displayName = "ᴜꜱᴇʀ";
        }

        settingsProfileName.setText(MiniFontConverter.convert(displayName));
        settingsAvatarInitial.setText(MiniFontConverter.convert(
                String.valueOf(displayName.charAt(0)).toUpperCase()));

        // ʟᴏᴀᴅ ꜱᴀᴠᴇᴅ ʙᴀᴄᴋɢʀᴏᴜɴᴅ ᴜʀʟ
        String savedUrl = sessionManager.getCustomBackgroundUrl();
        if (savedUrl != null) {
            backgroundUrlInput.setText(savedUrl);
        }

        // ᴄᴏɴᴅɪᴛɪᴏɴᴀʟ ᴠɪᴇᴡꜱ ʙᴀꜱᴇᴅ ᴏɴ ʟᴏɢɪɴ ᴛʏᴘᴇ
        if (sessionManager.isBotLogin()) {
            // ᴄᴏɴᴛᴇхᴛ ᴠɪᴇᴡ ʙ: ʙᴏᴛ ꜱᴇᴛᴛɪɴɢꜱ
            userSettingsContainer.setVisibility(View.GONE);
            botSettingsContainer.setVisibility(View.VISIBLE);
            settingsPanelTitle.setText(MiniFontConverter.convert("ʙᴏᴛ ꜱᴇᴛᴛɪɴɢꜱ"));
            settingsProfileStatus.setText(MiniFontConverter.convert("ʙᴏᴛ ᴀᴄᴄᴏᴜɴᴛ"));
        } else {
            // ᴄᴏɴᴛᴇхᴛ ᴠɪᴇᴡ ᴀ: ᴜꜱᴇʀ ᴀᴄᴄᴏᴜɴᴛ ꜱᴇᴛᴛɪɴɢꜱ
            userSettingsContainer.setVisibility(View.VISIBLE);
            botSettingsContainer.setVisibility(View.GONE);
            settingsPanelTitle.setText(MiniFontConverter.convert("ꜱᴇᴛᴛɪɴɢꜱ"));
            settingsProfileStatus.setText(MiniFontConverter.convert("@" + sessionManager.getUsername()));
        }
    }

    private void openSettingsPanel() {
        settingsPanel.setVisibility(View.VISIBLE);
        // Post to ensure layout is measured before animating
        settingsPanel.post(() -> {
            int panelWidth = settingsPanel.getMeasuredWidth();
            if (panelWidth == 0) {
                panelWidth = ((View) settingsPanel.getParent()).getMeasuredWidth();
            }
            settingsPanel.setTranslationX(panelWidth);
            settingsPanel.animate()
                    .translationX(0)
                    .setDuration(300)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        });
    }

    private void closeSettingsPanel() {
        settingsPanel.animate()
                .translationX(settingsPanel.getWidth())
                .setDuration(250)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setListener(new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        settingsPanel.setVisibility(View.GONE);
                        settingsPanel.animate().setListener(null);
                    }
                })
                .start();
    }

    // ═══════════════════════════════════════════════════════════
    // ʙᴀᴄᴋɢʀᴏᴜɴᴅ ᴍᴀɴᴀɢᴇᴍᴇɴᴛ
    // ═══════════════════════════════════════════════════════════

    private void loadBackground() {
        backgroundManager.applyStoredBackground(backgroundImage, sessionManager,
                new BackgroundManager.BackgroundLoadCallback() {
                    @Override public void onStartLoading() {}

                    @Override
                    public void onSuccess(String imageUrl) {
                        if (!"default".equals(imageUrl)) {
                            // ᴀᴅᴊᴜꜱᴛ ᴏᴠᴇʀʟᴀʏ ꜰᴏʀ ʙᴇᴛᴛᴇʀ ᴠɪꜱɪʙɪʟɪᴛʏ
                            dimOverlay.setBackgroundColor(getResources().getColor(
                                    R.color.glass_dark_surface, getTheme()));
                        }
                    }

                    @Override public void onError(String errorMessage) {}
                });
    }

    private void applyCustomBackground() {
        String url = backgroundUrlInput.getText().toString().trim();
        if (url.isEmpty()) {
            MiniFontConverter.showToast(this, "ᴘʟᴇᴀꜱᴇ ᴇɴᴛᴇʀ ᴀɴ ɪᴍᴀɢᴇ ᴜʀʟ", Toast.LENGTH_SHORT);
            return;
        }

        sessionManager.setCustomBackgroundUrl(url);
        backgroundManager.loadBackground(backgroundImage, url,
                new BackgroundManager.BackgroundLoadCallback() {
                    @Override
                    public void onStartLoading() {
                        MiniFontConverter.showToast(MainActivity.this,
                                "ʟᴏᴀᴅɪɴɢ ʙᴀᴄᴋɢʀᴏᴜɴᴅ...", Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onSuccess(String imageUrl) {
                        dimOverlay.setBackgroundColor(getResources().getColor(
                                R.color.glass_dark_surface, getTheme()));
                        MiniFontConverter.showToast(MainActivity.this,
                                "ʙᴀᴄᴋɢʀᴏᴜɴᴅ ᴀᴘᴘʟɪᴇᴅ", Toast.LENGTH_SHORT);
                    }

                    @Override
                    public void onError(String errorMessage) {
                        MiniFontConverter.showToast(MainActivity.this,
                                "ꜰᴀɪʟᴇᴅ ᴛᴏ ʟᴏᴀᴅ: " + errorMessage, Toast.LENGTH_LONG);
                    }
                });
    }

    private void resetBackground() {
        sessionManager.setCustomBackgroundUrl("");
        backgroundImage.setImageDrawable(null);
        dimOverlay.setBackgroundColor(getResources().getColor(
                R.color.glass_bg_layer1_light, getTheme()));
        backgroundUrlInput.setText("");
        MiniFontConverter.showToast(this, "ᴅᴇꜰᴀᴜʟᴛ ʙᴀᴄᴋɢʀᴏᴜɴᴅ ʀᴇꜱᴛᴏʀᴇᴅ", Toast.LENGTH_SHORT);
    }

    // ═══════════════════════════════════════════════════════════
    // ꜱᴇᴛᴛɪɴɢꜱ ᴀᴄᴛɪᴏɴꜱ
    // ═══════════════════════════════════════════════════════════

    private void openProfileEditor() {
        Intent intent = new Intent(this, ProfileSettingsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.glass_slide_in_right, R.anim.glass_slide_out_left);
    }

    private void openTwoStepSettings() {
        MiniFontConverter.showToast(this, "2ꜰᴀ ꜱᴇᴛᴛɪɴɢꜱ — ᴄᴏᴍɪɴɢ ꜱᴏᴏɴ", Toast.LENGTH_SHORT);
    }

    private void openActiveSessions() {
        Intent intent = new Intent(this, ActiveSessionsActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.glass_slide_in_right, R.anim.glass_slide_out_left);
    }

    private void openPrivacySettings() {
        MiniFontConverter.showToast(this, "ᴘʀɪᴠᴀᴄʏ ꜱᴇᴛᴛɪɴɢꜱ — ᴄᴏᴍɪɴɢ ꜱᴏᴏɴ", Toast.LENGTH_SHORT);
    }

    private void openBotNameEditor() {
        Intent intent = new Intent(this, BotManagementActivity.class);
        startActivity(intent);
        overridePendingTransition(R.anim.glass_slide_in_right, R.anim.glass_slide_out_left);
    }

    private void openBotDescriptionEditor() {
        openBotNameEditor();
    }

    private void openCommandInspector() {
        Intent intent = new Intent(this, BotManagementActivity.class);
        intent.putExtra("tab", "inspector");
        startActivity(intent);
        overridePendingTransition(R.anim.glass_slide_in_right, R.anim.glass_slide_out_left);
    }

    private void openWebhookSettings() {
        MiniFontConverter.showToast(this, "ᴡᴇʙʜᴏᴏᴋ ꜱᴇᴛᴛɪɴɢꜱ — ᴄᴏᴍɪɴɢ ꜱᴏᴏɴ", Toast.LENGTH_SHORT);
    }

    private void onNewChatClicked() {
        MiniFontConverter.showToast(this, "ɴᴇᴡ ᴄʜᴀᴛ — ᴄᴏᴍɪɴɢ ꜱᴏᴏɴ", Toast.LENGTH_SHORT);
    }

    // ═══════════════════════════════════════════════════════════
    // ʟᴏɢᴏᴜᴛ
    // ═══════════════════════════════════════════════════════════

    private void showLogoutConfirmDialog() {
        AlertDialog dialog = new AlertDialog.Builder(this, R.style.GlassAlertDialog)
                .setTitle(MiniFontConverter.convert("ʟᴏɢᴏᴜᴛ"))
                .setMessage(MiniFontConverter.convert("ᴀʀᴇ ʏᴏᴜ ꜱᴜʀᴇ ʏᴏᴜ ᴡᴀɴᴛ ᴛᴏ ʟᴏɢ ᴏᴜᴛ?"))
                .setPositiveButton(MiniFontConverter.convert("ʟᴏɢᴏᴜᴛ"), (d, which) -> performLogout())
                .setNegativeButton(MiniFontConverter.convert("ᴄᴀɴᴄᴇʟ"), null)
                .create();
        dialog.show();
    }

    private void performLogout() {
        sessionManager.clearSession();
        navigateToLogin();
    }

    // ═══════════════════════════════════════════════════════════
    // ɴᴀᴠɪɢᴀᴛɪᴏɴ
    // ═══════════════════════════════════════════════════════════

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        overridePendingTransition(R.anim.glass_slide_in_left, R.anim.glass_slide_out_right);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (settingsPanel.getVisibility() == View.VISIBLE) {
            closeSettingsPanel();
            return;
        }
        super.onBackPressed();
    }

    // ═══════════════════════════════════════════════════════════
    // ʟɪꜰᴇᴄʏᴄʟᴇ
    // ═══════════════════════════════════════════════════════════

    @Override
    protected void onResume() {
        super.onResume();
        sessionManager.updateLastActive();

        // ʀᴇʟᴏᴀᴅ ꜱᴇᴛᴛɪɴɢꜱ ɪɴ ᴄᴀꜱᴇ ᴘʀᴏꜰɪʟᴇ ᴡᴀꜱ ᴇᴅɪᴛᴇᴅ
        String displayName = sessionManager.getDisplayName();
        if (displayName.isEmpty()) {
            displayName = "ᴜꜱᴇʀ";
        }
        settingsProfileName.setText(MiniFontConverter.convert(displayName));
    }
}
