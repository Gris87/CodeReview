package com.griscom.codereview.review;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.griscom.codereview.listeners.OnReviewSurfaceDrawListener;

class DrawThread extends Thread
{
    private SurfaceHolder               mSurfaceHolder;
    private OnReviewSurfaceDrawListener mDrawer;
    private boolean                     mTerminated;
    private boolean                     mNeedRepaint;



    public DrawThread(SurfaceHolder surfaceHolder, OnReviewSurfaceDrawListener drawer)
    {
        mSurfaceHolder = surfaceHolder;
        mDrawer        = drawer;
        mTerminated    = false;
        mNeedRepaint   = true;
    }

    @Override
    public void interrupt()
    {
        mTerminated = true;

        super.interrupt();
    }

    public void repaint()
    {
        synchronized (mSurfaceHolder)
        {
            mNeedRepaint = true;
        }
    }

    @Override
    public void run()
    {
        while (!mTerminated)
        {
            Canvas canvas = null;

            try
            {
                boolean needRepaint;

                synchronized (mSurfaceHolder)
                {
                    needRepaint = mNeedRepaint;
                }

                if (
                    !needRepaint
                    ||
                    !mSurfaceHolder.getSurface().isValid()
                   )
                {
                    Thread.sleep(20);
                    continue;
                }

                synchronized (mSurfaceHolder)
                {
                    mNeedRepaint = false;
                }

                canvas = mSurfaceHolder.lockCanvas();
                mDrawer.onReviewSurfaceDraw(canvas);
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

