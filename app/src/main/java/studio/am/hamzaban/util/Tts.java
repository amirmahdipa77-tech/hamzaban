package studio.am.hamzaban.util;

import android.content.Context;
import android.speech.tts.TextToSpeech;

import java.util.Locale;

import studio.am.hamzaban.R;

public class Tts implements TextToSpeech.OnInitListener {

    private TextToSpeech engine;
    private boolean ready;

    public Tts(Context c) {
        try {
            engine = new TextToSpeech(c.getApplicationContext(), this);
        } catch (Throwable t) {
            engine = null;
        }
    }

    @Override
    public void onInit(int status) {
        ready = status == TextToSpeech.SUCCESS;
    }

    public void speak(Context c, String text, String langCode) {
        if (text == null || text.trim().isEmpty()) return;
        try {
            if (engine == null || !ready) {
                Ui.toast(c, c.getString(R.string.tts_unavailable));
                return;
            }
            Locale locale = Locale.forLanguageTag(langCode);
            int res = engine.setLanguage(locale);
            if (res == TextToSpeech.LANG_MISSING_DATA || res == TextToSpeech.LANG_NOT_SUPPORTED) {
                Ui.toast(c, c.getString(R.string.tts_unavailable));
                return;
            }
            engine.setSpeechRate(0.95f);
            engine.speak(text, TextToSpeech.QUEUE_FLUSH, null, "hamzaban");
        } catch (Throwable t) {
            Ui.toast(c, c.getString(R.string.tts_unavailable));
        }
    }

    public void shutdown() {
        try {
            if (engine != null) {
                engine.stop();
                engine.shutdown();
            }
        } catch (Throwable ignored) {
        }
        engine = null;
        ready = false;
    }
}
