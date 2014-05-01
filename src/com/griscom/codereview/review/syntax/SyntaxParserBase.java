package com.griscom.codereview.review.syntax;

import java.util.ArrayList;

import android.annotation.SuppressLint;

import com.griscom.codereview.review.TextRow;

@SuppressLint("DefaultLocale")
public abstract class SyntaxParserBase
{
    public abstract ArrayList<TextRow> parseFile(String fileName);

	public static SyntaxParserBase createParserByFileName(String fileName)
    {
        int index=fileName.lastIndexOf('.');

        if (index>0)
        {
            String extension=fileName.substring(index+1).toLowerCase();

            if (extension.equals("cs"))
            {
                return new CSharpSyntaxParser();
            }
        }

        return new PlainTextSyntaxParser();
    }
}
