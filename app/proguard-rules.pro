# ᴘʀᴏɢᴜᴀʀᴅ-ʀᴜʟᴇꜱ.ᴘʀᴏ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
# ᴘʀᴏɢᴜᴀʀᴅ ʀᴜʟᴇꜱ ꜰᴏʀ ʀᴇʟᴇᴀꜱᴇ ʙᴜɪʟᴅꜱ

# ɢʟɪᴅᴇ
-keep public class * implements com.bumptech.glide.module.GlideModule
-keep class * extends com.bumptech.glide.module.AppGlideModule {
 <init>(...);
}
-keep public enum com.bumptech.glide.load.ImageHeaderParser$** {
  **[] $VALUES;
  public *;
}
-keep class com.bumptech.glide.load.data.ParcelFileDescriptorRewinder$InternalRewinder {
  *** rewind();
}

# ɢꜱᴏɴ
-keepattributes Signature
-keepattributes *Annotation*
-dontwarn sun.misc.**
-keep class com.google.gson.** { *; }
-keep class * extends com.google.gson.TypeAdapter
-keep class * implements com.google.gson.TypeAdapterFactory
-keep class * implements com.google.gson.JsonSerializer
-keep class * implements com.google.gson.JsonDeserializer

# ᴏᴋʜᴛᴛᴘ
-dontwarn okhttp3.**
-dontwarn okio.**
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }

# ᴀɴᴅʀᴏɪᴅх
-keep class androidx.** { *; }
-keep interface androidx.** { *; }
-dontwarn androidx.**

# ᴛᴇʟᴇɢʀᴀᴍ ᴍᴏᴅᴇʟ ᴄʟᴀꜱꜱᴇꜱ
-keep class com.alternative.telegram.** { *; }

# ᴋᴇᴇᴘ ᴍɪɴɪ ꜰᴏɴᴛ ᴄᴏɴᴠᴇʀᴛᴇʀ
-keep class com.alternative.telegram.MiniFontConverter { *; }
-keep class com.alternative.telegram.SessionParser { *; }
-keep class com.alternative.telegram.SessionManager { *; }

# ɢᴇɴᴇʀᴀʟ
-keepattributes SourceFile,LineNumberTable
-keepattributes *JavascriptInterface*
-renamesourcefileattribute SourceFile
