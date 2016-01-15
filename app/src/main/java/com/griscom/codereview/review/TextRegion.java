package com.griscom.codereview.review;

import android.graphics.Canvas;
import android.graphics.Paint;

public class TextRegion
{
    private int    mPosition;
    private String mOriginalText;
    private String mDisplayedText;
    private Paint  mPaint;
    private float  mX;
    private float  mWidth;
    private float  mHeight;



    public TextRegion(String text, Paint paint, int position, int tabSize)
    {
        mPosition = position;
        mPaint    = paint;
        mX        = 0;

        setupDisplayedText(text, tabSize);
        updateSizes();
    }

    public void draw(Canvas canvas, float offsetX, float offsetY)
    {
        canvas.drawText(mDisplayedText, mX + offsetX, offsetY - mPaint.ascent(), mPaint);
    }

    private void setupDisplayedText(String text, int tabSize)
    {
        int index = text.indexOf('\t');

        if (index >= 0)
        {
            mOriginalText  = text;

            StringBuilder sb = new StringBuilder(mOriginalText);

            do
            {
                int requiredSpaces = tabSize - ((mPosition + index) % tabSize);
                sb.replace(index, index + 1, spaces(requiredSpaces));

                index = sb.indexOf("\t", index);
            } while (index >= 0);

            mDisplayedText = sb.toString();
        }
        else
        {
            mOriginalText  = null;
            mDisplayedText = text;
        }
    }

    private String spaces(int count)
    {
        StringBuilder sb = new StringBuilder();

        for (int i = 0; i < count; ++i)
        {
            sb.append(' ');
        }

        return sb.toString();
    }

    private void updateSizes()
    {
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();

        mWidth  = mPaint.measureText(mDisplayedText);
        mHeight = fontMetrics.descent - fontMetrics.ascent + fontMetrics.leading;
    }

    public String getOriginalText()
    {
        if (mOriginalText != null)
        {
            return mOriginalText;
        }
        else
        {
            return mDisplayedText;
        }
    }

    public void setOriginalText(String text)
    {
        mOriginalText  = null;
        mDisplayedText = text;

        updateSizes();
    }

    public void setFontSize(float textSize)
    {
        mPaint.setTextSize(textSize);

        updateSizes();
    }

    public void setTabSize(int tabSize)
    {
        if (mOriginalText != null)
        {
            setupDisplayedText(mOriginalText, tabSize);
            updateSizes();
        }
    }

    public void setX(float x)
    {
        mX = x;
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
        return mX + mWidth;
    }

    @Deprecated
    public float getBottom()// Do not use it. Use getHeight() instead
    {
        return mHeight;
    }
}