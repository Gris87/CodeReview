package com.griscom.codereview.review;

import android.graphics.Canvas;
import android.graphics.Paint;

public class TextRegion
{
    private String mText;
    private Paint  mPaint;
    private float  mAscent;
    private float  mX;
    private float  mWidth;
    private float  mHeight;

    public TextRegion(String text, Paint paint)
    {
        mText  = text;
        mPaint = paint;

        Paint.FontMetrics fontMetrics=mPaint.getFontMetrics();
        mAscent = -fontMetrics.ascent;
        mX      = 0;
        mWidth  = mPaint.measureText(mText);
        mHeight = fontMetrics.descent + mAscent + fontMetrics.leading;
    }

    public void draw(Canvas canvas, float offsetX, float offsetY)
    {
        canvas.drawText(mText, mX+offsetX, offsetY+mAscent, mPaint);
    }

    public void setX(float x)
    {
        mX=x;
    }

    public float getX()
    {
        return mX;
    }

    @Deprecated
    public float getY() // Do not use it. Always zero
    {
        return 0;
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

    @Deprecated
    public float getBottom()// Do not use it. Use getHeight() instead
    {
        return mHeight;
    }
}
