package com.griscom.codereview.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Single file database helper
 */
@SuppressWarnings("WeakerAccess")
public class SingleFileDatabase extends SQLiteOpenHelper
{
    @SuppressWarnings("unused")
    private static final String TAG = "SingleFileDatabase";



    private static final String DB_NAME    = "file";
    private static final int    DB_VERSION = 1;



    public static final String COLUMN_ID   = "_id";
    public static final String COLUMN_TYPE = "_type";



    private static final String[] ROWS_COLUMNS = {
                                                    COLUMN_ID,
                                                    COLUMN_TYPE
                                                 };



    public  static final String ROWS_TABLE_NAME   = "rows";
    private static final String ROWS_TABLE_CREATE = "CREATE TABLE " + ROWS_TABLE_NAME + ' ' +
                                                    '(' +
                                                         COLUMN_ID   + " INTEGER PRIMARY KEY, " +
                                                         COLUMN_TYPE + " TEXT NOT NULL"         +
                                                    ");";



    private String mDbName;



    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        return "SingleFileDatabase{" +
                "mDbName='" + mDbName + '\'' +
                '}';
    }

    /**
     * Constructs SingleFileDatabase instance
     * @param context    context
     * @param fileId     file ID in DB
     */
    private SingleFileDatabase(Context context, long fileId)
    {
        super(context, DB_NAME + fileId + ".db", null, DB_VERSION);

        mDbName = DB_NAME + fileId + ".db";
    }

    /**
     * Constructs SingleFileDatabase instance
     * @param context    context
     * @param fileId     file ID in DB
     */
    public static SingleFileDatabase newInstance(Context context, long fileId)
    {
        return new SingleFileDatabase(context, fileId);
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(ROWS_TABLE_CREATE);
    }

    /** {@inheritDoc} */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Nothing
    }

    /**
     * Inserts or updates specified row
     * @param db      database
     * @param row     row ID
     * @param type    type of row (reviewed/invalid)
     */
    public static void insertOrUpdateRow(SQLiteDatabase db, int row, char type)
    {
        ContentValues values = new ContentValues();

        values.put(COLUMN_ID,   row + 1);
        values.put(COLUMN_TYPE, String.valueOf(type));

        db.insertWithOnConflict(ROWS_TABLE_NAME, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Removes specified row
     * @param db     database
     * @param row    row ID
     */
    public static void removeRow(SQLiteDatabase db, int row)
    {
        db.delete(ROWS_TABLE_NAME, COLUMN_ID + "=?", new String[]{ String.valueOf(row + 1) });
    }

    /**
     * Gets rows from database
     * @param db    database
     * @return rows from database
     */
    public static Cursor getRows(SQLiteDatabase db)
    {
        return db.query(ROWS_TABLE_NAME, ROWS_COLUMNS, null, null, null, null, COLUMN_ID);
    }

    /**
     * Gets DB name
     * @return DB name
     */
    public String getDbName()
    {
        return mDbName;
    }
}
