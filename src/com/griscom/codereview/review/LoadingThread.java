package com.griscom.codereview.review;

import android.util.Log;

import com.griscom.codereview.listeners.OnDocumentLoadedListener;
import com.griscom.codereview.review.syntax.SyntaxParserBase;

public class LoadingThread extends Thread
{
    private static final String TAG = "LoadingThread";



    private SyntaxParserBase         mSyntaxParser;
    private OnDocumentLoadedListener mListener;
    private String                   mFileName;



    public LoadingThread(SyntaxParserBase syntaxParser, OnDocumentLoadedListener listener, String fileName)
    {
        mSyntaxParser = syntaxParser;
        mListener     = listener;
        mFileName     = fileName;
    }

    @Override
    public void run()
    {
        try
        {
            mListener.onDocumentLoaded(mSyntaxParser.parseFile(mFileName));
        }
        catch (InterruptedException e)
        {
            // Nothing
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception occured during parsing", e);
        }
    }
}
