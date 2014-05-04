package com.griscom.codereview.review;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.griscom.codereview.listeners.OnReviewSurfaceDrawListener;

class DrawThread extends Thread
{
    private SurfaceHolder               mSurfaceHolder;
    private OnReviewSurfaceDrawListener mDrawer;
    private boolean                     mNeedRepaint;

    public DrawThread(SurfaceHolder surfaceHolder, OnReviewSurfaceDrawListener drawer)
    {
        mSurfaceHolder = surfaceHolder;
        mDrawer        = drawer;
        mNeedRepaint   = true;
    }

    public void repaint()
    {
        synchronized (mSurfaceHolder)
        {
            mNeedRepaint=true;
        }
    }

    @Override
    public void run()
    {
        while (!Thread.interrupted())
        {
            Canvas canvas=null;

            try
            {
                if (
                    !mNeedRepaint
                    ||
                    !mSurfaceHolder.getSurface().isValid()
                   )
                {
                    Thread.sleep(20);
                    continue;
                }

                synchronized (mSurfaceHolder)
                {
                    mNeedRepaint=false;

                    canvas=mSurfaceHolder.lockCanvas();
                    mDrawer.onReviewSurfaceDraw(canvas);
                }
            }
            catch (Exception e)
            {
                // Nothing
            }

            if (canvas!=null)
            {
                mSurfaceHolder.unlockCanvasAndPost(canvas);
            }
        }
    }
}

