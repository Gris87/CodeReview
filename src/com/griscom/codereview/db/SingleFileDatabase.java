package com.griscom.codereview.db;

import android.database.sqlite.*;
import android.content.*;

public class SingleFileDatabase extends SQLiteOpenHelper
{
	private static final String DB_NAME    = "file";
	private static final int    DB_VERSION = 1;

	public SingleFileDatabase(Context context, int fileId)
	{
		super(context, DB_NAME+String.valueOf(fileId)+".db", null, DB_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db)
	{
		// TODO: Implement this method
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		// TODO: Implement this method
	}
}
