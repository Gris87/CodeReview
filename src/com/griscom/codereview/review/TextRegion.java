package com.griscom.codereview.review;

import android.graphics.Canvas;
import android.graphics.Paint;

public class TextRegion
{
    private String mText;
    private Paint  mPaint;

    public TextRegion(String text, Paint paint)
    {
        mText  = text;
        mPaint = paint;
    }

    public void draw(Canvas canvas, float x, float y)
    {
        canvas.drawText(mText, x, y, mPaint);
    }
}
