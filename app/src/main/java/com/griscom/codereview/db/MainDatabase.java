package com.griscom.codereview.db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.griscom.codereview.util.Utils;

import junit.framework.Assert;

import java.io.File;

/**
 * Main database helper
 */
@SuppressWarnings("WeakerAccess")
public class MainDatabase extends SQLiteOpenHelper
{
    @SuppressWarnings("unused")
    private static final String TAG = "MainDatabase";



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



    private static final String[] FILES_COLUMNS = {
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



    private static final String FILES_TABLE_NAME   = "files";
    private static final String FILES_TABLE_CREATE = "CREATE TABLE " + FILES_TABLE_NAME + ' ' +
                                                     '(' +
                                                          COLUMN_ID                + " INTEGER PRIMARY KEY, " +
                                                          COLUMN_PATH              + " TEXT NOT NULL, "       +
                                                          COLUMN_NAME              + " TEXT NOT NULL, "       +
                                                          COLUMN_MD5               + " TEXT NOT NULL, "       +
                                                          COLUMN_MODIFICATION_TIME + " INTEGER NOT NULL, "    +
                                                          COLUMN_REVIEWED_COUNT    + " INTEGER NOT NULL, "    +
                                                          COLUMN_INVALID_COUNT     + " INTEGER NOT NULL, "    +
                                                          COLUMN_NOTE_COUNT        + " INTEGER NOT NULL, "    +
                                                          COLUMN_ROW_COUNT         + " INTEGER NOT NULL, "    +
                                                          COLUMN_NOTE              + " TEXT NOT NULL"         +
                                                     ");";



    /**
     * Creates MainDatabase instance
     * @param context    context
     */
    private MainDatabase(Context context)
    {
        super(context, DB_NAME, null, DB_VERSION);
    }

    /**
     * Creates MainDatabase instance
     * @param context    context
     */
    public static MainDatabase newInstance(Context context)
    {
        return new MainDatabase(context);
    }

    /** {@inheritDoc} */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(FILES_TABLE_CREATE);
    }

    /** {@inheritDoc} */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        // Nothing
    }

    /**
     * Gets file ID by file path
     * @param db          database
     * @param filePath    path to file
     * @return file ID in DB
     */
    public static int getFileId(SQLiteDatabase db, String filePath)
    {
        int res = 0;

        long modifiedTime = new File(filePath).lastModified();



        Cursor cursor = getFile(db, filePath);

        int idIndex           = cursor.getColumnIndexOrThrow(COLUMN_ID);
        int modificationIndex = cursor.getColumnIndexOrThrow(COLUMN_MODIFICATION_TIME);

        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            if (cursor.getLong(modificationIndex) == modifiedTime)
            {
                res = cursor.getInt(idIndex);

                break;
            }

            cursor.moveToNext();
        }

        cursor.close();



        if (res == 0)
        {
            String md5 = Utils.md5ForFile(filePath);

            cursor  = getFileByMD5(db, md5);

            cursor.moveToFirst();

            if (!cursor.isAfterLast())
            {
                res = cursor.getInt(idIndex);
            }

            cursor.close();
        }



        return res;
    }

    /**
     * Gets or creates file ID by file path
     * @param db          database
     * @param filePath    path to file
     * @return file ID in DB
     */
    public static long getOrCreateFileId(SQLiteDatabase db, String filePath)
    {
        long res = 0;

        String md5 = null;
        long modifiedTime = new File(filePath).lastModified();

        int index = filePath.lastIndexOf('/');
        Assert.assertTrue("filePath doesn't contains \"/\"", index >= 0);

        String folder   = filePath.substring(0, index);
        String fileName = filePath.substring(index + 1);

        if (folder.isEmpty())
        {
            folder = "/";
        }



        Cursor cursor = getFile(db, folder, fileName);

        int idIndex           = cursor.getColumnIndexOrThrow(COLUMN_ID);
        int modificationIndex = cursor.getColumnIndexOrThrow(COLUMN_MODIFICATION_TIME);

        cursor.moveToFirst();

        while (!cursor.isAfterLast())
        {
            if (cursor.getLong(modificationIndex) == modifiedTime)
            {
                res = cursor.getInt(idIndex);

                break;
            }

            cursor.moveToNext();
        }

        cursor.close();



        if (res == 0)
        {
            md5 = Utils.md5ForFile(filePath);

            cursor  = getFileByMD5(db, md5);

            cursor.moveToFirst();

            if (!cursor.isAfterLast())
            {
                res = cursor.getInt(idIndex);
            }

            cursor.close();
        }



        if (res == 0)
        {
            ContentValues values = new ContentValues();

            values.put(COLUMN_PATH,              folder);
            values.put(COLUMN_NAME,              fileName);
            values.put(COLUMN_MD5,               md5);
            values.put(COLUMN_MODIFICATION_TIME, modifiedTime);
            values.put(COLUMN_REVIEWED_COUNT,    0);
            values.put(COLUMN_INVALID_COUNT,     0);
            values.put(COLUMN_NOTE_COUNT,        0);
            values.put(COLUMN_ROW_COUNT,         0);
            values.put(COLUMN_NOTE,              "");

            res = db.insertOrThrow(FILES_TABLE_NAME, null, values);
        }



        return res;
    }

    /**
     * Gets file note for specified file ID
     * @param db        database
     * @param fileId    file ID in DB
     * @return file note for specified file ID
     */
    public static String getFileNote(SQLiteDatabase db, int fileId)
    {
        String res = null;



        Cursor cursor = getFile(db, fileId);

        int noteIndex = cursor.getColumnIndexOrThrow(COLUMN_NOTE);

        cursor.moveToFirst();

        if (!cursor.isAfterLast())
        {
            res = cursor.getString(noteIndex);
        }

        cursor.close();



        return res;
    }

    /**
     * Updates file path for specified file ID
     * @param db          database
     * @param fileId      file ID in DB
     * @param filePath    path to file
     */
    public static void updateFilePath(SQLiteDatabase db, int fileId, String filePath)
    {
        ContentValues values = new ContentValues();

        int index = filePath.lastIndexOf('/');
        Assert.assertTrue("filePath doesn't contains \"/\"", index >= 0);

        String folder   = filePath.substring(0, index);
        String fileName = filePath.substring(index + 1);

        if (folder.isEmpty())
        {
            folder = "/";
        }

        values.put(COLUMN_PATH, folder);
        values.put(COLUMN_NAME, fileName);

        db.update(FILES_TABLE_NAME, values, COLUMN_ID + "=?", new String[]{ String.valueOf(fileId) });
    }

    /**
     * Updates file meta information (MD5 hash and modified time)
     * @param db          database
     * @param fileId      file ID in DB
     * @param filePath    filePath
     */
    public static void updateFileMeta(SQLiteDatabase db, int fileId, String filePath)
    {
        String md5        = Utils.md5ForFile(filePath);
        long modifiedTime = new File(filePath).lastModified();



        ContentValues values = new ContentValues();

        values.put(COLUMN_MD5,               md5);
        values.put(COLUMN_MODIFICATION_TIME, modifiedTime);

        db.update(FILES_TABLE_NAME, values, COLUMN_ID + "=?", new String[]{ String.valueOf(fileId) });
    }

    /**
     * Updates file stats
     * @param db               database
     * @param fileId           file ID in DB
     * @param reviewedCount    reviewed count
     * @param invalidCount     invalid count
     * @param noteCount        note count
     * @param rowCount         row count
     */
    public static void updateFileStats(SQLiteDatabase db, int fileId, int reviewedCount, int invalidCount, int noteCount, int rowCount)
    {
        ContentValues values = new ContentValues();

        values.put(COLUMN_REVIEWED_COUNT, reviewedCount);
        values.put(COLUMN_INVALID_COUNT,  invalidCount);
        values.put(COLUMN_NOTE_COUNT,     noteCount);
        values.put(COLUMN_ROW_COUNT,      rowCount);

        db.update(FILES_TABLE_NAME, values, COLUMN_ID + "=?", new String[]{ String.valueOf(fileId) });
    }

    /**
     * Updates file note
     * @param db        database
     * @param fileId    file ID in database
     * @param note      note
     */
    public static void updateFileNote(SQLiteDatabase db, int fileId, String note)
    {
        ContentValues values = new ContentValues();

        values.put(COLUMN_NOTE, note);

        db.update(FILES_TABLE_NAME, values, COLUMN_ID + "=?", new String[]{ String.valueOf(fileId) });
    }

    /**
     * Gets cursor for all files in specified path
     * @param db      database
     * @param path    path to folder
     * @return cursor for all files in specified path
     */
    public static Cursor getFiles(SQLiteDatabase db, String path)
    {
        return db.query(FILES_TABLE_NAME, FILES_COLUMNS, COLUMN_PATH + "=?", new String[]{ path }, null, null, null);
    }

    /**
     * Gets cursor for all files with specified path to file
     * @param db          database
     * @param filePath    path to file
     * @return cursor for all files with specified path to file
     */
    public static Cursor getFile(SQLiteDatabase db, String filePath)
    {
        int index = filePath.lastIndexOf('/');
        Assert.assertTrue("filePath doesn't contains \"/\"", index >= 0);

        String folder   = filePath.substring(0, index);
        String fileName = filePath.substring(index + 1);

        if (folder.isEmpty())
        {
            folder = "/";
        }

        return getFile(db, folder, fileName);
    }

    /**
     * Gets cursor for all files with specified path and file name
     * @param db          database
     * @param path        path to folder
     * @param fileName    file name
     * @return cursor for all files with specified path and file name
     */
    public static Cursor getFile(SQLiteDatabase db, String path, String fileName)
    {
        return db.query(FILES_TABLE_NAME, FILES_COLUMNS, COLUMN_PATH + "=? AND " + COLUMN_NAME + "=?", new String[]{ path, fileName }, null, null, null);
    }

    /**
     * Gets cursor for file with specified file ID in DB
     * @param db        database
     * @param fileId    file ID in DB
     * @return cursor for file with specified file ID in DB
     */
    public static Cursor getFile(SQLiteDatabase db, int fileId)
    {
        return db.query(FILES_TABLE_NAME, FILES_COLUMNS, COLUMN_ID + "=?", new String[]{ String.valueOf(fileId) }, null, null, null);
    }

    /**
     * Gets cursor for all files with specified MD5 hash
     * @param db     database
     * @param md5    MD5 hash
     * @return cursor for all files with specified MD5 hash
     */
    public static Cursor getFileByMD5(SQLiteDatabase db, String md5)
    {
        return db.query(FILES_TABLE_NAME, FILES_COLUMNS, COLUMN_MD5 + "=?", new String[]{ md5 }, null, null, null);
    }
}
