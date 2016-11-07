package com.griscom.codereview.review.syntax;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.griscom.codereview.review.TextDocument;
import com.griscom.codereview.review.TextRegion;
import com.griscom.codereview.review.TextRow;
import com.griscom.codereview.util.AppLog;

/**
 * Plain text syntax parser
 */
@SuppressWarnings("WeakerAccess")
public class PlainTextSyntaxParser extends SyntaxParserBase
{
    @SuppressWarnings("unused")
    private static final String TAG = "PlainTextSyntaxParser";



    /**
     * Creates PlainTextSyntaxParser instance
     * @param context    context
     */
    private PlainTextSyntaxParser(Context context)
    {
        super(context);
    }

    /**
     * Creates PlainTextSyntaxParser instance
     * @param context    context
     */
    public static PlainTextSyntaxParser newInstance(Context context)
    {
        return new PlainTextSyntaxParser(context);
    }

    /** {@inheritDoc} */
    @Override
    public TextDocument parseFile(String filePath)
    {
        TextDocument res = TextDocument.newInstance(this);

        try
        {
            int tabSize = getTabSize();

            Paint basePaint = new Paint();

            basePaint.setColor(Color.BLACK);
            basePaint.setTypeface(Typeface.MONOSPACE);
            basePaint.setTextSize(getFontSize());

            // ---------------------------------------------------------------

            createReader(filePath);

            boolean lastEnter = false;

            String line;
            while ((line = readLine()) != null)
            {
                lastEnter = !line.isEmpty() && line.charAt(line.length() - 1) == '\n';

                if (lastEnter)
                {
                    line = line.substring(0, line.length() - 1);
                }



                TextRow    newRow    = TextRow.newInstance();
                TextRegion newRegion = TextRegion.newInstance(line, basePaint, 0, tabSize);

                newRow.addTextRegion(newRegion);
                res.addTextRow(newRow);
            }

            if (lastEnter)
            {
                TextRow    newRow    = TextRow.newInstance();
                TextRegion newRegion = TextRegion.newInstance("", basePaint, 0, tabSize);

                newRow.addTextRegion(newRegion);
                res.addTextRow(newRow);
            }

            closeReader();
        }
        catch (Exception e)
        {
            AppLog.e(TAG, "Impossible to read file: " + filePath, e);
        }

        return res;
    }
}
