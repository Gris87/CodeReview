package com.griscom.codereview.review;
import java.util.ArrayList;

import junit.framework.Assert;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import com.griscom.codereview.BuildConfig;
import com.griscom.codereview.R;
import com.griscom.codereview.listeners.OnProgressChangedListener;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.ColorCache;
import com.griscom.codereview.other.SelectionColor;
import com.griscom.codereview.other.TouchMode;
import com.griscom.codereview.util.Utils;

public class TextDocument implements OnTouchListener
{
    private static final int   HIDE_BARS_MESSAGE   = 1;
    private static final int   HIGHLIGHT_MESSAGE   = 2;
    private static final int   SELECTION_MESSAGE   = 3;
    private static final int   SCROLL_MESSAGE      = 4;

    private static final int   AUTO_HIDE_DELAY     = 3000;
    private static final int   HIGHLIGHT_DELAY     = 250;
    private static final int   VIBRATOR_LONG_CLICK = 50;
    private static final float SELECTION_SPEED     = 0.01f;
    private static final float SELECTION_LOW_LIGHT = 0.75f;
    private static final float SCROLL_SPEED        = 10;

    private static final int   SCROLL_THRESHOLD    = 25;
    private static final int   BOTTOM_RIGHT_SPACE  = 250;



    private Context                   mContext;
    private Vibrator                  mVibrator;
    private ReviewSurfaceView         mParent;
    private DocumentHandler           mHandler;
    private ArrayList<TextRow>        mRows;
    private OnProgressChangedListener mProgressChangedListener;
    private int                       mProgress;
    private Paint                     mRowPaint;

    private float                     mIndexWidth;
    private float                     mX;
    private float                     mY;
    private float                     mWidth;
    private float                     mHeight;
    private float                     mViewWidth;
    private float                     mViewHeight;
    private float                     mOffsetX;
    private float                     mOffsetY;
    private float                     mScale;
    private int                       mVisibleBegin;
    private int                       mVisibleEnd;

    private TouchMode                 mTouchMode;
    private float                     mTouchX;
    private float                     mTouchY;
    private float                     mFingerDistance;
    private int                       mSelectionEnd;
    private SelectionColor            mSelectionColor;

    // USED IN HANDLER [
    private int                       mBarsAlpha;
    private int                       mHighlightedRow;
    private int                       mHighlightAlpha;
    private float                     mSelectionBrighness;
    private boolean                   mSelectionMakeLight;
    // USED IN HANDLER ]



    public TextDocument(Context context)
    {
        mContext                 = context;
        mVibrator                = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mParent                  = null;
        mHandler                 = null;
        mRows                    = new ArrayList<TextRow>();
        mProgressChangedListener = null;
        mProgress                = 0;
        mRowPaint                = new Paint();
        mRowPaint.setColor(Color.LTGRAY);
        mRowPaint.setTypeface(Typeface.MONOSPACE);
        mRowPaint.setTextSize(Utils.spToPixels(ApplicationSettings.fontSize(mContext), mContext));

        mIndexWidth              = 0;
        mX                       = 0;
        mY                       = 0;
        mWidth                   = 0;
        mHeight                  = 0;
        mViewWidth               = 0;
        mViewHeight              = 0;
        mOffsetX                 = 0;
        mOffsetY                 = 0;
        mScale                   = 1;
        mVisibleBegin            = -1;
        mVisibleEnd              = -1;

        mTouchMode               = TouchMode.NONE;
        mTouchX                  = 0;
        mTouchY                  = 0;
        mFingerDistance          = 0;
        mSelectionEnd            = -1;
        mSelectionColor          = SelectionColor.REVIEWED;

        mBarsAlpha               = 0;
        mHighlightedRow          = -1;
        mHighlightAlpha          = 0;
        mSelectionBrighness      = 1;
        mSelectionMakeLight      = false;
    }

    public void init(ReviewSurfaceView parent)
    {
        mParent=parent;
        mHandler=new DocumentHandler();

        mX=mContext.getResources().getDimensionPixelSize(R.dimen.review_horizontal_margin);
        mY=mContext.getResources().getDimensionPixelSize(R.dimen.review_vertical_margin);

        mIndexWidth=mRowPaint.measureText(String.valueOf(mRows.size()+1));
        mX+=mIndexWidth;

        onConfigurationChanged(mContext.getResources().getConfiguration());
        showBars();
    }

    public void draw(Canvas canvas)
    {
        synchronized(this)
        {
            canvas.scale(mScale, mScale);

            if (
                mY-mOffsetY>=0
                ||
                mY-mOffsetY+mHeight<=mViewHeight/mScale
                ||
                mX-mOffsetX>=0
                )
            {
                Paint backgroundPaint=new Paint();
                backgroundPaint.setColor(Color.WHITE);

                if (mY-mOffsetY>=0)
                {
                    canvas.drawRect(0, 0, mViewWidth/mScale, mY-mOffsetY, backgroundPaint);
                }

                if (mY-mOffsetY+mHeight<=mViewHeight/mScale)
                {
                    canvas.drawRect(0, mY-mOffsetY+mHeight, mViewWidth/mScale, mViewHeight/mScale, backgroundPaint);
                }

                if (mX-mOffsetX>=0)
                {
                    canvas.drawRect(0, 0, mX-mOffsetX, mViewHeight/mScale, backgroundPaint);
                }
            }

            for (int i=mVisibleBegin; i<mVisibleEnd; ++i)
            {
                int color;

                if (mHighlightAlpha>0 && i==mHighlightedRow)
                {
                    color=ColorCache.getSelectionColor();
                    color=Color.argb(mHighlightAlpha, Color.red(color), Color.green(color), Color.blue(color));
                }
                else
                if (
                    mSelectionEnd>=0
                    &&
                    (
                    (i>=mHighlightedRow && i<=mSelectionEnd)
                    ||
                    (i>=mSelectionEnd   && i<=mHighlightedRow)
                    )
                   )
                {
                    float selectionHSV[]=new float[3];
                    Color.colorToHSV(ColorCache.get(mSelectionColor), selectionHSV);
                    selectionHSV[2]=mSelectionBrighness;
                    color=Color.HSVToColor(selectionHSV);
                }
                else
                {
                    color=ColorCache.get(mRows.get(i).getSelectionColor());
                }

                Paint backgroundPaint=new Paint();
                backgroundPaint.setColor(color);

                // Draw row background
                canvas.drawRect(mX-mOffsetX, mY-mOffsetY+mRows.get(i).getY(), mViewWidth/mScale, mY-mOffsetY+mRows.get(i).getBottom(), backgroundPaint);

                // Draw row number
                canvas.drawText(String.valueOf(i+1), -mOffsetX, mY-mOffsetY+mRows.get(i).getY()-mRowPaint.ascent(), mRowPaint);

                // Draw row
                mRows.get(i).draw(canvas, mX-mOffsetX, mY-mOffsetY);
            }

            if (
                (mViewWidth>0  && (mWidth+BOTTOM_RIGHT_SPACE)>mViewWidth/mScale)
                ||
                (mViewHeight>0 && (mHeight+BOTTOM_RIGHT_SPACE)>mViewHeight/mScale)
                )
            {
                float density=mContext.getResources().getDisplayMetrics().scaledDensity;
                float margin=6*density/mScale;

                Paint barPaint=new Paint();

                barPaint.setARGB(mBarsAlpha, 180, 180, 180);
                barPaint.setStrokeWidth(4*density/mScale);

                if (mViewWidth>0 && (mWidth+BOTTOM_RIGHT_SPACE)>mViewWidth/mScale)
                {
                    float barLength   = (mViewWidth/mScale)/(mWidth+BOTTOM_RIGHT_SPACE);
                    float barWidth    = (mViewWidth/mScale)-margin*3;
                    float barPosition = barWidth*mOffsetX/(mWidth+BOTTOM_RIGHT_SPACE);

                    canvas.drawLine(barPosition+margin, (mViewHeight/mScale)-margin, barWidth*barLength+barPosition+margin, (mViewHeight/mScale)-margin, barPaint);
                }

                if (mViewHeight>0 && (mHeight+BOTTOM_RIGHT_SPACE)>mViewHeight/mScale)
                {
                    float barLength   = (mViewHeight/mScale)/(mHeight+BOTTOM_RIGHT_SPACE);
                    float barHeight   = (mViewHeight/mScale)-margin*3;
                    float barPosition = barHeight*mOffsetY/(mHeight+BOTTOM_RIGHT_SPACE);

                    canvas.drawLine((mViewWidth/mScale)-margin, barPosition+margin, (mViewWidth/mScale)-margin, barHeight*barLength+barPosition+margin, barPaint);
                }
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

        synchronized(this)
        {
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

            updateVisibleRanges();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        showBars();

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
            {
                mTouchMode      = TouchMode.NONE;

                mTouchX         = event.getX();
                mTouchY         = event.getY();

                int highlightedRow=-1;

                for (int i=mVisibleBegin; i<mVisibleEnd; ++i)
                {
                    if (mTouchY/mScale>=mY-mOffsetY+mRows.get(i).getY() && mTouchY/mScale<=mY-mOffsetY+mRows.get(i).getBottom())
                    {
                        highlightedRow=i;

                        mHandler.sendEmptyMessageDelayed(HIGHLIGHT_MESSAGE, HIGHLIGHT_DELAY);

                        break;
                    }
                }

                synchronized(this)
                {
                    mHighlightedRow = highlightedRow;
                    mHighlightAlpha = 0;
                    mSelectionEnd   = -1;
                }
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN:
            {
                if (
                    mTouchMode == TouchMode.NONE
                    ||
                    mTouchMode == TouchMode.ZOOM
                   )
                {
                    mTouchMode = TouchMode.ZOOM;

                    stopHighlight();

                    mFingerDistance = fingerDistance(event);
                }
            }
            break;

            case MotionEvent.ACTION_MOVE:
            {
                if (mTouchMode==TouchMode.SELECT)
                {
                    mTouchX = event.getX();
                    mTouchY = event.getY();

                    updateSelection();

                    mHandler.removeMessages(SCROLL_MESSAGE);

                    if (
                        mTouchY<mViewHeight/8
                        ||
                        mTouchY>mViewHeight*7/8
                        )
                    {
                        touchScroll();
                    }
                }
                else
                if (mTouchMode==TouchMode.ZOOM)
                {
                    if (mFingerDistance!=0 && event.getPointerCount()>=2)
                    {
                        float newDistance=fingerDistance(event);

                        synchronized(this)
                        {
                            mScale = mScale * newDistance/mFingerDistance;

                            if (mScale<0.001f)
                            {
                                mScale=0.001f;
                            }
                            else
                            if (mScale>10f)
                            {
                                mScale=10f;
                            }
                        }

                        mFingerDistance=newDistance;

                        updateVisibleRanges();
                        repaint();
                    }
                }
                else
                {
                    if (
                        mTouchMode==TouchMode.NONE
                        &&
                        (
                        Math.abs(mTouchX-event.getX())>SCROLL_THRESHOLD
                        ||
                        Math.abs(mTouchY-event.getY())>SCROLL_THRESHOLD
                        )
                       )
                    {
                        mTouchMode=TouchMode.DRAG;

                        stopHighlight();
                    }

                    if (mTouchMode==TouchMode.DRAG)
                    {
                        float newOffsetX=mOffsetX+(mTouchX-event.getX())/mScale;
                        float newOffsetY=mOffsetY+(mTouchY-event.getY())/mScale;

                        if (newOffsetX>mWidth-(mViewWidth/mScale)+BOTTOM_RIGHT_SPACE)
                        {
                            newOffsetX=mWidth-(mViewWidth/mScale)+BOTTOM_RIGHT_SPACE;
                        }

                        if (newOffsetX<0)
                        {
                            newOffsetX=0;
                        }

                        if (newOffsetY>mHeight-(mViewHeight/mScale)+BOTTOM_RIGHT_SPACE)
                        {
                            newOffsetY=mHeight-(mViewHeight/mScale)+BOTTOM_RIGHT_SPACE;
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
                            synchronized(this)
                            {
                                mOffsetX = newOffsetX;
                                mOffsetY = newOffsetY;
                            }

                            updateVisibleRanges();
                            repaint();
                        }



                        mTouchX = event.getX();
                        mTouchY = event.getY();
                    }
                }
            }
            break;

            case MotionEvent.ACTION_POINTER_UP:
            {
                // Nothing
            }
            break;

            default:
            {
                if (mTouchMode==TouchMode.SELECT)
                {
                    int firstRow;
                    int lastRow;

                    if (mSelectionEnd>mHighlightedRow)
                    {
                        firstRow = mHighlightedRow;
                        lastRow  = mSelectionEnd;
                    }
                    else
                    {
                        firstRow = mSelectionEnd;
                        lastRow  = mHighlightedRow;
                    }

                    int coloredRows=0;

                    for (int i=firstRow; i<=lastRow; ++i)
                    {
                        if (mRows.get(i).getSelectionColor()!=SelectionColor.CLEAR)
                        {
                            coloredRows++;
                        }
                    }

                    synchronized(this)
                    {
                        for (int i=firstRow; i<=lastRow; ++i)
                        {
                            mRows.get(i).setSelectionColor(mSelectionColor);

                            if (mRows.get(i).getSelectionColor()!=SelectionColor.CLEAR)
                            {
                                coloredRows--;
                            }
                        }
                    }

                    if (coloredRows!=0)
                    {
                        // Decrease because coloredRows will be negative if new colors added
                        mProgress-=coloredRows;

                        progressChanged();
                    }

                    mHighlightedRow = -1;
                    mSelectionEnd   = -1;

                    mHandler.removeMessages(SELECTION_MESSAGE);
                    mHandler.removeMessages(SCROLL_MESSAGE);

                    repaint();
                }
                else
                {
                    stopHighlight();
                }
            }
            break;
        }

        return true;
    }

    private float fingerDistance(MotionEvent event)
    {
        return (float) Math.sqrt(Math.pow(event.getX(0)-event.getX(1), 2)+Math.pow(event.getY(0)-event.getY(1), 2));
    }

    private void stopHighlight()
    {
        if (mHighlightedRow>=0)
        {
            synchronized(this)
            {
                mHighlightedRow = -1;
                mHighlightAlpha = 0;
            }

            mHandler.removeMessages(HIGHLIGHT_MESSAGE);
        }
    }

    private void repaint()
    {
        if (mParent!=null)
        {
            mParent.repaint();
        }
    }

    private void progressChanged()
    {
        if (mProgressChangedListener!=null)
        {
            mProgressChangedListener.onProgressChanged(mProgress*100/mRows.size());
        }
    }

    private void showBars()
    {

        if (
            (mViewWidth>0  && (mWidth+BOTTOM_RIGHT_SPACE)>mViewWidth/mScale)
            ||
            (mViewHeight>0 && (mHeight+BOTTOM_RIGHT_SPACE)>mViewHeight/mScale)
           )
        {
            synchronized(this)
            {
                mBarsAlpha=255;
            }

            mHandler.removeMessages(HIDE_BARS_MESSAGE);
            mHandler.sendEmptyMessageDelayed(HIDE_BARS_MESSAGE, AUTO_HIDE_DELAY);

            repaint();
        }
    }

    public void updateSelection()
    {
        int selectionEnd=mSelectionEnd;

        if (selectionEnd<0)
        {
            selectionEnd=mHighlightedRow;
        }

        while (selectionEnd>mVisibleBegin)
        {
            if (mTouchY/mScale<=mY-mOffsetY+mRows.get(selectionEnd-1).getBottom())
            {
                selectionEnd--;
            }
            else
            {
                break;
            }
        }

        while (selectionEnd<mVisibleEnd-1)
        {
            if (mTouchY/mScale>=mY-mOffsetY+mRows.get(selectionEnd+1).getY())
            {
                selectionEnd++;
            }
            else
            {
                break;
            }
        }

        if (mSelectionEnd!=selectionEnd)
        {
            synchronized(this)
            {
                mSelectionEnd=selectionEnd;
            }

            repaint();
        }
    }

    private void touchScroll()
    {
        float newOffsetY;

        if (mTouchY<mViewHeight/8)
        {
            newOffsetY=mOffsetY-SCROLL_SPEED/mScale;
        }
        else
        {
            newOffsetY=mOffsetY+SCROLL_SPEED/mScale;
        }

        if (newOffsetY>mHeight-(mViewHeight/mScale)+BOTTOM_RIGHT_SPACE)
        {
            newOffsetY=mHeight-(mViewHeight/mScale)+BOTTOM_RIGHT_SPACE;
        }

        if (newOffsetY<0)
        {
            newOffsetY=0;
        }



        if (mOffsetY != newOffsetY)
        {
            synchronized(this)
            {
                mOffsetY = newOffsetY;
            }

            mHandler.sendEmptyMessageDelayed(SCROLL_MESSAGE, 40);

            updateVisibleRanges();
            updateSelection();
            repaint();
        }
    }

    private void updateVisibleRanges()
    {
        if (mRows.size()==0)
        {
            if (BuildConfig.DEBUG)
            {
                Assert.assertEquals(mVisibleBegin, -1);
                Assert.assertEquals(mVisibleEnd,   -1);
            }

            return;
        }

        int visibleBegin = mVisibleBegin;
        int visibleEnd   = mVisibleEnd;

        if (visibleBegin<0)
        {
            visibleBegin=0;
        }

        while (visibleBegin>0)
        {
            if (mY-mOffsetY+mRows.get(visibleBegin-1).getBottom()>=0)
            {
                visibleBegin--;
            }
            else
            {
                break;
            }
        }

        while (visibleBegin<mRows.size())
        {
            if (mY-mOffsetY+mRows.get(visibleBegin).getBottom()<0)
            {
                visibleBegin++;
            }
            else
            {
                break;
            }
        }

        visibleEnd--;

        if (visibleEnd<visibleBegin)
        {
            visibleEnd=visibleBegin;
        }

        while (visibleEnd>visibleBegin)
        {
            if (mY-mOffsetY+mRows.get(visibleEnd).getY()>=mViewHeight/mScale)
            {
                visibleEnd--;
            }
            else
            {
                break;
            }
        }

        while (visibleEnd<mRows.size()-1)
        {
            if (mY-mOffsetY+mRows.get(visibleEnd+1).getY()<mViewHeight/mScale)
            {
                visibleEnd++;
            }
            else
            {
                break;
            }
        }

        if (visibleEnd<mRows.size())
        {
            visibleEnd++;
        }
        else
        {
            visibleEnd=mRows.size();
        }

        if (
            mVisibleBegin!=visibleBegin
            ||
            mVisibleEnd!=visibleEnd
           )
        {
            synchronized(this)
            {
                mVisibleBegin = visibleBegin;
                mVisibleEnd   = visibleEnd;
            }
        }
    }

    public void setSelectionColor(SelectionColor selectionColor)
    {
        mSelectionColor=selectionColor;
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener)
    {
        mProgressChangedListener=listener;
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

                case SELECTION_MESSAGE:
                    selection();
                break;

                case SCROLL_MESSAGE:
                    scroll();
                break;
            }
        }

        private void hideBars()
        {
            int barsAlpha=mBarsAlpha-20;

            if (barsAlpha>0)
            {
                sendEmptyMessageDelayed(HIDE_BARS_MESSAGE, 40);
            }
            else
            {
                barsAlpha=0;
            }

            if (mBarsAlpha!=barsAlpha)
            {
                synchronized(TextDocument.this)
                {
                    mBarsAlpha=barsAlpha;
                }

                repaint();
            }
        }

        private void highlight()
        {
            int highlightAlpha=mHighlightAlpha+20;

            if (highlightAlpha<255)
            {
                synchronized(TextDocument.this)
                {
                    mHighlightAlpha=highlightAlpha;
                }

                sendEmptyMessageDelayed(HIGHLIGHT_MESSAGE, 40);
            }
            else
            {
                mVibrator.vibrate(VIBRATOR_LONG_CLICK);

                mTouchMode=TouchMode.SELECT;

                synchronized(TextDocument.this)
                {
                    mHighlightAlpha     = 0;
                    mSelectionBrighness = 1;
                    mSelectionMakeLight = false;
                }

                updateSelection();

                sendEmptyMessageDelayed(SELECTION_MESSAGE, 40);
            }

            repaint();
        }

        private void selection()
        {
            boolean selectionMakeLight  = mSelectionMakeLight;
            float   selectionBrighness = mSelectionBrighness;

            if (selectionMakeLight)
            {
                selectionBrighness += SELECTION_SPEED;

                if (selectionBrighness>=1)
                {
                    selectionMakeLight = false;
                    selectionBrighness = 1;
                }
            }
            else
            {
                selectionBrighness -= SELECTION_SPEED;

                if (selectionBrighness<=SELECTION_LOW_LIGHT)
                {
                    selectionMakeLight = true;
                    selectionBrighness = SELECTION_LOW_LIGHT;
                }
            }

            sendEmptyMessageDelayed(SELECTION_MESSAGE, 40);

            if (
                mSelectionMakeLight!=selectionMakeLight
                ||
                mSelectionBrighness!=selectionBrighness
               )
            {
                synchronized(TextDocument.this)
                {
                    mSelectionMakeLight=selectionMakeLight;
                    mSelectionBrighness=selectionBrighness;
                }

                repaint();
            }
        }

        private void scroll()
        {
            touchScroll();
        }
    }
}
