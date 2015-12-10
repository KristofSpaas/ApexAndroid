package com.groep9.apex.apexandroid.DB;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class StappenDataSource {

    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_STAPPEN, MySQLiteHelper.COLUMN_DATEINMILLIS};
    private String[] MIN_STAPPEN =
            new String[]{"min(" + MySQLiteHelper.COLUMN_STAPPEN + ")"};
    private String[] MAX_STAPPEN =
            new String[]{"max(" + MySQLiteHelper.COLUMN_STAPPEN + ")"};

    public StappenDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public StappenDataItem createStappenDataItem(StappenDataItem stappenDataItem) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_STAPPEN, stappenDataItem.getStappen());
        values.put(MySQLiteHelper.COLUMN_DATEINMILLIS, stappenDataItem.getDateInMiliis());
        long insertId = database.insert(MySQLiteHelper.TABLE_STAPPENDATA, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STAPPENDATA,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        StappenDataItem newStappenDataItem = cursorToStappenDataItem(cursor);
        cursor.close();
        return newStappenDataItem;
    }

    public void updateStappenDataItem(StappenDataItem stappenDataItem) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_STAPPEN, stappenDataItem.getStappen());
        values.put(MySQLiteHelper.COLUMN_DATEINMILLIS, stappenDataItem.getDateInMiliis());

        database.update(MySQLiteHelper.TABLE_STAPPENDATA, values, "_id " + "=" + stappenDataItem.getId(), null);
    }

    public int getMinStappen() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STAPPENDATA,
                MIN_STAPPEN, null, null, null, null, null);

        cursor.moveToFirst();
        int minStappen = cursor.getInt(0);
        cursor.close();

        return minStappen;
    }

    public int getMaxStappen() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STAPPENDATA,
                MAX_STAPPEN, null, null, null, null, null);

        cursor.moveToFirst();
        int maxStappen = cursor.getInt(0);
        cursor.close();

        return maxStappen;
    }

    public List<StappenDataItem> getFiveLatestStappenDataItems() {
        List<StappenDataItem> stappenDataItems = new ArrayList<>();

        String ORDER_BY_FIVE_LATEST = "_id DESC LIMIT 5";
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STAPPENDATA,
                allColumns, null, null, null, null, ORDER_BY_FIVE_LATEST);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            StappenDataItem stappenDataItem = cursorToStappenDataItem(cursor);
            stappenDataItems.add(stappenDataItem);
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();

        return stappenDataItems;
    }

    public StappenDataItem getLatestStappenDataItem() {
        String ORDER_BY_LATEST = "_id DESC LIMIT 1";
        Cursor cursor = database.query(MySQLiteHelper.TABLE_STAPPENDATA,
                allColumns, null, null, null, null, ORDER_BY_LATEST);

        StappenDataItem stappenDataItem = null;

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            stappenDataItem = cursorToStappenDataItem(cursor);
        }

        cursor.close();

        return stappenDataItem;
    }

    private StappenDataItem cursorToStappenDataItem(Cursor cursor) {
        StappenDataItem stappenDataItem = new StappenDataItem();
        stappenDataItem.setId(cursor.getLong(0));
        stappenDataItem.setStappen(cursor.getInt(1));
        stappenDataItem.setDateInMiliis(cursor.getLong(2));
        return stappenDataItem;
    }

}
