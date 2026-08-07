package com.alternative.telegram;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

import android.content.Context;
import android.content.Intent;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.platform.app.InstrumentationRegistry;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(AndroidJUnit4.class)
public class StartupSmokeTest {

    private Context appContext;
    private SessionManager sessionManager;

    @Before
    public void resetSession() {
        appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
        sessionManager = SessionManager.getInstance(appContext);
        sessionManager.clearSession();
    }

    @Test
    public void loginScreenLaunches() {
        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.loginTitle)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void unauthenticatedDashboardRedirectsWithoutCrashing() {
        Intent intent = new Intent(appContext, MainActivity.class)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        appContext.startActivity(intent);

        onView(withId(R.id.loginTitle)).check(matches(isDisplayed()));
    }

    @Test
    public void malformedPersistedSettingsFallBackToLoginScreen() {
        appContext.getSharedPreferences("telegram_session_general", Context.MODE_PRIVATE)
                .edit()
                .putString("is_logged_in", "true")
                .putBoolean("custom_background_url", true)
                .commit();
        sessionManager.forceReinitialize(appContext);

        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.loginTitle)).check(matches(isDisplayed()));
        }
    }

    @Test
    public void staleLoginFlagFallsBackToLoginScreen() {
        appContext.getSharedPreferences("telegram_session_general", Context.MODE_PRIVATE)
                .edit()
                .putBoolean("is_logged_in", true)
                .putString("login_method", SessionManager.LOGIN_METHOD_BOT)
                .commit();
        sessionManager.forceReinitialize(appContext);

        try (ActivityScenario<LoginActivity> ignored =
                     ActivityScenario.launch(LoginActivity.class)) {
            onView(withId(R.id.loginTitle)).check(matches(isDisplayed()));
        }
    }
}
