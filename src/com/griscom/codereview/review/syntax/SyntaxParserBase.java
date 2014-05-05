package com.griscom.codereview.review.syntax;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.content.Context;
import android.preference.PreferenceManager;

import com.griscom.codereview.R;
import com.griscom.codereview.review.TextDocument;
import com.griscom.codereview.util.Utils;

@SuppressLint("DefaultLocale")
public abstract class SyntaxParserBase
{
    protected Context        mContext;
    protected BufferedReader mReader;

    protected SyntaxParserBase(Context context)
    {
        mContext = context;
        mReader  = null;
    }

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

    public abstract TextDocument parseFile(String fileName);

    protected void createReader(String fileName) throws FileNotFoundException
    {
        synchronized (this)
        {
            mReader=null;
            mReader=new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        }
    }

    protected String readLine() throws IOException
    {
        BufferedReader reader;

        synchronized (this)
        {
            reader=mReader;
        }

        if (reader!=null)
        {
            return reader.readLine();
        }

        return null;
    }

    public void closeReader() throws IOException
    {
        synchronized (this)
        {
            if (mReader!=null)
            {
                mReader.close();
                mReader=null;
            }
        }
    }

    protected float getFontSize()
    {
        int settingsFontSize=PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getString(R.string.pref_key_font_size), mContext.getResources().getInteger(R.integer.pref_default_font_size));

        return Utils.spToPixels(settingsFontSize, mContext);
    }

    protected int getTabSize()
    {
        return PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getString(R.string.pref_key_tab_size), mContext.getResources().getInteger(R.integer.pref_default_tab_size));
    }
}
