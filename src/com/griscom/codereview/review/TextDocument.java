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
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import com.griscom.codereview.BuildConfig;
import com.griscom.codereview.R;
import com.griscom.codereview.other.SelectionColor;
import com.griscom.codereview.listeners.*;

public class TextDocument implements OnTouchListener
{
    private static final String TAG = "TextDocument";

    private static final int   HIDE_BARS_MESSAGE   = 1;
    private static final int   HIGHLIGHT_MESSAGE   = 2;
    private static final int   SELECTION_MESSAGE   = 3;
    private static final int   SCROLL_MESSAGE      = 4;

    private static final int   AUTO_HIDE_DELAY     = 3000;
    private static final int   HIGHLIGHT_DELAY     = 250;
    private static final int   VIBRATOR_LONG_CLICK = 50;
    private static final float SELECTION_SPEED     = 0.01f;
    private static final float SELECTION_LOW_LIGHT = 0.6f;
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

    private float                     mX;
    private float                     mY;
    private float                     mWidth;
    private float                     mHeight;
    private float                     mViewWidth;
    private float                     mViewHeight;
    private float                     mOffsetX;
    private float                     mOffsetY;
    private int                       mVisibleBegin;
    private int                       mVisibleEnd;

    private boolean                   mTouchSelection;
    private boolean                   mTouchScroll;
    private float                     mTouchX;
    private float                     mTouchY;
    private int                       mSelectionEnd;
    private int                       mSelectionColor;
    private int                       mReviewedColor;
    private int                       mInvalidColor;
    private int                       mNoteColor;

    // USED IN HANDLER [
    private int                       mBarsAlpha;
    private int                       mHighlightedRow;
    private int                       mHighlightAlpha;
    private int                       mHighlightColor;
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

        mX                       = 0;
        mY                       = 0;
        mWidth                   = 0;
        mHeight                  = 0;
        mViewWidth               = 0;
        mViewHeight              = 0;
        mOffsetX                 = 0;
        mOffsetY                 = 0;
        mVisibleBegin            = -1;
        mVisibleEnd              = -1;

        mTouchSelection          = false;
        mTouchScroll             = false;
        mTouchX                  = 0;
        mTouchY                  = 0;
        mSelectionEnd            = -1;
        mSelectionColor          = 0;
        mReviewedColor           = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getString(R.string.pref_key_reviewed_color),  mContext.getResources().getInteger(R.integer.pref_default_reviewed_color));
        mInvalidColor            = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getString(R.string.pref_key_invalid_color),   mContext.getResources().getInteger(R.integer.pref_default_invalid_color));
        mNoteColor               = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getString(R.string.pref_key_note_color),      mContext.getResources().getInteger(R.integer.pref_default_note_color));

        mBarsAlpha               = 0;
        mHighlightedRow          = -1;
        mHighlightAlpha          = 0;
        mHighlightColor          = PreferenceManager.getDefaultSharedPreferences(mContext).getInt(mContext.getString(R.string.pref_key_selection_color), mContext.getResources().getInteger(R.integer.pref_default_selection_color));
        mSelectionBrighness      = 1;
        mSelectionMakeLight      = false;
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
        if (
            mY-mOffsetY>=0
            ||
            mY-mOffsetY+mHeight<=mViewHeight
           )
        {
            Paint backgroundPaint=new Paint();
            backgroundPaint.setColor(Color.WHITE);

            if (mY-mOffsetY>=0)
            {
                canvas.drawRect(0, 0, mViewWidth, mY-mOffsetY, backgroundPaint);
            }

            if (mY-mOffsetY+mHeight<=mViewHeight)
            {
                canvas.drawRect(0, mY-mOffsetY+mHeight, mViewWidth, mViewHeight, backgroundPaint);
            }
        }

        for (int i=mVisibleBegin; i<mVisibleEnd; ++i)
        {
            int color;

            if (mHighlightAlpha>0 && i==mHighlightedRow)
            {
                color=Color.argb(mHighlightAlpha, Color.red(mHighlightColor), Color.green(mHighlightColor), Color.blue(mHighlightColor));
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
                Color.colorToHSV(mSelectionColor, selectionHSV);
                selectionHSV[2]=mSelectionBrighness;
                color=Color.HSVToColor(selectionHSV);
            }
            else
            {
                color=mRows.get(i).getColor();
            }

            Paint backgroundPaint=new Paint();
            backgroundPaint.setColor(color);
            canvas.drawRect(0, mY-mOffsetY+mRows.get(i).getY(), mViewWidth, mY-mOffsetY+mRows.get(i).getBottom(), backgroundPaint);

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

        updateVisibleRanges();
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
            mSelectionEnd   = -1;

            for (int i=mVisibleBegin; i<mVisibleEnd; ++i)
            {
                if (mTouchY>=mY-mOffsetY+mRows.get(i).getY() && mTouchY<=mY-mOffsetY+mRows.get(i).getBottom())
                {
                    mHighlightedRow=i;

                    mHandler.sendEmptyMessageDelayed(HIGHLIGHT_MESSAGE, HIGHLIGHT_DELAY);

                    break;
                }
            }
        }
        else
        if (event.getAction()==MotionEvent.ACTION_MOVE)
        {
            if (mTouchSelection)
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

                        updateVisibleRanges();

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
					if (mRows.get(i).getColor()!=Color.WHITE)
					{
						coloredRows++;
					}
                }

                for (int i=firstRow; i<=lastRow; ++i)
                {
                    mRows.get(i).setColor(mSelectionColor);
					
					if (mRows.get(i).getColor()!=Color.WHITE)
					{
						coloredRows--;
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

    public void updateSelection()
    {
        int selectionEnd=mSelectionEnd;

        if (selectionEnd<0)
        {
            selectionEnd=mHighlightedRow;
        }

        while (selectionEnd>mVisibleBegin)
        {
            if (mTouchY<=mY-mOffsetY+mRows.get(selectionEnd-1).getBottom())
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
            if (mTouchY>=mY-mOffsetY+mRows.get(selectionEnd+1).getY())
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
            mSelectionEnd=selectionEnd;

            repaint();
        }
    }

    private void touchScroll()
    {
        float newOffsetY;

        if (mTouchY<mViewHeight/8)
        {
            newOffsetY=mOffsetY-SCROLL_SPEED;
        }
        else
        {
            newOffsetY=mOffsetY+SCROLL_SPEED;
        }

        if (newOffsetY>mHeight-mViewHeight+BOTTOM_RIGHT_SPACE)
        {
            newOffsetY=mHeight-mViewHeight+BOTTOM_RIGHT_SPACE;
        }

        if (newOffsetY<0)
        {
            newOffsetY=0;
        }



        if (mOffsetY != newOffsetY)
        {
            mOffsetY = newOffsetY;

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

        if (mVisibleBegin<0)
        {
            mVisibleBegin=0;
        }

        while (mVisibleBegin>0)
        {
            if (mY-mOffsetY+mRows.get(mVisibleBegin-1).getBottom()>=0)
            {
                mVisibleBegin--;
            }
            else
            {
                break;
            }
        }

        while (mVisibleBegin<mRows.size())
        {
            if (mY-mOffsetY+mRows.get(mVisibleBegin).getBottom()<0)
            {
                mVisibleBegin++;
            }
            else
            {
                break;
            }
        }

        mVisibleEnd--;

        if (mVisibleEnd<mVisibleBegin)
        {
            mVisibleEnd=mVisibleBegin;
        }

        while (mVisibleEnd>mVisibleBegin)
        {
            if (mY-mOffsetY+mRows.get(mVisibleEnd).getY()>=mViewHeight)
            {
                mVisibleEnd--;
            }
            else
            {
                break;
            }
        }

        while (mVisibleEnd<mRows.size()-1)
        {
            if (mY-mOffsetY+mRows.get(mVisibleEnd+1).getY()<mViewHeight)
            {
                mVisibleEnd++;
            }
            else
            {
                break;
            }
        }

        mVisibleEnd++;
    }

    public void setSelectionColor(SelectionColor colorType)
    {
        if (colorType==SelectionColor.REVIEWED_COLOR)
        {
            mSelectionColor=mReviewedColor;
        }
        else
        if (colorType==SelectionColor.INVALID_COLOR)
        {
            mSelectionColor=mInvalidColor;
        }
        else
        if (colorType==SelectionColor.NOTE_COLOR)
        {
            mSelectionColor=mNoteColor;
        }
		else
        if (colorType==SelectionColor.CLEAR_COLOR)
        {
            mSelectionColor=Color.WHITE;
        }
        else
        {
            Log.e(TAG, "Unknown selection color: "+String.valueOf(colorType));

            if (BuildConfig.DEBUG)
            {
                Assert.fail();
            }
        }
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
                mHighlightAlpha     = 0;
                mTouchSelection     = true;
                mSelectionBrighness = 1;
                mSelectionMakeLight = false;

                mVibrator.vibrate(VIBRATOR_LONG_CLICK);

                updateSelection();
                sendEmptyMessageDelayed(SELECTION_MESSAGE, 40);
            }

            repaint();
        }

        private void selection()
        {
            if (mSelectionMakeLight)
            {
                mSelectionBrighness += SELECTION_SPEED;

                if (mSelectionBrighness>=1)
                {
                    mSelectionMakeLight = false;
                    mSelectionBrighness = 1;
                }
            }
            else
            {
                mSelectionBrighness -= SELECTION_SPEED;

                if (mSelectionBrighness<=SELECTION_LOW_LIGHT)
                {
                    mSelectionMakeLight = true;
                    mSelectionBrighness = SELECTION_LOW_LIGHT;
                }
            }

            sendEmptyMessageDelayed(SELECTION_MESSAGE, 40);

            repaint();
        }

        private void scroll()
        {
            touchScroll();
        }
    }
}
