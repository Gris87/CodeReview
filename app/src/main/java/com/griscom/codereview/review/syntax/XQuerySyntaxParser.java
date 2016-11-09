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
 * X Query syntax parser
 */
@SuppressWarnings("WeakerAccess")
public final class XQuerySyntaxParser extends SyntaxParserBase
{
    @SuppressWarnings("unused")
    private static final String TAG = "XQuerySyntaxParser";



    /**
     * Creates XQuerySyntaxParser instance
     * @param context    context
     */
    private XQuerySyntaxParser(Context context)
    {
        super(context);
    }

    /**
     * Creates XQuerySyntaxParser instance
     * @param context    context
     */
    public static XQuerySyntaxParser newInstance(Context context)
    {
        return new XQuerySyntaxParser(context);
    }

    /** {@inheritDoc} */
    @Override
    public TextDocument parseFile(String filePath)
    {
        TextDocument res = TextDocument.newInstance(this);

        try
        {
            Paint basePaint = new Paint();

            basePaint.setColor(Color.BLACK);
            basePaint.setTypeface(Typeface.MONOSPACE);
            basePaint.setTextSize(getFontSize());

            Paint keywordPaint     = new Paint(basePaint);
            Paint typePaint        = new Paint(basePaint);
            Paint literalPaint     = new Paint(basePaint);
            Paint commentPaint     = new Paint(basePaint);
            Paint stringPaint      = new Paint(basePaint);
            Paint punctuationPaint = new Paint(basePaint);

            keywordPaint.setColor       (Color.rgb(150, 0,   85));
            keywordPaint.setTypeface    (Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));
            keywordPaint.setFakeBoldText(true);
            commentPaint.setColor       (Color.rgb(64,  128, 100));
            stringPaint.setColor        (Color.rgb(0,   0,   192));

            Map<String, Paint> colorsMap = new HashMap<>(6);
            colorsMap.put(Prettify.PR_KEYWORD,     keywordPaint);
            colorsMap.put(Prettify.PR_TYPE,        typePaint);
            colorsMap.put(Prettify.PR_LITERAL,     literalPaint);
            colorsMap.put(Prettify.PR_COMMENT,     commentPaint);
            colorsMap.put(Prettify.PR_STRING,      stringPaint);
            colorsMap.put(Prettify.PR_PUNCTUATION, punctuationPaint);

            setCommentPaint(commentPaint);

            // ---------------------------------------------------------------

            StringBuilder codeBuilder = new StringBuilder(0);

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
            List<ParseResult> results = new PrettifyParser().parse("xq", sourceCode);

            TextRow row = null;
            int curColumn = 0;

            for (ParseResult result : results)
            {
                if (row == null)
                {
                    row = TextRow.newInstance();
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

                boolean lastEnter = !content.isEmpty() && content.charAt(content.length() - 1) == '\n';

                do
                {
                    int index = content.indexOf('\n');

                    if (index < 0)
                    {
                        break;
                    }

                    String contentPart = content.substring(0, index);
                    content = content.substring(index + 1);

                    row.addTextRegion(TextRegion.newInstance(contentPart, selectedPaint, curColumn, tabSize));
                    res.addTextRow(row);

                    row = TextRow.newInstance();
                    curColumn = 0;
                } while (true);

                if (lastEnter || !content.isEmpty())
                {
                    row.addTextRegion(TextRegion.newInstance(content, selectedPaint, curColumn, tabSize));
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
        return "-- ";
    }
}
