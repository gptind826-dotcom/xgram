/*
 * ᴏᴛᴘᴠᴇʀɪꜰɪᴄᴀᴛɪᴏɴᴀᴄᴛɪᴠɪᴛʏ.ᴊᴀᴠᴀ — xɢʀᴀᴍ
 * ꜱᴍꜢ ᴏᴛᴘ ᴄᴏᴅᴇ ᴠᴇʀɪꜰɪᴄᴀᴛɪᴏɴ
 * ʜᴀɴᴅʟᴇꜱ ᴏᴛᴘ ɪɴᴘᴜᴛ ᴀɴᴅ 2ꜰᴀ ᴘᴀꜱꜱᴡᴏʀᴅ
 * ɪɴᴛᴇɢʀᴀᴛᴇᴅ ᴡɪᴛʜ ᴛᴇʟᴇɢʀᴀᴍ ᴀᴜᴛʜ ᴍᴀɴᴀɢᴇʀ
 */

package com.alternative.telegram;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.alternative.telegram.api.TelegramAuthManager;

public class OtpVerificationActivity extends AppCompatActivity {

    private TextView titleText;
    private TextView subtitleText;
    private ImageButton backButton;
    private EditText otpInput;
    private EditText twoFaInput;
    private Button verifyButton;
    private Button resendButton;
    private TextView timerText;

    private CountDownTimer countDownTimer;
    private TelegramAuthManager authManager;

    // Intent extras
    private String phoneNumber;
    private String countryCode;
    private String phone;
    private int apiId;
    private String apiHash;
    private int timeout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        authManager = TelegramAuthManager.getInstance(this);

        // Extract intent extras
        phoneNumber = getIntent().getStringExtra("phone_number");
        countryCode = getIntent().getStringExtra("country_code");
        phone = getIntent().getStringExtra("phone");
        apiId = getIntent().getIntExtra("api_id", 2040);
        apiHash = getIntent().getStringExtra("api_hash");
        timeout = Math.max(1, getIntent().getIntExtra("timeout", 120));

        if (countryCode == null || phone == null || apiHash == null) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        titleText = findViewById(R.id.otpTitle);
        subtitleText = findViewById(R.id.otpSubtitle);
        backButton = findViewById(R.id.otpBackButton);
        otpInput = findViewById(R.id.otpCodeInput);
        twoFaInput = findViewById(R.id.twoFaInput);
        verifyButton = findViewById(R.id.verifyButton);
        resendButton = findViewById(R.id.resendButton);
        timerText = findViewById(R.id.timerText);

        // ᴀᴘᴘʟʏ ᴍɪɴɪ ꜰᴏɴᴛ
        MiniFontConverter.apply(titleText);
        MiniFontConverter.apply(subtitleText);
        MiniFontConverter.setHint(otpInput, "ᴏᴛᴘ ᴄᴏᴅᴇ");
        MiniFontConverter.setHint(twoFaInput, "2ꜰᴀ ᴘᴀꜱꜱᴡᴏʀᴅ (ɪꜰ ᴇɴᴀʙʟᴇᴅ)");
        MiniFontConverter.setText(verifyButton, "ᴠᴇʀɪꜰʏ");

        subtitleText.setText(MiniFontConverter.convert("ᴇɴᴛᴇʀ ᴛʜᴇ ᴄᴏᴅᴇ ꜱᴇɴᴛ ᴛᴏ " + maskPhone(phoneNumber)));

        backButton.setOnClickListener(v -> cancelAndFinish());
        verifyButton.setOnClickListener(v -> verifyCode());
        resendButton.setOnClickListener(v -> resendCode());

        startResendTimer(timeout * 1000L);

        getOnBackPressedDispatcher().addCallback(this, new androidx.activity.OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                cancelAndFinish();
            }
        });
    }

    private void cancelAndFinish() {
        authManager.cancelPhoneAuth();
        setResult(RESULT_CANCELED);
        finish();
    }

    private void verifyCode() {
        String otp = otpInput.getText().toString().trim();
        if (otp.isEmpty()) {
            MiniFontConverter.showToast(this, "ᴘʟᴇᴀꜱᴇ ᴇɴᴛᴇʀ ᴛʜᴇ ᴏᴛᴘ ᴄᴏᴅᴇ", Toast.LENGTH_SHORT);
            return;
        }

        verifyButton.setEnabled(false);
        verifyButton.setText(MiniFontConverter.convert("ᴠᴇʀɪꜰʏɪɴɢ..."));

        // Return OTP to LoginActivity for verification
        String twoFa = twoFaInput.getText().toString().trim();

        Intent resultIntent = new Intent();
        resultIntent.putExtra("otp_code", otp);
        resultIntent.putExtra("country_code", countryCode);
        resultIntent.putExtra("phone", phone);
        resultIntent.putExtra("api_id", apiId);
        resultIntent.putExtra("api_hash", apiHash);
        if (!twoFa.isEmpty()) {
            resultIntent.putExtra("two_fa_password", twoFa);
        }

        setResult(RESULT_OK, resultIntent);
        finish();
    }

    private void resendCode() {
        resendButton.setEnabled(false);
        resendButton.setText(MiniFontConverter.convert("ʀᴇꜱᴇɴᴅɪɴɢ..."));

        // Re-request the code via auth manager
        authManager.cancelPhoneAuth();

        authManager.requestPhoneCode(countryCode, phone, apiId, apiHash,
                new TelegramAuthManager.PhoneCodeCallback() {
                    @Override
                    public void onCodeSent(String type, int timeout) {
                        resendButton.setText(MiniFontConverter.convert("ʀᴇꜱᴇɴᴅ"));
                        MiniFontConverter.showToast(OtpVerificationActivity.this,
                                "ᴄᴏᴅᴇ ʀᴇꜱᴇɴᴛ", Toast.LENGTH_SHORT);
                        startResendTimer(Math.max(1, timeout) * 1000L);
                    }

                    @Override
                    public void onError(String error) {
                        resendButton.setEnabled(true);
                        resendButton.setText(MiniFontConverter.convert("ʀᴇꜱᴇɴᴅ"));
                        MiniFontConverter.showToast(OtpVerificationActivity.this,
                                "ꜰᴀɪʟᴇᴅ: " + error, Toast.LENGTH_LONG);
                    }
                });
    }

    private void startResendTimer(long durationMs) {
        resendButton.setEnabled(false);
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(durationMs, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerText.setText(MiniFontConverter.convert(
                        "ʀᴇꜱᴇɴᴅ ɪɴ " + (millisUntilFinished / 1000) + " ꜱᴇᴄᴏɴᴅꜱ"));
            }

            @Override
            public void onFinish() {
                timerText.setText("");
                resendButton.setEnabled(true);
            }
        }.start();
    }

    private String maskPhone(String phone) {
        if (phone == null || phone.length() < 4) return "***";
        return phone.substring(0, 2) + "***" + phone.substring(phone.length() - 2);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
