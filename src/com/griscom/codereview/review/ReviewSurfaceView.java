package com.griscom.codereview.review;
import android.content.Context;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class ReviewSurfaceView extends SurfaceView implements SurfaceHolder.Callback
{
    private DrawThread drawThread;

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
        getHolder().addCallback(this);
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height)
	{
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder)
	{
        drawThread=new DrawThread(getHolder());
        drawThread.start();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder)
	{
        drawThread.terminate();

		do
		{
            try
			{
                drawThread.join();
                return;
            }
			catch (Exception e)
			{
            }
        } while(true);
    }
}
