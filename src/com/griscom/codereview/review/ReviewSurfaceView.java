package com.griscom.codereview.review;

import android.annotation.SuppressLint;
import android.content.Context;
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
    private TextDocument       mLastLoadedDocument;



    @SuppressLint("HandlerLeak")
	private Handler mHandler=new Handler()
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case LOADED_MESSAGE:
                {
                    mDocument           = mLastLoadedDocument;
                    mLastLoadedDocument = null;

                    mDocument.setX(mContext.getResources().getDimensionPixelSize(R.dimen.review_horizontal_margin));
                    mDocument.setY(mContext.getResources().getDimensionPixelSize(R.dimen.review_vertical_margin));

                    repaint(200);
                }
                break;

                case REPAINT_MESSAGE:
                {
                    if (mDrawThread!=null)
                    {
                        mDrawThread.repaint();
                    }
                }
                break;
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
        mContext       = context;
        mSurfaceHolder = getHolder();
        mDrawThread    = null;
        mSyntaxParser  = null;
        mDocument      = null;

        setOnTouchListener(this);
    }

    public void pause()
    {
        stopLoadingThread();
        stopDrawThread();
    }

    public void resume()
    {
        stopDrawThread();

        reload();
		repaint(200);

        mDrawThread=new DrawThread(mSurfaceHolder, this);
        mDrawThread.start();
    }

    @Override
    public void onReviewSurfaceDraw(Canvas canvas)
    {
        canvas.drawColor(Color.WHITE);

        if (mDocument!=null)
        {
            mDocument.draw(canvas, 0, 0);
        }
        else
        {
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

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        // TODO Auto-generated method stub
        return false;
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
            mDrawThread.terminate();
            mDrawThread.interrupt();

            do
            {
                try
                {
                    mDrawThread.join();
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

        // Maybe reload but no
        // reload();
    }
}
