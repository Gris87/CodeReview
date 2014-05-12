package com.griscom.codereview.other;
import android.preference.*;
import android.content.*;
import com.griscom.codereview.*;

public class ApplicationSettings
{
	public static int reviewedColor(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_key_reviewed_color),  context.getResources().getInteger(R.integer.pref_default_reviewed_color));
	}
	
	public static int invalidColor(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_key_invalid_color),   context.getResources().getInteger(R.integer.pref_default_invalid_color));
	}
	
	public static int noteColor(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_key_note_color),      context.getResources().getInteger(R.integer.pref_default_note_color));
	}
	
	public static int selectionColor(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_key_selection_color), context.getResources().getInteger(R.integer.pref_default_selection_color));
	}
	
	public static int fontSize(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_key_font_size),       context.getResources().getInteger(R.integer.pref_default_font_size));
	}
	
	public static int tabSize(Context context)
	{
		return PreferenceManager.getDefaultSharedPreferences(context).getInt(context.getString(R.string.pref_key_tab_size),        context.getResources().getInteger(R.integer.pref_default_tab_size));
	}
}
