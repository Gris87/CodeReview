package com.griscom.codereview.review;

import android.graphics.Canvas;
import android.graphics.Paint;

@SuppressWarnings("WeakerAccess")
public final class TextRegion
{
    private Paint  mPaint         = null;
    private int    mPosition      = 0;
    private String mOriginalText  = null;
    private String mDisplayedText = null;
    private float  mX             = 0;
    private float  mWidth         = 0;
    private float  mHeight        = 0;



    @Override
    public String toString()
    {
        return "TextRegion{" +
                "mPaint="            + mPaint         +
                ", mPosition="       + mPosition      +
                ", mOriginalText='"  + mOriginalText  + '\'' +
                ", mDisplayedText='" + mDisplayedText + '\'' +
                ", mX="              + mX             +
                ", mWidth="          + mWidth         +
                ", mHeight="         + mHeight        +
                '}';
    }

    private TextRegion(String text, Paint paint, int position, int tabSize)
    {
        mPaint    = paint;
        mPosition = position;
        mX        = 0;

        setupDisplayedText(text, tabSize);
        updateSizes();
    }

    public static TextRegion newInstance(String text, Paint paint, int position, int tabSize)
    {
        return new TextRegion(text, paint, position, tabSize);
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
                int requiredSpaces = tabSize - (mPosition + index) % tabSize;
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

    private static String spaces(int count)
    {
        StringBuilder sb = new StringBuilder(0);

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

    @SuppressWarnings("unused")
    public float getX()
    {
        return mX;
    }

    @SuppressWarnings({"SameReturnValue", "MethodMayBeStatic", "unused", "MethodReturnAlwaysConstant"})
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

    @SuppressWarnings("unused")
    public float getRight()
    {
        return mX + mWidth;
    }

    @Deprecated
    @SuppressWarnings("unused")
    public float getBottom()// Do not use it. Use getHeight() instead
    {
        return mHeight;
    }
}
