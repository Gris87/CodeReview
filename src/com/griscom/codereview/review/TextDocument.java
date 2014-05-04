package com.griscom.codereview.review;
import java.util.ArrayList;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class TextDocument implements OnTouchListener
{
    private static final int HIGHLIGHT_MESSAGE = 1;



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
            float margin=4*density;

            Paint barPaint=new Paint();

            barPaint.setARGB(255, 140, 140, 140);
            barPaint.setStrokeWidth(4*density);

            if (mViewWidth>0 && mWidth>mViewWidth)
            {
                float barLength=mViewWidth/mWidth;

                canvas.drawLine(mViewWidth-margin, mY+margin, mViewWidth-margin, mViewHeight-margin, barPaint);
            }

            if (mViewHeight>0 && mHeight>mViewHeight)
            {
                float barLength=mViewWidth/mWidth;

                canvas.drawLine(mViewWidth-margin, mY+margin, mViewWidth-margin, mViewHeight-margin, barPaint);
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

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
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

    public void setParent(ReviewSurfaceView parent)
    {
        mParent=parent;

        mViewWidth  = mParent.getWidth();
        mViewHeight = mParent.getHeight();

        mHandler=new DocumentHandler();
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
                case HIGHLIGHT_MESSAGE:
                {
                }
                break;
            }
        }
    }
}
