package com.griscom.codereview.review;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnTouchListener;

import com.griscom.codereview.R;
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
    private TextDocument       mDocument;

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
        mDocument      = new TextDocument();

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

        mDocument.draw(canvas, 0, 0);
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        // TODO Auto-generated method stub
        return false;
    }

    public void reload()
    {
        mDocument=mSyntaxParser.parseFile(mFileName);

        mDocument.setX(mContext.getResources().getDimensionPixelSize(R.dimen.review_horizontal_margin));
        mDocument.setY(mContext.getResources().getDimensionPixelSize(R.dimen.review_vertical_margin));
    }

    public void setFileName(String fileName)
    {
        mFileName=fileName;

        mSyntaxParser=SyntaxParserBase.createParserByFileName(mFileName, mContext);

        // Maybe reload but no
        // reload();
    }
}
