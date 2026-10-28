package studio.am.hamzaban.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.Tasks;
import com.google.mlkit.common.model.DownloadConditions;
import com.google.mlkit.common.model.RemoteModelManager;
import com.google.mlkit.nl.translate.TranslateRemoteModel;
import com.google.mlkit.nl.translate.Translator;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import studio.am.hamzaban.R;
import studio.am.hamzaban.core.TranslatorEngine;
import studio.am.hamzaban.data.Entry;
import studio.am.hamzaban.data.HistoryRepo;

public class TranslateViewModel extends AndroidViewModel {

    public static final int IDLE = 0;
    public static final int WORKING = 1;
    public static final int NEEDS_MODEL = 2;
    public static final int DONE = 3;
    public static final int ERROR = 4;

    public static class State {
        public final int kind;
        public final String text;
        public final String message;
        public final long entryId;
        public final String lang;

        State(int kind, String text, String message, long entryId, String lang) {
            this.kind = kind;
            this.text = text;
            this.message = message;
            this.entryId = entryId;
            this.lang = lang;
        }
    }

    private final MutableLiveData<State> state = new MutableLiveData<>(new State(IDLE, null, null, -1, null));
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private int jobSeq;
    private String lastSrc = "fa";
    private String lastTgt = "en";
    private String lastText = "";

    public TranslateViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<State> state() {
        return state;
    }

    public String lastSrc() {
        return lastSrc;
    }

    public String lastTgt() {
        return lastTgt;
    }

    public void reset() {
        jobSeq++;
        state.setValue(new State(IDLE, null, null, -1, null));
    }

    public void translate(String src, String tgt, String text) {
        final String trimmed = text == null ? "" : text.trim();
        if (trimmed.isEmpty()) {
            state.setValue(new State(ERROR, null, getApplication().getString(R.string.err_empty), -1, null));
            return;
        }
        lastSrc = src;
        lastTgt = tgt;
        lastText = trimmed;
        final int seq = ++jobSeq;
        state.setValue(new State(WORKING, null, null, -1, null));
        exec.execute(() -> run(seq, src, tgt, trimmed, false));
    }

    /** Downloads the missing model, then translates automatically. */
    public void downloadAndTranslate(String lang) {
        final int seq = ++jobSeq;
        state.setValue(new State(WORKING, null, null, -1, null));
        exec.execute(() -> {
            try {
                TranslateRemoteModel model = new TranslateRemoteModel.Builder(lang).build();
                Tasks.await(RemoteModelManager.getInstance().download(
                        model, new DownloadConditions.Builder().build()), 8, TimeUnit.MINUTES);
            } catch (Throwable t) {
                if (seq == jobSeq) post(new State(ERROR, null, getApplication().getString(R.string.err_network), -1, null));
                return;
            }
            run(seq, lastSrc, lastTgt, lastText, true);
        });
    }

    private void run(int seq, String src, String tgt, String text, boolean skipCheck) {
        try {
            if (!skipCheck) {
                String missing = missingModel(src, tgt);
                if (missing != null) {
                    if (seq == jobSeq) post(new State(NEEDS_MODEL, null, null, -1, missing));
                    return;
                }
            }
            Translator translator = TranslatorEngine.get(src, tgt);
            String out = Tasks.await(translator.translate(text), 90, TimeUnit.SECONDS);
            if (out == null || out.trim().isEmpty()) {
                if (seq == jobSeq) post(new State(ERROR, null, getApplication().getString(R.string.err_generic), -1, null));
                return;
            }
            final Entry entry = new Entry(0, src, tgt, text, out.trim(), System.currentTimeMillis(), false);
            HistoryRepo.get(getApplication()).insert(entry, id -> {
                if (seq == jobSeq) post(new State(DONE, out.trim(), null, id, null));
            });
        } catch (Throwable t) {
            if (seq == jobSeq) post(new State(ERROR, null, messageFor(t), -1, null));
        }
    }

    private String messageFor(Throwable t) {
        String msg = t.getMessage() == null ? "" : t.getMessage().toLowerCase(java.util.Locale.US);
        if (msg.contains("network") || msg.contains("download") || msg.contains("connection")) {
            return getApplication().getString(R.string.err_network);
        }
        return getApplication().getString(R.string.err_generic);
    }

    private String missingModel(String src, String tgt) {
        try {
            RemoteModelManager manager = RemoteModelManager.getInstance();
            TranslateRemoteModel srcModel = new TranslateRemoteModel.Builder(src).build();
            TranslateRemoteModel tgtModel = new TranslateRemoteModel.Builder(tgt).build();
            Boolean hasSrc = Tasks.await(manager.isModelDownloaded(srcModel), 15, TimeUnit.SECONDS);
            if (hasSrc == null || !hasSrc) return src;
            Boolean hasTgt = Tasks.await(manager.isModelDownloaded(tgtModel), 15, TimeUnit.SECONDS);
            if (hasTgt == null || !hasTgt) return tgt;
            return null;
        } catch (Throwable t) {
            return null;
        }
    }

    private void post(State s) {
        state.postValue(s);
    }

    public void setFav(long id, boolean fav) {
        if (id <= 0) return;
        HistoryRepo.get(getApplication()).setFav(id, fav, null);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        exec.shutdownNow();
    }
}
