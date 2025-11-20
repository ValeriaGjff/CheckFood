package com.example.checkfood;

public class HistoryItem {
    public long id;
    public String barcode;
    public String name;
    public long timestamp; // millis
    public boolean safe;

    public HistoryItem(long id, String barcode, String name, long timestamp, boolean safe) {
        this.id = id;
        this.barcode = barcode;
        this.name = name;
        this.timestamp = timestamp;
        this.safe = safe;
    }
}