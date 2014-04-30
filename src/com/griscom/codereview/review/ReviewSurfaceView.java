package com.griscom.codereview.review;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.griscom.codereview.listeners.OnReviewSurfaceDrawListener;

public class ReviewSurfaceView extends SurfaceView implements OnReviewSurfaceDrawListener
{
    private SurfaceHolder mSurfaceHolder;
    private DrawThread    mDrawThread;

    public ReviewSurfaceView(Context context)
	{
        super(context);

        init();
    }

    public ReviewSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init();
    }

    public ReviewSurfaceView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init();
    }

    private void init()
    {
        mSurfaceHolder = getHolder();
        mDrawThread    = null;
    }

    public void pause()
    {
        if (mDrawThread!=null)
        {
            mDrawThread.terminate();

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

    public void resume()
    {
        pause();

        mDrawThread=new DrawThread(mSurfaceHolder, this);
        mDrawThread.start();
    }

    @Override
    public void onReviewSurfaceDraw(Canvas canvas)
    {
        Paint paint=new Paint();

        paint.setARGB(255, 255, 255, 255);

        canvas.drawLine(0, 0, 100, 100, paint);
    }
}
