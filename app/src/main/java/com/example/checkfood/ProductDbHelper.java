package com.example.checkfood;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.content.ContentValues;
import android.database.Cursor;

public class ProductDbHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "products.db";
    private static final int DATABASE_VERSION = 1;

    public ProductDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(
                "CREATE TABLE IF NOT EXISTS products (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                        "barcode TEXT UNIQUE, " +
                        "name TEXT, " +
                        "category TEXT, " +
                        "ingredients TEXT)"
        );
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS products");
        onCreate(db);
    }

    public void addProduct(String barcode, String name, String category, String ingredients) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("barcode", barcode);
        values.put("name", name);
        values.put("category", category);
        values.put("ingredients", ingredients);
        db.insertWithOnConflict("products", null, values, SQLiteDatabase.CONFLICT_REPLACE);
        db.close();
    }

    public String getIngredientsByBarcode(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("products",
                new String[]{"ingredients"},
                "barcode=?",
                new String[]{barcode},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String ingredients = cursor.getString(0);
            cursor.close();
            db.close();
            return ingredients;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }

    public String getNameByBarcode(String barcode) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query("products",
                new String[]{"name"},
                "barcode=?",
                new String[]{barcode},
                null, null, null);
        if (cursor != null && cursor.moveToFirst()) {
            String name = cursor.getString(0);
            cursor.close();
            db.close();
            return name;
        }
        if (cursor != null) cursor.close();
        db.close();
        return null;
    }
}