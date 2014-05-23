package com.griscom.codereview.review;

import java.util.ArrayList;

import junit.framework.Assert;
import android.graphics.Canvas;

import com.griscom.codereview.BuildConfig;
import com.griscom.codereview.other.SelectionColor;

public class TextRow
{
    private ArrayList<TextRegion> mRegions;
    private SelectionColor        mSelectionColor;
    private float                 mY;
    private float                 mWidth;
    private float                 mHeight;



    public TextRow()
    {
        mRegions = new ArrayList<TextRegion>();

        mSelectionColor = SelectionColor.CLEAR;
        mY              = 0;
        mWidth          = 0;
        mHeight         = 0;
    }

    public void draw(Canvas canvas, float offsetX, float offsetY)
    {
        for (int i=0; i<mRegions.size(); ++i)
        {
            mRegions.get(i).draw(canvas, offsetX, mY+offsetY+mHeight-mRegions.get(i).getHeight());
        }
    }

    public void addTextRegion(TextRegion region)
    {
        if (mRegions.size()>0 && region.getOriginalText().equals(""))
        {
            return;
        }

        if (BuildConfig.DEBUG)
        {
            Assert.assertTrue(mRegions.size()==0 || !mRegions.get(mRegions.size()-1).getOriginalText().equals(""));
        }

        mRegions.add(region);

        updateSizeByRegion(region);
    }

    private void updateSizes()
    {
        mWidth  = 0;
        mHeight = 0;

        for (int i=0; i<mRegions.size(); ++i)
        {
            updateSizeByRegion(mRegions.get(i));
        }
    }

    private void updateSizeByRegion(TextRegion region)
    {
        region.setX(mWidth);

        mWidth+=region.getWidth();

        if (region.getHeight()>mHeight)
        {
            mHeight=region.getHeight();
        }
    }

    public void setFontSize(float textSize)
    {
        for (int i=0; i<mRegions.size(); ++i)
        {
            mRegions.get(i).setFontSize(textSize);
        }

        updateSizes();
    }

    public void setTabSize(int tabSize)
    {
        for (int i=0; i<mRegions.size(); ++i)
        {
            mRegions.get(i).setTabSize(tabSize);
        }

        updateSizes();
    }

    public void setSelectionColor(SelectionColor selectionColor)
    {
        mSelectionColor=selectionColor;
    }

    public SelectionColor getSelectionColor()
    {
        return mSelectionColor;
    }

    public void setY(float y)
    {
        mY=y;
    }

    @Deprecated
    public float getX() // Do not use it. Always zero
    {
        return 0;
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

    @Deprecated
    public float getRight() // Do not use it. Use getWidth() instead
    {
        return mWidth;
    }

    public float getBottom()
    {
        return mY+mHeight;
    }
}
