package com.griscom.codereview.review.syntax;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.annotation.SuppressLint;
import android.content.Context;

import com.griscom.codereview.other.ApplicationSettings;
import com.griscom.codereview.review.TextDocument;
import com.griscom.codereview.util.Utils;
import android.graphics.*;

@SuppressLint("DefaultLocale")
public abstract class SyntaxParserBase
{
    protected Context        mContext;
    protected BufferedReader mReader;
	protected Paint          mCommentPaint;



    protected SyntaxParserBase(Context context)
    {
        mContext      = context;
        mReader       = null;
		mCommentPaint = null;
    }

    public static SyntaxParserBase createParserByFileName(String fileName, Context context)
    {
		fileName=fileName.toLowerCase();
		
        int index=fileName.lastIndexOf('.');

        if (index>0)
        {
            String extension=fileName.substring(index+1);
            
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
                return new CljSyntaxParser(context);
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
                return new HsSyntaxParser(context);
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
                return new XqSyntaxParser(context);
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
            mReader=null;
            mReader=new BufferedReader(new InputStreamReader(new FileInputStream(fileName)));
        }
    }

    protected String readLine() throws IOException
    {
        BufferedReader reader;

        synchronized (this)
        {
            reader=mReader;
        }

        if (reader!=null)
        {
            return reader.readLine();
        }

        return null;
    }

    public void closeReader() throws IOException
    {
        synchronized (this)
        {
            if (mReader!=null)
            {
                mReader.close();
                mReader=null;
            }
        }
    }

    protected float getFontSize()
    {
        return Utils.spToPixels(ApplicationSettings.fontSize(mContext), mContext);
    }

    protected int getTabSize()
    {
        return ApplicationSettings.tabSize(mContext);
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
	
	public Paint getCommentPaint()
	{
		return mCommentPaint;
	}
}
