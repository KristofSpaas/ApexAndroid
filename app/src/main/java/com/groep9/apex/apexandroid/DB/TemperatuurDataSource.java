package com.groep9.apex.apexandroid.DB;


import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class TemperatuurDataSource {
    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_TEMPERATUUR, MySQLiteHelper.COLUMN_DATEINMILLIS};
    private String[] MIN_TEMPERATUUR =
            new String[]{"min(" + MySQLiteHelper.COLUMN_TEMPERATUUR + ")"};
    private String[] MAX_TEMPERATUUR =
            new String[]{"max(" + MySQLiteHelper.COLUMN_TEMPERATUUR + ")"};

    public TemperatuurDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public TemperatuurDataItem createTemperatuurDataItem(TemperatuurDataItem temperatuurDataItem) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_TEMPERATUUR, temperatuurDataItem.getTemperatuur());
        values.put(MySQLiteHelper.COLUMN_DATEINMILLIS, temperatuurDataItem.getDateInMiliis());
        long insertId = database.insert(MySQLiteHelper.TABLE_TEMPERATUURDATA, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_TEMPERATUURDATA,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        TemperatuurDataItem newTemperatuurDataItem = cursorToTemperatuurDataItem(cursor);
        cursor.close();
        return newTemperatuurDataItem;
    }

    public List<TemperatuurDataItem> getSixLatestTemperatuurDataItems() {
        List<TemperatuurDataItem> temperatuurDataItems = new ArrayList<>();

        String ORDER_BY_LATEST_SIX = "_id DESC LIMIT 6";
        Cursor cursor = database.query(MySQLiteHelper.TABLE_TEMPERATUURDATA,
                allColumns, null, null, null, null, ORDER_BY_LATEST_SIX);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            TemperatuurDataItem temperatuurDataItem = cursorToTemperatuurDataItem(cursor);
            temperatuurDataItems.add(temperatuurDataItem);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return temperatuurDataItems;
    }

    public TemperatuurDataItem getLatestTemperatuurDataItem() {
        String ORDER_BY_LATEST = "_id DESC LIMIT 1";
        Cursor cursor = database.query(MySQLiteHelper.TABLE_TEMPERATUURDATA,
                allColumns, null, null, null, null, ORDER_BY_LATEST);

        TemperatuurDataItem temperatuurDataItem = null;

        if (cursor.getCount() != 0) {
            cursor.moveToFirst();
            temperatuurDataItem = cursorToTemperatuurDataItem(cursor);
        }

        cursor.close();

        return temperatuurDataItem;
    }

    public float getMinTemperatuur() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_TEMPERATUURDATA,
                MIN_TEMPERATUUR, null, null, null, null, null);

        cursor.moveToFirst();
        float minTemperatuur = cursor.getFloat(0);
        cursor.close();

        return minTemperatuur;
    }

    public float getMaxTemperatuur() {
        Cursor cursor = database.query(MySQLiteHelper.TABLE_TEMPERATUURDATA,
                MAX_TEMPERATUUR, null, null, null, null, null);

        cursor.moveToFirst();
        float maxTemperatuur = cursor.getFloat(0);
        cursor.close();

        return maxTemperatuur;
    }

    private TemperatuurDataItem cursorToTemperatuurDataItem(Cursor cursor) {
        TemperatuurDataItem temperatuurDataItem = new TemperatuurDataItem();
        temperatuurDataItem.setId(cursor.getLong(0));
        temperatuurDataItem.setTemperatuur(cursor.getFloat(1));
        temperatuurDataItem.setDateInMiliis(cursor.getLong(2));
        return temperatuurDataItem;
    }

}
