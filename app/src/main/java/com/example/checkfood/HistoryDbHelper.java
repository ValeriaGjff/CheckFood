package com.example.checkfood;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

import java.util.ArrayList;
import java.util.List;

public class HistoryDbHelper extends SQLiteOpenHelper {
    private static final String DB_NAME = "history.db";
    private static final int DB_VERSION = 1;

    public HistoryDbHelper(Context ctx) { super(ctx, DB_NAME, null, DB_VERSION); }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS history (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "barcode TEXT," +
                "name TEXT," +
                "ts INTEGER," +
                "safe INTEGER)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS history");
        onCreate(db);
    }

    public void insert(String barcode, String name, long ts, boolean safe) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("barcode", barcode);
        cv.put("name", name);
        cv.put("ts", ts);
        cv.put("safe", safe ? 1 : 0);
        db.insert("history", null, cv);
        db.close();
    }

    public List<HistoryItem> getAll() {
        List<HistoryItem> res = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT id,barcode,name,ts,safe FROM history ORDER BY ts DESC", null);
        while (c.moveToNext()) {
            res.add(new HistoryItem(
                    c.getLong(0),
                    c.getString(1),
                    c.getString(2),
                    c.getLong(3),
                    c.getInt(4) == 1
            ));
        }
        c.close();
        db.close();
        return res;
    }
}