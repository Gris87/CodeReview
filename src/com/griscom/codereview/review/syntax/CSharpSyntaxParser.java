package com.griscom.codereview.review.syntax;

import java.io.BufferedReader;
import java.io.FileReader;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.griscom.codereview.review.TextDocument;
import com.griscom.codereview.review.TextRegion;
import com.griscom.codereview.review.TextRow;

public class CSharpSyntaxParser extends SyntaxParserBase
{
    private static final String TAG="CSharpSyntaxParser";

    public CSharpSyntaxParser(Context context)
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
            basePaint.setTextSize(getFontSize());

            // ---------------------------------------------------------------

            BufferedReader reader=new BufferedReader(new FileReader(fileName));

            String line;
            while ((line = reader.readLine()) != null)
            {
                TextRow newRow       = new TextRow();
                TextRegion newRegion = new TextRegion(line, basePaint);



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
