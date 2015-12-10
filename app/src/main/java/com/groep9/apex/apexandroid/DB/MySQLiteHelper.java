package com.groep9.apex.apexandroid.DB;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

public class MySQLiteHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "apexhealth.db";
    private static final int DATABASE_VERSION = 1;

    public static final String COLUMN_ID = "_id";
    public static final String COLUMN_DATEINMILLIS = "dateinmillis";

    public static final String TABLE_HARTSLAGDATA = "hartslagdata";
    public static final String COLUMN_HARTSLAG = "hartslag";

    public static final String TABLE_TEMPERATUURDATA = "temperatuurdata";
    public static final String COLUMN_TEMPERATUUR = "temperatuur";

    public static final String TABLE_STAPPENDATA = "stappendata";
    public static final String COLUMN_STAPPEN = "stappen";

    public static final String TABLE_ADVIES = "advies";
    public static final String COLUMN_ADVIESTITEL = "adviestitel";
    public static final String COLUMN_ADVIESCONTENT = "adviescontent";
    public static final String COLUMN_ADVIESCATEGORIE = "adviescategorie";


    private static final String DATABASE_CREATE_HARTSLAGDATA = "create table "
            + TABLE_HARTSLAGDATA + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_HARTSLAG + " integer not null, "
            + COLUMN_DATEINMILLIS + " long not null);";

    private static final String DATABASE_CREATE_TEMPERATUURDATA = "create table "
            + TABLE_TEMPERATUURDATA + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_TEMPERATUUR + " float not null, "
            + COLUMN_DATEINMILLIS + " long not null);";

    private static final String DATABASE_CREATE_STAPPENDATA = "create table "
            + TABLE_STAPPENDATA + "("
            + COLUMN_ID + " integer primary key autoincrement, "
            + COLUMN_STAPPEN + " integer not null, "
            + COLUMN_DATEINMILLIS + " long not null);";

    private static final String DATABASE_CREATE_ADVIES = "create table "
            + TABLE_ADVIES + "("
            + COLUMN_ID + " integer primary key, "
            + COLUMN_ADVIESTITEL + " text not null, "
            + COLUMN_ADVIESCONTENT + " text not null, "
            + COLUMN_ADVIESCATEGORIE + " integer not null);";

    public MySQLiteHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE_HARTSLAGDATA);
        db.execSQL(DATABASE_CREATE_TEMPERATUURDATA);
        db.execSQL(DATABASE_CREATE_STAPPENDATA);
        db.execSQL(DATABASE_CREATE_ADVIES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.w(MySQLiteHelper.class.getName(),
                "Upgrading database from version " + oldVersion + " to "
                        + newVersion + ", which will destroy all old data");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_HARTSLAGDATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TEMPERATUURDATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_STAPPENDATA);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ADVIES);
        onCreate(db);
    }
}
