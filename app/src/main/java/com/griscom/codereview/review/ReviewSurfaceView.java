package com.griscom.codereview.review;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.griscom.codereview.R;
import com.griscom.codereview.db.MainDatabase;
import com.griscom.codereview.db.SingleFileDatabase;
import com.griscom.codereview.listeners.OnCommentDialogRequestedListener;
import com.griscom.codereview.listeners.OnFileNoteLoadedListener;
import com.griscom.codereview.listeners.OnNoteSupportListener;
import com.griscom.codereview.listeners.OnProgressChangedListener;
import com.griscom.codereview.listeners.OnReviewSurfaceDrawListener;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.ColorCache;
import com.griscom.codereview.other.RowType;
import com.griscom.codereview.other.SelectionType;
import com.griscom.codereview.other.SyntaxParserType;
import com.griscom.codereview.review.syntax.SyntaxParserBase;
import com.griscom.codereview.util.AppLog;
import com.griscom.codereview.util.Utils;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * SurfaceView that used in Review activity
 */
public class ReviewSurfaceView extends SurfaceView implements OnTouchListener, OnReviewSurfaceDrawListener
{
    @SuppressWarnings("unused")
    private static final String TAG = "ReviewSurfaceView";



    private static final int REPAINT_MESSAGE = 1;



    private LoadingTask                      mLoadingTask;
    private DrawThread                       mDrawThread;
    private String                           mFilePath;
    private int                              mFileId;
    private long                             mModifiedTime;
    private int                              mSyntaxParserType;
    private SyntaxParserBase                 mSyntaxParser;
    private TextDocument                     mDocument;
    private int                              mFontSize;
    private int                              mTabSize;
    private int                              mSelectionType;
    private OnNoteSupportListener            mNoteSupportListener;
    private OnFileNoteLoadedListener         mFileNoteLoadedListener;
    private OnProgressChangedListener        mProgressChangedListener;
    private OnCommentDialogRequestedListener mCommentDialogRequestedListener;



    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler()
    {
        /** {@inheritDoc} */
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case REPAINT_MESSAGE:
                {
                    if (mDrawThread != null)
                    {
                        mDrawThread.repaint();
                    }
                }
                break;
            }
        }
    };



    /**
     * Creates ReviewSurfaceView instance
     * @param context    context
     */
    public ReviewSurfaceView(Context context)
    {
        super(context);

        init();
    }

    /**
     * Creates ReviewSurfaceView instance
     * @param context    context
     * @param attrs      attributes
     */
    public ReviewSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init();
    }

    /**
     * Creates ReviewSurfaceView instance
     * @param context    context
     * @param attrs      attributes
     * @param defStyle   default style
     */
    public ReviewSurfaceView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init();
    }

    /**
     * Initializes instance
     */
    private void init()
    {
        mLoadingTask                    = null;
        mDrawThread                     = null;
        mFilePath                       = null;
        mFileId                         = 0;
        mModifiedTime                   = 0;
        mSyntaxParserType               = SyntaxParserType.AUTOMATIC;
        mSyntaxParser                   = null;
        mDocument                       = null;
        mFontSize                       = ApplicationSettings.getFontSize();
        mTabSize                        = ApplicationSettings.getTabSize();
        mSelectionType                  = SelectionType.REVIEWED;
        mNoteSupportListener            = null;
        mFileNoteLoadedListener         = null;
        mProgressChangedListener        = null;
        mCommentDialogRequestedListener = null;
    }

    /**
     * Handler for pause event
     */
    public void onPause()
    {
        stopLoadingTask();
        stopDrawThread();
    }

    /**
     * Handler for resume event
     */
    public void onResume()
    {
        reload();
        repaint(200);

        mDrawThread = new DrawThread(getHolder(), this);
        mDrawThread.start();
    }

    /** {@inheritDoc} */
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        if (mDocument != null)
        {
            mDocument.onConfigurationChanged(newConfig);
        }

        repaint(80);
    }

    /** {@inheritDoc} */
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        return mDocument == null || mDocument.onTouch(v, event);
    }

    /**
     * Handler for comment entered event
     * @param firstRow    first row
     * @param lastRow     last row
     * @param comment     comment
     */
    public void onCommentEntered(int firstRow, int lastRow, String comment)
    {
        if (mDocument != null)
        {
            mDocument.onCommentEntered(firstRow, lastRow, comment);
        }
    }

    /**
     * Handler for comment canceled event
     */
    public void onCommentCanceled()
    {
        if (mDocument != null)
        {
            mDocument.onCommentCanceled();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onReviewSurfaceDraw(Canvas canvas)
    {
        TextDocument document;
        long modifiedTime;

        synchronized(this)
        {
            document     = mDocument;
            modifiedTime = mModifiedTime;
        }

        if (document != null)
        {
            document.draw(canvas);
        }
        else
        {
            canvas.drawColor(ColorCache.get(SelectionType.CLEAR));

            Paint paint = new Paint();

            paint.setColor(Color.GRAY);
            paint.setTextSize(Utils.spToPixels(36, getContext()));
            paint.setTextAlign(Align.CENTER);

            if (modifiedTime == -1)
            {
                canvas.drawText(getContext().getString(R.string.review_file_not_found), getWidth() * 0.5f, getHeight() * 0.5f, paint);
            }
            else
            {
                canvas.drawText(getContext().getString(R.string.review_loading),        getWidth() * 0.5f, getHeight() * 0.5f, paint);
            }
        }
    }

    /**
     * Saves document
     */
    public void saveRequested()
    {
        if (mDocument != null)
        {
            try
            {
                ArrayList<TextRow> rows = mDocument.getRows();



                PrintWriter writer = new PrintWriter(mFilePath);

                for (int i = 0; i < rows.size() - 1; ++i)
                {
                    writer.println(rows.get(i).toString());
                }

                if (rows.size() > 0)
                {
                    writer.print(rows.get(rows.size() - 1).toString());
                }

                writer.close();



                long modifiedTime = new File(mFilePath).lastModified();

                synchronized(this)
                {
                    mModifiedTime = modifiedTime;
                }
            }
            catch (Exception e)
            {
                AppLog.e(TAG, "Impossible to save file: " + mFilePath, e);
            }
        }
    }

    /**
     * Repaints surface
     */
    public void repaint()
    {
        repaint(0);
    }

    /**
     * Repaints surface with specified timeout
     * @param timeout    timeout
     */
    private void repaint(long timeout)
    {
        if (timeout > 0)
        {
            mHandler.sendEmptyMessageDelayed(REPAINT_MESSAGE, timeout);
        }
        else
        {
            mHandler.sendEmptyMessage(REPAINT_MESSAGE);
        }
    }

    /**
     * Reloads document
     */
    public void reload()
    {
        File file = new File(mFilePath);

        if (!file.exists() || mModifiedTime != file.lastModified())
        {
            forceReload();
        }
    }

    /**
     * Reloads document with the force
     */
    public void forceReload()
    {
        File file = new File(mFilePath);

        stopLoadingTask();

        synchronized(this)
        {
            if (!file.exists())
            {
                mModifiedTime = -1;
            }

            mDocument = null;
        }

        repaint();

        if (file.exists())
        {
            mLoadingTask = new LoadingTask();
            mLoadingTask.execute();
        }
    }

    /**
     * Stops loading task
     */
    private void stopLoadingTask()
    {
        if (mLoadingTask != null)
        {
            mLoadingTask.interrupt();
            mLoadingTask = null;
        }
    }

    /**
     * Stops draw thread
     */
    private void stopDrawThread()
    {
        if (mDrawThread != null)
        {
            mDrawThread.interrupt();

            do
            {
                try
                {
                    mDrawThread.join();
                    mDrawThread = null;

                    return;
                }
                catch (Exception e)
                {
                    // Nothing
                }
            } while(true);
        }
    }

    /**
     * Sets path to file
     * @param filePath    path to file
     */
    public void setFilePath(String filePath)
    {
        mFilePath = filePath;
    }

    /**
     * Gets path to file
     * @return path to file
     */
    public String getFilePath()
    {
        return mFilePath;
    }

    /**
     * Sets file ID in DB
     * @param fileId    file ID in DB
     */
    public void setFileId(int fileId)
    {
        mFileId = fileId;
    }

    /**
     * Gets file ID in DB
     * @return file ID in DB
     */
    public int getFileId()
    {
        return mFileId;
    }

    /**
     * Sets syntax parser type
     * @param syntaxParserType    syntax parser type
     */
    public void setSyntaxParserType(int syntaxParserType)
    {
        mSyntaxParserType = syntaxParserType;

        mSyntaxParser = SyntaxParserBase.createParserByType(mFilePath, getContext(), mSyntaxParserType);

        if (mNoteSupportListener != null && mSyntaxParser != null)
        {
            mNoteSupportListener.onNoteSupport(!TextUtils.isEmpty(mSyntaxParser.getCommentLine()));
        }
    }

    /**
     * Gets syntax parser type
     * @return syntax parser type
     */
    public int getSyntaxParserType()
    {
        return mSyntaxParserType;
    }

    /**
     * Sets font size
     * @param fontSize    font size
     */
    public void setFontSize(int fontSize)
    {
        if (mFontSize != fontSize)
        {
            mFontSize = fontSize;

            if (mDocument != null)
            {
                mDocument.setFontSize(mFontSize);
            }
        }
    }

    /**
     * Sets tab size
     * @param tabSize    tab size
     */
    public void setTabSize(int tabSize)
    {
        if (mTabSize != tabSize)
        {
            mTabSize = tabSize;

            if (mDocument != null)
            {
                mDocument.setTabSize(mTabSize);
            }
        }
    }

    /**
     * Sets selection type
     * @param selectionType    selection type
     */
    public void setSelectionType(int selectionType)
    {
        if (mSelectionType != selectionType)
        {
            mSelectionType = selectionType;

            if (mDocument != null)
            {
                mDocument.setSelectionType(mSelectionType);
            }
        }
    }

    /**
     * Sets note support listener
     * @param listener    note support listener
     */
    public void setOnNoteSupportListener(OnNoteSupportListener listener)
    {
        mNoteSupportListener = listener;

        if (mNoteSupportListener != null && mSyntaxParser != null)
        {
            mNoteSupportListener.onNoteSupport(!TextUtils.isEmpty(mSyntaxParser.getCommentLine()));
        }
    }

    /**
     * Sets file note loaded listener
     * @param listener    file note loaded listener
     */
    public void setOnFileNoteLoadedListener(OnFileNoteLoadedListener listener)
    {
        mFileNoteLoadedListener = listener;
    }

    /**
     * Sets progress changed listener
     * @param listener    progress changed listener
     */
    public void setOnProgressChangedListener(OnProgressChangedListener listener)
    {
        mProgressChangedListener = listener;

        if (mDocument != null)
        {
            mDocument.setOnProgressChangedListener(mProgressChangedListener);
        }
    }

    /**
     * Sets comment dialog requested listener
     * @param listener    comment dialog requested listener
     */
    public void setOnCommentDialogRequestedListener(OnCommentDialogRequestedListener listener)
    {
        mCommentDialogRequestedListener = listener;

        if (mDocument != null)
        {
            mDocument.setOnCommentDialogRequestedListener(mCommentDialogRequestedListener);
        }
    }



    /**
     * File loading task
     */
    private class LoadingTask extends AsyncTask<Void, Void, TextDocument>
    {
        private Context          mContext;
        private String           mPath;
        private int              mDbFileId;
        private SyntaxParserBase mParser;
        private String           mNote;



        /** {@inheritDoc} */
        @Override
        protected void onPreExecute()
        {
            mContext  = getContext();
            mPath     = mFilePath;
            mDbFileId = mFileId;
            mParser   = mSyntaxParser;
            mNote     = null;
        }

        /**
         * Interrupts execution
         */
        @SuppressWarnings("WeakerAccess")
        public void interrupt()
        {
            cancel(true);

            try
            {
                mParser.closeReader();
            }
            catch (Exception e)
            {
                AppLog.e(TAG, "Impossible to close parser", e);
            }
        }

        /** {@inheritDoc} */
        @Override
        protected TextDocument doInBackground(Void... params)
        {
            TextDocument document = null;

            SQLiteDatabase db = null;

            try
            {
                document = mParser.parseFile(mPath);

                document.setParent(ReviewSurfaceView.this);

                if (mDbFileId <= 0)
                {
                    MainDatabase helper = new MainDatabase(mContext);
                    db = helper.getReadableDatabase();

                    mDbFileId = helper.getFileId(db, mPath);
                    mNote     = helper.getFileNote(db, mDbFileId);

                    db.close();
                    db = null;
                }

                ArrayList<TextRow> rows = document.getRows();

                if (mDbFileId > 0)
                {
                    SingleFileDatabase helper = new SingleFileDatabase(mContext, mDbFileId);

                    db            = helper.getReadableDatabase();
                    Cursor cursor = helper.getRows(db);

                    int idIndex   = cursor.getColumnIndexOrThrow(SingleFileDatabase.COLUMN_ID);
                    int typeIndex = cursor.getColumnIndexOrThrow(SingleFileDatabase.COLUMN_TYPE);

                    cursor.moveToFirst();

                    while (!cursor.isAfterLast())
                    {
                        int row = cursor.getInt(idIndex) - 1;

                        if (row >= 0 && row < rows.size())
                        {
                            String typeStr = cursor.getString(typeIndex);

                            char type = !TextUtils.isEmpty(typeStr) ? typeStr.charAt(0) : '-';

                            switch (type)
                            {
                                case RowType.REVIEWED:
                                {
                                    rows.get(row).setSelectionType(SelectionType.REVIEWED);
                                }
                                break;

                                case RowType.INVALID:
                                {
                                    rows.get(row).setSelectionType(SelectionType.INVALID);
                                }
                                break;

                                default:
                                {
                                    AppLog.wtf(TAG, "Unknown row type \"" + String.valueOf(type) + "\" in database \"" + helper.getDbName() + "\"");
                                }
                                break;
                            }
                        }
                        else
                        {
                            AppLog.wtf(TAG, "Unexpected row id (" + String.valueOf(row) + ") with row count (" + String.valueOf(rows.size()) + ")");
                        }

                        cursor.moveToNext();
                    }

                    cursor.close();
                }

                int reviewedCount = 0;
                int invalidCount  = 0;
                int noteCount     = 0;

                for (int i = 0; i < rows.size(); ++i)
                {
                    TextRow row = rows.get(i);

                    row.checkForComment(mParser);

                    switch (row.getSelectionType())
                    {
                        case SelectionType.REVIEWED:
                        {
                            ++reviewedCount;
                        }
                        break;

                        case SelectionType.INVALID:
                        {
                            ++invalidCount;
                        }
                        break;

                        case SelectionType.NOTE:
                        {
                            ++noteCount;
                        }
                        break;

                        case SelectionType.CLEAR:
                        {
                            // Nothing
                        }
                        break;

                        default:
                        {
                            AppLog.wtf(TAG, "Unknown selection type: " + String.valueOf(row.getSelectionType()));
                        }
                        break;
                    }
                }

                document.setProgress(reviewedCount, invalidCount, noteCount);
            }
            catch (Exception e)
            {
                AppLog.wtf(TAG, "Exception occurred during parsing", e);
            }

            if (db != null)
            {
                db.close();
            }

            return document;
        }

        /** {@inheritDoc} */
        @Override
        protected void onPostExecute(TextDocument textDocument)
        {
            if (textDocument != null)
            {
                textDocument.init();

                textDocument.setFontSize(mFontSize);
                textDocument.setTabSize(mTabSize);
                textDocument.setSelectionType(mSelectionType);
                textDocument.setOnProgressChangedListener(mProgressChangedListener);
                textDocument.setOnCommentDialogRequestedListener(mCommentDialogRequestedListener);

                if (!TextUtils.isEmpty(mNote))
                {
                    mFileNoteLoadedListener.onFileNoteLoaded(mNote);
                }



                long modifiedTime = new File(mPath).lastModified();

                synchronized(this)
                {
                    mModifiedTime = modifiedTime;
                    mDocument     = textDocument;
                }

                repaint();
            }
        }
    }
}
