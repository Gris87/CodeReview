package com.griscom.codereview.review;

import com.griscom.codereview.listeners.OnDocumentLoadedListener;
import com.griscom.codereview.review.syntax.SyntaxParserBase;

public class LoadingThread extends Thread
{
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
        mListener.onDocumentLoaded(mSyntaxParser.parseFile(mFileName));
    }
}
