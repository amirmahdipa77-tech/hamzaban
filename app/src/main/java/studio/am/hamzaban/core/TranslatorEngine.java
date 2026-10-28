package studio.am.hamzaban.core;

import com.google.mlkit.nl.translate.TranslatorOptions;
import com.google.mlkit.nl.translate.Translation;
import com.google.mlkit.nl.translate.Translator;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class TranslatorEngine {

    private static final Map<String, Translator> CACHE = new ConcurrentHashMap<>();

    private TranslatorEngine() {
    }

    private static String key(String src, String tgt) {
        return src + ">" + tgt;
    }

    public static Translator get(String src, String tgt) {
        String k = key(src, tgt);
        Translator existing = CACHE.get(k);
        if (existing != null) return existing;
        if (CACHE.size() > 4) closeAll();
        TranslatorOptions options = new TranslatorOptions.Builder()
                .setSourceLanguage(src)
                .setTargetLanguage(tgt)
                .build();
        Translator created = Translation.getClient(options);
        CACHE.put(k, created);
        return created;
    }

    public static void closeAll() {
        for (Translator t : CACHE.values()) {
            try {
                t.close();
            } catch (Throwable ignored) {
            }
        }
        CACHE.clear();
    }
}
