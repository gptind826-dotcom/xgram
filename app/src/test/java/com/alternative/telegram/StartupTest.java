package com.alternative.telegram;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ApplicationProvider;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = {24, 34})
public class StartupTest {

    private Context context;
    private SessionManager sessionManager;

    @Before
    public void resetSession() {
        context = ApplicationProvider.getApplicationContext();
        context.getSharedPreferences("telegram_session_secure", Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences("telegram_session_fallback", Context.MODE_PRIVATE).edit().clear().commit();
        context.getSharedPreferences("telegram_session_general", Context.MODE_PRIVATE).edit().clear().commit();
        SessionManager.getInstance(context).forceReinitialize(context);
        sessionManager = SessionManager.getInstance(context);
    }

    @Test
    public void loginActivityStarts() {
        try (ActivityController<LoginActivity> controller =
                     Robolectric.buildActivity(LoginActivity.class).setup()) {
            LoginActivity activity = controller.get();
            assertFalse(activity.isFinishing());
            assertNotNull(activity.findViewById(R.id.loginTitle));
            assertNotNull(activity.findViewById(R.id.loginFormOverlay));
        }
    }

    @Test
    public void mainActivityStartsWithStoredSession() {
        sessionManager.savePhoneSession("+1", "5551234567", 1L);
        sessionManager.setDisplayName("Test User");

        try (ActivityController<MainActivity> controller =
                     Robolectric.buildActivity(MainActivity.class).setup()) {
            MainActivity activity = controller.get();
            assertFalse(activity.isFinishing());
            assertNotNull(activity.findViewById(R.id.mainViewPager));
            assertNotNull(activity.findViewById(R.id.settingsPanel));
        }
    }

    @Test
    public void loginActivityRecoversFromMalformedPreferences() {
        context.getSharedPreferences("telegram_session_general", Context.MODE_PRIVATE)
                .edit()
                .putString("is_logged_in", "true")
                .putBoolean("custom_background_url", true)
                .commit();
        sessionManager.forceReinitialize(context);
        sessionManager = SessionManager.getInstance(context);

        try (ActivityController<LoginActivity> controller =
                     Robolectric.buildActivity(LoginActivity.class).setup()) {
            LoginActivity activity = controller.get();
            assertFalse(activity.isFinishing());
            assertNotNull(activity.findViewById(R.id.loginTitle));
            assertFalse(sessionManager.isLoggedIn());
            assertFalse(context.getSharedPreferences(
                            "telegram_session_general", Context.MODE_PRIVATE)
                    .contains("custom_background_url"));
        }
    }

    @Test
    public void secondaryActivitiesInflate() {
        Intent otpIntent = new Intent(context, OtpVerificationActivity.class)
                .putExtra("phone_number", "+15551234567")
                .putExtra("country_code", "+1")
                .putExtra("phone", "5551234567")
                .putExtra("api_id", 2040)
                .putExtra("api_hash", "test")
                .putExtra("timeout", 1);

        try (ActivityController<OtpVerificationActivity> ignored =
                     Robolectric.buildActivity(OtpVerificationActivity.class, otpIntent).setup();
             ActivityController<ProfileSettingsActivity> ignoredProfile =
                     Robolectric.buildActivity(ProfileSettingsActivity.class).setup();
             ActivityController<BotManagementActivity> ignoredBot =
                     Robolectric.buildActivity(BotManagementActivity.class).setup();
             ActivityController<ActiveSessionsActivity> ignoredSessions =
                     Robolectric.buildActivity(ActiveSessionsActivity.class).setup()) {
            assertNotNull(ignored.get().findViewById(R.id.verifyButton));
            assertNotNull(ignoredProfile.get().findViewById(R.id.saveProfileButton));
            assertNotNull(ignoredBot.get().findViewById(R.id.saveBotButton));
            assertNotNull(ignoredSessions.get().findViewById(R.id.sessionsContainer));
        }
    }
}
