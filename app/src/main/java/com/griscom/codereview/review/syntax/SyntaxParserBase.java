package com.griscom.codereview.review.syntax;

import android.content.Context;
import android.graphics.Paint;
import android.support.annotation.Nullable;

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
import java.util.Locale;

/**
 * Base class for SyntaxParser
 */
@SuppressWarnings("WeakerAccess")
public abstract class SyntaxParserBase
{
    @SuppressWarnings("unused")
    private static final String TAG = "SyntaxParserBase";



    private Context        mContext      = null;
    private BufferedReader mReader       = null;
    private Paint          mCommentPaint = null;
    private final Object   mLock         = new Object();



    @Override
    public String toString()
    {
        BufferedReader reader;

        synchronized(mLock)
        {
            reader = mReader;
        }

        return "SyntaxParserBase{" +
                "mContext="        + mContext      +
                ", mReader="       + reader        +
                ", mCommentPaint=" + mCommentPaint +
                ", mLock="         + mLock         +
                '}';
    }

    /**
     * Creates SyntaxParserBase instance
     * @param context    context
     */
    @SuppressWarnings("ImplicitCallToSuper")
    protected SyntaxParserBase(Context context)
    {
        mContext      = context;
        mReader       = null;
        mCommentPaint = null;
    }

    /**
     * Creates SyntaxParser instance based on specified type or file name
     * @param filePath    path to file
     * @param context     context
     * @param type        syntax parser type
     * @return SyntaxParserBase instance
     */
    public static SyntaxParserBase createParserByType(String filePath, Context context, int type)
    {
        switch (type)
        {
            case SyntaxParserType.AUTOMATIC: return createParserByFileName(filePath, context);

            case SyntaxParserType.APOLLO:             return ApolloSyntaxParser.newInstance(context);
            case SyntaxParserType.BASH:               return BashSyntaxParser.newInstance(context);
            case SyntaxParserType.BASIC:              return BasicSyntaxParser.newInstance(context);
            case SyntaxParserType.CLOJURE:            return ClojureSyntaxParser.newInstance(context);
            case SyntaxParserType.C_PLUS_PLUS:        return CPlusPlusSyntaxParser.newInstance(context);
            case SyntaxParserType.C_SHARP:            return CSharpSyntaxParser.newInstance(context);
            case SyntaxParserType.CSS:                return CssSyntaxParser.newInstance(context);
            case SyntaxParserType.CSS_KW:             return CssKwSyntaxParser.newInstance(context);
            case SyntaxParserType.CSS_STR:            return CssStrSyntaxParser.newInstance(context);
            case SyntaxParserType.DART:               return DartSyntaxParser.newInstance(context);
            case SyntaxParserType.ERLANG:             return ErlangSyntaxParser.newInstance(context);
            case SyntaxParserType.GO:                 return GoSyntaxParser.newInstance(context);
            case SyntaxParserType.HASKELL:            return HaskellSyntaxParser.newInstance(context);
            case SyntaxParserType.JAVA:               return JavaSyntaxParser.newInstance(context);
            case SyntaxParserType.LISP:               return LispSyntaxParser.newInstance(context);
            case SyntaxParserType.LLVM:               return LlvmSyntaxParser.newInstance(context);
            case SyntaxParserType.LUA:                return LuaSyntaxParser.newInstance(context);
            case SyntaxParserType.MATLAB:             return MatlabSyntaxParser.newInstance(context);
            case SyntaxParserType.MATLAB_IDENTIFIERS: return MatlabIdentifiersSyntaxParser.newInstance(context);
            case SyntaxParserType.MATLAB_OPERATORS:   return MatlabOperatorsSyntaxParser.newInstance(context);
            case SyntaxParserType.ML:                 return MlSyntaxParser.newInstance(context);
            case SyntaxParserType.MUMPS:              return MumpsSyntaxParser.newInstance(context);
            case SyntaxParserType.N:                  return NSyntaxParser.newInstance(context);
            case SyntaxParserType.PASCAL:             return PascalSyntaxParser.newInstance(context);
            case SyntaxParserType.R:                  return RSyntaxParser.newInstance(context);
            case SyntaxParserType.RD:                 return RdSyntaxParser.newInstance(context);
            case SyntaxParserType.SCALA:              return ScalaSyntaxParser.newInstance(context);
            case SyntaxParserType.SQL:                return SqlSyntaxParser.newInstance(context);
            case SyntaxParserType.TCL:                return TclSyntaxParser.newInstance(context);
            case SyntaxParserType.TEX:                return TexSyntaxParser.newInstance(context);
            case SyntaxParserType.VHDL:               return VisualBasicSyntaxParser.newInstance(context);
            case SyntaxParserType.VISUAL_BASIC:       return VhdlSyntaxParser.newInstance(context);
            case SyntaxParserType.WIKI:               return WikiSyntaxParser.newInstance(context);
            case SyntaxParserType.XML:                return XmlSyntaxParser.newInstance(context);
            case SyntaxParserType.X_QUERY:            return XQuerySyntaxParser.newInstance(context);
            case SyntaxParserType.YAML:               return YamlSyntaxParser.newInstance(context);
            case SyntaxParserType.PLAIN_TEXT:         return PlainTextSyntaxParser.newInstance(context);

            default:
            {
                AppLog.wtf(TAG, "Unknown syntax parser type: " + type);
            }
        }

        return PlainTextSyntaxParser.newInstance(context);
    }

    /**
     * Creates SyntaxParser instance based on specified file name
     * @param path      path to file
     * @param context   context
     * @return SyntaxParserBase instance
     */
    private static SyntaxParserBase createParserByFileName(String path, Context context)
    {
        String filePath = path.toLowerCase(Locale.getDefault());

        int index = filePath.lastIndexOf('.');

        if (index > 0)
        {
            String extension = filePath.substring(index + 1);

            if (
                extension.equals("apollo")
                ||
                extension.equals("agc")
                ||
                extension.equals("aea")
               )
            {
                return ApolloSyntaxParser.newInstance(context);
            }

            if (extension.equals("sh"))
            {
                return BashSyntaxParser.newInstance(context);
            }

            if (
                extension.equals("basic")
                ||
                extension.equals("cbm")
               )
            {
                return BasicSyntaxParser.newInstance(context);
            }

            if (extension.equals("clj"))
            {
                return ClojureSyntaxParser.newInstance(context);
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
                return CPlusPlusSyntaxParser.newInstance(context);
            }

            if (extension.equals("cs"))
            {
                return CSharpSyntaxParser.newInstance(context);
            }

            if (extension.equals("css"))
            {
                return CssSyntaxParser.newInstance(context);
            }

            if (extension.equals("css-kw"))
            {
                return CssKwSyntaxParser.newInstance(context);
            }

            if (extension.equals("css-str"))
            {
                return CssStrSyntaxParser.newInstance(context);
            }

            if (extension.equals("dart"))
            {
                return DartSyntaxParser.newInstance(context);
            }

            if (
                extension.equals("erlang")
                ||
                extension.equals("erl")
               )
            {
                return ErlangSyntaxParser.newInstance(context);
            }

            if (extension.equals("go"))
            {
                return GoSyntaxParser.newInstance(context);
            }

            if (extension.equals("hs"))
            {
                return HaskellSyntaxParser.newInstance(context);
            }

            if (extension.equals("java"))
            {
                return JavaSyntaxParser.newInstance(context);
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
                return LispSyntaxParser.newInstance(context);
            }

            if (
                extension.equals("llvm")
                ||
                extension.equals("ll")
               )
            {
                return LlvmSyntaxParser.newInstance(context);
            }

            if (extension.equals("lua"))
            {
                return LuaSyntaxParser.newInstance(context);
            }

            if (extension.equals("matlab"))
            {
                return MatlabSyntaxParser.newInstance(context);
            }

            if (extension.equals("matlab-identifiers"))
            {
                return MatlabIdentifiersSyntaxParser.newInstance(context);
            }

            if (extension.equals("matlab-operators"))
            {
                return MatlabOperatorsSyntaxParser.newInstance(context);
            }

            if (
                extension.equals("fs")
                ||
                extension.equals("ml")
               )
            {
                return MlSyntaxParser.newInstance(context);
            }

            if (extension.equals("mumps"))
            {
                return MumpsSyntaxParser.newInstance(context);
            }

            if (
                extension.equals("n")
                ||
                extension.equals("nemerle")
               )
            {
                return NSyntaxParser.newInstance(context);
            }

            if (
                extension.equals("pas")
                ||
                extension.equals("pascal")
               )
            {
                return PascalSyntaxParser.newInstance(context);
            }

            if (
                extension.equals("r")
                ||
                extension.equals("s")
                ||
                extension.equals("splus")
               )
            {
                return RSyntaxParser.newInstance(context);
            }

            if (extension.equals("rd"))
            {
                return RdSyntaxParser.newInstance(context);
            }

            if (extension.equals("scala"))
            {
                return ScalaSyntaxParser.newInstance(context);
            }

            if (extension.equals("sql"))
            {
                return SqlSyntaxParser.newInstance(context);
            }

            if (extension.equals("tcl"))
            {
                return TclSyntaxParser.newInstance(context);
            }

            if (
                extension.equals("latex")
                ||
                extension.equals("tex")
                )
            {
                return TexSyntaxParser.newInstance(context);
            }

            if (
                extension.equals("vb")
                ||
                extension.equals("vbs")
                )
            {
                return VisualBasicSyntaxParser.newInstance(context);
            }

            if (
                extension.equals("vhdl")
                ||
                extension.equals("vhd")
                )
            {
                return VhdlSyntaxParser.newInstance(context);
            }

            if (filePath.endsWith(".wiki.meta"))
            {
                return WikiSyntaxParser.newInstance(context);
            }

            if (
                extension.equals("xml")
                ||
                extension.equals("html")
                ||
                extension.equals("ui")
               )
            {
                return XmlSyntaxParser.newInstance(context);
            }

            if (
                extension.equals("xq")
                ||
                extension.equals("xquery")
                )
            {
                return XQuerySyntaxParser.newInstance(context);
            }

            if (
                extension.equals("yaml")
                ||
                extension.equals("yml")
                )
            {
                return YamlSyntaxParser.newInstance(context);
            }
        }

        return PlainTextSyntaxParser.newInstance(context);
    }

    /**
     * Parses file and returns TextDocument instance with parsed syntax
     * @param filePath    path to file
     * @return TextDocument instance with parsed syntax
     */
    public abstract TextDocument parseFile(String filePath);

    /**
     * Creates file reader for specified file
     * @param filePath    path to file
     * @throws FileNotFoundException
     */
    protected void createReader(String filePath) throws FileNotFoundException
    {
        synchronized(mLock)
        {
            mReader = null;
            mReader = new BufferedReader(new InputStreamReader(new FileInputStream(filePath)));
        }
    }

    /**
     * Reads line from file reader
     * @return line from file reader
     * @throws IOException if impossible to read line from file reader
     */
    @Nullable
    protected String readLine() throws IOException
    {
        BufferedReader reader;

        synchronized(mLock)
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
                    res = new StringBuilder(0);
                }

                //noinspection NumericCastThatLosesPrecision
                res.append((char)oneChar);

                if (oneChar == '\n')
                {
                    return res.toString();
                }
            } while(true);
        }

        return null;
    }

    /**
     * Closes reader
     * @throws IOException if impossible to close reader
     */
    public void closeReader() throws IOException
    {
        synchronized(mLock)
        {
            if (mReader != null)
            {
                mReader.close();
                mReader = null;
            }
        }
    }

    /**
     * Gets font size
     * @return font size
     */
    protected float getFontSize()
    {
        return Utils.spToPixels(ApplicationSettings.getFontSize(), mContext);
    }

    /**
     * Gets tab size
     * @return tab size
     */
    protected int getTabSize()
    {
        return ApplicationSettings.getTabSize();
    }

    /**
     * Gets start line comment characters
     * @return start line comment characters
     */
    @Nullable
    public String getCommentLine()
    {
        return null;
    }

    /**
     * Gets end line comment characters
     * @return end line comment characters
     */
    @Nullable
    public String getCommentLineEnd()
    {
        return null;
    }

    /**
     * Gets context
     * @return context
     */
    public Context getContext()
    {
        return mContext;
    }

    /**
     * Sets comment paint
     * @param paint    comment paint
     */
    protected void setCommentPaint(Paint paint)
    {
        mCommentPaint = paint;
    }

    /**
     * Gets comment paint
     * @return comment paint
     */
    public Paint getCommentPaint()
    {
        return mCommentPaint;
    }
}
