package com.griscom.codereview.listeners;

import android.graphics.Canvas;

/**
 * ReviewSurfaceView draw listener
 */
public interface OnReviewSurfaceDrawListener
{
    /**
     * Handler for drawing TextDocument on ReviewSurfaceView canvas
     * @param canvas    canvas
     */
    void onReviewSurfaceDraw(Canvas canvas);
}
