package com.griscom.codereview.listeners;

/**
 * Note support listener
 */
public interface OnNoteSupportListener
{
    /**
     * Handler for informing about note support
     * @param noteSupported    true, if note supported
     */
    @SuppressWarnings("BooleanParameter")
    void onNoteSupport(boolean noteSupported);
}
