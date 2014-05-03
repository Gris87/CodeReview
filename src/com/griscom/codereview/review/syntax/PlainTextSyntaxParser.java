package com.griscom.codereview.review.syntax;

import java.io.BufferedReader;
import java.io.FileReader;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import com.griscom.codereview.review.TextDocument;
import com.griscom.codereview.review.TextRegion;
import com.griscom.codereview.review.TextRow;

public class PlainTextSyntaxParser extends SyntaxParserBase
{
    private static final String TAG="PlainTextSyntaxParser";

    public PlainTextSyntaxParser(Context context)
    {
        super(context);
    }

    @Override
    public TextDocument parseFile(String fileName)
    {
        TextDocument res=new TextDocument();

        try
        {
            Paint basePaint=new Paint();

            basePaint.setColor(Color.BLACK);
            basePaint.setTypeface(Typeface.MONOSPACE);
            basePaint.setTextSize(getFontSize());

            // ---------------------------------------------------------------

            int tabSize=getTabSize();

            BufferedReader reader=new BufferedReader(new FileReader(fileName));

            String line;
            while ((line = reader.readLine()) != null)
            {
                TextRow newRow       = new TextRow();
                TextRegion newRegion = new TextRegion(line, basePaint, 0, tabSize);



                newRow.addTextRegion(newRegion);
                res.addTextRow(newRow);
            }

            reader.close();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Impossible to read file: "+fileName, e);
        }

        return res;
    }
}
