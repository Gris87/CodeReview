package com.griscom.codereview.listeners;

import com.griscom.codereview.review.TextDocument;

/**
 * Document loaded listener
 */
public interface OnDocumentLoadedListener
{
    /**
     * Handler for document loaded event
     * @param document    document
     */
    void onDocumentLoaded(TextDocument document);
}
