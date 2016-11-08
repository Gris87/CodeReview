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
@SuppressWarnings("PublicConstructor")
public class ReviewSurfaceView extends SurfaceView implements OnTouchListener, OnReviewSurfaceDrawListener
{
    @SuppressWarnings("unused")
    private static final String TAG = "ReviewSurfaceView";



    private static final int REPAINT_MESSAGE = 1;



    private LoadingTask                      mLoadingTask                    = null;
    private DrawThread                       mDrawThread                     = null;
    private String                           mFilePath                       = null;
    private long                             mFileId                         = 0;
    private long                             mModifiedTime                   = 0;
    private int                              mSyntaxParserType               = 0;
    private SyntaxParserBase                 mSyntaxParser                   = null;
    private TextDocument                     mDocument                       = null;
    private int                              mFontSize                       = 0;
    private int                              mTabSize                        = 0;
    private int                              mSelectionType                  = 0;
    private OnNoteSupportListener            mNoteSupportListener            = null;
    private OnFileNoteLoadedListener         mFileNoteLoadedListener         = null;
    private OnProgressChangedListener        mProgressChangedListener        = null;
    private OnCommentDialogRequestedListener mCommentDialogRequestedListener = null;
    private final Object                     mLock                           = new Object();



    @SuppressLint("HandlerLeak")
    private final Handler mHandler = new Handler()
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

                default:
                {
                    AppLog.wtf(TAG, "Unknown message type: " + msg.what);
                }
            }
        }
    };


    /** {@inheritDoc} */
    @Override
    public String toString()
    {
        TextDocument document;
        long         modifiedTime;

        synchronized(mLock)
        {
            document     = mDocument;
            modifiedTime = mModifiedTime;
        }

        return "ReviewSurfaceView{" +
                "mLoadingTask="                      + mLoadingTask                    +
                ", mDrawThread="                     + mDrawThread                     +
                ", mFilePath='"                      + mFilePath + '\''                +
                ", mFileId="                         + mFileId                         +
                ", mModifiedTime="                   + modifiedTime                    +
                ", mSyntaxParserType="               + mSyntaxParserType               +
                ", mSyntaxParser="                   + mSyntaxParser                   +
                ", mDocument="                       + document                        +
                ", mFontSize="                       + mFontSize                       +
                ", mTabSize="                        + mTabSize                        +
                ", mSelectionType="                  + mSelectionType                  +
                ", mNoteSupportListener="            + mNoteSupportListener            +
                ", mFileNoteLoadedListener="         + mFileNoteLoadedListener         +
                ", mProgressChangedListener="        + mProgressChangedListener        +
                ", mCommentDialogRequestedListener=" + mCommentDialogRequestedListener +
                ", mLock="                           + mLock                           +
                ", mHandler="                        + mHandler                        +
                '}';
    }

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
        mSyntaxParserType               = SyntaxParserType.AUTOMATIC;
        mSyntaxParser                   = null;
        mFontSize                       = ApplicationSettings.getFontSize();
        mTabSize                        = ApplicationSettings.getTabSize();
        mSelectionType                  = SelectionType.REVIEWED;
        mNoteSupportListener            = null;
        mFileNoteLoadedListener         = null;
        mProgressChangedListener        = null;
        mCommentDialogRequestedListener = null;

        synchronized(mLock)
        {
            mModifiedTime = 0;
            mDocument     = null;
        }
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

        mDrawThread = DrawThread.newInstance(getHolder(), this);
        mDrawThread.start();
    }

    /** {@inheritDoc} */
    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        TextDocument document;

        synchronized(mLock)
        {
            document = mDocument;
        }

        if (document != null)
        {
            document.onConfigurationChanged();
        }

        repaint(80);
    }

    /** {@inheritDoc} */
    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        TextDocument document;

        synchronized(mLock)
        {
            document = mDocument;
        }

        return document == null || document.onTouch(v, event);
    }

    /**
     * Handler for comment entered event
     * @param firstRow    first row
     * @param lastRow     last row
     * @param comment     comment
     */
    public void onCommentEntered(int firstRow, int lastRow, String comment)
    {
        TextDocument document;

        synchronized(mLock)
        {
            document = mDocument;
        }

        if (document != null)
        {
            document.onCommentEntered(firstRow, lastRow, comment);
        }
    }

    /**
     * Handler for comment canceled event
     */
    public void onCommentCanceled()
    {
        TextDocument document;

        synchronized(mLock)
        {
            document = mDocument;
        }

        if (document != null)
        {
            document.onCommentCanceled();
        }
    }

    /** {@inheritDoc} */
    @Override
    public void onReviewSurfaceDraw(Canvas canvas)
    {
        TextDocument document;
        long         modifiedTime;

        synchronized(mLock)
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
        TextDocument document;

        synchronized(mLock)
        {
            document = mDocument;
        }

        if (document != null)
        {
            PrintWriter writer = null;

            try
            {
                ArrayList<TextRow> rows = document.getRows();



                //noinspection IOResourceOpenedButNotSafelyClosed
                writer = new PrintWriter(mFilePath);

                for (int i = 0; i < rows.size() - 1; ++i)
                {
                    writer.println(rows.get(i).getString());
                }

                if (!rows.isEmpty())
                {
                    writer.print(rows.get(rows.size() - 1).getString());
                }



                long modifiedTime = new File(mFilePath).lastModified();

                synchronized(mLock)
                {
                    mModifiedTime = modifiedTime;
                }
            }
            catch (Exception e)
            {
                AppLog.e(TAG, "Impossible to save file: " + mFilePath, e);
            }

            if (writer != null)
            {
                writer.close();
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

        long modifiedTime;

        synchronized(mLock)
        {
            modifiedTime = mModifiedTime;
        }

        if (!file.exists() || modifiedTime != file.lastModified())
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

        synchronized(mLock)
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
            mLoadingTask = LoadingTask.newInstance(this);
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
                catch (Exception ignored)
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
    public void setFileId(long fileId)
    {
        mFileId = fileId;
    }

    /**
     * Gets file ID in DB
     * @return file ID in DB
     */
    public long getFileId()
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
            mNoteSupportListener.onNoteSupport(TextUtils.isEmpty(mSyntaxParser.getCommentLine()) ? OnNoteSupportListener.UNSUPPORTED : OnNoteSupportListener.SUPPORTED);
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



            TextDocument document;

            synchronized(mLock)
            {
                document = mDocument;
            }

            if (document != null)
            {
                document.setFontSize(mFontSize);
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



            TextDocument document;

            synchronized(mLock)
            {
                document = mDocument;
            }

            if (document != null)
            {
                document.setTabSize(mTabSize);
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



            TextDocument document;

            synchronized(mLock)
            {
                document = mDocument;
            }

            if (document != null)
            {
                document.setSelectionType(mSelectionType);
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
            mNoteSupportListener.onNoteSupport(TextUtils.isEmpty(mSyntaxParser.getCommentLine()) ? OnNoteSupportListener.UNSUPPORTED : OnNoteSupportListener.SUPPORTED);
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



        TextDocument document;

        synchronized(mLock)
        {
            document = mDocument;
        }

        if (document != null)
        {
            document.setOnProgressChangedListener(mProgressChangedListener);
        }
    }

    /**
     * Sets comment dialog requested listener
     * @param listener    comment dialog requested listener
     */
    public void setOnCommentDialogRequestedListener(OnCommentDialogRequestedListener listener)
    {
        mCommentDialogRequestedListener = listener;



        TextDocument document;

        synchronized(mLock)
        {
            document = mDocument;
        }

        if (document != null)
        {
            document.setOnCommentDialogRequestedListener(mCommentDialogRequestedListener);
        }
    }



    /**
     * File loading task
     */
    @SuppressWarnings("WeakerAccess")
    private static class LoadingTask extends AsyncTask<Void, Void, TextDocument>
    {
        @SuppressWarnings("FieldAccessedSynchronizedAndUnsynchronized")
        private ReviewSurfaceView mReviewSurfaceView = null;
        private Context           mContext           = null;
        private String            mPath              = null;
        private long              mDbFileId          = 0;
        private SyntaxParserBase  mParser            = null;
        private String            mNote              = null;



        /** {@inheritDoc} */
        @Override
        public String toString()
        {
            return "LoadingTask{" +
                    "mReviewSurfaceView=" + mReviewSurfaceView +
                    ", mContext="         + mContext           +
                    ", mPath='"           + mPath              + '\'' +
                    ", mDbFileId="        + mDbFileId          +
                    ", mParser="          + mParser            +
                    ", mNote='"           + mNote              + '\'' +
                    '}';
        }

        /**
         * Creates LoadingTask instance for provided ReviewSurfaceView
         * @param view    ReviewSurfaceView
         */
        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        private LoadingTask(ReviewSurfaceView view)
        {
            mReviewSurfaceView = view;
            mContext           = mReviewSurfaceView.getContext();
            mPath              = mReviewSurfaceView.mFilePath;
            mDbFileId          = mReviewSurfaceView.mFileId;
            mParser            = mReviewSurfaceView.mSyntaxParser;
            mNote              = null;
        }

        /**
         * Creates LoadingTask instance for provided ReviewSurfaceView
         * @param view    ReviewSurfaceView
         */
        public static LoadingTask newInstance(ReviewSurfaceView view)
        {
            return new LoadingTask(view);
        }

        /**
         * Interrupts execution
         */
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

                document.setParent(mReviewSurfaceView);

                if (mDbFileId <= 0)
                {
                    MainDatabase helper = MainDatabase.newInstance(mContext);
                    db = helper.getReadableDatabase();

                    mDbFileId = MainDatabase.getFileId(db, mPath);
                    mNote     = MainDatabase.getFileNote(db, mDbFileId);

                    db.close();
                    db = null;
                }

                ArrayList<TextRow> rows = document.getRows();

                if (mDbFileId > 0)
                {
                    SingleFileDatabase helper = SingleFileDatabase.newInstance(mContext, mDbFileId);

                    db            = helper.getReadableDatabase();
                    Cursor cursor = SingleFileDatabase.getRows(db);

                    int idIndex   = cursor.getColumnIndexOrThrow(SingleFileDatabase.COLUMN_ID);
                    int typeIndex = cursor.getColumnIndexOrThrow(SingleFileDatabase.COLUMN_TYPE);

                    cursor.moveToFirst();

                    while (!cursor.isAfterLast())
                    {
                        int row = cursor.getInt(idIndex) - 1;

                        if (row >= 0 && row < rows.size())
                        {
                            String typeStr = cursor.getString(typeIndex);

                            char type = TextUtils.isEmpty(typeStr) ? '-' : typeStr.charAt(0);

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
                                    AppLog.wtf(TAG, "Unknown row type \"" + type + "\" in database \"" + helper.getDbName() + '\"');
                                }
                                break;
                            }
                        }
                        else
                        {
                            AppLog.wtf(TAG, "Unexpected row id (" + row + ") with row count (" + rows.size() + ')');
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
                            AppLog.wtf(TAG, "Unknown selection type: " + row.getSelectionType());
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
        @SuppressWarnings("AccessingNonPublicFieldOfAnotherObject")
        @Override
        protected void onPostExecute(TextDocument textDocument)
        {
            if (textDocument != null)
            {
                textDocument.init();

                textDocument.setFontSize(mReviewSurfaceView.mFontSize);
                textDocument.setTabSize(mReviewSurfaceView.mTabSize);
                textDocument.setSelectionType(mReviewSurfaceView.mSelectionType);
                textDocument.setOnProgressChangedListener(mReviewSurfaceView.mProgressChangedListener);
                textDocument.setOnCommentDialogRequestedListener(mReviewSurfaceView.mCommentDialogRequestedListener);

                if (!TextUtils.isEmpty(mNote))
                {
                    mReviewSurfaceView.mFileNoteLoadedListener.onFileNoteLoaded(mNote);
                }



                long modifiedTime = new File(mPath).lastModified();

                synchronized(mReviewSurfaceView.mLock)
                {
                    mReviewSurfaceView.mModifiedTime = modifiedTime;
                    mReviewSurfaceView.mDocument     = textDocument;
                }

                mReviewSurfaceView.repaint();

                mReviewSurfaceView.mLoadingTask = null;
                mReviewSurfaceView = null;
            }
        }
    }
}
