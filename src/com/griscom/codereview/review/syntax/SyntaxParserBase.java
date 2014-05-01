package com.griscom.codereview.review.syntax;

import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.PreferenceManager;

import com.griscom.codereview.R;
import com.griscom.codereview.review.TextRow;

@SuppressLint("DefaultLocale")
public abstract class SyntaxParserBase
{
    protected Context mContext;

    protected SyntaxParserBase(Context context)
    {
        mContext=context;
    }

    public abstract ArrayList<TextRow> parseFile(String fileName);

	public static SyntaxParserBase createParserByFileName(String fileName, Context context)
    {
        int index=fileName.lastIndexOf('.');

        if (index>0)
        {
            String extension=fileName.substring(index+1).toLowerCase();

            if (extension.equals("cs"))
            {
                return new CSharpSyntaxParser(context);
            }
        }

        return new PlainTextSyntaxParser(context);
    }

    public int getFontSize()
    {
         return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getString(R.string.pref_key_font_size), mContext.getResources().getInteger(R.integer.pref_default_font_size));
    }
}
