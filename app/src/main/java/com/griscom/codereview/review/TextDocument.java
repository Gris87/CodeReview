package com.griscom.codereview.review;

import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.os.Vibrator;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.WindowManager;

import com.griscom.codereview.R;
import com.griscom.codereview.db.MainDatabase;
import com.griscom.codereview.db.SingleFileDatabase;
import com.griscom.codereview.listeners.OnCommentDialogRequestedListener;
import com.griscom.codereview.listeners.OnProgressChangedListener;
import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.ColorCache;
import com.griscom.codereview.other.RowType;
import com.griscom.codereview.other.SelectionType;
import com.griscom.codereview.other.TouchMode;
import com.griscom.codereview.review.syntax.SyntaxParserBase;
import com.griscom.codereview.util.AppLog;
import com.griscom.codereview.util.Utils;

import junit.framework.Assert;

import java.util.ArrayList;

/**
 * Text document
 */
@SuppressWarnings({"WeakerAccess", "FieldAccessedSynchronizedAndUnsynchronized"})
public class TextDocument implements OnTouchListener
{
    @SuppressWarnings("unused")
    private static final String TAG = "TextDocument";



    private static final int   HIDE_BARS_MESSAGE = 1;
    private static final int   HIGHLIGHT_MESSAGE = 2;
    private static final int   SELECTION_MESSAGE = 3;
    private static final int   SCROLL_MESSAGE    = 4;

    private static final int   AUTO_HIDE_DELAY             = 3000;
    private static final int   HIGHLIGHT_DELAY             = 250;
    private static final int   VIBRATOR_LONG_CLICK         = 50;
    private static final float SELECTION_SPEED             = 0.01f;
    private static final float SELECTION_LOW_LIGHT         = 0.75f;
    private static final float SCROLL_SPEED                = 10;
    private static final float SCROLL_MULTIPLIER_INCREMENT = 1.01f;
    private static final float SCROLL_MULTIPLIER_MAX       = 10;

    private static final int   SCROLL_THRESHOLD            = 25;



    private SyntaxParserBase                 mSyntaxParser                   = null;
    private Context                          mContext                        = null;
    private ReviewSurfaceView                mParent                         = null;
    private Vibrator                         mVibrator                       = null;
    private DocumentHandler                  mHandler                        = null;
    private ArrayList<TextRow>               mRows                           = null;
    private OnProgressChangedListener        mProgressChangedListener        = null;
    private OnCommentDialogRequestedListener mCommentDialogRequestedListener = null;
    private int                              mReviewedCount                  = 0;
    private int                              mInvalidCount                   = 0;
    private int                              mNoteCount                      = 0;
    private int                              mFontSize                       = 0;
    private int                              mTabSize                        = 0;
    private Paint                            mRowPaint                       = null;
    private final Object                     mLock                           = new Object();

    private float                            mIndexWidth                     = 0;
    private float                            mX                              = 0;
    private float                            mY                              = 0;
    private float                            mWidth                          = 0;
    private float                            mHeight                         = 0;
    private float                            mViewWidth                      = 0;
    private float                            mViewHeight                     = 0;
    private float                            mOffsetX                        = 0;
    private float                            mOffsetY                        = 0;
    private float                            mScale                          = 0;
    private int                              mVisibleBegin                   = 0;
    private int                              mVisibleEnd                     = 0;

    private int                              mTouchMode                      = 0;
    private float                            mTouchX                         = 0;
    private float                            mTouchY                         = 0;
    private float                            mFingerDistance                 = 0;
    private float                            mTouchMiddleX                   = 0;
    private float                            mTouchMiddleY                   = 0;
    private float                            mScrollMultiplier               = 0;
    private int                              mSelectionEnd                   = 0;
    private int                              mSelectionType                  = 0;

    // USED IN HANDLER [
    private int                              mBarsAlpha                      = 0;
    private int                              mHighlightedRow                 = 0;
    private int                              mHighlightAlpha                 = 0;
    private float                            mSelectionBrightness            = 0;
    private boolean                          mSelectionMakeLight             = false;
    // USED IN HANDLER ]



    @Override
    public String toString()
    {
        return "TextDocument{" +
                "mSyntaxParser="                     + mSyntaxParser                   +
                ", mContext="                        + mContext                        +
                ", mParent="                         + mParent                         +
                ", mVibrator="                       + mVibrator                       +
                ", mHandler="                        + mHandler                        +
                ", mRows="                           + mRows                           +
                ", mProgressChangedListener="        + mProgressChangedListener        +
                ", mCommentDialogRequestedListener=" + mCommentDialogRequestedListener +
                ", mReviewedCount="                  + mReviewedCount                  +
                ", mInvalidCount="                   + mInvalidCount                   +
                ", mNoteCount="                      + mNoteCount                      +
                ", mFontSize="                       + mFontSize                       +
                ", mTabSize="                        + mTabSize                        +
                ", mRowPaint="                       + mRowPaint                       +
                ", mLock="                           + mLock                           +
                ", mIndexWidth="                     + mIndexWidth                     +
                ", mX="                              + mX                              +
                ", mY="                              + mY                              +
                ", mWidth="                          + mWidth                          +
                ", mHeight="                         + mHeight                         +
                ", mViewWidth="                      + mViewWidth                      +
                ", mViewHeight="                     + mViewHeight                     +
                ", mOffsetX="                        + mOffsetX                        +
                ", mOffsetY="                        + mOffsetY                        +
                ", mScale="                          + mScale                          +
                ", mVisibleBegin="                   + mVisibleBegin                   +
                ", mVisibleEnd="                     + mVisibleEnd                     +
                ", mTouchMode="                      + mTouchMode                      +
                ", mTouchX="                         + mTouchX                         +
                ", mTouchY="                         + mTouchY                         +
                ", mFingerDistance="                 + mFingerDistance                 +
                ", mTouchMiddleX="                   + mTouchMiddleX                   +
                ", mTouchMiddleY="                   + mTouchMiddleY                   +
                ", mScrollMultiplier="               + mScrollMultiplier               +
                ", mSelectionEnd="                   + mSelectionEnd                   +
                ", mSelectionType="                  + mSelectionType                  +
                ", mBarsAlpha="                      + mBarsAlpha                      +
                ", mHighlightedRow="                 + mHighlightedRow                 +
                ", mHighlightAlpha="                 + mHighlightAlpha                 +
                ", mSelectionBrightness="            + mSelectionBrightness            +
                ", mSelectionMakeLight="             + mSelectionMakeLight             +
                '}';
    }

    private TextDocument(SyntaxParserBase parser)
    {
        mSyntaxParser                   = parser;
        mContext                        = mSyntaxParser.getContext();
        mParent                         = null;
        mVibrator                       = (Vibrator)mContext.getSystemService(Context.VIBRATOR_SERVICE);
        mHandler                        = null;
        mRows                           = new ArrayList<>(0);
        mProgressChangedListener        = null;
        mCommentDialogRequestedListener = null;
        mReviewedCount                  = 0;
        mInvalidCount                   = 0;
        mNoteCount                      = 0;
        mFontSize                       = ApplicationSettings.getFontSize();
        mTabSize                        = ApplicationSettings.getTabSize();
        mRowPaint                       = new Paint();
        mRowPaint.setColor(Color.LTGRAY);
        mRowPaint.setTypeface(Typeface.MONOSPACE);
        mRowPaint.setTextSize(Utils.spToPixels(mFontSize, mContext));

        mIndexWidth                     = 0;
        mX                              = 0;
        mY                              = 0;
        mWidth                          = 0;
        mHeight                         = 0;
        mViewWidth                      = 0;
        mViewHeight                     = 0;
        mOffsetX                        = 0;
        mOffsetY                        = 0;
        mScale                          = 1;
        mVisibleBegin                   = -1;
        mVisibleEnd                     = -1;

        mTouchMode                      = TouchMode.NONE;
        mTouchX                         = 0;
        mTouchY                         = 0;
        mFingerDistance                 = 0;
        mTouchMiddleX                   = -1;
        mTouchMiddleY                   = -1;
        mScrollMultiplier               = 1;
        mSelectionEnd                   = -1;
        mSelectionType                  = SelectionType.REVIEWED;

        mBarsAlpha                      = 0;
        mHighlightedRow                 = -1;
        mHighlightAlpha                 = 0;
        mSelectionBrightness            = 1;
        mSelectionMakeLight             = false;
    }

    public static TextDocument newInstance(SyntaxParserBase parser)
    {
        return new TextDocument(parser);
    }

    public void init()
    {
        mHandler = new DocumentHandler();

        mX = mContext.getResources().getDimensionPixelSize(R.dimen.review_horizontal_margin);
        mY = mContext.getResources().getDimensionPixelSize(R.dimen.review_vertical_margin);

        mIndexWidth = mRowPaint.measureText(String.valueOf(mRows.size() + 1));
        mX += mIndexWidth;

        onConfigurationChanged();
        showBars();
    }

    public void draw(Canvas canvas)
    {
        synchronized(mLock)
        {
            float density = mContext.getResources().getDisplayMetrics().scaledDensity;

            canvas.scale(mScale, mScale);

            if (
                mY - mOffsetY >= 0
                ||
                mY - mOffsetY + mHeight <= mViewHeight / mScale
                ||
                mX - mOffsetX >= 0
                )
            {
                Paint backgroundPaint = new Paint();
                backgroundPaint.setColor(ColorCache.get(SelectionType.CLEAR));

                if (mY - mOffsetY >= 0)
                {
                    canvas.drawRect(0, 0, mViewWidth / mScale, mY - mOffsetY, backgroundPaint);
                }

                if (mY - mOffsetY + mHeight <= mViewHeight / mScale)
                {
                    canvas.drawRect(0, mY - mOffsetY + mHeight, mViewWidth / mScale, mViewHeight / mScale, backgroundPaint);
                }

                if (mX - mOffsetX >= 0)
                {
                    canvas.drawRect(0, 0, mX - mOffsetX, mViewHeight / mScale, backgroundPaint);
                }
            }

            for (int i = mVisibleBegin; i < mVisibleEnd; ++i)
            {
                int color;

                if (mHighlightAlpha > 0 && i == mHighlightedRow)
                {
                    color = ApplicationSettings.getSelectionColor();
                    color = Color.argb(mHighlightAlpha, Color.red(color), Color.green(color), Color.blue(color));
                }
                else
                if (
                    mSelectionEnd >= 0
                    &&
                    (
                     i >= mHighlightedRow && i <= mSelectionEnd
                     ||
                     i >= mSelectionEnd   && i <= mHighlightedRow
                    )
                   )
                {
                    float[] selectionHSV = new float[3];
                    Color.colorToHSV(ColorCache.get(mSelectionType), selectionHSV);
                    selectionHSV[2] = mSelectionBrightness;
                    color = Color.HSVToColor(selectionHSV);
                }
                else
                {
                    color = ColorCache.get(mRows.get(i).getSelectionType());
                }

                Paint backgroundPaint = new Paint();
                backgroundPaint.setColor(color);

                // Draw row background
                canvas.drawRect(mX - mOffsetX, mY - mOffsetY + mRows.get(i).getY(), mViewWidth / mScale, mY - mOffsetY + mRows.get(i).getBottom(), backgroundPaint);

                // Draw row number
                canvas.drawText(String.valueOf(i + 1),  - mOffsetX, mY - mOffsetY + mRows.get(i).getY() - mRowPaint.ascent(), mRowPaint);

                // Draw row
                mRows.get(i).draw(canvas, mX - mOffsetX, mY - mOffsetY);
            }

            // Draw scroll bars
            {
                float margin = 6 * density / mScale;

                Paint barPaint = new Paint();

                barPaint.setARGB(mBarsAlpha, 180, 180, 180);
                barPaint.setStrokeWidth(4 * density / mScale);

                // Draw horizontal bar
                {
                    float barLength   = mViewWidth / mScale / (getRight() + mViewWidth / mScale);
                    float barWidth    = mViewWidth / mScale - margin * 3;
                    float barPosition = barWidth * mOffsetX / (getRight() + mViewWidth / mScale);

                    canvas.drawLine(barPosition + margin, mViewHeight / mScale - margin, barWidth * barLength + barPosition + margin, mViewHeight / mScale - margin, barPaint);
                }

                // Draw vertical bar
                {
                    float barLength   = mViewHeight / mScale / (getBottom() + mViewHeight / mScale);
                    float barHeight   = mViewHeight / mScale - margin * 3;
                    float barPosition = barHeight * mOffsetY / (getBottom() + mViewHeight / mScale);

                    canvas.drawLine(mViewWidth / mScale - margin, barPosition + margin, mViewWidth / mScale - margin, barHeight * barLength + barPosition + margin, barPaint);
                }
            }
        }
    }

    public void addTextRow(TextRow row)
    {
        mRows.add(row);

        updateSizeByRow(row);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @SuppressWarnings("WeakerAccess")
    public void onConfigurationChanged()
    {
        WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();

        synchronized(mLock)
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1)
            {
                Point size = new Point();
                display.getSize(size);

                mViewWidth  = size.x;
                mViewHeight = size.y;
            }
            else
            {
                //noinspection deprecation
                mViewWidth  = display.getWidth();
                //noinspection deprecation
                mViewHeight = display.getHeight();
            }

            updateVisibleRanges();
        }
    }

    @Override
    public boolean onTouch(View v, MotionEvent event)
    {
        showBars();

        switch (event.getAction() & MotionEvent.ACTION_MASK)
        {
            case MotionEvent.ACTION_DOWN:
            {
                mTouchMode      = TouchMode.NONE;

                mTouchX         = event.getX();
                mTouchY         = event.getY();

                int highlightedRow = -1;

                for (int i = mVisibleBegin; i < mVisibleEnd; ++i)
                {
                    if (mTouchY / mScale >= mY - mOffsetY + mRows.get(i).getY() && mTouchY / mScale <= mY - mOffsetY + mRows.get(i).getBottom())
                    {
                        highlightedRow = i;

                        mHandler.sendEmptyMessageDelayed(HIGHLIGHT_MESSAGE, HIGHLIGHT_DELAY);

                        break;
                    }
                }

                synchronized(mLock)
                {
                    mHighlightedRow = highlightedRow;
                    mHighlightAlpha = 0;
                    mSelectionEnd   = -1;
                }
            }
            break;

            case MotionEvent.ACTION_POINTER_DOWN:
            {
                if (
                    mTouchMode == TouchMode.NONE
                    ||
                    mTouchMode == TouchMode.ZOOM
                   )
                {
                    mTouchMode = TouchMode.ZOOM;

                    stopHighlight();

                    mFingerDistance = fingerDistance(event);
                    mTouchMiddleX   = (event.getX(0) + event.getX(1)) * 0.5f;
                    mTouchMiddleY   = (event.getY(0) + event.getY(1)) * 0.5f;
                }
            }
            break;

            case MotionEvent.ACTION_MOVE:
            {
                if (mTouchMode == TouchMode.SELECT)
                {
                    mTouchX = event.getX();
                    mTouchY = event.getY();

                    updateSelection();

                    mHandler.removeMessages(SCROLL_MESSAGE);

                    if (
                        mTouchY < mViewHeight / 8
                        ||
                        mTouchY > mViewHeight * 7 / 8
                       )
                    {
                        touchScroll();
                    }
                    else
                    {
                        mScrollMultiplier = 1;
                    }
                }
                else
                if (mTouchMode == TouchMode.ZOOM)
                {
                    if (mFingerDistance != 0 && event.getPointerCount() >= 2)
                    {
                        float newDistance = fingerDistance(event);
                        float scale       = mScale * newDistance / mFingerDistance;

                        if (scale < 0.25f)
                        {
                            scale = 0.25f;
                        }
                        else
                        if (scale > 10.0f)
                        {
                            scale = 10.0f;
                        }

                        mFingerDistance = newDistance;

                        //noinspection FloatingPointEquality
                        if (mScale != scale)
                        {
                            PointF newOffsets = new PointF(mOffsetX + mTouchMiddleX / mScale * (1 - mScale / scale), mOffsetY + mTouchMiddleY / mScale * (1 - mScale / scale));

                            fitOffsets(newOffsets);



                            synchronized(mLock)
                            {
                                mScale = scale;

                                mOffsetX = newOffsets.x;
                                mOffsetY = newOffsets.y;
                            }

                            updateVisibleRanges();
                            repaint();
                        }
                    }
                }
                else
                {
                    if (
                        mTouchMode == TouchMode.NONE
                        &&
                        (
                         Math.abs(mTouchX - event.getX()) > SCROLL_THRESHOLD
                         ||
                         Math.abs(mTouchY - event.getY()) > SCROLL_THRESHOLD
                        )
                       )
                    {
                        mTouchMode = TouchMode.DRAG;

                        stopHighlight();
                    }

                    if (mTouchMode == TouchMode.DRAG)
                    {
                        PointF newOffsets = new PointF(mOffsetX + (mTouchX - event.getX()) / mScale, mOffsetY + (mTouchY - event.getY()) / mScale);

                        fitOffsets(newOffsets);


                        //noinspection FloatingPointEquality
                        if (
                            mOffsetX != newOffsets.x
                            ||
                            mOffsetY != newOffsets.y
                            )
                        {
                            synchronized(mLock)
                            {
                                mOffsetX = newOffsets.x;
                                mOffsetY = newOffsets.y;
                            }

                            updateVisibleRanges();
                            repaint();
                        }



                        mTouchX = event.getX();
                        mTouchY = event.getY();
                    }
                }
            }
            break;

            case MotionEvent.ACTION_POINTER_UP:
            {
                // Nothing
            }
            break;

            default:
            {
                if (mTouchMode == TouchMode.SELECT)
                {
                    mHandler.removeMessages(SCROLL_MESSAGE);

                    int firstRow;
                    int lastRow;

                    if (mSelectionEnd > mHighlightedRow)
                    {
                        firstRow = mHighlightedRow;
                        lastRow  = mSelectionEnd;
                    }
                    else
                    {
                        firstRow = mSelectionEnd;
                        lastRow  = mHighlightedRow;
                    }

                    if (mSelectionType == SelectionType.NOTE)
                    {
                        mCommentDialogRequestedListener.onCommentDialogRequested(firstRow, lastRow, "");
                    }
                    else
                    {
                        performSelection(firstRow, lastRow, null);
                        finishSelection();
                    }
                }
                else
                if (mTouchMode == TouchMode.ZOOM)
                {
                    mTouchMiddleX = -1;
                    mTouchMiddleY = -1;
                }
                else
                {
                    stopHighlight();
                }
            }
            break;
        }

        return true;
    }

    public void onCommentEntered(int firstRow, int lastRow, String comment)
    {
        String commentLine = comment;

        if (!commentLine.isEmpty())
        {
            String commentLineStart = mSyntaxParser.getCommentLine();
            String commentLineEnd   = mSyntaxParser.getCommentLineEnd();

            if (commentLineStart != null)
            {
                if (!commentLineStart.isEmpty() && commentLineStart.charAt(commentLineStart.length() - 1) == ' ')
                {
                    commentLine = commentLineStart + "TODO: " + commentLine;
                }
                else
                {
                    commentLine = commentLineStart + " TODO: " + commentLine;
                }
            }

            if (commentLineEnd != null)
            {
                if (!commentLineEnd.isEmpty() && commentLineEnd.charAt(0) == ' ')
                {
                    commentLine += commentLineEnd;
                }
                else
                {
                    commentLine += ' ' + commentLineEnd;
                }
            }
        }



        performSelection(firstRow, lastRow, commentLine);
        updateSizes();

        saveFile();

        finishSelection();
    }

    public void onCommentCanceled()
    {
        finishSelection();
    }

    private static float fingerDistance(MotionEvent event)
    {
        float dx = event.getX(0) - event.getX(1);
        float dy = event.getY(0) - event.getY(1);

        //noinspection NumericCastThatLosesPrecision
        return (float) Math.sqrt(dx * dx + dy * dy);
    }

    private void stopHighlight()
    {
        if (mHighlightedRow >= 0)
        {
            synchronized(mLock)
            {
                mHighlightedRow = -1;
                mHighlightAlpha = 0;
            }

            mHandler.removeMessages(HIGHLIGHT_MESSAGE);
        }
    }

    private void performSelection(int firstRow, int lastRow, String comment)
    {
        int reviewedCount = 0;
        int invalidCount  = 0;
        int noteCount     = 0;

        for (int i = firstRow; i <= lastRow; ++i)
        {
            switch (mRows.get(i).getSelectionType())
            {
                case SelectionType.REVIEWED:
                    --reviewedCount;
                break;
                case SelectionType.INVALID:
                    --invalidCount;
                break;
                case SelectionType.NOTE:
                    --noteCount;
                break;
                case SelectionType.CLEAR:
                    // Nothing
                break;
                default:
                    AppLog.wtf(TAG, "Unknown selection type: " + mRows.get(i).getSelectionType());
                break;
            }
        }

        MainDatabase helper = MainDatabase.newInstance(mContext);
        SQLiteDatabase db = helper.getWritableDatabase();

        long fileId = mParent.getFileId();

        if (fileId <= 0)
        {
            fileId = MainDatabase.getOrCreateFileId(db, mParent.getFilePath());
            mParent.setFileId(fileId);
        }

        db.close();

        SingleFileDatabase fileHelper = SingleFileDatabase.newInstance(mContext, fileId);
        db = fileHelper.getWritableDatabase();

        synchronized(mLock)
        {
            for (int i = firstRow; i <= lastRow; ++i)
            {
                TextRow row = mRows.get(i);

                if (comment != null)
                {
                    row.setComment(comment, mSyntaxParser.getCommentPaint());
                }
                else
                {
                    row.setSelectionType(mSelectionType);
                }

                switch (mRows.get(i).getSelectionType())
                {
                    case SelectionType.REVIEWED:
                        ++reviewedCount;
                        SingleFileDatabase.insertOrUpdateRow(db, i, RowType.REVIEWED);
                    break;
                    case SelectionType.INVALID:
                        ++invalidCount;
                        SingleFileDatabase.insertOrUpdateRow(db, i, RowType.INVALID);
                    break;
                    case SelectionType.NOTE:
                        ++noteCount;
                        SingleFileDatabase.removeRow(db, i);
                    break;
                    case SelectionType.CLEAR:
                        SingleFileDatabase.removeRow(db, i);
                    break;
                    default:
                        AppLog.wtf(TAG, "Unknown selection type: " + mRows.get(i).getSelectionType());
                    break;
                }
            }
        }

        db.close();

        setProgress(
                    mReviewedCount + reviewedCount,
                    mInvalidCount  + invalidCount,
                    mNoteCount     + noteCount
                   );
    }

    private void finishSelection()
    {
        mHighlightedRow = -1;
        mSelectionEnd   = -1;

        mHandler.removeMessages(SELECTION_MESSAGE);

        repaint();
    }

    private void repaint()
    {
        if (mParent != null)
        {
            mParent.repaint();
        }
    }

    private void saveFile()
    {
        if (mParent != null)
        {
            mParent.saveRequested();

            if (mParent.getFileId() > 0)
            {
                MainDatabase helper = MainDatabase.newInstance(mContext);
                SQLiteDatabase db = helper.getWritableDatabase();

                MainDatabase.updateFileMeta(db, mParent.getFileId(), mParent.getFilePath());

                db.close();
            }
        }
    }

    private void progressChanged()
    {
        if (mProgressChangedListener != null)
        {
            if (mRows.isEmpty())
            {
                mProgressChangedListener.onProgressChanged(100);
            }
            else
            {
                int progress = mReviewedCount + mInvalidCount + mNoteCount;
                int percent  = progress * 100 / mRows.size();

                if (percent == 0 && progress > 0)
                {
                    percent = 1;
                }

                mProgressChangedListener.onProgressChanged(percent);
            }
        }
    }

    private void showBars()
    {
        synchronized(mLock)
        {
            mBarsAlpha = 255;
        }

        mHandler.removeMessages(HIDE_BARS_MESSAGE);
        mHandler.sendEmptyMessageDelayed(HIDE_BARS_MESSAGE, AUTO_HIDE_DELAY);

        repaint();
    }

    private void updateSelection()
    {
        int selectionEnd = mSelectionEnd;

        if (selectionEnd < 0)
        {
            selectionEnd = mHighlightedRow;
        }

        while (selectionEnd > mVisibleBegin)
        {
            if (mTouchY / mScale <= mY - mOffsetY + mRows.get(selectionEnd - 1).getBottom())
            {
                --selectionEnd;
            }
            else
            {
                break;
            }
        }

        while (selectionEnd < mVisibleEnd - 1)
        {
            if (mTouchY / mScale >= mY - mOffsetY + mRows.get(selectionEnd + 1).getY())
            {
                ++selectionEnd;
            }
            else
            {
                break;
            }
        }

        if (mSelectionEnd != selectionEnd)
        {
            synchronized(mLock)
            {
                mSelectionEnd = selectionEnd;
            }

            repaint();
        }
    }

    private void touchScroll()
    {
        mScrollMultiplier *= SCROLL_MULTIPLIER_INCREMENT;

        if (mScrollMultiplier > SCROLL_MULTIPLIER_MAX)
        {
            mScrollMultiplier = SCROLL_MULTIPLIER_MAX;
        }



        PointF newOffsets = new PointF(mOffsetX, mOffsetY);

        if (mTouchY < mViewHeight / 8)
        {
            newOffsets.y = mOffsetY - SCROLL_SPEED / mScale * mScrollMultiplier;
        }
        else
        {
            newOffsets.y = mOffsetY + SCROLL_SPEED / mScale * mScrollMultiplier;
        }

        fitOffsets(newOffsets);



        //noinspection FloatingPointEquality
        if (mOffsetY != newOffsets.y)
        {
            synchronized(mLock)
            {
                mOffsetY = newOffsets.y;
            }

            mHandler.sendEmptyMessageDelayed(SCROLL_MESSAGE, 40);

            updateVisibleRanges();
            updateSelection();
            repaint();
        }
    }

    private void fitOffsets(PointF offsets)
    {
        if (offsets.x > getRight())
        {
            offsets.x = getRight();
        }

        if (offsets.x < 0)
        {
            offsets.x = 0;
        }

        if (offsets.y > getBottom())
        {
            offsets.y = getBottom();
        }

        if (offsets.y < 0)
        {
            offsets.y = 0;
        }
    }

    private void updateVisibleRanges()
    {
        if (mRows.isEmpty())
        {
            Assert.assertEquals("mVisibleBegin must be -1", mVisibleBegin, -1);
            Assert.assertEquals("mVisibleEnd must be -1",   mVisibleEnd,   -1);

            return;
        }

        int visibleBegin = mVisibleBegin;
        int visibleEnd   = mVisibleEnd;

        if (visibleBegin < 0)
        {
            visibleBegin = 0;
        }

        while (visibleBegin > 0)
        {
            if (mY - mOffsetY + mRows.get(visibleBegin - 1).getBottom() >= 0)
            {
                --visibleBegin;
            }
            else
            {
                break;
            }
        }

        while (visibleBegin < mRows.size())
        {
            if (mY - mOffsetY + mRows.get(visibleBegin).getBottom() < 0)
            {
                ++visibleBegin;
            }
            else
            {
                break;
            }
        }

        --visibleEnd;

        if (visibleEnd < visibleBegin)
        {
            visibleEnd = visibleBegin;
        }

        while (visibleEnd > visibleBegin)
        {
            if (mY - mOffsetY + mRows.get(visibleEnd).getY() >= mViewHeight / mScale)
            {
                --visibleEnd;
            }
            else
            {
                break;
            }
        }

        while (visibleEnd < mRows.size() - 1)
        {
            if (mY - mOffsetY + mRows.get(visibleEnd + 1).getY() < mViewHeight / mScale)
            {
                ++visibleEnd;
            }
            else
            {
                break;
            }
        }

        if (visibleEnd < mRows.size())
        {
            ++visibleEnd;
        }
        else
        {
            visibleEnd = mRows.size();
        }

        if (
            mVisibleBegin != visibleBegin
            ||
            mVisibleEnd != visibleEnd
           )
        {
            synchronized(mLock)
            {
                mVisibleBegin = visibleBegin;
                mVisibleEnd   = visibleEnd;
            }
        }
    }

    private void updateSizes()
    {
        mWidth  = 0;
        mHeight = 0;

        for (int i = 0; i < mRows.size(); ++i)
        {
            updateSizeByRow(mRows.get(i));
        }

        // -----------------------------------------------

        PointF newOffsets = new PointF(mOffsetX, mOffsetY);

        fitOffsets(newOffsets);



        //noinspection FloatingPointEquality
        if (
            mOffsetX != newOffsets.x
            ||
            mOffsetY != newOffsets.y
            )
        {
            synchronized(mLock)
            {
                mOffsetX = newOffsets.x;
                mOffsetY = newOffsets.y;
            }
        }

        updateVisibleRanges();
        repaint();
    }

    private void updateSizeByRow(TextRow row)
    {
        row.setY(mHeight);

        mHeight += row.getHeight();

        if (row.getWidth() > mWidth)
        {
            mWidth = row.getWidth();
        }
    }

    public void setParent(ReviewSurfaceView parent)
    {
        mParent = parent;
    }

    public ArrayList<TextRow> getRows()
    {
       return mRows;
    }

    public void setFontSize(int fontSize)
    {
        if (mFontSize != fontSize)
        {
            //noinspection UnnecessaryExplicitNumericCast
            mOffsetX *= (float)fontSize / (float)mFontSize;
            //noinspection UnnecessaryExplicitNumericCast
            mOffsetY *= (float)fontSize / (float)mFontSize;

            mFontSize = fontSize;
            float textSize = Utils.spToPixels(mFontSize, mContext);

            for (int i = 0; i < mRows.size(); ++i)
            {
                mRows.get(i).setFontSize(textSize);
            }

            mRowPaint.setTextSize(textSize);

            mX -= mIndexWidth;
            mIndexWidth = mRowPaint.measureText(String.valueOf(mRows.size() + 1));
            mX += mIndexWidth;

            updateSizes();
        }
    }

    public void setTabSize(int tabSize)
    {
        if (mTabSize != tabSize)
        {
            mTabSize = tabSize;

            for (int i = 0; i < mRows.size(); ++i)
            {
                mRows.get(i).setTabSize(mTabSize);
            }

            updateSizes();
        }
    }

    public void setSelectionType(int selectionType)
    {
        mSelectionType = selectionType;
    }

    public void setOnProgressChangedListener(OnProgressChangedListener listener)
    {
        mProgressChangedListener = listener;
        progressChanged();
    }

    public void setOnCommentDialogRequestedListener(OnCommentDialogRequestedListener listener)
    {
        mCommentDialogRequestedListener = listener;
    }

    public void setProgress(int reviewedCount, int invalidCount, int noteCount)
    {
        if (
            mReviewedCount != reviewedCount
            ||
            mInvalidCount  != invalidCount
            ||
            mNoteCount     != noteCount
           )
        {
            mReviewedCount = reviewedCount;
            mInvalidCount  = invalidCount;
            mNoteCount     = noteCount;

            MainDatabase helper = MainDatabase.newInstance(mContext);
            SQLiteDatabase db = helper.getWritableDatabase();

            long fileId = mParent.getFileId();

            if (fileId <= 0)
            {
                fileId = MainDatabase.getOrCreateFileId(db, mParent.getFilePath());
                mParent.setFileId(fileId);
            }

            MainDatabase.updateFileStats(db, fileId, mReviewedCount, mInvalidCount, mNoteCount, mRows.size());

            db.close();

            progressChanged();
        }
    }

    @SuppressWarnings("unused")
    public void setX(float x)
    {
        mX = x;
    }

    @SuppressWarnings("unused")
    public void setY(float y)
    {
        mY = y;
    }

    @SuppressWarnings("unused")
    public float getX()
    {
        return mX;
    }

    @SuppressWarnings("unused")
    public float getY()
    {
        return mY;
    }

    @SuppressWarnings("unused")
    public float getWidth()
    {
        return mWidth;
    }

    @SuppressWarnings("unused")
    public float getHeight()
    {
        return mHeight;
    }

    @SuppressWarnings("WeakerAccess")
    public float getRight()
    {
        return mX + mWidth;
    }

    @SuppressWarnings("WeakerAccess")
    public float getBottom()
    {
        return mY + mHeight;
    }



    @SuppressWarnings("NonStaticInnerClassInSecureContext")
    @SuppressLint("HandlerLeak")
    private class DocumentHandler extends Handler
    {
        private DocumentHandler()
        {
        }

        @Override
        public void handleMessage(Message msg)
        {
            switch (msg.what)
            {
                case HIDE_BARS_MESSAGE:
                {
                    hideBars();
                }
                break;

                case HIGHLIGHT_MESSAGE:
                {
                    highlight();
                }
                break;

                case SELECTION_MESSAGE:
                {
                    selection();
                }
                break;

                case SCROLL_MESSAGE:
                {
                    scroll();
                }
                break;

                default:
                {
                    AppLog.wtf(TAG, "Unknown message type: " + msg.what);
                }
                break;
            }
        }

        private void hideBars()
        {
            int barsAlpha = mBarsAlpha - 20;

            if (barsAlpha > 0)
            {
                sendEmptyMessageDelayed(HIDE_BARS_MESSAGE, 40);
            }
            else
            {
                barsAlpha = 0;
            }

            if (mBarsAlpha != barsAlpha)
            {
                synchronized(mLock)
                {
                    mBarsAlpha = barsAlpha;
                }

                repaint();
            }
        }

        private void highlight()
        {
            int highlightAlpha = mHighlightAlpha + 20;

            if (highlightAlpha < 255)
            {
                synchronized(mLock)
                {
                    mHighlightAlpha = highlightAlpha;
                }

                sendEmptyMessageDelayed(HIGHLIGHT_MESSAGE, 40);
            }
            else
            {
                mVibrator.vibrate(VIBRATOR_LONG_CLICK);

                mTouchMode        = TouchMode.SELECT;
                mScrollMultiplier = 1;

                synchronized(mLock)
                {
                    mHighlightAlpha      = 0;
                    mSelectionBrightness = 1;
                    mSelectionMakeLight  = false;
                }

                updateSelection();

                sendEmptyMessageDelayed(SELECTION_MESSAGE, 40);
            }

            repaint();
        }

        private void selection()
        {
            boolean selectionMakeLight  = mSelectionMakeLight;
            float   selectionBrightness = mSelectionBrightness;

            if (selectionMakeLight)
            {
                selectionBrightness += SELECTION_SPEED;

                if (selectionBrightness >= 1)
                {
                    selectionMakeLight = false;
                    selectionBrightness = 1;
                }
            }
            else
            {
                selectionBrightness -= SELECTION_SPEED;

                if (selectionBrightness <= SELECTION_LOW_LIGHT)
                {
                    selectionMakeLight = true;
                    selectionBrightness = SELECTION_LOW_LIGHT;
                }
            }

            sendEmptyMessageDelayed(SELECTION_MESSAGE, 40);

            //noinspection FloatingPointEquality
            if (
                mSelectionMakeLight != selectionMakeLight
                 ||
                mSelectionBrightness != selectionBrightness
               )
            {
                synchronized(mLock)
                {
                    mSelectionMakeLight  = selectionMakeLight;
                    mSelectionBrightness = selectionBrightness;
                }

                repaint();
            }
        }

        private void scroll()
        {
            touchScroll();
        }
    }
}
