package com.griscom.codereview.listeners;

/**
 * Progress changed listener
 */
public interface OnProgressChangedListener
{
    /**
     * Handler for progress changed event
     * @param progress    progress value from 0 to 100
     */
    void onProgressChanged(int progress);
}
