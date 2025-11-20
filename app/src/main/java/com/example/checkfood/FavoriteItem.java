package com.example.checkfood;

public class FavoriteItem {
    public final String barcode;
    public final String name;
    public final long ts;

    public FavoriteItem(String barcode, String name, long ts) {
        this.barcode = barcode;
        this.name = name;
        this.ts = ts;
    }
}