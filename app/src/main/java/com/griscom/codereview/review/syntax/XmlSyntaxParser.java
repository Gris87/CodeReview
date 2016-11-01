package com.griscom.codereview.review.syntax;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Typeface;

import com.griscom.codereview.review.TextDocument;
import com.griscom.codereview.review.TextRegion;
import com.griscom.codereview.review.TextRow;
import com.griscom.codereview.util.AppLog;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import prettify.PrettifyParser;
import prettify.parser.Prettify;
import syntaxhighlight.ParseResult;

/**
 * XML syntax parser
 */
@SuppressWarnings("WeakerAccess")
public class XmlSyntaxParser extends SyntaxParserBase
{
    @SuppressWarnings("unused")
    private static final String TAG = "XmlSyntaxParser";



    /**
     * Creates XmlSyntaxParser instance
     * @param context    context
     */
    public XmlSyntaxParser(Context context)
    {
        super(context);
    }

    /** {@inheritDoc} */
    @Override
    public TextDocument parseFile(String filePath)
    {
        TextDocument res = new TextDocument(this);

        try
        {
            Paint basePaint = new Paint();

            basePaint.setColor(Color.BLACK);
            basePaint.setTypeface(Typeface.MONOSPACE);
            basePaint.setTextSize(getFontSize());

            Paint tagPaint         = new Paint(basePaint);
            Paint attribNamePaint  = new Paint(basePaint);
            Paint attribValuePaint = new Paint(basePaint);
            Paint commentPaint     = new Paint(basePaint);
            Paint stringPaint      = new Paint(basePaint);
            Paint punctuationPaint = new Paint(basePaint);

            tagPaint.setColor        (Color.rgb(64,  128, 128));
            tagPaint.setTypeface     (Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            tagPaint.setFakeBoldText (true);
            attribNamePaint.setColor (Color.rgb(172, 0,   172));
            attribValuePaint.setColor(Color.rgb(40,  0,   255));
            commentPaint.setColor    (Color.rgb(64,  96,  192));
            stringPaint.setColor     (Color.rgb(40,  0,   255));

            Map<String, Paint> colorsMap = new HashMap<>();
            colorsMap.put(Prettify.PR_TAG,          tagPaint);
            colorsMap.put(Prettify.PR_ATTRIB_NAME,  attribNamePaint);
            colorsMap.put(Prettify.PR_ATTRIB_VALUE, attribValuePaint);
            colorsMap.put(Prettify.PR_COMMENT,      commentPaint);
            colorsMap.put(Prettify.PR_STRING,       stringPaint);
            colorsMap.put(Prettify.PR_PUNCTUATION,  punctuationPaint);

            setCommentPaint(commentPaint);

            // ---------------------------------------------------------------

            StringBuilder codeBuilder = new StringBuilder();

            createReader(filePath);

            String line;
            while ((line = readLine()) != null)
            {
                codeBuilder.append(line);
            }

            closeReader();

            // ---------------------------------------------------------------

            int tabSize = getTabSize();

            String sourceCode = codeBuilder.toString();
            List<ParseResult> results = new PrettifyParser().parse("xml", sourceCode);

            TextRow row = null;
            int curColumn = 0;

            for (ParseResult result : results)
            {
                if (row == null)
                {
                    row = new TextRow();
                }

                String type    = result.getStyleKeys().get(0);
                String content = sourceCode.substring(result.getOffset(), result.getOffset() + result.getLength());

                Paint selectedPaint = colorsMap.get(type);

                if (selectedPaint == null)
                {
                    if (!type.equals(Prettify.PR_PLAIN))
                    {
                        AppLog.wtf(TAG, "Unhandled syntax type: " + type);
                    }

                    selectedPaint = basePaint;
                }

                boolean lastEnter = content.endsWith("\n");

                do
                {
                    int index = content.indexOf('\n');

                    if (index < 0)
                    {
                        break;
                    }

                    String contentPart = content.substring(0, index);
                    content = content.substring(index + 1);

                    row.addTextRegion(new TextRegion(contentPart, selectedPaint, curColumn, tabSize));
                    res.addTextRow(row);

                    row = new TextRow();
                    curColumn = 0;
                } while (true);

                if (lastEnter || !content.equals(""))
                {
                    row.addTextRegion(new TextRegion(content, selectedPaint, curColumn, tabSize));
                    curColumn += content.length();
                }
            }

            if (row != null && row.hasRegions())
            {
                res.addTextRow(row);
            }
        }
        catch (Exception e)
        {
            AppLog.e(TAG, "Impossible to read file: " + filePath, e);
        }

        return res;
    }

    /** {@inheritDoc} */
    @Override
    public String getCommentLine()
    {
        return "<!--";
    }

    /** {@inheritDoc} */
    @Override
    public String getCommentLineEnd()
    {
        return "-->";
    }
}
