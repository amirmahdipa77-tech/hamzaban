package studio.am.hamzaban.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class DbHelper extends SQLiteOpenHelper {

    private static final String NAME = "hamzaban.db";
    private static final int VERSION = 1;

    public DbHelper(Context c) {
        super(c.getApplicationContext(), NAME, null, VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE entries ("
                + "_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                + "src TEXT NOT NULL,"
                + "tgt TEXT NOT NULL,"
                + "body TEXT NOT NULL,"
                + "out TEXT NOT NULL,"
                + "ts INTEGER NOT NULL,"
                + "fav INTEGER NOT NULL DEFAULT 0)");
        db.execSQL("CREATE INDEX idx_entries_ts ON entries (ts DESC)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // version 1: nothing to migrate yet
    }
}
