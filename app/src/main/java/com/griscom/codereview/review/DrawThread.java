package com.griscom.codereview.review;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.griscom.codereview.listeners.OnReviewSurfaceDrawListener;

/**
 * Draw thread
 */
class DrawThread extends Thread
{
    @SuppressWarnings("unused")
    private static final String TAG = "DrawThread";



    private SurfaceHolder               mSurfaceHolder;
    private OnReviewSurfaceDrawListener mDrawer;
    private boolean                     mTerminated;
    private boolean                     mNeedRepaint;



    /**
     * Creates DrawThread instance
     * @param surfaceHolder    surface holder
     * @param drawer           drawer
     */
    @SuppressWarnings("WeakerAccess")
    public DrawThread(SurfaceHolder surfaceHolder, OnReviewSurfaceDrawListener drawer)
    {
        mSurfaceHolder = surfaceHolder;
        mDrawer        = drawer;
        mTerminated    = false;
        mNeedRepaint   = true;
    }

    /** {@inheritDoc} */
    @Override
    public void interrupt()
    {
        mTerminated = true;

        super.interrupt();
    }

    /**
     * Sets flag for repainting
     */
    @SuppressWarnings("WeakerAccess")
    public void repaint()
    {
        synchronized (this)
        {
            mNeedRepaint = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run()
    {
        while (!mTerminated)
        {
            Canvas canvas = null;

            try
            {
                boolean needRepaint;

                synchronized (this)
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

                synchronized (this)
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

