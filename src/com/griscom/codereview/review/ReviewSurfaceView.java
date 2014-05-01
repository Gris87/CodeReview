package com.griscom.codereview.review;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.griscom.codereview.listeners.OnReviewSurfaceDrawListener;
import com.griscom.codereview.review.syntax.PlainTextSyntaxParser;
import com.griscom.codereview.review.syntax.SyntaxParserBase;

public class ReviewSurfaceView extends SurfaceView implements OnReviewSurfaceDrawListener, OnTouchListener
{
    private Context            mContext;
    private SurfaceHolder      mSurfaceHolder;
    private DrawThread         mDrawThread;
    private String             mFileName;
    private SyntaxParserBase   mSyntaxParser;
    private ArrayList<TextRow> mRows;

    public ReviewSurfaceView(Context context)
	{
        super(context);

        init(context);
    }

    public ReviewSurfaceView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        init(context);
    }

    public ReviewSurfaceView(Context context, AttributeSet attrs, int defStyle)
    {
        super(context, attrs, defStyle);

        init(context);
    }

    private void init(Context context)
    {
        mContext       = context;
        mSurfaceHolder = getHolder();
        mDrawThread    = null;
        mSyntaxParser  = new PlainTextSyntaxParser(mContext);
        mRows          = new ArrayList<TextRow>();

        setOnTouchListener(this);
    }

    public void pause()
    {
        if (mDrawThread!=null)
        {
            mDrawThread.terminate();

            do
            {
                try
                {
                    mDrawThread.join();
                    return;
                }
                catch (Exception e)
                {
                }
            } while(true);
        }
    }

    public void resume()
    {
        pause();

        reload();

        mDrawThread=new DrawThread(mSurfaceHolder, this);
        mDrawThread.start();
    }

    @Override
    public void onReviewSurfaceDraw(Canvas canvas)
    {
        canvas.drawColor(Color.WHITE);

        for (int i=0; i<mRows.size(); ++i)
        {
            mRows.get(i).draw(canvas, 0, 0);
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void reload()
    {
        mRows=mSyntaxParser.parseFile(mFileName);
    }

    public void setFileName(String fileName)
    {
        mFileName=fileName;

        mSyntaxParser=SyntaxParserBase.createParserByFileName(mFileName, mContext);

        // Maybe reload but no
        // reload();
    }
}
