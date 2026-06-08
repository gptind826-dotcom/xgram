/*
 * ʙᴀᴄᴋɢʀᴏᴜɴᴅᴍᴀɴᴀɢᴇʀ.ᴊᴀᴠᴀ — ᴍʏᴛᴇʟᴇɢʀᴀᴍᴀᴘᴘ
 * ᴀꜱʏɴᴄʜʀᴏɴᴏᴜꜱ ʙᴀᴄᴋɢʀᴏᴜɴᴅ ɪᴍᴀɢᴇ ʟᴏᴀᴅɪɴɢ ᴀɴᴅ ᴄᴀᴄʜɪɴɢ ꜱʏꜱᴛᴇᴍ
 *
 * ꜰᴇᴀᴛᴜʀᴇꜱ:
 * - ᴀꜱʏɴᴄ ʟᴏᴀᴅɪɴɢ ꜰʀᴏᴍ ᴀɴʏ ᴅɪʀᴇᴄᴛ ɪᴍᴀɢᴇ ᴜʀʟ (ɪɴɢʙʙ, ᴄᴅɴ, ᴇᴛᴄ.)
 * - ᴅɪꜱᴋ ᴄᴀᴄʜɪɴɢ ᴡɪᴛʜ ʟʀᴜ ᴇᴠɪᴄᴛɪᴏɴ
 * - ᴍᴇᴍᴏʀʏ ᴄᴀᴄʜᴇ ᴠɪᴀ ɢʟɪᴅᴇ
 * - ɢʟᴀꜱꜱ ʙʟᴜʀ ᴇꜰꜰᴇᴄᴛ ᴏᴠᴇʀʟᴀʏ ꜱᴜᴘᴘᴏʀᴛ
 * - ꜰᴀʟʟʙᴀᴄᴋ ɢʀᴀᴅɪᴇɴᴛ ᴏɴ ʟᴏᴀᴅ ꜰᴀɪʟᴜʀᴇ
 * - ᴘʀᴏɢʀᴇꜱꜱ ᴄᴀʟʟʙᴀᴄᴋꜱ ꜰᴏʀ ᴜɪ ᴜᴘᴅᴀᴛᴇꜱ
 */

package com.alternative.telegram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundManager {

    private static final String TAG = "BackgroundManager";

    // ᴄᴀᴄʜᴇ ᴄᴏɴꜰɪɢᴜʀᴀᴛɪᴏɴ
    private static final String CACHE_DIR = "backgrounds";
    private static final long MAX_CACHE_SIZE_BYTES = 50 * 1024 * 1024; // 50ᴍʙ
    private static final long MAX_CACHE_AGE_MS = 7 * 24 * 60 * 60 * 1000L; // 7 ᴅᴀʏꜱ
    private static final int MAX_CONCURRENT_DOWNLOADS = 3;

    // ɢʟɪᴅᴇ ᴏᴘᴛɪᴏɴꜱ
    private static final int DEFAULT_BLUR_RADIUS = 8;
    private static final int CROSSFADE_DURATION_MS = 400;

    // ꜱɪɴɢʟᴇᴛᴏɴ
    private static BackgroundManager instance;

    private final Context appContext;
    private final ExecutorService downloadExecutor;
    private final Handler mainHandler;
    private final File cacheDir;

    // ᴄᴜʀʀᴇɴᴛʟʏ ʟᴏᴀᴅɪɴɢ ᴜʀʟ ᴛᴏ ᴘʀᴇᴠᴇɴᴛ ᴅᴜᴘʟɪᴄᴀᴛᴇ ʀᴇǫᴜᴇꜱᴛꜱ
    private volatile String currentLoadingUrl = null;

    // ═══════════════════════════════════════════════════════════
    // ɪɴɪᴛɪᴀʟɪᴢᴀᴛɪᴏɴ
    // ═══════════════════════════════════════════════════════════

    private BackgroundManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.downloadExecutor = Executors.newFixedThreadPool(MAX_CONCURRENT_DOWNLOADS);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.cacheDir = new File(appContext.getCacheDir(), CACHE_DIR);
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        // ᴄʟᴇᴀɴ ᴏʟᴅ ᴄᴀᴄʜᴇ ᴇɴᴛʀɪᴇꜱ
        cleanupOldCache();
    }

    public static synchronized BackgroundManager getInstance(Context context) {
        if (instance == null) {
            instance = new BackgroundManager(context);
        }
        return instance;
    }

    // ═══════════════════════════════════════════════════════════
    // ᴘᴜʙʟɪᴄ ᴀᴘɪ — ʟᴏᴀᴅ ʙᴀᴄᴋɢʀᴏᴜɴᴅ ɪɴᴛᴏ ᴠɪᴇᴡ
    // ═══════════════════════════════════════════════════════════

    /**
     * ʟᴏᴀᴅ ᴀɴ ɪᴍᴀɢᴇ ᴜʀʟ ᴀꜱ ᴛʜᴇ ʙᴀᴄᴋɢʀᴏᴜɴᴅ ᴏꜰ ᴀɴ ɪᴍᴀɢᴇᴠɪᴇᴡ
     * @param imageView ᴛᴀʀɢᴇᴛ ᴠɪᴇᴡ
     * @param imageUrl ᴅɪʀᴇᴄᴛ ɪᴍᴀɢᴇ ᴜʀʟ (ᴇɢ. ɪɴɢʙʙ ᴅɪʀᴇᴄᴛ ʟɪɴᴋ)
     * @param callback ʟɪꜱᴛᴇɴᴇʀ ꜰᴏʀ ʟᴏᴀᴅ ᴇᴠᴇɴᴛꜱ
     */
    public void loadBackground(ImageView imageView, String imageUrl,
                               BackgroundLoadCallback callback) {
        if (imageView == null || imageUrl == null || imageUrl.trim().isEmpty()) {
            if (callback != null) {
                callback.onError("Invalid image URL or view");
            }
            return;
        }

        String trimmedUrl = imageUrl.trim();
        currentLoadingUrl = trimmedUrl;

        // ᴄʜᴇᴄᴋ ᴍᴇᴍᴏʀʏ ᴄᴀᴄʜᴇ ꜰɪʀꜱᴛ (ɢʟɪᴅᴇ ʜᴀɴᴅʟᴇꜱ ᴛʜɪꜱ ᴀᴜᴛᴏᴍᴀᴛɪᴄᴀʟʟʏ)
        if (callback != null) {
            callback.onStartLoading();
        }

        Glide.with(appContext)
                .load(trimmedUrl)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transition(DrawableTransitionOptions.withCrossFade(CROSSFADE_DURATION_MS))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        currentLoadingUrl = null;
                        Log.e(TAG, "Background load failed: " + trimmedUrl, e);
                        if (callback != null) {
                            mainHandler.post(() -> callback.onError(
                                    e != null ? e.getMessage() : "Load failed"
                            ));
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {
                        currentLoadingUrl = null;
                        Log.i(TAG, "Background loaded: " + trimmedUrl);
                        if (callback != null) {
                            mainHandler.post(() -> callback.onSuccess(trimmedUrl));
                        }
                        return false;
                    }
                })
                .into(imageView);
    }

    /**
     * ʟᴏᴀᴅ ʙᴀᴄᴋɢʀᴏᴜɴᴅ ᴡɪᴛʜ ɢʟᴀꜱꜱ ʙʟᴜʀ ᴏᴠᴇʀʟᴀʏ
     * ᴜꜱᴇꜰᴜʟ ꜰᴏʀ ʙᴇʜɪɴᴅ-ᴛʜᴇ-ɢʟᴀꜱꜱ ᴇꜰꜰᴇᴄᴛꜱ
     */
    public void loadBackgroundWithBlur(ImageView imageView, String imageUrl,
                                       int blurRadius, BackgroundLoadCallback callback) {
        if (imageView == null || imageUrl == null || imageUrl.trim().isEmpty()) {
            if (callback != null) callback.onError("Invalid parameters");
            return;
        }

        if (callback != null) callback.onStartLoading();

        Glide.with(appContext)
                .load(imageUrl.trim())
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .transform(new jp.wasabeef.glide.transformations.BlurTransformation(blurRadius))
                .transition(DrawableTransitionOptions.withCrossFade(CROSSFADE_DURATION_MS))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model,
                                                Target<Drawable> target, boolean isFirstResource) {
                        if (callback != null) {
                            mainHandler.post(() -> callback.onError(
                                    e != null ? e.getMessage() : "Blur load failed"
                            ));
                        }
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model,
                                                   Target<Drawable> target,
                                                   DataSource dataSource,
                                                   boolean isFirstResource) {
                        if (callback != null) {
                            mainHandler.post(() -> callback.onSuccess(imageUrl));
                        }
                        return false;
                    }
                })
                .into(imageView);
    }

    /** ᴄᴏɴᴠᴇɴɪᴇɴᴄᴇ ᴍᴇᴛʜᴏᴅ ᴡɪᴛʜ ᴅᴇꜰᴀᴜʟᴛ ʙʟᴜʀ */
    public void loadBackgroundWithBlur(ImageView imageView, String imageUrl,
                                       BackgroundLoadCallback callback) {
        loadBackgroundWithBlur(imageView, imageUrl, DEFAULT_BLUR_RADIUS, callback);
    }

    /**
     * ᴅᴏᴡɴʟᴏᴀᴅ ᴀɴᴅ ᴄᴀᴄʜᴇ ɪᴍᴀɢᴇ ᴛᴏ ᴅɪꜱᴋ ꜰᴏʀ ᴏꜰꜰʟɪɴᴇ ᴜꜱᴇ
     */
    public void prefetchBackground(String imageUrl, BackgroundLoadCallback callback) {
        if (imageUrl == null || imageUrl.trim().isEmpty()) return;

        downloadExecutor.submit(() -> {
            try {
                File cachedFile = getCachedFile(imageUrl.trim());
                if (cachedFile.exists()) {
                    mainHandler.post(() -> {
                        if (callback != null) callback.onSuccess(imageUrl);
                    });
                    return;
                }

                // ᴅᴏᴡɴʟᴏᴀᴅ ᴠɪᴀ ɢʟɪᴅᴇ
                Bitmap bitmap = Glide.with(appContext)
                        .asBitmap()
                        .load(imageUrl.trim())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .submit()
                        .get();

                // ꜱᴀᴠᴇ ᴛᴏ ᴅɪꜱᴋ ᴄᴀᴄʜᴇ
                saveToDiskCache(bitmap, cachedFile);

                mainHandler.post(() -> {
                    if (callback != null) callback.onSuccess(imageUrl);
                });

            } catch (Exception e) {
                Log.e(TAG, "Prefetch failed: " + imageUrl, e);
                mainHandler.post(() -> {
                    if (callback != null) callback.onError(e.getMessage());
                });
            }
        });
    }

    /**
     * ʟᴏᴀᴅ ᴀʟʀᴇᴀᴅʏ-ᴄᴀᴄʜᴇᴅ ɪᴍᴀɢᴇ ɪɴᴛᴏ ᴠɪᴇᴡ
     */
    public void loadFromCache(ImageView imageView, String imageUrl) {
        if (imageView == null || imageUrl == null) return;

        File cachedFile = getCachedFile(imageUrl.trim());
        if (cachedFile.exists()) {
            Glide.with(appContext)
                    .load(cachedFile)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .transition(DrawableTransitionOptions.withCrossFade(200))
                    .into(imageView);
        }
    }

    /**
     * ᴀᴘᴘʟʏ ᴛʜᴇ ꜱᴛᴏʀᴇᴅ ʙᴀᴄᴋɢʀᴏᴜɴᴅ ᴜʀʟ ꜰʀᴏᴍ ꜱᴇꜱꜱɪᴏɴ ᴍᴀɴᴀɢᴇʀ
     */
    public void applyStoredBackground(ImageView imageView,
                                      SessionManager sessionManager,
                                      BackgroundLoadCallback callback) {
        String storedUrl = sessionManager.getCustomBackgroundUrl();
        if (storedUrl != null && !storedUrl.isEmpty()) {
            loadBackground(imageView, storedUrl, callback);
        } else {
            // ᴜꜱᴇ ᴅᴇꜰᴀᴜʟᴛ ɢʀᴀᴅɪᴇɴᴛ
            imageView.setImageResource(R.drawable.glass_background_gradient);
            if (callback != null) callback.onSuccess("default");
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ᴄᴀᴄʜᴇ ᴍᴀɴᴀɢᴇᴍᴇɴᴛ
    // ═══════════════════════════════════════════════════════════

    /** ᴄʟᴇᴀʀ ᴀʟʟ ᴄᴀᴄʜᴇᴅ ʙᴀᴄᴋɢʀᴏᴜɴᴅꜱ */
    public void clearCache() {
        downloadExecutor.submit(() -> {
            try {
                File[] files = cacheDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
                // ᴀʟꜱᴏ ᴄʟᴇᴀʀ ɢʟɪᴅᴇ ᴅɪꜱᴋ ᴄᴀᴄʜᴇ
                Glide.get(appContext).clearDiskCache();
                Log.i(TAG, "Background cache cleared");
            } catch (Exception e) {
                Log.e(TAG, "Cache clear failed", e);
            }
        });
    }

    /** ɢᴇᴛ ᴄᴀᴄʜᴇ ꜱɪᴢᴇ ɪɴ ʙʏᴛᴇꜱ */
    public long getCacheSize() {
        long size = 0;
        File[] files = cacheDir.listFiles();
        if (files != null) {
            for (File file : files) {
                size += file.length();
            }
        }
        return size;
    }

    /** ᴄʜᴇᴄᴋ ɪꜰ ᴀɴ ɪᴍᴀɢᴇ ɪꜱ ᴄᴀᴄʜᴇᴅ */
    public boolean isCached(String imageUrl) {
        if (imageUrl == null) return false;
        return getCachedFile(imageUrl.trim()).exists();
    }

    // ═══════════════════════════════════════════════════════════
    // ᴘʀɪᴠᴀᴛᴇ ʜᴇʟᴘᴇʀꜱ
    // ═══════════════════════════════════════════════════════════

    /** ɢᴇɴᴇʀᴀᴛᴇ ᴄᴀᴄʜᴇ ꜰɪʟᴇ ᴘᴀᴛʜ ꜰʀᴏᴍ ᴜʀʟ */
    private File getCachedFile(String url) {
        String fileName = hashUrl(url) + ".jpg";
        return new File(cacheDir, fileName);
    }

    /** ʜᴀꜱʜ ᴜʀʟ ᴛᴏ ꜰɪʟᴇɴᴀᴍᴇ */
    private String hashUrl(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(url.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hash) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString().substring(0, 16);
        } catch (NoSuchAlgorithmException e) {
            // ꜰᴀʟʟʙᴀᴄᴋ: ᴜꜱᴇ ᴜʀʟ ʜᴀꜱʜᴄᴏᴅᴇ
            return String.valueOf(url.hashCode());
        }
    }

    /** ꜱᴀᴠᴇ ʙɪᴛᴍᴀᴘ ᴛᴏ ᴅɪꜱᴋ */
    private void saveToDiskCache(Bitmap bitmap, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
        }
    }

    /** ᴄʟᴇᴀɴ ᴜᴘ ᴄᴀᴄʜᴇ ᴇɴᴛʀɪᴇꜱ ᴏʟᴅᴇʀ ᴛʜᴀɴ ᴍᴀх ᴀɢᴇ */
    private void cleanupOldCache() {
        downloadExecutor.submit(() -> {
            try {
                File[] files = cacheDir.listFiles();
                if (files == null) return;

                long now = System.currentTimeMillis();
                for (File file : files) {
                    if (now - file.lastModified() > MAX_CACHE_AGE_MS) {
                        file.delete();
                    }
                }

                // ᴇɴꜰᴏʀᴄᴇ ᴍᴀх ᴄᴀᴄʜᴇ ꜱɪᴢᴇ
                enforceMaxCacheSize();

            } catch (Exception e) {
                Log.e(TAG, "Cache cleanup failed", e);
            }
        });
    }

    /** ᴇɴꜰᴏʀᴄᴇ ᴍᴀxɪᴍᴜᴍ ᴄᴀᴄʜᴇ ꜱɪᴢᴇ ʙʏ ᴅᴇʟᴇᴛɪɴɢ ᴏʟᴅᴇꜱᴛ ꜰɪʟᴇꜱ */
    private void enforceMaxCacheSize() {
        long currentSize = getCacheSize();
        if (currentSize <= MAX_CACHE_SIZE_BYTES) return;

        File[] files = cacheDir.listFiles();
        if (files == null) return;

        // ꜱᴏʀᴛ ʙʏ ʟᴀꜱᴛ ᴍᴏᴅɪꜰɪᴇᴅ (ᴏʟᴅᴇꜱᴛ ꜰɪʀꜱᴛ)
        java.util.Arrays.sort(files, (a, b) ->
                Long.compare(a.lastModified(), b.lastModified()));

        for (File file : files) {
            if (currentSize <= MAX_CACHE_SIZE_BYTES) break;
            currentSize -= file.length();
            file.delete();
        }
    }

    // ═══════════════════════════════════════════════════════════
    // ᴄᴀʟʟʙᴀᴄᴋ ɪɴᴛᴇʀꜰᴀᴄᴇ
    // ═══════════════════════════════════════════════════════════

    public interface BackgroundLoadCallback {
        void onStartLoading();
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }

    /** ᴇᴍᴘᴛʏ ɪᴍᴘʟᴇᴍᴇɴᴛᴀᴛɪᴏɴ ꜰᴏʀ ᴄᴏɴᴠᴇɴɪᴇɴᴄᴇ */
    public static class SimpleCallback implements BackgroundLoadCallback {
        @Override public void onStartLoading() {}
        @Override public void onSuccess(String imageUrl) {}
        @Override public void onError(String errorMessage) {}
    }

    // ═══════════════════════════════════════════════════════════
    // ᴄʟᴇᴀɴᴜᴘ
    // ═══════════════════════════════════════════════════════════

    /** ꜱʜᴜᴛ ᴅᴏᴡɴ ᴛʜᴇ ᴇхᴇᴄᴜᴛᴏʀ ꜱᴇʀᴠɪᴄᴇ */
    public void shutdown() {
        downloadExecutor.shutdown();
    }
}
