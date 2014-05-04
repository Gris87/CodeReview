package com.griscom.codereview.review.syntax;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.PreferenceManager;

import com.griscom.codereview.R;
import com.griscom.codereview.review.TextDocument;
import com.griscom.codereview.util.Utils;

@SuppressLint("DefaultLocale")
public abstract class SyntaxParserBase
{
    protected Context mContext;

    protected SyntaxParserBase(Context context)
    {
        mContext=context;
    }

    public abstract TextDocument parseFile(String fileName) throws InterruptedException;

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

    public float getFontSize()
    {
        int settingsFontSize=PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getString(R.string.pref_key_font_size), mContext.getResources().getInteger(R.integer.pref_default_font_size));

        return Utils.spToPixels(settingsFontSize, mContext);
    }

    public int getTabSize()
    {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getString(R.string.pref_key_tab_size), mContext.getResources().getInteger(R.integer.pref_default_tab_size));
    }
}
