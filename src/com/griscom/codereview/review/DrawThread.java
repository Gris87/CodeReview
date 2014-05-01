package com.griscom.codereview.review;

import android.graphics.Canvas;
import android.view.SurfaceHolder;

import com.griscom.codereview.listeners.OnReviewSurfaceDrawListener;

class DrawThread extends Thread
{
    private SurfaceHolder               mSurfaceHolder;
    private OnReviewSurfaceDrawListener mDrawer;
    private boolean                     mTerminated;

    public DrawThread(SurfaceHolder surfaceHolder, OnReviewSurfaceDrawListener drawer)
	{
		mSurfaceHolder = surfaceHolder;
		mDrawer        = drawer;
		mTerminated    = false;
    }

    public void terminate()
	{
        mTerminated=true;
    }

    @Override
    public void run()
	{
        while (!mTerminated)
		{
            if (!mSurfaceHolder.getSurface().isValid())
            {
                continue;
            }

            Canvas canvas=null;

            try
			{
                canvas=mSurfaceHolder.lockCanvas(null);

                synchronized (mSurfaceHolder)
				{
                    mDrawer.onReviewSurfaceDraw(canvas);
                }
            }
            finally
			{
                if (canvas != null)
				{
                    mSurfaceHolder.unlockCanvasAndPost(canvas);
                }
            }
        }
    }
}
