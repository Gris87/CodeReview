package com.griscom.codereview.listeners;

/**
 * CommentDialog requested listener
 */
public interface OnCommentDialogRequestedListener
{
    /**
     * Handler for CommentDialog requested event
     * @param firstRow    first selected row
     * @param lastRow     last selected row
     * @param comment     comment
     */
    void onCommentDialogRequested(int firstRow, int lastRow, String comment);
}
