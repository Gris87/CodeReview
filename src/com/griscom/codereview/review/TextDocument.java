package com.griscom.codereview.review;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

public class TextDocument implements OnTouchListener
{
    private static final int HIDE_BARS_MESSAGE = 1;
    private static final int HIGHLIGHT_MESSAGE = 2;



    private Context            mContext;
    private ReviewSurfaceView  mParent;
    private DocumentHandler    mHandler;
    private ArrayList<TextRow> mRows;
    private float              mX;
    private float              mY;
    private float              mWidth;
    private float              mHeight;
    private float              mOffsetX;
    private float              mOffsetY;
    private float              mViewWidth;
    private float              mViewHeight;

    // USED IN HANDLER [
    private int                mBarsAlpha;
    // USED IN HANDLER ]



    public TextDocument(Context context)
    {
        mContext    = context;
        mParent     = null;
        mHandler    = null;
        mRows       = new ArrayList<TextRow>();

        mX          = 0;
        mY          = 0;
        mWidth      = 0;
        mHeight     = 0;
        mOffsetX    = 0;
        mOffsetY    = 0;
        mViewWidth  = 0;
        mViewHeight = 0;

        mBarsAlpha  = 0;
    }

    public void init(ReviewSurfaceView parent)
    {
        mParent=parent;
        mHandler=new DocumentHandler();

        onConfigurationChanged(mContext.getResources().getConfiguration());
        showBars();
    }

    public void draw(Canvas canvas)
    {
        for (int i=0; i<mRows.size(); ++i)
        {
            mRows.get(i).draw(canvas, mX-mOffsetX, mY-mOffsetY);
        }

        if (
            (mViewWidth>0 && mWidth>mViewWidth)
            ||
            (mViewHeight>0 && mHeight>mViewHeight)
           )
        {
            float density=mContext.getResources().getDisplayMetrics().scaledDensity;
            float margin=6*density;

            Paint barPaint=new Paint();
            Paint barBackgroundPaint=new Paint();

            barPaint.setARGB(mBarsAlpha, 180, 180, 180);
            barPaint.setStrokeWidth(4*density);

            barBackgroundPaint.setARGB(mBarsAlpha, 140, 140, 140);
            barBackgroundPaint.setStrokeWidth(8*density);

            if (mViewWidth>0 && mWidth>mViewWidth)
            {
                float barLength=mViewWidth/mWidth;
                float barWidth=mViewWidth-margin*3;
                float barPosition=0;

                canvas.drawLine(margin,             mViewHeight-margin, barWidth+margin,           mViewHeight-margin, barBackgroundPaint);
                canvas.drawLine(barPosition+margin, mViewHeight-margin, barWidth*barLength+margin, mViewHeight-margin, barPaint);
            }

            if (mViewHeight>0 && mHeight>mViewHeight)
            {
                float barLength=mViewHeight/mHeight;
                float barHeight=mViewHeight-margin*3;
                float barPosition=0;

                canvas.drawLine(mViewWidth-margin, margin,             mViewWidth-margin, barHeight+margin,           barBackgroundPaint);
                canvas.drawLine(mViewWidth-margin, barPosition+margin, mViewWidth-margin, barHeight*barLength+margin, barPaint);
            }
        }
    }

    public void addTextRow(TextRow row)
    {
        mRows.add(row);

        row.setY(mHeight);

        mHeight+=row.getHeight();

        if (row.getWidth()>mWidth)
        {
            mWidth=row.getWidth();
        }
    }

    @SuppressWarnings("deprecation")
    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    public void onConfigurationChanged(Configuration newConfig)
    {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
        {
            Point size = new Point();
            display.getSize(size);

            mViewWidth  = size.x;
            mViewHeight = size.y;
        }
        else
        {
            mViewWidth  = display.getWidth();
            mViewHeight = display.getHeight();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        showBars();

        // TODO Auto-generated method stub
        return false;
    }

    private void repaint()
    {
        if (mParent!=null)
        {
            mParent.repaint();
        }
    }

    private void showBars()
    {

        if (
            (mViewWidth>0 && mWidth>mViewWidth)
            ||
            (mViewHeight>0 && mHeight>mViewHeight)
            )
        {
            mBarsAlpha=255;

            mHandler.removeMessages(HIDE_BARS_MESSAGE);
            mHandler.sendEmptyMessageDelayed(HIDE_BARS_MESSAGE, 1000);

            repaint();
        }
    }

    public void setX(float x)
    {
        mX=x;
    }

    public void setY(float y)
    {
        mY=y;
    }

    public float getX()
    {
        return mX;
    }

    public float getY()
    {
        return mY;
    }

    public float getWidth()
    {
        return mWidth;
    }

    public float getHeight()
    {
        return mHeight;
    }

    public float getRight()
    {
        return mX+mWidth;
    }

    public float getBottom()
    {
        return mY+mHeight;
    }



    @SuppressLint("HandlerLeak")
    private class DocumentHandler extends Handler
    {
        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case HIDE_BARS_MESSAGE:
                    hideBars();
                break;

                case HIGHLIGHT_MESSAGE:
                    highlight();
                break;
            }
        }

        private void hideBars()
        {
            mBarsAlpha-=20;

            if (mBarsAlpha>0)
            {
                sendEmptyMessageDelayed(HIDE_BARS_MESSAGE, 40);
            }
            else
            {
                mBarsAlpha=0;
            }

            repaint();
        }

        private void highlight()
        {

        }
    }
}
