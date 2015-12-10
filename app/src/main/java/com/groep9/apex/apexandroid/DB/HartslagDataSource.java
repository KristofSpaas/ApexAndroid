package com.groep9.apex.apexandroid.DB;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class HartslagDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_HARTSLAG, MySQLiteHelper.COLUMN_DATEINMILLIS};
    private String[] MIN_HARTSLAG =
            new String[]{"min(" + MySQLiteHelper.COLUMN_HARTSLAG + ")"};
    private String[] MAX_HARTSLAG =
            new String[]{"max(" + MySQLiteHelper.COLUMN_HARTSLAG + ")"};

    public HartslagDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public HartslagDataItem createHartslagDataItem(HartslagDataItem hartslagDataItem) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_HARTSLAG, hartslagDataItem.getHartslag());
        values.put(MySQLiteHelper.COLUMN_DATEINMILLIS, hartslagDataItem.getDateInMiliis());
        long insertId = database.insert(MySQLiteHelper.TABLE_HARTSLAGDATA, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_HARTSLAGDATA,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        HartslagDataItem newHartslagDataItem = cursorToHartslagDataItem(cursor);
        cursor.close();
        return newHartslagDataItem;
    }

    public List<HartslagDataItem> getSixLatestHartslagDataItems() {
        List<HartslagDataItem> hartslagDataItems = new ArrayList<>();

        String ORDER_BY_SIX_LATEST = "_id DESC LIMIT 6";
        Cursor cursor = database.query(MySQLiteHelper.TABLE_HARTSLAGDATA,
                allColumns, null, null, null, null, ORDER_BY_SIX_LATEST);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            HartslagDataItem hartslagDataItem = cursorToHartslagDataItem(cursor);
            hartslagDataItems.add(hartslagDataItem);
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();

        return hartslagDataItems;
    }

    public HartslagDataItem getLatestHartslagDataItem() {
        String ORDER_BY_LATEST = "_id DESC LIMIT 1";
        Cursor cursor = database.query(MySQLiteHelper.TABLE_HARTSLAGDATA,
                allColumns, null, null, null, null, ORDER_BY_LATEST);

        HartslagDataItem hartslagDataItem = null;

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            hartslagDataItem = cursorToHartslagDataItem(cursor);
        }

        cursor.close();

        return hartslagDataItem;
    }

    public int getMinHartslag() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_HARTSLAGDATA,
                MIN_HARTSLAG, null, null, null, null, null);

        cursor.moveToFirst();
        int minHartslag = cursor.getInt(0);
        cursor.close();

        return minHartslag;
    }

    public int getMaxHartslag() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_HARTSLAGDATA,
                MAX_HARTSLAG, null, null, null, null, null);

        cursor.moveToFirst();
        int maxHartslag = cursor.getInt(0);
        cursor.close();

        return maxHartslag;
    }

    private HartslagDataItem cursorToHartslagDataItem(Cursor cursor) {
        HartslagDataItem hartslagDataItem = new HartslagDataItem();
        hartslagDataItem.setId(cursor.getLong(0));
        hartslagDataItem.setHartslag(cursor.getInt(1));
        hartslagDataItem.setDateInMiliis(cursor.getLong(2));
        return hartslagDataItem;
    }

}
