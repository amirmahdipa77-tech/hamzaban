package studio.am.hamzaban.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import studio.am.hamzaban.core.Lexicon;
import studio.am.hamzaban.core.NmtEngine;
import studio.am.hamzaban.core.Translator;

public class TranslateViewModel extends AndroidViewModel {

    public static final int IDLE = 0;
    public static final int LIVE = 1;

    public static final class State {
        public final int kind;
        public final String text;
        public final int mode;
        public final boolean engineReady;
        public final boolean engineWorking;

        State(int kind, String text, int mode, boolean engineReady, boolean engineWorking) {
            this.kind = kind;
            this.text = text;
            this.mode = mode;
            this.engineReady = engineReady;
            this.engineWorking = engineWorking;
        }
    }

    private final MutableLiveData<State> state =
            new MutableLiveData<>(new State(IDLE, "", 0, false, false));
    private final ExecutorService exec = Executors.newSingleThreadExecutor();

    private int seq;
    private String currentSrc = "fa";
    private String currentTgt = "en";
    private String currentText = "";

    public TranslateViewModel(@NonNull Application app) {
        super(app);
        exec.execute(() -> {
            try {
                Lexicon.get(app);
            } catch (Throwable ignored) {
            }
        });
    }

    public LiveData<State> state() {
        return state;
    }

    public boolean engineReady(String src, String tgt) {
        return NmtEngine.isReady(src, tgt);
    }

    public void warmEngine(String src, String tgt) {
        NmtEngine.prepare(getApplication(), src, tgt, ready -> {
            State s = state.getValue();
            if (s != null && currentText.length() > 0) {
                state.postValue(new State(LIVE, s.text, s.mode, ready, false));
            }
        });
    }

    /** Real-time entry point: dictionary answer instantly, neural answer when ready. */
    public void translate(final String src, final String tgt, final String text) {
        currentSrc = src;
        currentTgt = tgt;
        currentText = text == null ? "" : text;
        final int job = ++seq;

        String trimmed = currentText.trim();
        if (trimmed.isEmpty()) {
            state.setValue(new State(IDLE, "", 0, NmtEngine.isReady(src, tgt), false));
            return;
        }

        boolean toEnglish = "fa".equals(src);
        String dictText = "";
        int dictMode = 0;
        try {
            Translator.Result r = Translator.translate(trimmed, toEnglish);
            if (r != null && r.mode != Translator.MODE_NONE && !r.text.isEmpty()) {
                dictText = r.text;
                dictMode = r.mode;
            }
        } catch (Throwable ignored) {
        }
        final String fallback = dictText;
        final int fallbackMode = dictMode;
        final boolean ready = NmtEngine.isReady(src, tgt);
        state.setValue(new State(LIVE, fallback, fallbackMode, ready, !ready));

        if (!ready) {
            NmtEngine.prepare(getApplication(), src, tgt, ok -> {
                if (job != seq) return;
                State s = state.getValue();
                state.postValue(new State(LIVE, s == null ? fallback : s.text,
                        s == null ? fallbackMode : s.mode, ok, false));
                if (ok && !currentText.trim().isEmpty()) {
                    translate(currentSrc, currentTgt, currentText);
                }
            });
            return;
        }

        exec.execute(() -> {
            try {
                String out = NmtEngine.translate(src, tgt, trimmed);
                if (job == seq && !out.isEmpty()) {
                    state.postValue(new State(LIVE, out, 3, true, false));
                }
            } catch (Throwable ignored) {
            }
        });
    }

    public void reset() {
        seq++;
        currentText = "";
        state.setValue(new State(IDLE, "", 0, NmtEngine.isReady(currentSrc, currentTgt), false));
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        exec.shutdownNow();
    }
}
