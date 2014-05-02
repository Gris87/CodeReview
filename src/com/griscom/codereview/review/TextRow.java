package com.griscom.codereview.review;

import java.util.ArrayList;

import android.graphics.Canvas;
import android.content.*;

import com.griscom.codereview.R;

public class TextRow
{
    private ArrayList<TextRegion> mRegions;
	private Context               mContext;
	private float                 mY;
	private float                 mWidth;
	private float                 mHeight;

    public TextRow(Context context)
    {
        mRegions = new ArrayList<TextRegion>();
		mContext = context;
		
		mY      = 0;
		mWidth  = mContext.getResources().getDimensionPixelSize(R.dimen.review_horizontal_margin);
		mHeight = 0;
    }
	
	public void draw(Canvas canvas, float offsetX, float offsetY)
    {
        for (int i=0; i<mRegions.size(); ++i)
		{
			mRegions.get(i).draw(canvas, offsetX, offsetY);
		}
    }

    public void addTextRegion(TextRegion region)
    {
        mRegions.add(region);
		
		region.setX(mWidth);
		region.setY(mY);
		
		mWidth+=region.getWidth();
		
		if (region.getHeight()>mHeight)
		{
			mHeight=region.getHeight();
		}
    }
	
	public void setY(float y)
	{
		mY=y;
		
		for (int i=0; i<mRegions.size(); ++i)
		{
			mRegions.get(i).setY(mY);
		}
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
