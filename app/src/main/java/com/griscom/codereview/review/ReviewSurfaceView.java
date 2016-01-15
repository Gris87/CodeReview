package com.griscom.codereview.review;

import java.io.File;
import java.io.PrintWriter;
import java.util.ArrayList;

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
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.griscom.codereview.R;
import com.griscom.codereview.listeners.OnDocumentLoadedListener;
import com.griscom.codereview.listeners.OnNoteSupportListener;
import com.griscom.codereview.listeners.OnProgressChangedListener;
import com.griscom.codereview.listeners.OnReviewSurfaceDrawListener;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.SelectionColor;
import com.griscom.codereview.review.syntax.SyntaxParserBase;
import com.griscom.codereview.util.Utils;

public class ReviewSurfaceView extends SurfaceView implements OnReviewSurfaceDrawListener, OnDocumentLoadedListener, OnTouchListener
{
    private static final String TAG = "ReviewSurfaceView";

    private static final int LOADED_MESSAGE  = 1;
    private static final int REPAINT_MESSAGE = 2;



    private Context                   mContext;
    private SurfaceHolder             mSurfaceHolder;
    private LoadingThread             mLoadingThread;
    private DrawThread                mDrawThread;
    private String                    mFileName;
    private int                       mFileId;
    private long                      mModifiedTime;
    private SyntaxParserBase          mSyntaxParser;
    private TextDocument              mDocument;
    private int                       mFontSize;
    private int                       mTabSize;
    private SelectionColor            mSelectionColor;
    private OnNoteSupportListener     mNoteSupportListener;
    private OnProgressChangedListener mProgressChangedListener;

    // USED IN HANDLER [
    private TextDocument              mLastLoadedDocument;
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
            mLastLoadedDocument.setSelectionColor(mSelectionColor);
            mLastLoadedDocument.setOnProgressChangedListener(mProgressChangedListener);

            long modifiedTime = new File(mFileName).lastModified();

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
        mContext                 = context;
        mSurfaceHolder           = getHolder();
        mLoadingThread           = null;
        mDrawThread              = null;
        mFileId                  = 0;
        mModifiedTime            = 0;
        mSyntaxParser            = null;
        mDocument                = null;
        mFontSize                = ApplicationSettings.fontSize(mContext);
        mTabSize                 = ApplicationSettings.tabSize(mContext);
        mSelectionColor          = SelectionColor.REVIEWED;
        mNoteSupportListener     = null;
        mProgressChangedListener = null;
        mLastLoadedDocument      = null;
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
            canvas.drawColor(Color.WHITE);

            Paint paint = new Paint();

            paint.setColor(Color.GRAY);
            paint.setTextSize(Utils.spToPixels(36, mContext));
            paint.setTextAlign(Align.CENTER);

            if (modifiedTime == -1)
            {
                canvas.drawText(mContext.getString(R.string.file_not_found), getWidth() * 0.5f, getHeight() * 0.5f, paint);
            }
            else
            {
                canvas.drawText(mContext.getString(R.string.loading),        getWidth() * 0.5f, getHeight() * 0.5f, paint);
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
                PrintWriter        writer = new PrintWriter(mFileName);

                for (int i = 0; i < rows.size() - 1; ++i)
                {
                    writer.println(rows.get(i).toString());
                }

                if (rows.size() > 0)
                {
                    writer.print(rows.get(rows.size() - 1));
                }

                writer.close();

                long modifiedTime = new File(mFileName).lastModified();

                synchronized(this)
                {
                    mModifiedTime = modifiedTime;
                }
            }
            catch(Exception e)
            {
                Log.e(TAG, "Impossible to save file: " + mFileName, e);
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
        File file = new File(mFileName);

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
                mLoadingThread = new LoadingThread(this, mSyntaxParser, this, mFileName, mFileId);
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

    public String getFileName()
    {
        return mFileName;
    }

    public int getFileId()
    {
        return mFileId;
    }

    public void setFileName(String fileName, int fileId)
    {
        mFileName = fileName;
        mFileId   = fileId;

        mSyntaxParser = SyntaxParserBase.createParserByFileName(mFileName, mContext);

        if (mNoteSupportListener != null && mSyntaxParser != null)
        {
            mNoteSupportListener.onNoteSupport(!TextUtils.isEmpty(mSyntaxParser.getCommentLine()));
        }

        // Maybe reload but no. Calls once and reload will come at the next step
        // reload();
    }

    public void setFileId(int fileId)
    {
        mFileId = fileId;
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

    public void setSelectionColor(SelectionColor selectionColor)
    {
        if (mSelectionColor != selectionColor)
        {
            mSelectionColor = selectionColor;

            if (mDocument != null)
            {
                mDocument.setSelectionColor(mSelectionColor);
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
}