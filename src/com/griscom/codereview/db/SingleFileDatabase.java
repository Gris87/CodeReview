package com.griscom.codereview.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class SingleFileDatabase extends SQLiteOpenHelper
{
    private static final String DB_NAME    = "file";
    private static final int    DB_VERSION = 1;



    public static final String COLUMN_ID       = "_id";
    public static final String COLUMN_ROW_ID   = "_rowId";
    public static final String COLUMN_ROW_TYPE = "_rowType";



    public static final String[] ROWS_COLUMNS = {
                                                     COLUMN_ID,
                                                     COLUMN_ROW_ID,
                                                     COLUMN_ROW_TYPE
                                                 };



    public  static final String ROWS_TABLE_NAME   = "rows";
    private static final String ROWS_TABLE_CREATE = "CREATE TABLE " + ROWS_TABLE_NAME + " " +
                                                    "(" +
                                                         COLUMN_ID       + " INTEGER PRIMARY KEY, " +
                                                         COLUMN_ROW_ID   + " INTEGER, "             +
                                                         COLUMN_ROW_TYPE + " TEXT"                  +
                                                    ");";


    private String mDbName;

    public SingleFileDatabase(Context context, int fileId)
    {
        super(context, DB_NAME+String.valueOf(fileId)+".db", null, DB_VERSION);

        mDbName=DB_NAME+String.valueOf(fileId)+".db";
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(ROWS_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Nothing
    }

    public void insertOrUpdateRow(SQLiteDatabase db, int row, char type)
    {
        Cursor cursor=db.query(ROWS_TABLE_NAME, ROWS_COLUMNS, COLUMN_ROW_ID+"=?", new String[]{String.valueOf(row+1)}, null, null, null, null);

        if (cursor.getCount()==1)
        {
            cursor.moveToFirst();

            if (cursor.getString(cursor.getColumnIndexOrThrow(COLUMN_ROW_TYPE)).charAt(0)!=type)
            {
                ContentValues values=new ContentValues();

                values.put(COLUMN_ROW_TYPE, String.valueOf(type));

                db.update(ROWS_TABLE_NAME, values, COLUMN_ROW_ID+"=?", new String[]{String.valueOf(row+1)});
            }
        }
        else
        {
            ContentValues values=new ContentValues();

            values.put(COLUMN_ROW_ID,   row+1);
            values.put(COLUMN_ROW_TYPE, String.valueOf(type));

            db.insert(ROWS_TABLE_NAME, null, values);
        }
    }

    public void removeRow(SQLiteDatabase db, int row)
    {
        db.delete(ROWS_TABLE_NAME, COLUMN_ROW_ID+"=?", new String[]{String.valueOf(row+1)});
    }

    public Cursor getRows(SQLiteDatabase db)
    {
        return db.query(ROWS_TABLE_NAME, ROWS_COLUMNS, null, null, null, null, COLUMN_ROW_ID);
    }

    public String getDbName()
    {
        return mDbName;
    }
}
