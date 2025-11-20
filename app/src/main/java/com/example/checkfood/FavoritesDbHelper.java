package com.example.checkfood;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class FavoritesDbHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "favorites.db";
    private static final int DB_VER = 1;

    public FavoritesDbHelper(Context ctx) {
        super(ctx, DB_NAME, null, DB_VER);
    }

    @Override public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE IF NOT EXISTS favorites (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                "barcode TEXT UNIQUE," +
                "name TEXT," +
                "ts INTEGER)");
    }

    @Override public void onUpgrade(SQLiteDatabase db, int oldV, int newV) {
        db.execSQL("DROP TABLE IF EXISTS favorites");
        onCreate(db);
    }

    public boolean isFavorite(String barcode) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.query("favorites", new String[]{"barcode"},
                "barcode=?", new String[]{barcode}, null, null, null);
        boolean exists = c.moveToFirst();
        c.close(); db.close();
        return exists;
    }

    public void add(String barcode, String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("barcode", barcode);
        cv.put("name", name);
        cv.put("ts", System.currentTimeMillis());
        db.insertWithOnConflict("favorites", null, cv, SQLiteDatabase.CONFLICT_IGNORE);
        db.close();
    }

    public void remove(String barcode) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete("favorites", "barcode=?", new String[]{barcode});
        db.close();
    }

    public List<FavoriteItem> getAll() {
        List<FavoriteItem> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT barcode,name,ts FROM favorites ORDER BY ts DESC", null);
        while (c.moveToNext()) {
            list.add(new FavoriteItem(
                    c.getString(0),
                    c.getString(1),
                    c.getLong(2)
            ));
        }
        c.close(); db.close();
        return list;
    }
}