package studio.am.hamzaban.core;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;
import com.google.mlkit.nl.translate.TranslatorOptions;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/** On-device neural engine with a cached, lazily downloaded language model. */
public final class NmtEngine {

    public interface Cb {
        void onResult(boolean ready);
    }

    private static final Map<String, Translator> CACHE = new ConcurrentHashMap<>();
    private static final Map<String, Boolean> READY = new ConcurrentHashMap<>();
    private static final ExecutorService EXEC = Executors.newSingleThreadExecutor();
    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    private NmtEngine() {
    }

    private static String key(String src, String tgt) {
        return src + ">" + tgt;
    }

    public static boolean isReady(String src, String tgt) {
        return Boolean.TRUE.equals(READY.get(key(src, tgt)));
    }

    private static Translator client(String src, String tgt) {
        String k = key(src, tgt);
        Translator existing = CACHE.get(k);
        if (existing != null) return existing;
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(src)
                .setTargetLanguage(tgt)
                .build();
        Translator created = Translation.getClient(options);
        CACHE.put(k, created);
        return created;
    }

    /** Downloads the model if needed, then reports readiness on the main thread. */
    public static void prepare(Context c, final String src, final String tgt, final Cb cb) {
        if (isReady(src, tgt)) {
            if (cb != null) MAIN.post(() -> cb.onResult(true));
            return;
        }
        final Context app = c.getApplicationContext();
        EXEC.execute(() -> {
            boolean ok = false;
            try {
                RemoteModelManager manager = RemoteModelManager.getInstance();
                Boolean a = Tasks.await(manager.isModelDownloaded(
                        new TranslateRemoteModel.Builder(src).build()), 20, TimeUnit.SECONDS);
                if (a == null || !a) {
                    Tasks.await(manager.download(new TranslateRemoteModel.Builder(src).build(),
                            new DownloadConditions.Builder().build()), 15, TimeUnit.MINUTES);
                }
                Boolean b = Tasks.await(manager.isModelDownloaded(
                        new TranslateRemoteModel.Builder(tgt).build()), 20, TimeUnit.SECONDS);
                if (b == null || !b) {
                    Tasks.await(manager.download(new TranslateRemoteModel.Builder(tgt).build(),
                            new DownloadConditions.Builder().build()), 15, TimeUnit.MINUTES);
                }
                client(src, tgt);
                ok = true;
            } catch (Throwable ignored) {
                ok = false;
            }
            if (ok) READY.put(key(src, tgt), Boolean.TRUE);
            final boolean ready = ok;
            if (cb != null) MAIN.post(() -> cb.onResult(ready));
            if (!ready) app.getSharedPreferences("hamzaban_prefs", 0);
        });
    }

    /** Blocking translation; call from a worker thread. */
    public static String translate(String src, String tgt, String text) throws Exception {
        Translator translator = client(src, tgt);
        Tasks.await(translator.downloadModelIfNeeded(
                new DownloadConditions.Builder().build()), 2, TimeUnit.MINUTES);
        String out = Tasks.await(translator.translate(text), 60, TimeUnit.SECONDS);
        READY.put(key(src, tgt), Boolean.TRUE);
        return out == null ? "" : out.trim();
    }

    public static void release() {
        for (Translator t : CACHE.values()) {
            try {
                t.close();
            } catch (Throwable ignored) {
            }
        }
        CACHE.clear();
        READY.clear();
    }
}
