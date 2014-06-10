package com.griscom.codereview.review;

import java.util.ArrayList;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.griscom.codereview.db.DbRowType;
import com.griscom.codereview.db.MainDatabase;
import com.griscom.codereview.db.SingleFileDatabase;
import com.griscom.codereview.listeners.OnDocumentLoadedListener;
import com.griscom.codereview.other.SelectionColor;
import com.griscom.codereview.review.syntax.SyntaxParserBase;

public class LoadingThread extends Thread
{
    private static final String TAG = "LoadingThread";



    private ReviewSurfaceView        mParent;
    private SyntaxParserBase         mSyntaxParser;
    private OnDocumentLoadedListener mListener;
    private String                   mFileName;
    private int                      mFileId;



    public LoadingThread(ReviewSurfaceView parent, SyntaxParserBase syntaxParser, OnDocumentLoadedListener listener, String fileName, int fileId)
    {
        mParent       = parent;
        mSyntaxParser = syntaxParser;
        mListener     = listener;
        mFileName     = fileName;
        mFileId       = fileId;
    }

    @Override
    public void interrupt()
    {
        super.interrupt();

        try
        {
            mSyntaxParser.closeReader();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Impossible to close parser", e);
        }
    }

    @Override
    public void run()
    {
        SQLiteDatabase db=null;

        try
        {
            TextDocument document=mSyntaxParser.parseFile(mFileName);

            document.setParent(mParent);

            if (mFileId<=0)
            {
                MainDatabase helper=new MainDatabase(mSyntaxParser.getContext());
                db=helper.getReadableDatabase();

                mFileId=helper.getFile(db, mFileName);

                db.close();
                db=null;
            }

            ArrayList<TextRow> rows=document.getRows();

            if (mFileId>0)
            {
                SingleFileDatabase helper=new SingleFileDatabase(mSyntaxParser.getContext(), mFileId);

                db            = helper.getReadableDatabase();
                Cursor cursor = helper.getRows(db);

                int rowIndex  = cursor.getColumnIndexOrThrow(SingleFileDatabase.COLUMN_ROW_ID);
                int typeIndex = cursor.getColumnIndexOrThrow(SingleFileDatabase.COLUMN_ROW_TYPE);

                cursor.moveToFirst();

                while (!cursor.isAfterLast())
                {
                    int  row  = cursor.getInt(rowIndex)-1;
                    char type = cursor.getString(typeIndex).charAt(0);

                    switch (type)
                    {
                        case DbRowType.REVIEWED:
                            rows.get(row).setSelectionColor(SelectionColor.REVIEWED);
                        break;
                        case DbRowType.INVALID:
                            rows.get(row).setSelectionColor(SelectionColor.INVALID);
                        break;
                        default:
                            Log.e(TAG, "Unknown row type \""+String.valueOf(type)+"\" in database \""+helper.getDbName()+"\"");
                        break;
                    }

                    cursor.moveToNext();
                }
            }

            int reviewedCount = 0;
            int invalidCount  = 0;
            int noteCount     = 0;

            for (int i=0; i<rows.size(); ++i)
            {
                TextRow row=rows.get(i);

                row.checkForComment(mSyntaxParser);

                switch (row.getSelectionColor())
                {
                    case REVIEWED:
                        ++reviewedCount;
                    break;
                    case INVALID:
                        ++invalidCount;
                    break;
                    case NOTE:
                        ++noteCount;
                    break;
                    case CLEAR:
                        // Nothing
                    break;
                    default:
                        Log.e(TAG, "Unknown selection color: "+String.valueOf(row.getSelectionColor()));
                    break;
                }
            }

            document.setProgress(reviewedCount, invalidCount, noteCount);

            mListener.onDocumentLoaded(document);
        }
        catch (Exception e)
        {
            Log.e(TAG, "Exception occured during parsing", e);
        }

        if (db!=null)
        {
            db.close();
        }
    }
}
