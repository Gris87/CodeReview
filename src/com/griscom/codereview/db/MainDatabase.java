package com.griscom.codereview.db;

import android.database.sqlite.*;
import android.content.*;

public class MainDatabase extends SQLiteOpenHelper
{
	private static final String DB_NAME    = "main.db";
	private static final int    DB_VERSION = 1;
	
	
	
	public static final String COLUMN_ID             = "_id";
    public static final String COLUMN_NAME           = "_name";
	public static final String COLUMN_PATH           = "_path";
	public static final String COLUMN_REVIEWED_COUNT = "_reviewedCount";
	public static final String COLUMN_INVALID_COUNT  = "_invalidCount";
	public static final String COLUMN_NOTE_COUNT     = "_noteCount";
	public static final String COLUMN_ROW_COUNT      = "_rowCount";
	public static final String COLUMN_NOTE           = "_note";



    public static final String[] FILES_COLUMNS = {
		                                             COLUMN_ID,
													 COLUMN_NAME,
													 COLUMN_PATH,
													 COLUMN_REVIEWED_COUNT,
													 COLUMN_INVALID_COUNT,
													 COLUMN_NOTE_COUNT,
													 COLUMN_ROW_COUNT,
													 COLUMN_NOTE
												 };


												   
    public  static final String FILES_TABLE_NAME   = "files";
    private static final String FILES_TABLE_CREATE = "CREATE TABLE " + FILES_TABLE_NAME + " " +
	                                                 "(" +
													      COLUMN_ID             + " INTEGER PRIMARY KEY, " +
														  COLUMN_NAME           + " TEXT, "                +
														  COLUMN_PATH           + " TEXT, "                +
														  COLUMN_REVIEWED_COUNT + " INTEGER, "             +
														  COLUMN_INVALID_COUNT  + " INTEGER, "             +
														  COLUMN_NOTE_COUNT     + " INTEGER, "             +
														  COLUMN_ROW_COUNT      + " INTEGER, "             +
														  COLUMN_NOTE           + " TEXT"                  +
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
}
