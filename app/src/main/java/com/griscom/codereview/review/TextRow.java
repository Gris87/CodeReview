package com.griscom.codereview.review;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextUtils;

import com.griscom.codereview.other.SelectionType;
import com.griscom.codereview.review.syntax.SyntaxParserBase;

import junit.framework.Assert;

import java.util.ArrayList;
import java.util.Locale;

@SuppressWarnings("WeakerAccess")
public class TextRow
{
    private ArrayList<TextRegion> mRegions       = null;
    private int                   mSelectionType = 0;
    private float                 mY             = 0;
    private float                 mWidth         = 0;
    private float                 mHeight        = 0;
    private int                   mCommentIndex  = 0;



    @Override
    public String toString()
    {
        return "TextRow{" +
                "mRegions="         + mRegions       +
                ", mSelectionType=" + mSelectionType +
                ", mY="             + mY             +
                ", mWidth="         + mWidth         +
                ", mHeight="        + mHeight        +
                ", mCommentIndex="  + mCommentIndex  +
                '}';
    }

    private TextRow()
    {
        mRegions = new ArrayList<>(0);

        mSelectionType = SelectionType.CLEAR;
        mY             = 0;
        mWidth         = 0;
        mHeight        = 0;

        mCommentIndex  = -1;
    }

    public static TextRow newInstance()
    {
        return new TextRow();
    }

    public void draw(Canvas canvas, float offsetX, float offsetY)
    {
        for (int i = 0; i < mRegions.size(); ++i)
        {
            mRegions.get(i).draw(canvas, offsetX, mY + offsetY + mHeight - mRegions.get(i).getHeight());
        }
    }

    public void addTextRegion(TextRegion region)
    {
        if (!mRegions.isEmpty() && region.getOriginalText().isEmpty())
        {
            return;
        }

        mRegions.add(region);

        updateSizeByRegion(region);
    }

    public void checkForComment(SyntaxParserBase parser)
    {
        Assert.assertTrue("Comment already exists", mCommentIndex < 0);



        String commentBegin = parser.getCommentLine();

        if (commentBegin != null)
        {
            commentBegin = commentBegin.toUpperCase(Locale.getDefault());

            String commentEnd = parser.getCommentLineEnd();

            if (commentEnd != null)
            {
                commentEnd = commentEnd.toUpperCase(Locale.getDefault());
            }

            for (int i = 0; i < mRegions.size(); ++i)
            {
                String text = mRegions.get(i).getOriginalText().toUpperCase(Locale.getDefault());

                if (
                    text.contains(commentBegin)
                    &&
                    text.contains("TODO")
                    &&
                    (
                     commentEnd == null
                     ||
                     text.contains(commentEnd)
                    )
                   )
                {
                    mCommentIndex = i;
                    mSelectionType = SelectionType.NOTE;

                    return;
                }
            }
        }
    }

    private void updateSizes()
    {
        mWidth  = 0;
        mHeight = 0;

        for (int i = 0; i < mRegions.size(); ++i)
        {
            updateSizeByRegion(mRegions.get(i));
        }
    }

    private void updateSizeByRegion(TextRegion region)
    {
        region.setX(mWidth);

        mWidth += region.getWidth();

        if (region.getHeight() > mHeight)
        {
            mHeight = region.getHeight();
        }
    }

    public String getString()
    {
        StringBuilder res = new StringBuilder(0);

        for (int i = 0; i < mRegions.size(); ++i)
        {
            res.append(mRegions.get(i).getOriginalText());
        }

        return res.toString();
    }

    public boolean hasRegions()
    {
        return !mRegions.isEmpty();
    }

    public void setComment(String comment, Paint paint)
    {
        String newComment = comment;

        if (TextUtils.isEmpty(newComment))
        {
            if (mCommentIndex >= 0)
            {
                mRegions.remove(mCommentIndex);
                mCommentIndex = -1;

                mSelectionType = SelectionType.CLEAR;
            }
        }
        else
        {
            if (
                !mRegions.isEmpty()
                &&
                (
                 mCommentIndex < 0
                 &&
                 !mRegions.get(mRegions.size() - 1).getOriginalText().isEmpty()

                 ||

                 mCommentIndex > 0
                 &&
                 !mRegions.get(mCommentIndex - 1).getOriginalText().isEmpty()
                )
               )
            {
                newComment = ' ' + newComment;
            }

            if (mCommentIndex >= 0)
            {
                mRegions.get(mCommentIndex).setOriginalText(newComment);

                updateSizes();
            }
            else
            {
                addTextRegion(TextRegion.newInstance(newComment, paint, 0, 4));
                mCommentIndex = mRegions.size() - 1;
                mSelectionType = SelectionType.NOTE;
            }
        }
    }

    public void setFontSize(float textSize)
    {
        for (int i = 0; i < mRegions.size(); ++i)
        {
            mRegions.get(i).setFontSize(textSize);
        }

        updateSizes();
    }

    public void setTabSize(int tabSize)
    {
        for (int i = 0; i < mRegions.size(); ++i)
        {
            mRegions.get(i).setTabSize(tabSize);
        }

        updateSizes();
    }

    public void setSelectionType(int selectionType)
    {
        if (mSelectionType != SelectionType.NOTE)
        {
            mSelectionType = selectionType;
        }
    }

    public int getSelectionType()
    {
        return mSelectionType;
    }

    public void setY(float y)
    {
        mY = y;
    }

    @SuppressWarnings({"SameReturnValue", "unused", "MethodMayBeStatic", "MethodReturnAlwaysConstant"})
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

    @SuppressWarnings("unused")
    @Deprecated
    public float getRight() // Do not use it. Use getWidth() instead
    {
        return mWidth;
    }

    public float getBottom()
    {
        return mY + mHeight;
    }
}
