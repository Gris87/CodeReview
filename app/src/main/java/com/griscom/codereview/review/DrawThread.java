package com.griscom.codereview.review;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.griscom.codereview.listeners.OnReviewSurfaceDrawListener;

/**
 * Draw thread
 */
@SuppressWarnings("WeakerAccess")
public class DrawThread extends Thread
{
    @SuppressWarnings("unused")
    private static final String TAG = "DrawThread";



    private SurfaceHolder               mSurfaceHolder = null;
    private OnReviewSurfaceDrawListener mDrawer        = null;
    private volatile boolean            mRunning       = false;
    private boolean                     mNeedRepaint   = false;
    private final Object                mLock          = new Object();



    @Override
    public String toString()
    {
        boolean needRepaint;

        synchronized(mLock)
        {
            needRepaint = mNeedRepaint;
        }

        return "DrawThread{" +
                "mSurfaceHolder=" + mSurfaceHolder +
                ", mDrawer="      + mDrawer        +
                ", mRunning="     + mRunning       +
                ", mNeedRepaint=" + needRepaint    +
                ", mLock="        + mLock          +
                '}';
    }

    /**
     * Creates DrawThread instance
     * @param surfaceHolder    surface holder
     * @param drawer           drawer
     */
    private DrawThread(SurfaceHolder surfaceHolder, OnReviewSurfaceDrawListener drawer)
    {
        mSurfaceHolder = surfaceHolder;
        mDrawer        = drawer;
        mRunning       = true;
        mNeedRepaint   = true;
    }

    /**
     * Creates DrawThread instance
     * @param surfaceHolder    surface holder
     * @param drawer           drawer
     */
    public static DrawThread newInstance(SurfaceHolder surfaceHolder, OnReviewSurfaceDrawListener drawer)
    {
        return new DrawThread(surfaceHolder, drawer);
    }

    /** {@inheritDoc} */
    @Override
    public void interrupt()
    {
        mRunning = false;

        super.interrupt();
    }

    /**
     * Sets flag for repainting
     */
    @SuppressWarnings("WeakerAccess")
    public void repaint()
    {
        synchronized(mLock)
        {
            mNeedRepaint = true;
        }
    }

    /** {@inheritDoc} */
    @Override
    public void run()
    {
        while (mRunning)
        {
            Canvas canvas = null;

            try
            {
                boolean needRepaint;

                synchronized(mLock)
                {
                    needRepaint = mNeedRepaint;
                }

                if (
                    needRepaint
                    &&
                    mSurfaceHolder.getSurface().isValid()
                   )
                {
                    synchronized(mLock)
                    {
                        mNeedRepaint = false;
                    }

                    canvas = mSurfaceHolder.lockCanvas();
                    mDrawer.onReviewSurfaceDraw(canvas);
                }
                else
                {
                    Thread.sleep(20);
                }
            }
            catch (Exception ignored)
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

