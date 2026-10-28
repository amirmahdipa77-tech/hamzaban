package studio.am.hamzaban.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HistoryRepo {

    public interface Cb<T> {
        void onResult(T value);
    }

    private static final int MAX_ROWS = 500;

    private static volatile HistoryRepo instance;

    public static HistoryRepo get(Context c) {
        if (instance == null) {
            synchronized (HistoryRepo.class) {
                if (instance == null) instance = new HistoryRepo(c.getApplicationContext());
            }
        }
        return instance;
    }

    private final DbHelper db;
    private final ExecutorService exec = Executors.newSingleThreadExecutor();
    private final Handler main = new Handler(Looper.getMainLooper());

    private HistoryRepo(Context c) {
        db = new DbHelper(c);
    }

    public void insert(final Entry e, final Cb<Long> cb) {
        exec.execute(() -> {
            long id = -1;
            try {
                SQLiteDatabase d = db.getWritableDatabase();
                ContentValues v = new ContentValues();
                v.put("src", e.src);
                v.put("tgt", e.tgt);
                v.put("body", e.body);
                v.put("out", e.out);
                v.put("ts", e.ts);
                v.put("fav", e.fav ? 1 : 0);
                id = d.insert("entries", null, v);
                d.execSQL("DELETE FROM entries WHERE fav = 0 AND _id NOT IN "
                        + "(SELECT _id FROM entries WHERE fav = 0 ORDER BY ts DESC LIMIT " + MAX_ROWS + ")");
            } catch (Throwable ignored) {
            }
            final long result = id;
            if (cb != null) main.post(() -> cb.onResult(result));
        });
    }

    public void query(final boolean favOnly, final String query, final Cb<List<Entry>> cb) {
        exec.execute(() -> {
            List<Entry> out = new ArrayList<>();
            Cursor c = null;
            try {
                SQLiteDatabase d = db.getReadableDatabase();
                String where = favOnly ? "fav = 1" : "1 = 1";
                String[] args = null;
                if (query != null && !query.trim().isEmpty()) {
                    where += " AND (body LIKE ? OR out LIKE ?)";
                    String like = "%" + query.trim() + "%";
                    args = new String[]{like, like};
                }
                c = d.query("entries", null, where, args, null, null, "ts DESC", String.valueOf(MAX_ROWS));
                while (c.moveToNext()) {
                    out.add(new Entry(
                            c.getLong(c.getColumnIndexOrThrow("_id")),
                            c.getString(c.getColumnIndexOrThrow("src")),
                            c.getString(c.getColumnIndexOrThrow("tgt")),
                            c.getString(c.getColumnIndexOrThrow("body")),
                            c.getString(c.getColumnIndexOrThrow("out")),
                            c.getLong(c.getColumnIndexOrThrow("ts")),
                            c.getInt(c.getColumnIndexOrThrow("fav")) == 1));
                }
            } catch (Throwable ignored) {
            } finally {
                if (c != null) {
                    try {
                        c.close();
                    } catch (Throwable ignored) {
                    }
                }
            }
            final List<Entry> result = out;
            main.post(() -> cb.onResult(result));
        });
    }

    public void setFav(final long id, final boolean fav, final Cb<Boolean> cb) {
        exec.execute(() -> {
            try {
                SQLiteDatabase d = db.getWritableDatabase();
                ContentValues v = new ContentValues();
                v.put("fav", fav ? 1 : 0);
                d.update("entries", v, "_id = ?", new String[]{String.valueOf(id)});
            } catch (Throwable ignored) {
            }
            if (cb != null) main.post(() -> cb.onResult(fav));
        });
    }

    public void delete(final long id, final Cb<Void> cb) {
        exec.execute(() -> {
            try {
                db.getWritableDatabase().delete("entries", "_id = ?", new String[]{String.valueOf(id)});
            } catch (Throwable ignored) {
            }
            if (cb != null) main.post(() -> cb.onResult(null));
        });
    }

    public void clearAll(final Cb<Void> cb) {
        exec.execute(() -> {
            try {
                db.getWritableDatabase().delete("entries", null, null);
            } catch (Throwable ignored) {
            }
            if (cb != null) main.post(() -> cb.onResult(null));
        });
    }
}
