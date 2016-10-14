package com.griscom.codereview.review.syntax;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Paint;

import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.other.SyntaxParserType;
import com.griscom.codereview.review.TextDocument;
import com.griscom.codereview.util.AppLog;
import com.griscom.codereview.util.Utils;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

@SuppressLint("DefaultLocale")
public abstract class SyntaxParserBase
{
    @SuppressWarnings("unused")
    private static final String TAG = "SyntaxParserBase";



    private Context        mContext;
    private BufferedReader mReader;
    private Paint          mCommentPaint;



    protected SyntaxParserBase(Context context)
    {
        mContext      = context;
        mReader       = null;
        mCommentPaint = null;
    }

    public static SyntaxParserBase createParserByType(String fileName, Context context, int type)
    {
        switch (type)
        {
            case SyntaxParserType.AUTOMATIC: return createParserByFileName(fileName, context);

            case SyntaxParserType.APOLLO:             return new ApolloSyntaxParser(context);
            case SyntaxParserType.BASH:               return new BashSyntaxParser(context);
            case SyntaxParserType.BASIC:              return new BasicSyntaxParser(context);
            case SyntaxParserType.CLOJURE:            return new ClojureSyntaxParser(context);
            case SyntaxParserType.C_PLUS_PLUS:        return new CPlusPlusSyntaxParser(context);
            case SyntaxParserType.C_SHARP:            return new CSharpSyntaxParser(context);
            case SyntaxParserType.CSS:                return new CssSyntaxParser(context);
            case SyntaxParserType.CSS_KW:             return new CssKwSyntaxParser(context);
            case SyntaxParserType.CSS_STR:            return new CssStrSyntaxParser(context);
            case SyntaxParserType.DART:               return new DartSyntaxParser(context);
            case SyntaxParserType.ERLANG:             return new ErlangSyntaxParser(context);
            case SyntaxParserType.GO:                 return new GoSyntaxParser(context);
            case SyntaxParserType.HASKELL:            return new HaskellSyntaxParser(context);
            case SyntaxParserType.JAVA:               return new JavaSyntaxParser(context);
            case SyntaxParserType.LISP:               return new LispSyntaxParser(context);
            case SyntaxParserType.LLVM:               return new LlvmSyntaxParser(context);
            case SyntaxParserType.LUA:                return new LuaSyntaxParser(context);
            case SyntaxParserType.MATLAB:             return new MatlabSyntaxParser(context);
            case SyntaxParserType.MATLAB_IDENTIFIERS: return new MatlabIdentifiersSyntaxParser(context);
            case SyntaxParserType.MATLAB_OPERATORS:   return new MatlabOperatorsSyntaxParser(context);
            case SyntaxParserType.ML:                 return new MlSyntaxParser(context);
            case SyntaxParserType.MUMPS:              return new MumpsSyntaxParser(context);
            case SyntaxParserType.N:                  return new NSyntaxParser(context);
            case SyntaxParserType.PASCAL:             return new PascalSyntaxParser(context);
            case SyntaxParserType.R:                  return new RSyntaxParser(context);
            case SyntaxParserType.RD:                 return new RdSyntaxParser(context);
            case SyntaxParserType.SCALA:              return new ScalaSyntaxParser(context);
            case SyntaxParserType.SQL:                return new SqlSyntaxParser(context);
            case SyntaxParserType.TCL:                return new TclSyntaxParser(context);
            case SyntaxParserType.TEX:                return new TexSyntaxParser(context);
            case SyntaxParserType.VHDL:               return new VisualBasicSyntaxParser(context);
            case SyntaxParserType.VISUAL_BASIC:       return new VhdlSyntaxParser(context);
            case SyntaxParserType.WIKI:               return new WikiSyntaxParser(context);
            case SyntaxParserType.XML:                return new XmlSyntaxParser(context);
            case SyntaxParserType.X_QUERY:            return new XQuerySyntaxParser(context);
            case SyntaxParserType.YAML:               return new YamlSyntaxParser(context);
            case SyntaxParserType.PLAIN_TEXT:         return new PlainTextSyntaxParser(context);

            default:
            {
                AppLog.wtf(TAG, "Unknown syntax parser type: " + String.valueOf(type));
            }
        }

        return new PlainTextSyntaxParser(context);
    }

    public static SyntaxParserBase createParserByFileName(String fileName, Context context)
    {
        fileName = fileName.toLowerCase();

        int index = fileName.lastIndexOf('.');

        if (index > 0)
        {
            String extension = fileName.substring(index + 1);

            if (
                extension.equals("apollo")
                ||
                extension.equals("agc")
                ||
                extension.equals("aea")
               )
            {
                return new ApolloSyntaxParser(context);
            }

            if (extension.equals("sh"))
            {
                return new BashSyntaxParser(context);
            }

            if (
                extension.equals("basic")
                ||
                extension.equals("cbm")
               )
            {
                return new BasicSyntaxParser(context);
            }

            if (extension.equals("clj"))
            {
                return new ClojureSyntaxParser(context);
            }

            if (
                extension.equals("c")
                ||
                extension.equals("h")
                ||
                extension.equals("cpp")
                ||
                extension.equals("hpp")
               )
            {
                return new CPlusPlusSyntaxParser(context);
            }

            if (extension.equals("cs"))
            {
                return new CSharpSyntaxParser(context);
            }

            if (extension.equals("css"))
            {
                return new CssSyntaxParser(context);
            }

            if (extension.equals("css-kw"))
            {
                return new CssKwSyntaxParser(context);
            }

            if (extension.equals("css-str"))
            {
                return new CssStrSyntaxParser(context);
            }

            if (extension.equals("dart"))
            {
                return new DartSyntaxParser(context);
            }

            if (
                extension.equals("erlang")
                ||
                extension.equals("erl")
               )
            {
                return new ErlangSyntaxParser(context);
            }

            if (extension.equals("go"))
            {
                return new GoSyntaxParser(context);
            }

            if (extension.equals("hs"))
            {
                return new HaskellSyntaxParser(context);
            }

            if (extension.equals("java"))
            {
                return new JavaSyntaxParser(context);
            }

            if (
                extension.equals("cl")
                ||
                extension.equals("el")
                ||
                extension.equals("lisp")
                ||
                extension.equals("lsp")
                ||
                extension.equals("scm")
                ||
                extension.equals("ss")
                ||
                extension.equals("rkt")
               )
            {
                return new LispSyntaxParser(context);
            }

            if (
                extension.equals("llvm")
                ||
                extension.equals("ll")
               )
            {
                return new LlvmSyntaxParser(context);
            }

            if (extension.equals("lua"))
            {
                return new LuaSyntaxParser(context);
            }

            if (extension.equals("matlab"))
            {
                return new MatlabSyntaxParser(context);
            }

            if (extension.equals("matlab-identifiers"))
            {
                return new MatlabIdentifiersSyntaxParser(context);
            }

            if (extension.equals("matlab-operators"))
            {
                return new MatlabOperatorsSyntaxParser(context);
            }

            if (
                extension.equals("fs")
                ||
                extension.equals("ml")
               )
            {
                return new MlSyntaxParser(context);
            }

            if (extension.equals("mumps"))
            {
                return new MumpsSyntaxParser(context);
            }

            if (
                extension.equals("n")
                ||
                extension.equals("nemerle")
               )
            {
                return new NSyntaxParser(context);
            }

            if (
                extension.equals("pas")
                ||
                extension.equals("pascal")
               )
            {
                return new PascalSyntaxParser(context);
            }

            if (
                extension.equals("r")
                ||
                extension.equals("s")
                ||
                extension.equals("splus")
               )
            {
                return new RSyntaxParser(context);
            }

            if (extension.equals("rd"))
            {
                return new RdSyntaxParser(context);
            }

            if (extension.equals("scala"))
            {
                return new ScalaSyntaxParser(context);
            }

            if (extension.equals("sql"))
            {
                return new SqlSyntaxParser(context);
            }

            if (extension.equals("tcl"))
            {
                return new TclSyntaxParser(context);
            }

            if (
                extension.equals("latex")
                ||
                extension.equals("tex")
                )
            {
                return new TexSyntaxParser(context);
            }

            if (
                extension.equals("vb")
                ||
                extension.equals("vbs")
                )
            {
                return new VisualBasicSyntaxParser(context);
            }

            if (
                extension.equals("vhdl")
                ||
                extension.equals("vhd")
                )
            {
                return new VhdlSyntaxParser(context);
            }

            if (fileName.endsWith(".wiki.meta"))
            {
                return new WikiSyntaxParser(context);
            }

            if (
                extension.equals("xml")
                ||
                extension.equals("html")
                ||
                extension.equals("ui")
               )
            {
                return new XmlSyntaxParser(context);
            }

            if (
                extension.equals("xq")
                ||
                extension.equals("xquery")
                )
            {
                return new XQuerySyntaxParser(context);
            }

            if (
                extension.equals("yaml")
                ||
                extension.equals("yml")
                )
            {
                return new YamlSyntaxParser(context);
            }
        }

        return new PlainTextSyntaxParser(context);
    }

    public abstract TextDocument parseFile(String fileName);

    protected void createReader(String fileName) throws FileNotFoundException
    {
        synchronized (this)
        {
            mReader = null;
            mReader = new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        }
    }

    protected String readLine() throws IOException
    {
        BufferedReader reader;

        synchronized (this)
        {
            reader = mReader;
        }

        if (reader != null)
        {
            StringBuilder res = null;

            do
            {
                int oneChar = reader.read();

                if (oneChar < 0)
                {
                    return res != null? res.toString() : null;
                }

                if (res == null)
                {
                    res = new StringBuilder();
                }

                res.append((char)oneChar);

                if (oneChar == '\n')
                {
                    return res.toString();
                }
            } while(true);
        }

        return null;
    }

    public void closeReader() throws IOException
    {
        synchronized (this)
        {
            if (mReader != null)
            {
                mReader.close();
                mReader = null;
            }
        }
    }

    protected float getFontSize()
    {
        return Utils.spToPixels(ApplicationSettings.getFontSize(), mContext);
    }

    protected int getTabSize()
    {
        return ApplicationSettings.getTabSize();
    }

    public String getCommentLine()
    {
        return null;
    }

    public String getCommentLineEnd()
    {
        return null;
    }

    public Context getContext()
    {
        return mContext;
    }

    public void setCommentPaint(Paint paint)
    {
        mCommentPaint = paint;
    }

    public Paint getCommentPaint()
    {
        return mCommentPaint;
    }
}
