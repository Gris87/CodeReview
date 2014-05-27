package com.griscom.codereview.review.syntax;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import prettify.PrettifyParser;
import prettify.parser.Prettify;
import syntaxhighlight.ParseResult;
import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;
import android.util.Log;

import com.griscom.codereview.review.TextDocument;
import com.griscom.codereview.review.TextRegion;
import com.griscom.codereview.review.TextRow;

public class RSyntaxParser extends SyntaxParserBase
{
    private static final String TAG="RSyntaxParser";

    public RSyntaxParser(Context context)
    {
        super(context);
    }

    @Override
    public TextDocument parseFile(String fileName)
    {
        TextDocument res=new TextDocument(this);

        try
        {
            Paint basePaint=new Paint();

            basePaint.setColor(Color.BLACK);
            basePaint.setTypeface(Typeface.MONOSPACE);
            basePaint.setTextSize(getFontSize());

            Paint keywordPaint     = new Paint(basePaint);
            Paint typePaint        = new Paint(basePaint);
            Paint literalPaint     = new Paint(basePaint);
            mCommentPaint          = new Paint(basePaint);
            Paint stringPaint      = new Paint(basePaint);
            Paint punctuationPaint = new Paint(basePaint);

            keywordPaint.setColor       (Color.rgb(150, 0,   85));
            keywordPaint.setTypeface    (Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            keywordPaint.setFakeBoldText(true);
            mCommentPaint.setColor      (Color.rgb(64,  128, 100));
            stringPaint.setColor        (Color.rgb(0,   0,   192));

            Map<String, Paint> colorsMap = new HashMap<String, Paint>();
            colorsMap.put(Prettify.PR_KEYWORD,     keywordPaint);
            colorsMap.put(Prettify.PR_TYPE,        typePaint);
            colorsMap.put(Prettify.PR_LITERAL,     literalPaint);
            colorsMap.put(Prettify.PR_COMMENT,     mCommentPaint);
            colorsMap.put(Prettify.PR_STRING,      stringPaint);
            colorsMap.put(Prettify.PR_PUNCTUATION, punctuationPaint);

            // ---------------------------------------------------------------

            StringBuilder codeBuilder=new StringBuilder();

            createReader(fileName);

            String line;
            while ((line = readLine()) != null)
            {
                codeBuilder.append(line);
                codeBuilder.append("\n");
            }

            closeReader();

            // ---------------------------------------------------------------

            int tabSize=getTabSize();

            String sourceCode=codeBuilder.toString();
            List<ParseResult> results=new PrettifyParser().parse("r", sourceCode);

            TextRow row=null;
            int curColumn=0;

            for (ParseResult result : results)
            {
                if (row==null)
                {
                    row=new TextRow();
                }

                String type    = result.getStyleKeys().get(0);
                String content = sourceCode.substring(result.getOffset(), result.getOffset() + result.getLength());

                Paint selectedPaint=colorsMap.get(type);

                if (selectedPaint==null)
                {
                    if (!type.equals(Prettify.PR_PLAIN))
                    {
                        Log.e(TAG, "Unhandled syntax type: "+type);
                    }

                    selectedPaint=basePaint;
                }

                do
                {
                    int index=content.indexOf('\n');

                    if (index<0)
                    {
                        break;
                    }

                    String contentPart=content.substring(0, index);
                    content=content.substring(index+1);

                    row.addTextRegion(new TextRegion(contentPart, selectedPaint, curColumn, tabSize));
                    res.addTextRow(row);

                    row=new TextRow();
                    curColumn=0;
                } while (true);

                if (!content.equals(""))
                {
                    row.addTextRegion(new TextRegion(content, selectedPaint, curColumn, tabSize));
                    curColumn+=content.length();
                }
            }

            if (row!=null)
            {
                res.addTextRow(row);
            }
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
	    // TODO: Check it
        return "-- ";
    }
}
