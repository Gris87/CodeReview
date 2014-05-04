package com.griscom.codereview.review;
import java.util.ArrayList;

import android.graphics.Canvas;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

public class TextDocument implements OnTouchListener
{
    private ArrayList<TextRow> mRows;
    private float              mX;
    private float              mY;
    private float              mWidth;
    private float              mHeight;
    private float              mOffsetX;
    private float              mOffsetY;

    public TextDocument()
    {
        mRows    = new ArrayList<TextRow>();

        mX       = 0;
        mY       = 0;
        mWidth   = 0;
        mHeight  = 0;
        mOffsetX = 0;
        mOffsetY = 0;
    }
    public void draw(Canvas canvas)
    {
        for (int i=0; i<mRows.size(); ++i)
        {
            mRows.get(i).draw(canvas, mX+mOffsetX, mY+mOffsetY);
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
}
