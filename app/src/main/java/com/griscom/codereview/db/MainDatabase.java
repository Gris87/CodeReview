package com.griscom.codereview.db;

import java.io.File;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.griscom.codereview.util.Utils;

public class MainDatabase extends SQLiteOpenHelper
{
    private static final String DB_NAME    = "main.db";
    private static final int    DB_VERSION = 1;



    public static final String COLUMN_ID                = "_id";
    public static final String COLUMN_PATH              = "_path";
    public static final String COLUMN_NAME              = "_name";
    public static final String COLUMN_MD5               = "_md5";
    public static final String COLUMN_MODIFICATION_TIME = "_modificationTime";
    public static final String COLUMN_REVIEWED_COUNT    = "_reviewedCount";
    public static final String COLUMN_INVALID_COUNT     = "_invalidCount";
    public static final String COLUMN_NOTE_COUNT        = "_noteCount";
    public static final String COLUMN_ROW_COUNT         = "_rowCount";
    public static final String COLUMN_NOTE              = "_note";



    public static final String[] FILES_COLUMNS = {
                                                     COLUMN_ID,
                                                     COLUMN_PATH,
                                                     COLUMN_NAME,
                                                     COLUMN_MD5,
                                                     COLUMN_MODIFICATION_TIME,
                                                     COLUMN_REVIEWED_COUNT,
                                                     COLUMN_INVALID_COUNT,
                                                     COLUMN_NOTE_COUNT,
                                                     COLUMN_ROW_COUNT,
                                                     COLUMN_NOTE
                                                 };



    public  static final String FILES_TABLE_NAME   = "files";
    private static final String FILES_TABLE_CREATE = "CREATE TABLE " + FILES_TABLE_NAME + " "  +
                                                     "("  +
                                                          COLUMN_ID                + " INTEGER PRIMARY KEY, "  +
                                                          COLUMN_PATH              + " TEXT, "                 +
                                                          COLUMN_NAME              + " TEXT, "                 +
                                                          COLUMN_MD5               + " TEXT, "                 +
                                                          COLUMN_MODIFICATION_TIME + " INTEGER, "              +
                                                          COLUMN_REVIEWED_COUNT    + " INTEGER, "              +
                                                          COLUMN_INVALID_COUNT     + " INTEGER, "              +
                                                          COLUMN_NOTE_COUNT        + " INTEGER, "              +
                                                          COLUMN_ROW_COUNT         + " INTEGER, "              +
                                                          COLUMN_NOTE              + " TEXT"                   +
                                                     ");";



    public MainDatabase(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(FILES_TABLE_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Nothing
    }

    public int getFile(SQLiteDatabase db, String fileName)
    {
        String md5        = Utils.md5ForFile(fileName);
        long modifiedTime = new File(fileName).lastModified();

        Cursor cursor = getFileByMD5(db, md5);

        int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
        int modificationIndex = cursor.getColumnIndexOrThrow(COLUMN_MODIFICATION_TIME);

        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            if (cursor.getLong(modificationIndex) == modifiedTime)
            {
                return cursor.getInt(idIndex);
            }

            cursor.moveToNext();
        }

        return 0;
    }

    public int getOrCreateFile(SQLiteDatabase db, String fileName)
    {
        String md5        = Utils.md5ForFile(fileName);
        long modifiedTime = new File(fileName).lastModified();

        Cursor cursor = getFileByMD5(db, md5);

        int idIndex = cursor.getColumnIndexOrThrow(COLUMN_ID);
        int modificationIndex = cursor.getColumnIndexOrThrow(COLUMN_MODIFICATION_TIME);

        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            if (cursor.getLong(modificationIndex) == modifiedTime)
            {
                return cursor.getInt(idIndex);
            }

            cursor.moveToNext();
        }

        //-------------------------------------------------------------- -  -
        // Create new

        ContentValues values = new ContentValues();

        String folder = fileName.substring(0, fileName.lastIndexOf('/'));

        if (folder.equals(""))
        {
            folder = "/";
        }

        values.put(COLUMN_PATH,              folder);
        values.put(COLUMN_NAME,              fileName.substring(fileName.lastIndexOf('/') + 1));
        values.put(COLUMN_MD5,               md5);
        values.put(COLUMN_MODIFICATION_TIME, modifiedTime);
        values.put(COLUMN_REVIEWED_COUNT,    0);
        values.put(COLUMN_INVALID_COUNT,     0);
        values.put(COLUMN_NOTE_COUNT,        0);
        values.put(COLUMN_ROW_COUNT,         0);
        values.put(COLUMN_NOTE,              "");

        return (int)db.insertOrThrow(FILES_TABLE_NAME, null, values);
    }

    public void updateFilePath(SQLiteDatabase db, int fileId, String fileName)
    {
        ContentValues values = new ContentValues();

        String folder = fileName.substring(0, fileName.lastIndexOf('/'));

        if (folder.equals(""))
        {
            folder = "/";
        }

        values.put(COLUMN_PATH, folder);
        values.put(COLUMN_NAME, fileName.substring(fileName.lastIndexOf('/') + 1));

        db.update(FILES_TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(fileId)});
    }

    public void updateFileMeta(SQLiteDatabase db, int fileId, String fileName)
    {
        String md5        = Utils.md5ForFile(fileName);
        long modifiedTime = new File(fileName).lastModified();



        ContentValues values = new ContentValues();

        values.put(COLUMN_MD5,               md5);
        values.put(COLUMN_MODIFICATION_TIME, modifiedTime);

        db.update(FILES_TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(fileId)});
    }

    public void updateFileStats(SQLiteDatabase db, int fileId, int reviewedCount, int invalidCount, int noteCount, int rowCount)
    {
        ContentValues values = new ContentValues();

        values.put(COLUMN_REVIEWED_COUNT, reviewedCount);
        values.put(COLUMN_INVALID_COUNT,  invalidCount);
        values.put(COLUMN_NOTE_COUNT,     noteCount);
        values.put(COLUMN_ROW_COUNT,      rowCount);

        db.update(FILES_TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(fileId)});
    }

    public void updateFileNote(SQLiteDatabase db, int fileId, String note)
    {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOTE, note);

        db.update(FILES_TABLE_NAME, values, COLUMN_ID + "=?", new String[]{String.valueOf(fileId)});
    }

    public Cursor getFiles(SQLiteDatabase db, String path)
    {
        return db.query(FILES_TABLE_NAME, FILES_COLUMNS, COLUMN_PATH + "=?", new String[]{path}, null, null, null);
    }

    public Cursor getFile(SQLiteDatabase db, String path, String name)
    {
        return db.query(FILES_TABLE_NAME, FILES_COLUMNS, COLUMN_PATH + "=? AND " + COLUMN_NAME + "=?", new String[]{path, name}, null, null, null);
    }

    public Cursor getFileByMD5(SQLiteDatabase db, String md5)
    {
        return db.query(FILES_TABLE_NAME, FILES_COLUMNS, COLUMN_MD5 + "=?", new String[]{md5}, null, null, null);
    }
}
