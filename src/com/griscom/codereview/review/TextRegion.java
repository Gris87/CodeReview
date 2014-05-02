package com.griscom.codereview.review;

import android.graphics.Canvas;
import android.graphics.Paint;

public class TextRegion
{
    private String mText;
    private Paint  mPaint;
	private float  mAscent;
	private float  mX;
	private float  mY;
	private float  mWidth;
	private float  mHeight;

    public TextRegion(String text, Paint paint)
    {
        mText  = text;
        mPaint = paint;
		mX     = 0;
		mY     = 0;

		Paint.FontMetrics fontMetrics=mPaint.getFontMetrics();
		mAscent = -fontMetrics.ascent;
		mWidth  = mPaint.measureText(mText);
		mHeight = fontMetrics.descent + mAscent + fontMetrics.leading;
    }

    public void draw(Canvas canvas, float offsetX, float offsetY)
    {
        canvas.drawText(mText, mX+offsetX, mY+offsetY+mAscent, mPaint);
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
