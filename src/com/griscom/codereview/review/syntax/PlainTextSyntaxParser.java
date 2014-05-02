package com.griscom.codereview.review.syntax;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.griscom.codereview.R;
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
    public ArrayList<TextRow> parseFile(String fileName)
    {
        ArrayList<TextRow> res=new ArrayList<TextRow>();

        try
        {
            Paint basePaint=new Paint();

            basePaint.setColor(Color.BLACK);
            basePaint.setTextSize(getFontSize());

            // ---------------------------------------------------------------

            float curY=mContext.getResources().getDimensionPixelSize(R.dimen.review_vertical_margin);

            BufferedReader reader=new BufferedReader(new FileReader(fileName));

            String line;
            while ((line = reader.readLine()) != null)
            {
                TextRow newRow       = new TextRow(mContext);
                TextRegion newRegion = new TextRegion(line, basePaint);



                newRow.setY(curY);
                newRow.addTextRegion(newRegion);

                curY+=newRow.getHeight();

                res.add(newRow);
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
