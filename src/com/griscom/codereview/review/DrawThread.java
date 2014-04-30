package com.griscom.codereview.review;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.SurfaceHolder;

class DrawThread extends Thread
{
    private boolean mTerminated;
    private SurfaceHolder mSurfaceHolder;

    public DrawThread(SurfaceHolder surfaceHolder)
	{
		mTerminated    = false;
        mSurfaceHolder = surfaceHolder;
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
            Canvas canvas=null;

            try
			{
                canvas=mSurfaceHolder.lockCanvas(null);

                synchronized (mSurfaceHolder)
				{
                    Paint paint=new Paint();

                    paint.setARGB(255, 255, 255, 255);

                    canvas.drawLine(0, 0, 100, 100, paint);
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
