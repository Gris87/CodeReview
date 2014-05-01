package com.griscom.codereview.review.syntax;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;

import android.content.Context;
import android.util.Log;

import com.griscom.codereview.review.TextRow;

public class CSharpSyntaxParser extends SyntaxParserBase
{
    private static final String TAG="CSharpSyntaxParser";

    public CSharpSyntaxParser(Context context)
    {
        super(context);
    }

    @Override
    public ArrayList<TextRow> parseFile(String fileName)
    {
        ArrayList<TextRow> res=new ArrayList<TextRow>();

        try
        {
            BufferedReader reader=new BufferedReader(new FileReader(fileName));

            String line;
            while ((line = reader.readLine()) != null)
            {

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
