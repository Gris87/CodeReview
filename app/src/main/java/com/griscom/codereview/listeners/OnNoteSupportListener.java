package com.griscom.codereview.listeners;

/**
 * Note support listener
 */
@SuppressWarnings("ConstantDeclaredInInterface")
public interface OnNoteSupportListener
{
    int UNSUPPORTED = 0;
    int SUPPORTED   = 1;

    /**
     * Handler for informing about note support
     * @param noteSupported    {@link #SUPPORTED}, if note supported
     */
    @SuppressWarnings("BooleanParameter")
    void onNoteSupport(int noteSupported);
}
