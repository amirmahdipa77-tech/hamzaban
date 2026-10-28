package studio.am.hamzaban.vm;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import java.util.Collections;
import java.util.List;

import studio.am.hamzaban.data.Entry;
import studio.am.hamzaban.data.HistoryRepo;

public class HistoryViewModel extends AndroidViewModel {

    private final MutableLiveData<List<Entry>> items = new MutableLiveData<>(Collections.emptyList());
    private boolean favOnly;
    private String query = "";

    public HistoryViewModel(@NonNull Application app) {
        super(app);
    }

    public LiveData<List<Entry>> items() {
        return items;
    }

    public void load(boolean favOnly, String query) {
        this.favOnly = favOnly;
        this.query = query == null ? "" : query;
        HistoryRepo.get(getApplication()).query(favOnly, this.query, items::postValue);
    }

    public void refresh() {
        load(favOnly, query);
    }

    public void toggleFav(Entry entry) {
        HistoryRepo.get(getApplication()).setFav(entry.id, !entry.fav, fav -> refresh());
    }

    public void delete(Entry entry) {
        HistoryRepo.get(getApplication()).delete(entry.id, v -> refresh());
    }

    public void clearAll() {
        HistoryRepo.get(getApplication()).clearAll(v -> refresh());
    }
}
