package com.griscom.codereview.db;

import android.database.sqlite.*;
import android.content.*;

public class SingleFileDatabase extends SQLiteOpenHelper
{
	private static final String DB_NAME    = "file";
	private static final int    DB_VERSION = 1;
	
	
	
	public static final char ROW_TYPE_NONE     = '-';
	public static final char ROW_TYPE_REVIEWED = 'r';
	public static final char Row_TYPE_INVALID  = 'i';
	
	

	public static final String COLUMN_ID       = "_id";
    public static final String COLUMN_ROW_ID   = "_rowId";
	public static final String COLUMN_ROW_TYPE = "_rowType";



    public static final String[] FILES_COLUMNS = {
		                                             COLUMN_ID,
													 COLUMN_ROW_ID,
													 COLUMN_ROW_TYPE
												 };



    public  static final String ROWS_TABLE_NAME   = "rows";
    private static final String ROWS_TABLE_CREATE = "CREATE TABLE " + ROWS_TABLE_NAME + " " +
	                                                "(" +
													     COLUMN_ID       + " INTEGER PRIMARY KEY, " +
												         COLUMN_ROW_ID   + " INTEGER, "             +
														 COLUMN_ROW_TYPE + " CHAR"                  + // TODO: Check it
													");";
	
	

	public SingleFileDatabase(Context context, int fileId)
	{
		super(context, DB_NAME+String.valueOf(fileId)+".db", null, DB_VERSION);
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
}
