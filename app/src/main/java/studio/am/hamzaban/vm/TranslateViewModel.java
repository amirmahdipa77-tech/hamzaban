package studio.am.hamzaban.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import studio.am.hamzaban.R;
import studio.am.hamzaban.core.Lexicon;
import studio.am.hamzaban.core.Translator;
import studio.am.hamzaban.data.Entry;
import studio.am.hamzaban.data.HistoryRepo;

public class TranslateViewModel extends AndroidViewModel {

    public static final int IDLE = 0;
    public static final int WORKING = 1;
    public static final int DONE = 2;
    public static final int ERROR = 3;

    public static class State {
        public final int kind;
        public final String text;
        public final String message;
        public final long entryId;
        public final int mode;

        State(int kind, String text, String message, long entryId, int mode) {
            this.kind = kind;
            this.text = text;
            this.message = message;
            this.entryId = entryId;
            this.mode = mode;
        }
    }

    private final MutableLiveData<State> state =
            new MutableLiveData<>(new State(IDLE, null, null, -1, Translator.MODE_NONE));
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private int jobSeq;
    private volatile boolean lexiconReady;

    public TranslateViewModel(@NonNull Application app) {
        super(app);
        warmUp();
    }

    public LiveData<State> state() {
        return state;
    }

    public void warmUp() {
        exec.execute(() -> {
            try {
                Lexicon.get(getApplication());
                lexiconReady = true;
            } catch (Throwable ignored) {
            }
        });
    }

    public boolean lexiconReady() {
        return lexiconReady;
    }

    public void reset() {
        jobSeq++;
        state.setValue(new State(IDLE, null, null, -1, Translator.MODE_NONE));
    }

    public void translate(final String srcLang, final String tgtLang, final String text) {
        final String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            state.setValue(new State(ERROR, null,
                    getApplication().getString(R.string.err_empty), -1, Translator.MODE_NONE));
            return;
        }
        final int seq = ++jobSeq;
        state.setValue(new State(WORKING, null, null, -1, Translator.MODE_NONE));
        exec.execute(() -> {
            try {
                Lexicon.get(getApplication());
                lexiconReady = true;
                boolean toEnglish = "fa".equals(srcLang);
                Translator.Result result = Translator.translate(trimmed, toEnglish);
                if (result.mode == Translator.MODE_NONE || result.text.isEmpty()) {
                    if (seq == jobSeq) {
                        post(new State(ERROR, null,
                                getApplication().getString(R.string.err_not_found), -1,
                                Translator.MODE_NONE));
                    }
                    return;
                }
                final Entry entry = new Entry(0, srcLang, tgtLang, trimmed,
                        result.text, System.currentTimeMillis(), false);
                HistoryRepo.get(getApplication()).insert(entry, id -> {
                    if (seq == jobSeq) {
                        post(new State(DONE, result.text, null, id, result.mode));
                    }
                });
            } catch (Throwable t) {
                if (seq == jobSeq) {
                    post(new State(ERROR, null,
                            getApplication().getString(R.string.err_generic), -1, Translator.MODE_NONE));
                }
            }
        });
    }

    public void setFav(long id, boolean fav) {
        if (id <= 0) return;
        HistoryRepo.get(getApplication()).setFav(id, fav, null);
    }

    private void post(State s) {
        state.postValue(s);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        exec.shutdownNow();
    }
}
