/*
 * ᴍɪɴɪꜰᴏɴᴛᴄᴏɴᴠᴇʀᴛᴇʀ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ᴜɴɪᴠᴇʀꜱᴀʟ ᴍɪɴɪ-ᴜɴɪᴄᴏᴅᴇ ꜰᴏɴᴛ ᴛʀᴀɴꜱꜰᴏʀᴍᴀᴛɪᴏɴ ᴇɴɢɪɴᴇ
 *
 * ᴄᴏɴᴠᴇʀᴛꜱ ᴀʟʟ ᴠɪꜱɪʙʟᴇ ᴛᴇхᴛ ᴛᴏ ᴍɪɴɪ-ᴜɴɪᴄᴏᴅᴇ ꜱᴍᴀʟʟ ᴄᴀᴘꜱ ꜱᴛʏʟᴇ
 * ᴇх: "Hello World" → "ʜᴇʟʟᴏ ᴡᴏʀʟᴅ"
 * ᴇх: "Settings" → "ꜱᴇᴛᴛɪɴɢꜱ"
 * ᴇх: "John Doe" → "ᴊᴏʜɴ ᴅᴏᴇ"
 *
 * ᴜꜱᴇᴅ ꜰᴏʀ: ᴛᴇхᴛᴠɪᴇᴡꜱ, ʙᴜᴛᴛᴏɴꜱ, ᴛɪᴛʟᴇꜱ, ᴄʜᴀᴛ ᴍᴇꜱꜱᴀɢᴇꜱ,
 * ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴꜱ, ᴅɪᴀʟᴏɢꜱ, ᴇʀʀᴏʀ ᴍᴇꜱꜱᴀɢᴇꜱ, ꜱʏꜱᴛᴇᴍ ᴍᴇꜱꜱᴀɢᴇꜱ
 */

package com.alternative.telegram;

import android.content.Context;
import android.graphics.Typeface;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.style.StyleSpan;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.Toolbar;

import androidx.appcompat.app.ActionBar;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.android.material.snackbar.Snackbar;

import java.util.HashMap;
import java.util.Map;

public class MiniFontConverter {

    // ═══════════════════════════════════════════════════════════
    // ᴜɴɪᴄᴏᴅᴇ ᴍᴀᴘᴘɪɴɢ: ʟᴀᴛɪɴ → ᴍɪɴɪ ꜱᴍᴀʟʟ ᴄᴀᴘꜱ (ᴜ+1D00 ʙʟᴏᴄᴋ)
    // ═══════════════════════════════════════════════════════════

    private static final Map<Character, Character> MINI_FONT_MAP = new HashMap<>();
    private static final Map<Character, Character> REVERSE_MAP = new HashMap<>();

    static {
        // ʟᴏᴡᴇʀᴄᴀꜱᴇ ᴍᴀᴘᴘɪɴɢꜱ
        MINI_FONT_MAP.put('a', '\u1D00'); // ᴀ
        MINI_FONT_MAP.put('b', '\u0299'); // ʙ
        MINI_FONT_MAP.put('c', '\u1D04'); // ᴄ
        MINI_FONT_MAP.put('d', '\u1D05'); // ᴅ
        MINI_FONT_MAP.put('e', '\u1D07'); // ᴇ
        MINI_FONT_MAP.put('f', '\uA730'); // ꜰ
        MINI_FONT_MAP.put('g', '\u0262'); // ɢ
        MINI_FONT_MAP.put('h', '\u029C'); // ʜ
        MINI_FONT_MAP.put('i', '\u026A'); // ɪ
        MINI_FONT_MAP.put('j', '\u1D0A'); // ᴊ
        MINI_FONT_MAP.put('k', '\u1D0B'); // ᴋ
        MINI_FONT_MAP.put('l', '\u029F'); // ʟ
        MINI_FONT_MAP.put('m', '\u1D0D'); // ᴍ
        MINI_FONT_MAP.put('n', '\u0274'); // ɴ
        MINI_FONT_MAP.put('o', '\u1D0F'); // ᴏ
        MINI_FONT_MAP.put('p', '\u1D18'); // ᴘ
        MINI_FONT_MAP.put('q', '\uA7EE'); // Ꝼ (fallback to regular q shape)
        MINI_FONT_MAP.put('r', '\u0280'); // ʀ
        MINI_FONT_MAP.put('s', '\uA731'); // ꜱ
        MINI_FONT_MAP.put('t', '\u1D1B'); // ᴛ
        MINI_FONT_MAP.put('u', '\u1D1C'); // ᴜ
        MINI_FONT_MAP.put('v', '\u1D20'); // ᴠ
        MINI_FONT_MAP.put('w', '\u1D21'); // ᴡ
        MINI_FONT_MAP.put('x', '\u02E3'); // ˣ (using modifier)
        MINI_FONT_MAP.put('y', '\u028F'); // ʏ
        MINI_FONT_MAP.put('z', '\u1D22'); // ᴢ

        // ᴜᴘᴘᴇʀᴄᴀꜱᴇ ᴍᴀᴘᴘɪɴɢꜱ (ᴀʟꜱᴏ ᴛᴏ ꜱᴍᴀʟʟ ᴄᴀᴘꜱ)
        MINI_FONT_MAP.put('A', '\u1D00'); // ᴀ
        MINI_FONT_MAP.put('B', '\u0299'); // ʙ
        MINI_FONT_MAP.put('C', '\u1D04'); // ᴄ
        MINI_FONT_MAP.put('D', '\u1D05'); // ᴅ
        MINI_FONT_MAP.put('E', '\u1D07'); // ᴇ
        MINI_FONT_MAP.put('F', '\uA730'); // ꜰ
        MINI_FONT_MAP.put('G', '\u0262'); // ɢ
        MINI_FONT_MAP.put('H', '\u029C'); // ʜ
        MINI_FONT_MAP.put('I', '\u026A'); // ɪ
        MINI_FONT_MAP.put('J', '\u1D0A'); // ᴊ
        MINI_FONT_MAP.put('K', '\u1D0B'); // ᴋ
        MINI_FONT_MAP.put('L', '\u029F'); // ʟ
        MINI_FONT_MAP.put('M', '\u1D0D'); // ᴍ
        MINI_FONT_MAP.put('N', '\u0274'); // ɴ
        MINI_FONT_MAP.put('O', '\u1D0F'); // ᴏ
        MINI_FONT_MAP.put('P', '\u1D18'); // ᴘ
        MINI_FONT_MAP.put('Q', 'Q');      // Q (no mini variant)
        MINI_FONT_MAP.put('R', '\u0280'); // ʀ
        MINI_FONT_MAP.put('S', '\uA731'); // ꜱ
        MINI_FONT_MAP.put('T', '\u1D1B'); // ᴛ
        MINI_FONT_MAP.put('U', '\u1D1C'); // ᴜ
        MINI_FONT_MAP.put('V', '\u1D20'); // ᴠ
        MINI_FONT_MAP.put('W', '\u1D21'); // ᴡ
        MINI_FONT_MAP.put('X', '\u02E3'); // ˣ
        MINI_FONT_MAP.put('Y', '\u028F'); // ʏ
        MINI_FONT_MAP.put('Z', '\u1D22'); // ᴢ

        // ᴅɪɢɪᴛ ᴍᴀᴘᴘɪɴɢꜱ (ꜱᴜʙꜱᴄʀɪᴘᴛ ꜱᴛʏʟᴇ)
        MINI_FONT_MAP.put('0', '\u2080'); // ₀
        MINI_FONT_MAP.put('1', '\u2081'); // ₁
        MINI_FONT_MAP.put('2', '\u2082'); // ₂
        MINI_FONT_MAP.put('3', '\u2083'); // ₃
        MINI_FONT_MAP.put('4', '\u2084'); // ₄
        MINI_FONT_MAP.put('5', '\u2085'); // ₅
        MINI_FONT_MAP.put('6', '\u2086'); // ₆
        MINI_FONT_MAP.put('7', '\u2087'); // ₇
        MINI_FONT_MAP.put('8', '\u2088'); // ₈
        MINI_FONT_MAP.put('9', '\u2089'); // ₉

        // ʙᴜɪʟᴅ ʀᴇᴠᴇʀꜱᴇ ᴍᴀᴘ ꜰᴏʀ ᴅᴇᴄᴏᴅɪɴɢ ɪꜰ ɴᴇᴇᴅᴇᴅ
        for (Map.Entry<Character, Character> entry : MINI_FONT_MAP.entrySet()) {
            REVERSE_MAP.put(entry.getValue(), entry.getKey());
        }

        // ꜱᴘᴇᴄɪᴀʟ ᴄʜᴀʀᴀᴄᴛᴇʀꜱ (ᴘᴀꜱꜱ ᴛʜʀᴏᴜɢʜ ᴜɴᴄʜᴀɴɢᴇᴅ)
        // ꜱᴘᴀᴄᴇ, ᴘᴜɴᴄᴛᴜᴀᴛɪᴏɴ, ᴇᴍᴏᴊɪ, ᴄʏʀɪʟʟɪᴄ, ᴇᴛᴄ.
    }

    private MiniFontConverter() {
        // ᴜᴛɪʟɪᴛʏ ᴄʟᴀꜱꜱ — ɴᴏ ɪɴꜱᴛᴀɴᴛɪᴀᴛɪᴏɴ
    }

    /**
     * ᴄᴏɴᴠᴇʀᴛꜱ ᴀɴʏ ʟᴀᴛɪɴ ꜱᴛʀɪɴɢ ᴛᴏ ᴍɪɴɪ-ᴜɴɪᴄᴏᴅᴇ ꜱᴍᴀʟʟ ᴄᴀᴘꜱ
     * ɴᴏɴ-ʟᴀᴛɪɴ ᴄʜᴀʀᴀᴄᴛᴇʀꜱ ᴀʀᴇ ʟᴇꜰᴛ ᴜɴᴄʜᴀɴɢᴇᴅ
     */
    public static String convert(String input) {
        if (input == null || input.isEmpty()) {
            return input;
        }

        StringBuilder result = new StringBuilder(input.length());
        for (int i = 0; i < input.length(); i++) {
            char c = input.charAt(i);
            Character mini = MINI_FONT_MAP.get(c);
            if (mini != null) {
                result.append(mini);
            } else {
                result.append(c);
            }
        }
        return result.toString();
    }

    /**
     * ǫᴜɪᴄᴋ ᴄʜᴇᴄᴋ: ɪꜱ ᴛʜɪꜱ ᴄʜᴀʀᴀᴄᴛᴇʀ ᴀ ʟᴀᴛɪɴ ʟᴇᴛᴛᴇʀ ᴡᴇ ᴄᴀɴ ᴄᴏɴᴠᴇʀᴛ?
     */
    public static boolean isConvertible(char c) {
        return MINI_FONT_MAP.containsKey(c);
    }

    /**
     * ᴄʜᴇᴄᴋ ɪꜰ ᴀ ꜱᴛʀɪɴɢ ɴᴇᴇᴅꜱ ᴄᴏɴᴠᴇʀꜱɪᴏɴ
     */
    public static boolean needsConversion(String input) {
        if (input == null || input.isEmpty()) {
            return false;
        }
        for (int i = 0; i < input.length(); i++) {
            if (MINI_FONT_MAP.containsKey(input.charAt(i))) {
                return true;
            }
        }
        return false;
    }

    // ═══════════════════════════════════════════════════════════
    // ᴠɪᴇᴡ ᴡʀᴀᴘᴘᴇʀꜱ — ᴀᴜᴛᴏ-ᴀᴘᴘʟʏ ᴍɪɴɪ ꜰᴏɴᴛ ᴛᴏ ᴀɴʏ ᴠɪᴇᴡ
    // ═══════════════════════════════════════════════════════════

    /** ᴀᴘᴘʟʏ ᴍɪɴɪ ꜰᴏɴᴛ ᴛᴏ ᴀ ᴛᴇхᴛᴠɪᴇᴡ */
    public static void apply(TextView textView) {
        if (textView == null) return;
        CharSequence current = textView.getText();
        if (current != null) {
            textView.setText(convert(current.toString()));
        }
    }

    /** ꜱᴇᴛ ᴛᴇхᴛ ᴡɪᴛʜ ᴀᴜᴛᴏᴍᴀᴛɪᴄ ᴍɪɴɪ ꜰᴏɴᴛ ᴄᴏɴᴠᴇʀꜱɪᴏɴ */
    public static void setText(TextView textView, String text) {
        if (textView == null) return;
        textView.setText(convert(text));
    }

    /** ᴀᴘᴘʟʏ ᴛᴏ ʙᴜᴛᴛᴏɴ */
    public static void apply(Button button) {
        if (button == null) return;
        CharSequence current = button.getText();
        if (current != null) {
            button.setText(convert(current.toString()));
        }
    }

    /** ꜱᴇᴛ ʙᴜᴛᴛᴏɴ ᴛᴇхᴛ ᴡɪᴛʜ ᴍɪɴɪ ꜰᴏɴᴛ */
    public static void setText(Button button, String text) {
        if (button == null) return;
        button.setText(convert(text));
    }

    /** ᴀᴘᴘʟʏ ᴛᴏ ᴇᴅɪᴛᴛᴇхᴛ ʜɪɴᴛ */
    public static void setHint(EditText editText, String hint) {
        if (editText == null) return;
        editText.setHint(convert(hint));
    }

    /** ᴀᴘᴘʟʏ ᴛᴏ ᴛᴏᴏʟʙᴀʀ ᴛɪᴛʟᴇ */
    public static void setTitle(Toolbar toolbar, String title) {
        if (toolbar == null) return;
        toolbar.setTitle(convert(title));
    }

    /** ᴀᴘᴘʟʏ ᴛᴏ ᴀᴄᴛɪᴏɴ ʙᴀʀ */
    public static void setTitle(ActionBar actionBar, String title) {
        if (actionBar == null) return;
        actionBar.setTitle(convert(title));
    }

    // ═══════════════════════════════════════════════════════════
    // ᴄᴏɴᴠᴇɴɪᴇɴᴄᴇ ᴍᴇᴛʜᴏᴅꜱ ꜰᴏʀ ᴄᴏᴍᴍᴏɴ ᴜɪ ᴘᴀᴛᴛᴇʀɴꜱ
    // ═══════════════════════════════════════════════════════════

    /** ᴀᴘᴘʟʏ ᴛᴏ ᴀʟʟ ᴄʜɪʟᴅ ᴛᴇхᴛᴠɪᴇᴡꜱ ᴏꜰ ᴀ ᴘᴀʀᴇɴᴛ (ʀᴇᴄᴜʀꜱɪᴠᴇ) */
    public static void applyToAllTextViews(android.view.ViewGroup parent) {
        if (parent == null) return;
        for (int i = 0; i < parent.getChildCount(); i++) {
            android.view.View child = parent.getChildAt(i);
            if (child instanceof TextView) {
                apply((TextView) child);
            } else if (child instanceof android.view.ViewGroup) {
                applyToAllTextViews((android.view.ViewGroup) child);
            }
        }
    }

    /** ᴍɪɴɪ ꜰᴏɴᴛ ᴛᴏᴀꜱᴛ */
    public static void showToast(Context context, String message, int duration) {
        Toast.makeText(context, convert(message), duration).show();
    }

    /** ᴍɪɴɪ ꜰᴏɴᴛ ꜱɴᴀᴄᴋʙᴀʀ */
    public static void showSnackbar(android.view.View anchor, String message, int duration) {
        Snackbar snackbar = Snackbar.make(anchor, convert(message), duration);
        snackbar.show();
    }

    /** ᴍɪɴɪ ꜰᴏɴᴛ ꜱɴᴀᴄᴋʙᴀʀ ᴡɪᴛʜ ᴀᴄᴛɪᴏɴ */
    public static void showSnackbar(android.view.View anchor, String message,
                                    String actionText, android.view.View.OnClickListener action,
                                    int duration) {
        Snackbar snackbar = Snackbar.make(anchor, convert(message), duration);
        snackbar.setAction(convert(actionText), action);
        snackbar.show();
    }

    /** ᴄᴏɴᴠᴇʀᴛ ꜰᴏʀ ᴅᴇʙᴜɢ ʟᴏɢꜱ (ᴍɪɴɪ ꜱᴛʏʟᴇ) */
    public static String logTag(String tag) {
        return convert(tag);
    }

    /** ᴄᴏɴᴠᴇʀᴛ ꜰᴏʀ ɴᴏᴛɪꜰɪᴄᴀᴛɪᴏɴ ᴛɪᴛʟᴇꜱ ᴀɴᴅ ʙᴏᴅɪᴇꜱ */
    public static android.app.Notification.Builder applyToNotification(
            Context context, android.app.Notification.Builder builder,
            String title, String content) {
        return builder
                .setContentTitle(convert(title))
                .setContentText(convert(content));
    }

    /** ɢᴇᴛ ᴛʜᴇ ᴜɴɪᴄᴏᴅᴇ ᴍᴀᴘ ꜰᴏʀ ᴄᴜꜱᴛᴏᴍ ᴜꜱᴇ */
    public static Map<Character, Character> getFontMap() {
        return new HashMap<>(MINI_FONT_MAP);
    }

    /** ɢᴇᴛ ᴄʜᴀʀᴀᴄᴛᴇʀ ᴄᴏᴜɴᴛ (ᴍɪɴɪ ᴄʜᴀʀꜱ ᴍᴀʏ ʙᴇ ᴄᴏᴍʙɪɴɪɴɢ) */
    public static int getDisplayLength(String text) {
        if (text == null) return 0;
        return text.codePointCount(0, text.length());
    }
}
