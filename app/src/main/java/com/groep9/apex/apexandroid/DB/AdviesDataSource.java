package com.groep9.apex.apexandroid.DB;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;

public class AdviesDataSource {
    // Database fields
    private SQLiteDatabase database;
    private MySQLiteHelper dbHelper;
    private String[] allColumns = {MySQLiteHelper.COLUMN_ID,
            MySQLiteHelper.COLUMN_ADVIESTITEL, MySQLiteHelper.COLUMN_ADVIESCONTENT, MySQLiteHelper.COLUMN_ADVIESCATEGORIE};

    public AdviesDataSource(Context context) {
        dbHelper = new MySQLiteHelper(context);
    }

    public void open() throws SQLException {
        database = dbHelper.getWritableDatabase();
    }

    public void close() {
        dbHelper.close();
    }

    public AdviesItem createAdviesItem(AdviesItem adviesItem) {
        ContentValues values = new ContentValues();
        values.put(MySQLiteHelper.COLUMN_ID, adviesItem.getId());
        values.put(MySQLiteHelper.COLUMN_ADVIESTITEL, adviesItem.getTitel());
        values.put(MySQLiteHelper.COLUMN_ADVIESCONTENT, adviesItem.getContent());
        values.put(MySQLiteHelper.COLUMN_ADVIESCATEGORIE, adviesItem.getCategoryId());
        long insertId = database.insert(MySQLiteHelper.TABLE_ADVIES, null,
                values);
        Cursor cursor = database.query(MySQLiteHelper.TABLE_ADVIES,
                allColumns, MySQLiteHelper.COLUMN_ID + " = " + insertId, null,
                null, null, null);
        cursor.moveToFirst();
        AdviesItem newAdviesItem = cursorToAdviesItem(cursor);
        cursor.close();
        return newAdviesItem;
    }

    public void deleteAdviesItem(AdviesItem adviesItem) {
        long id = adviesItem.getId();
        System.out.println("Advies deleted with id: " + id);
        database.delete(MySQLiteHelper.TABLE_ADVIES, MySQLiteHelper.COLUMN_ID
                + " = " + id, null);
    }

    public List<AdviesItem> getAllAdviesItems() {
        List<AdviesItem> adviezen = new ArrayList<>();

        Cursor cursor = database.query(MySQLiteHelper.TABLE_ADVIES,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            AdviesItem adviesItem = cursorToAdviesItem(cursor);
            adviezen.add(adviesItem);
            cursor.moveToNext();
        }

        // make sure to close the cursor
        cursor.close();
        return adviezen;
    }

    public boolean adviceExistInDb(int id) {
        String where = MySQLiteHelper.COLUMN_ID + " = " + id;
        Cursor cursor = database.query(MySQLiteHelper.TABLE_ADVIES,
                allColumns, where, null, null, null, null);
        if (cursor.getCount() <= 0) {
            cursor.close();
            return false;
        }
        cursor.close();
        return true;
    }

    private AdviesItem cursorToAdviesItem(Cursor cursor) {
        AdviesItem adviesItem = new AdviesItem();
        adviesItem.setId(cursor.getLong(0));
        adviesItem.setTitel(cursor.getString(1));
        adviesItem.setContent(cursor.getString(2));
        adviesItem.setCategoryId(cursor.getLong(3));
        return adviesItem;
    }
}
