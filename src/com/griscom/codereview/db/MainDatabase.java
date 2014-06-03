package com.griscom.codereview.db;

import android.database.sqlite.*;
import android.content.*;

public class MainDatabase extends SQLiteOpenHelper
{
	private static final String DB_NAME    = "main.db";
	private static final int    DB_VERSION = 1;
	
	public MainDatabase(Context context)
	{
		super(context, DB_NAME, null, DB_VERSION);
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
