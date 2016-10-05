package com.griscom.codereview.review;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.griscom.codereview.R;
import com.griscom.codereview.listeners.OnCommentDialogRequestedListener;
import com.griscom.codereview.listeners.OnDocumentLoadedListener;
import com.griscom.codereview.listeners.OnNoteSupportListener;
import com.griscom.codereview.listeners.OnProgressChangedListener;
import com.griscom.codereview.listeners.OnReviewSurfaceDrawListener;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.ColorCache;
import com.griscom.codereview.other.SelectionType;
import com.griscom.codereview.review.syntax.SyntaxParserBase;
import com.griscom.codereview.util.AppLog;
import com.griscom.codereview.util.Utils;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

/**
 * SurfaceView that used in Review activity
 */
public class ReviewSurfaceView extends SurfaceView implements OnReviewSurfaceDrawListener, OnDocumentLoadedListener, OnTouchListener
{
    private static final String TAG = "ReviewSurfaceView";

    private static final int LOADED_MESSAGE  = 1;
    private static final int REPAINT_MESSAGE = 2;



    private Context                          mContext;
    private SurfaceHolder                    mSurfaceHolder;
    private LoadingThread                    mLoadingThread;
    private DrawThread                       mDrawThread;
    private String                           mFilePath;
    private int                              mFileId;
    private long                             mModifiedTime;
    private SyntaxParserBase                 mSyntaxParser;
    private TextDocument                     mDocument;
    private int                              mFontSize;
    private int                              mTabSize;
    private int                              mSelectionType;
    private OnNoteSupportListener            mNoteSupportListener;
    private OnProgressChangedListener        mProgressChangedListener;
    private OnCommentDialogRequestedListener mCommentDialogRequestedListener;

    // USED IN HANDLER [
    private TextDocument                     mLastLoadedDocument;
    // USED IN HANDLER ]



    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case LOADED_MESSAGE:
                    loaded();
                break;

                case REPAINT_MESSAGE:
                    repaint();
                break;
            }
        }

        private void loaded()
        {
            mLastLoadedDocument.init();

            mLastLoadedDocument.setFontSize(mFontSize);
            mLastLoadedDocument.setTabSize(mTabSize);
            mLastLoadedDocument.setSelectionType(mSelectionType);
            mLastLoadedDocument.setOnProgressChangedListener(mProgressChangedListener);
            mLastLoadedDocument.setOnCommentDialogRequestedListener(mCommentDialogRequestedListener);

            long modifiedTime = new File(mFilePath).lastModified();

            synchronized(this)
            {
                mModifiedTime       = modifiedTime;

                mDocument           = mLastLoadedDocument;
                mLastLoadedDocument = null;
            }

            repaint();
        }

        private void repaint()
        {
            if (mDrawThread != null)
            {
                mDrawThread.repaint();
            }
        }
    };



    public ReviewSurfaceView(Context context)
    {
        super(context);

        init(context);
    }

    public ReviewSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init(context);
    }

    public ReviewSurfaceView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init(context);
    }

    private void init(Context context)
    {
        mContext                        = context;
        mSurfaceHolder                  = getHolder();
        mLoadingThread                  = null;
        mDrawThread                     = null;
        mFileId                         = 0;
        mModifiedTime                   = 0;
        mSyntaxParser                   = null;
        mDocument                       = null;
        mFontSize                       = ApplicationSettings.getFontSize();
        mTabSize                        = ApplicationSettings.getTabSize();
        mSelectionType                  = SelectionType.REVIEWED;
        mNoteSupportListener            = null;
        mProgressChangedListener        = null;
        mCommentDialogRequestedListener = null;
        mLastLoadedDocument             = null;
    }

    public void onDestroy()
    {
        synchronized(this)
        {
            mDocument = null;
        }
    }

    public void onPause()
    {
        stopLoadingThread();
        stopDrawThread();
    }

    public void onResume()
    {
        stopDrawThread();

        reload();
        repaint(200);

        mDrawThread = new DrawThread(mSurfaceHolder, this);
        mDrawThread.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        if (mDocument != null)
        {
            mDocument.onConfigurationChanged(newConfig);
        }

        repaint(80);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (mDocument != null)
        {
            return mDocument.onTouch(v, event);
        }

        return true;
    }

    public void onCommentEntered(int firstRow, int lastRow, String comment)
    {
        if (mDocument != null)
        {
            mDocument.onCommentEntered(firstRow, lastRow, comment);
        }
    }

    public void onCommentCanceled()
    {
        if (mDocument != null)
        {
            mDocument.onCommentCanceled();
        }
    }

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
            paint.setTextSize(Utils.spToPixels(36, mContext));
            paint.setTextAlign(Align.CENTER);

            if (modifiedTime == -1)
            {
                canvas.drawText(mContext.getString(R.string.review_file_not_found), getWidth() * 0.5f, getHeight() * 0.5f, paint);
            }
            else
            {
                canvas.drawText(mContext.getString(R.string.review_loading),        getWidth() * 0.5f, getHeight() * 0.5f, paint);
            }
        }
    }

    @Override
    public void onDocumentLoaded(TextDocument document)
    {
        mLastLoadedDocument = document;
        mHandler.sendEmptyMessage(LOADED_MESSAGE);
    }

    public void saveRequested()
    {
        if (mDocument != null)
        {
            try
            {
                ArrayList<TextRow> rows   = mDocument.getRows();
                PrintWriter        writer = new PrintWriter(mFilePath);

                for (int i = 0; i < rows.size() - 1; ++i)
                {
                    writer.println(rows.get(i).toString());
                }

                if (rows.size() > 0)
                {
                    writer.print(rows.get(rows.size() - 1));
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

    public void repaint()
    {
        repaint(0);
    }

    public void repaint(long timeout)
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

    public void reload()
    {
        File file = new File(mFilePath);

        if (!file.exists() || mModifiedTime != file.lastModified())
        {
            stopLoadingThread();

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
                mLoadingThread = new LoadingThread(this, mSyntaxParser, this, mFilePath, mFileId);
                mLoadingThread.start();
            }
        }
    }

    private void stopLoadingThread()
    {
        if (mLoadingThread != null)
        {
            mLoadingThread.interrupt();

            do
            {
                try
                {
                    mLoadingThread.join();
                    mLoadingThread = null;
                    return;
                }
                catch (Exception e)
                {
                }
            } while(true);
        }
    }

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
                }
            } while(true);
        }
    }

    public void setFilePath(String filePath, int fileId)
    {
        mFilePath = filePath;
        mFileId   = fileId;

        mSyntaxParser = SyntaxParserBase.createParserByFileName(mFilePath, mContext);

        if (mNoteSupportListener != null && mSyntaxParser != null)
        {
            mNoteSupportListener.onNoteSupport(!TextUtils.isEmpty(mSyntaxParser.getCommentLine()));
        }

        // Maybe reload but no. Calls once and reload will come at the next step
        // reload();
    }

    public String getFilePath()
    {
        return mFilePath;
    }

    public void setFileId(int fileId)
    {
        mFileId = fileId;
    }

    public int getFileId()
    {
        return mFileId;
    }

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

    public void setOnNoteSupportListener(OnNoteSupportListener listener)
    {
        mNoteSupportListener = listener;

        if (mNoteSupportListener != null && mSyntaxParser != null)
        {
            mNoteSupportListener.onNoteSupport(!TextUtils.isEmpty(mSyntaxParser.getCommentLine()));
        }
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener)
    {
        mProgressChangedListener = listener;

        if (mDocument != null)
        {
            mDocument.setOnProgressChangedListener(mProgressChangedListener);
        }
    }

    public void setOnCommentDialogRequestedListener(OnCommentDialogRequestedListener listener)
    {
        mCommentDialogRequestedListener = listener;

        if (mDocument != null)
        {
            mDocument.setOnCommentDialogRequestedListener(mCommentDialogRequestedListener);
        }
    }
}
