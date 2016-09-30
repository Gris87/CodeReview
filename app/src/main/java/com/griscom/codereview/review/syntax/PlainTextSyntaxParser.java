package com.griscom.codereview.review.syntax;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.griscom.codereview.review.TextDocument;
import com.griscom.codereview.review.TextRegion;
import com.griscom.codereview.review.TextRow;
import com.griscom.codereview.util.AppLog;

public class PlainTextSyntaxParser extends SyntaxParserBase
{
    private static final String TAG = "PlainTextSyntaxParser";

    public PlainTextSyntaxParser(Context context)
    {
        super(context);
    }

    @Override
    public TextDocument parseFile(String fileName)
    {
        TextDocument res = new TextDocument(this);

        try
        {
            int tabSize = getTabSize();

            Paint basePaint = new Paint();

            basePaint.setColor(Color.BLACK);
            basePaint.setTypeface(Typeface.MONOSPACE);
            basePaint.setTextSize(getFontSize());

            // ---------------------------------------------------------------

            createReader(fileName);

            boolean lastEnter = false;

            String line;
            while ((line = readLine()) != null)
            {
                lastEnter = line.endsWith("\n");

                if (lastEnter)
                {
                    line = line.substring(0, line.length() - 1);
                }



                TextRow    newRow    = new TextRow();
                TextRegion newRegion = new TextRegion(line, basePaint, 0, tabSize);

                newRow.addTextRegion(newRegion);
                res.addTextRow(newRow);
            }

            if (lastEnter)
            {
                TextRow    newRow    = new TextRow();
                TextRegion newRegion = new TextRegion("", basePaint, 0, tabSize);

                newRow.addTextRegion(newRegion);
                res.addTextRow(newRow);
            }

            closeReader();
        }
        catch (Exception e)
        {
            AppLog.e(TAG, "Impossible to read file: " + fileName, e);
        }

        return res;
    }
}
