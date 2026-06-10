/*
 * BackgroundManager.java — XGram
 * Asynchronous background image loading and caching system
 */

package com.alternative.telegram;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BackgroundManager {

    private static final String TAG = "BackgroundManager";

    private static final String CACHE_DIR = "backgrounds";
    private static final long MAX_CACHE_SIZE_BYTES = 50 * 1024 * 1024;
    private static final long MAX_CACHE_AGE_MS = 7 * 24 * 60 * 60 * 1000L;
    private static final int MAX_CONCURRENT_DOWNLOADS = 3;

    private static final int DEFAULT_BLUR_RADIUS = 8;
    private static final int CROSSFADE_DURATION_MS = 400;

    private static BackgroundManager instance;

    private final Context appContext;
    private final ExecutorService downloadExecutor;
    private final Handler mainHandler;
    private final File cacheDir;

    private volatile String currentLoadingUrl = null;

    private BackgroundManager(Context context) {
        this.appContext = context.getApplicationContext();
        this.downloadExecutor = Executors.newFixedThreadPool(MAX_CONCURRENT_DOWNLOADS);
        this.mainHandler = new Handler(Looper.getMainLooper());
        this.cacheDir = new File(appContext.getCacheDir(), CACHE_DIR);
        if (!cacheDir.exists()) {
            try {
                cacheDir.mkdirs();
            } catch (Exception e) {
                Log.e(TAG, "Failed to create cache directory", e);
            }
        }
        cleanupOldCache();
    }

    public static synchronized BackgroundManager getInstance(Context context) {
        if (instance == null) {
            instance = new BackgroundManager(context);
        }
        return instance;
    }

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

        if (callback != null) {
            callback.onStartLoading();
        }

        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Exception loading background", e);
            currentLoadingUrl = null;
            if (callback != null) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        }
    }

    public void loadBackgroundWithBlur(ImageView imageView, String imageUrl,
                                       int blurRadius, BackgroundLoadCallback callback) {
        if (imageView == null || imageUrl == null || imageUrl.trim().isEmpty()) {
            if (callback != null) callback.onError("Invalid parameters");
            return;
        }

        if (callback != null) callback.onStartLoading();

        try {
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
        } catch (Exception e) {
            Log.e(TAG, "Exception loading blurred background", e);
            if (callback != null) {
                mainHandler.post(() -> callback.onError(e.getMessage()));
            }
        }
    }

    public void loadBackgroundWithBlur(ImageView imageView, String imageUrl,
                                       BackgroundLoadCallback callback) {
        loadBackgroundWithBlur(imageView, imageUrl, DEFAULT_BLUR_RADIUS, callback);
    }

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

                Bitmap bitmap = Glide.with(appContext)
                        .asBitmap()
                        .load(imageUrl.trim())
                        .diskCacheStrategy(DiskCacheStrategy.ALL)
                        .submit()
                        .get();

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

    public void loadFromCache(ImageView imageView, String imageUrl) {
        if (imageView == null || imageUrl == null) return;

        File cachedFile = getCachedFile(imageUrl.trim());
        if (cachedFile.exists()) {
            try {
                Glide.with(appContext)
                        .load(cachedFile)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .transition(DrawableTransitionOptions.withCrossFade(200))
                        .into(imageView);
            } catch (Exception e) {
                Log.e(TAG, "Error loading from cache", e);
            }
        }
    }

    public void applyStoredBackground(ImageView imageView,
                                      SessionManager sessionManager,
                                      BackgroundLoadCallback callback) {
        if (imageView == null || sessionManager == null) {
            if (callback != null) callback.onError("Invalid parameters");
            return;
        }
        try {
            String storedUrl = sessionManager.getCustomBackgroundUrl();
            if (storedUrl != null && !storedUrl.isEmpty()) {
                loadBackground(imageView, storedUrl, callback);
            } else {
                imageView.setImageResource(R.drawable.glass_background_gradient);
                if (callback != null) callback.onSuccess("default");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error applying stored background", e);
            try {
                imageView.setImageResource(R.drawable.glass_background_gradient);
            } catch (Exception ex) {
                Log.e(TAG, "Error setting fallback background", ex);
            }
            if (callback != null) callback.onError(e.getMessage());
        }
    }

    public void clearCache() {
        downloadExecutor.submit(() -> {
            try {
                File[] files = cacheDir.listFiles();
                if (files != null) {
                    for (File file : files) {
                        file.delete();
                    }
                }
                try {
                    Glide.get(appContext).clearDiskCache();
                } catch (Exception e) {
                    Log.e(TAG, "Error clearing Glide disk cache", e);
                }
                Log.i(TAG, "Background cache cleared");
            } catch (Exception e) {
                Log.e(TAG, "Cache clear failed", e);
            }
        });
    }

    public long getCacheSize() {
        long size = 0;
        try {
            File[] files = cacheDir.listFiles();
            if (files != null) {
                for (File file : files) {
                    size += file.length();
                }
            }
        } catch (Exception e) {
            Log.e(TAG, "Error getting cache size", e);
        }
        return size;
    }

    public boolean isCached(String imageUrl) {
        if (imageUrl == null) return false;
        try {
            return getCachedFile(imageUrl.trim()).exists();
        } catch (Exception e) {
            return false;
        }
    }

    private File getCachedFile(String url) {
        String fileName = hashUrl(url) + ".jpg";
        return new File(cacheDir, fileName);
    }

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
            return String.valueOf(url.hashCode());
        }
    }

    private void saveToDiskCache(Bitmap bitmap, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
            fos.flush();
        }
    }

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
                enforceMaxCacheSize();
            } catch (Exception e) {
                Log.e(TAG, "Cache cleanup failed", e);
            }
        });
    }

    private void enforceMaxCacheSize() {
        try {
            long currentSize = getCacheSize();
            if (currentSize <= MAX_CACHE_SIZE_BYTES) return;

            File[] files = cacheDir.listFiles();
            if (files == null) return;

            java.util.Arrays.sort(files, (a, b) ->
                    Long.compare(a.lastModified(), b.lastModified()));

            for (File file : files) {
                if (currentSize <= MAX_CACHE_SIZE_BYTES) break;
                currentSize -= file.length();
                file.delete();
            }
        } catch (Exception e) {
            Log.e(TAG, "Error enforcing max cache size", e);
        }
    }

    public interface BackgroundLoadCallback {
        void onStartLoading();
        void onSuccess(String imageUrl);
        void onError(String errorMessage);
    }

    public static class SimpleCallback implements BackgroundLoadCallback {
        @Override public void onStartLoading() {}
        @Override public void onSuccess(String imageUrl) {}
        @Override public void onError(String errorMessage) {}
    }

    public void shutdown() {
        try {
            downloadExecutor.shutdown();
        } catch (Exception e) {
            Log.e(TAG, "Error shutting down executor", e);
        }
    }
}
