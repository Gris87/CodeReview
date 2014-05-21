package com.griscom.codereview.review.syntax;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import com.griscom.codereview.review.TextDocument;
import com.griscom.codereview.review.TextRegion;
import com.griscom.codereview.review.TextRow;

public class BashSyntaxParser extends SyntaxParserBase
{
    private static final String TAG="BashSyntaxParser";

    public BashSyntaxParser(Context context)
    {
        super(context);
    }

    @Override
    public TextDocument parseFile(String fileName)
    {
        TextDocument res=new TextDocument(mContext);

        try
        {
            int tabSize=getTabSize();

            Paint basePaint=new Paint();

            basePaint.setColor(Color.BLACK);
            basePaint.setTypeface(Typeface.MONOSPACE);
            basePaint.setTextSize(getFontSize());

            // ---------------------------------------------------------------

            createReader(fileName);

            String line;
            while ((line = readLine()) != null)
            {
                TextRow    newRow    = new TextRow();
                TextRegion newRegion = new TextRegion(line, basePaint, 0, tabSize);



                newRow.addTextRegion(newRegion);
                res.addTextRow(newRow);
            }

            closeReader();
        }
        catch (Exception e)
        {
            Log.e(TAG, "Impossible to read file: "+fileName, e);
        }

        return res;
    }

	@Override
	public String getCommentLine()
	{
		return "#";
	}
}
