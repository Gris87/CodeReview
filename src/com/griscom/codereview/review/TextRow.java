package com.griscom.codereview.review;

import java.util.ArrayList;

public class TextRow
{
    private ArrayList<TextRegion> mRegions;

    public TextRow()
    {
        mRegions=new ArrayList<TextRegion>();
    }

    public void addTextRegion(TextRegion region)
    {
        mRegions.add(region);
    }
}
