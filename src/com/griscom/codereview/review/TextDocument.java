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

import com.griscom.codereview.R;

public class TextDocument implements OnTouchListener
{
    private static final int HIDE_BARS_MESSAGE  = 1;
    private static final int HIGHLIGHT_MESSAGE  = 2;

    private static final int AUTO_HIDE_DELAY    = 3000;

    private static final int SCROLL_THRESHOLD   = 25;
    private static final int BOTTOM_RIGHT_SPACE = 250;



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

    private boolean            mTouchSelection;
    private boolean            mTouchScroll;
    private float              mTouchX;
    private float              mTouchY;

    // USED IN HANDLER [
    private int                mBarsAlpha;
    private int                mHighlightedRow;
    private int                mHighlightAlpha;
    private int                mHighlightColor;
    // USED IN HANDLER ]



    public TextDocument(Context context)
    {
        mContext        = context;
        mParent         = null;
        mHandler        = null;
        mRows           = new ArrayList<TextRow>();

        mX              = 0;
        mY              = 0;
        mWidth          = 0;
        mHeight         = 0;
        mOffsetX        = 0;
        mOffsetY        = 0;
        mViewWidth      = 0;
        mViewHeight     = 0;

        mTouchSelection = false;
        mTouchScroll    = false;
        mTouchX         = 0;
        mTouchY         = 0;

        mBarsAlpha      = 0;
        mHighlightedRow = -1;
        mHighlightAlpha = 0;
        mHighlightColor = mContext.getResources().getColor(R.color.highlight);
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
        // TODO: Only visible
        for (int i=0; i<mRows.size(); ++i)
        {
            if (mHighlightAlpha>0 && i==mHighlightedRow)
            {
                Paint highlightPaint=new Paint();

                highlightPaint.setColor(mHighlightColor);
                highlightPaint.setAlpha(mHighlightAlpha);

                canvas.drawRect(0, mY-mOffsetY+mRows.get(i).getY(), mViewWidth, mY-mOffsetY+mRows.get(i).getBottom(), highlightPaint);
            }

            mRows.get(i).draw(canvas, mX-mOffsetX, mY-mOffsetY);
        }

        if (
            (mViewWidth>0  && (mWidth+BOTTOM_RIGHT_SPACE)>mViewWidth)
            ||
            (mViewHeight>0 && (mHeight+BOTTOM_RIGHT_SPACE)>mViewHeight)
           )
        {
            float density=mContext.getResources().getDisplayMetrics().scaledDensity;
            float margin=6*density;

            Paint barPaint=new Paint();

            barPaint.setARGB(mBarsAlpha, 180, 180, 180);
            barPaint.setStrokeWidth(4*density);

            if (mViewWidth>0 && (mWidth+BOTTOM_RIGHT_SPACE)>mViewWidth)
            {
                float barLength   = mViewWidth/(mWidth+BOTTOM_RIGHT_SPACE);
                float barWidth    = mViewWidth-margin*3;
                float barPosition = barWidth*mOffsetX/(mWidth+BOTTOM_RIGHT_SPACE);

                canvas.drawLine(barPosition+margin, mViewHeight-margin, barWidth*barLength+barPosition+margin, mViewHeight-margin, barPaint);
            }

            if (mViewHeight>0 && (mHeight+BOTTOM_RIGHT_SPACE)>mViewHeight)
            {
                float barLength   = mViewHeight/(mHeight+BOTTOM_RIGHT_SPACE);
                float barHeight   = mViewHeight-margin*3;
                float barPosition = barHeight*mOffsetY/(mHeight+BOTTOM_RIGHT_SPACE);

                canvas.drawLine(mViewWidth-margin, barPosition+margin, mViewWidth-margin, barHeight*barLength+barPosition+margin, barPaint);
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

        if (event.getAction()==MotionEvent.ACTION_DOWN)
        {
            mTouchSelection = false;
            mTouchScroll    = false;

            mTouchX         = event.getX();
            mTouchY         = event.getY();

            mHighlightedRow = -1;
            mHighlightAlpha = 0;

            // TODO: Only visible
            for (int i=0; i<mRows.size(); ++i)
            {
                if (mTouchY>=mY-mOffsetY+mRows.get(i).getY() && mTouchY<=mY-mOffsetY+mRows.get(i).getBottom())
                {
                    mHighlightedRow=i;

                    mHandler.sendEmptyMessageDelayed(HIGHLIGHT_MESSAGE, 1000);

                    break;
                }
            }
        }
        else
        if (event.getAction()==MotionEvent.ACTION_MOVE)
        {
            if (mTouchSelection)
            {

            }
            else
            {
                if (
                    !mTouchScroll
                    &&
                    (
                     Math.abs(mTouchX-event.getX())>SCROLL_THRESHOLD
                     ||
                     Math.abs(mTouchY-event.getY())>SCROLL_THRESHOLD
                    )
                   )
                {
                    mTouchScroll=true;

                    if (mHighlightedRow>=0)
                    {
                        mHighlightedRow = -1;
                        mHighlightAlpha = 0;

                        mHandler.removeMessages(HIGHLIGHT_MESSAGE);
                    }
                }

                if (mTouchScroll)
                {
                    float newOffsetX=mOffsetX+(mTouchX-event.getX());
                    float newOffsetY=mOffsetY+(mTouchY-event.getY());

                    if (newOffsetX>mWidth-mViewWidth+BOTTOM_RIGHT_SPACE)
                    {
                        newOffsetX=mWidth-mViewWidth+BOTTOM_RIGHT_SPACE;
                    }

                    if (newOffsetX<0)
                    {
                        newOffsetX=0;
                    }

                    if (newOffsetY>mHeight-mViewHeight+BOTTOM_RIGHT_SPACE)
                    {
                        newOffsetY=mHeight-mViewHeight+BOTTOM_RIGHT_SPACE;
                    }

                    if (newOffsetY<0)
                    {
                        newOffsetY=0;
                    }



                    if (
                        mOffsetX != newOffsetX
                        ||
                        mOffsetY != newOffsetY
                       )
                    {
                        mOffsetX = newOffsetX;
                        mOffsetY = newOffsetY;

                        repaint();
                    }



                    mTouchX = event.getX();
                    mTouchY = event.getY();
                }
            }
        }
        else
        {
            if (mTouchSelection)
            {
                // TODO: Implement it
            }
            else
            {
                if (mHighlightedRow>=0)
                {
                    mHighlightedRow = -1;
                    mHighlightAlpha = 0;

                    mHandler.removeMessages(HIGHLIGHT_MESSAGE);
                }
            }
        }

        return true;
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
            (mViewWidth>0  && (mWidth+BOTTOM_RIGHT_SPACE)>mViewWidth)
            ||
            (mViewHeight>0 && (mHeight+BOTTOM_RIGHT_SPACE)>mViewHeight)
           )
        {
            mBarsAlpha=255;

            mHandler.removeMessages(HIDE_BARS_MESSAGE);
            mHandler.sendEmptyMessageDelayed(HIDE_BARS_MESSAGE, AUTO_HIDE_DELAY);

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
            mHighlightAlpha+=20;

            if (mHighlightAlpha<255)
            {
                sendEmptyMessageDelayed(HIGHLIGHT_MESSAGE, 40);
            }
            else
            {
                mHighlightAlpha=0;
                mTouchSelection=true;
            }

            repaint();
        }
    }
}
