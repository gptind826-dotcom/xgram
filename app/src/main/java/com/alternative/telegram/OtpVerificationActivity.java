/*
 * ᴏᴛᴘᴠᴇʀɪꜰɪᴄᴀᴛɪᴏɴᴀᴄᴛɪᴠɪᴛʏ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ꜱᴍꜱ ᴏᴛᴘ ᴄᴏᴅᴇ ᴠᴇʀɪꜰɪᴄᴀᴛɪᴏɴ
 * ʜᴀɴᴅʟᴇꜱ ᴏᴛᴘ ɪɴᴘᴜᴛ ᴀɴᴅ 2ꜰᴀ ᴘᴀꜱꜱᴡᴏʀᴅ
 */

package com.alternative.telegram;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

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

        String phoneNumber = getIntent().getStringExtra("phone_number");
        subtitleText.setText(MiniFontConverter.convert("ᴇɴᴛᴇʀ ᴛʜᴇ ᴄᴏᴅᴇ ꜱᴇɴᴛ ᴛᴏ " + maskPhone(phoneNumber)));

        backButton.setOnClickListener(v -> finish());
        verifyButton.setOnClickListener(v -> verifyCode());
        resendButton.setOnClickListener(v -> resendCode());

        startResendTimer();
    }

    private void verifyCode() {
        String otp = otpInput.getText().toString().trim();
        if (otp.isEmpty()) {
            MiniFontConverter.showToast(this, "ᴘʟᴇᴀꜱᴇ ᴇɴᴛᴇʀ ᴛʜᴇ ᴏᴛᴘ ᴄᴏᴅᴇ", Toast.LENGTH_SHORT);
            return;
        }

        // ᴠᴇʀɪꜰʏ ᴏᴛᴘ (ᴍᴏᴄᴋ)
        MiniFontConverter.showToast(this, "ᴄᴏᴅᴇ ᴠᴇʀɪꜰɪᴇᴅ", Toast.LENGTH_SHORT);
        setResult(RESULT_OK);
        finish();
    }

    private void resendCode() {
        MiniFontConverter.showToast(this, "ᴄᴏᴅᴇ ʀᴇꜱᴇɴᴛ", Toast.LENGTH_SHORT);
        startResendTimer();
    }

    private void startResendTimer() {
        resendButton.setEnabled(false);
        if (countDownTimer != null) countDownTimer.cancel();

        countDownTimer = new CountDownTimer(60000, 1000) {
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

    @Override
    public void onBackPressed() {
        finish();
        overridePendingTransition(R.anim.glass_slide_in_left, R.anim.glass_slide_out_right);
    }
}
