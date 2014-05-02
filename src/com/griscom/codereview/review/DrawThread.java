package com.griscom.codereview.review;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.griscom.codereview.listeners.OnReviewSurfaceDrawListener;

class DrawThread extends Thread
{
    private SurfaceHolder               mSurfaceHolder;
    private OnReviewSurfaceDrawListener mDrawer;
    private boolean                     mTerminated;
    private boolean                     mNeedRefresh;

    public DrawThread(SurfaceHolder surfaceHolder, OnReviewSurfaceDrawListener drawer)
    {
        mSurfaceHolder = surfaceHolder;
        mDrawer        = drawer;
        mTerminated    = false;
        mNeedRefresh   = true;
    }

    public void terminate()
    {
        mTerminated=true;
    }

    public void invalidate()
    {
        mNeedRefresh=true;
    }

    @Override
    public void run()
    {
        while (!mTerminated)
        {
            Canvas canvas=null;

            try
            {
                if (
                    !mNeedRefresh
                    ||
                    !mSurfaceHolder.getSurface().isValid()
                   )
                {
                    Thread.sleep(20);
                    continue;
                }

                canvas=mSurfaceHolder.lockCanvas();
                mNeedRefresh=false;

                synchronized (mSurfaceHolder)
                {
                    mDrawer.onReviewSurfaceDraw(canvas);
                }
            }
            catch (Exception e)
            {
                // Nothing
            }

            if (canvas != null)
            {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}

