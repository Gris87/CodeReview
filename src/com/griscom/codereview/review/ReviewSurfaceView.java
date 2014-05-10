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
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.griscom.codereview.R;
import com.griscom.codereview.listeners.OnDocumentLoadedListener;
import com.griscom.codereview.listeners.OnReviewSurfaceDrawListener;
import com.griscom.codereview.other.SelectionColor;
import com.griscom.codereview.review.syntax.SyntaxParserBase;
import com.griscom.codereview.util.Utils;

public class ReviewSurfaceView extends SurfaceView implements OnReviewSurfaceDrawListener, OnDocumentLoadedListener, OnTouchListener
{
    private static final int LOADED_MESSAGE  = 1;
    private static final int REPAINT_MESSAGE = 2;



    private Context            mContext;
    private SurfaceHolder      mSurfaceHolder;
    private LoadingThread      mLoadingThread;
    private DrawThread         mDrawThread;
    private String             mFileName;
    private SyntaxParserBase   mSyntaxParser;
    private TextDocument       mDocument;
    private SelectionColor     mSelectionColor;

    // USED IN HANDLER [
    private TextDocument       mLastLoadedDocument;
    // USED IN HANDLER ]



    @SuppressLint("HandlerLeak")
    private Handler mHandler=new Handler()
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
            mDocument           = mLastLoadedDocument;
            mLastLoadedDocument = null;

            mDocument.init(ReviewSurfaceView.this);

            mDocument.setX(mContext.getResources().getDimensionPixelSize(R.dimen.review_horizontal_margin));
            mDocument.setY(mContext.getResources().getDimensionPixelSize(R.dimen.review_vertical_margin));
            mDocument.setSelectionColor(mSelectionColor);

            repaint();
        }

        private void repaint()
        {
            if (mDrawThread!=null)
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
        mContext            = context;
        mSurfaceHolder      = getHolder();
        mLoadingThread      = null;
        mDrawThread         = null;
        mSyntaxParser       = null;
        mDocument           = null;
        mSelectionColor     = SelectionColor.REVIEWED_COLOR;
        mLastLoadedDocument = null;
    }

    public void onDestroy()
    {
        mDocument=null;
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

        mDrawThread=new DrawThread(mSurfaceHolder, this);
        mDrawThread.start();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig)
    {
        if (mDocument!=null)
        {
            mDocument.onConfigurationChanged(newConfig);
        }

        repaint(80);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        if (mDocument!=null)
        {
            return mDocument.onTouch(v, event);
        }

        return true;
    }

    @Override
    public void onReviewSurfaceDraw(Canvas canvas)
    {
        if (mDocument!=null)
        {
            mDocument.draw(canvas);
        }
        else
        {
            canvas.drawColor(Color.WHITE);

            Paint paint=new Paint();

            paint.setColor(Color.GRAY);
            paint.setTextSize(Utils.spToPixels(36, mContext));
            paint.setTextAlign(Align.CENTER);

            canvas.drawText(mContext.getString(R.string.loading), getWidth()*0.5f, getHeight()*0.5f, paint);
        }
    }

    @Override
    public void onDocumentLoaded(TextDocument document)
    {
        mLastLoadedDocument=document;
        mHandler.sendEmptyMessage(LOADED_MESSAGE);
    }

    public void repaint()
    {
        repaint(0);
    }

    public void repaint(long timeout)
    {
        if (timeout>0)
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
        stopLoadingThread();

        mDocument=null;
        repaint();

        mLoadingThread=new LoadingThread(mSyntaxParser, this, mFileName);
        mLoadingThread.start();
    }

    private void stopLoadingThread()
    {
        if (mLoadingThread!=null)
        {
            mLoadingThread.interrupt();

            do
            {
                try
                {
                    mLoadingThread.join();
                    mLoadingThread=null;
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
        if (mDrawThread!=null)
        {
            mDrawThread.interrupt();

            do
            {
                try
                {
                    mDrawThread.join();
                    mDrawThread=null;
                    return;
                }
                catch (Exception e)
                {
                }
            } while(true);
        }
    }

    public void setFileName(String fileName)
    {
        mFileName=fileName;

        mSyntaxParser=SyntaxParserBase.createParserByFileName(mFileName, mContext);

        // Maybe reload but no. Calls once and reload will come at the next step
        // reload();
    }

    public void setSelectionColor(SelectionColor colorType)
    {
        if (mSelectionColor!=colorType)
        {
            mSelectionColor=colorType;

            if (mDocument!=null)
            {
                mDocument.setSelectionColor(mSelectionColor);
            }
        }
    }
}
