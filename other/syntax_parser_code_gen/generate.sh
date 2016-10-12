#!/bin/bash



##########################################################
#                       Parameters
##########################################################



SYNTAX_PARSERS_DIR=../../app/src/main/java/com/griscom/codereview/review/syntax

SYNTAX_PARSERS=(
    Apollo
    Bash
    Basic
    Clojure
    CPlusPlus
    CSharp
    CssKw
    CssStr
    Css
    Dart
    Erlang
    Go
    Haskell
    Java
    Lisp
    Llvm
    Lua
    MatlabIdentifiers
    MatlabOperators
    Matlab
    Ml
    Mumps
    N
    Pascal
    Rd
    R
    Scala
    Sql
    Tcl
    Tex
    Vhdl
    VisualBasic
    Wiki
    Xml
    XQuery
    Yaml
    PlainText
)

SYNTAX_PARSERS_NAMES=(
    "Apollo"
    "Bash"
    "Basic"
    "Clojure"
    "C++"
    "C#"
    "CSS"
    "CSS KW"
    "CSS STR"
    "Dart"
    "Erlang"
    "Go"
    "Haskell"
    "Java"
    "LISP"
    "LLVM"
    "LUA"
    "MATLAB"
    "MATLAB Identifiers"
    "MATLAB Operators"
    "ML"
    "MUMPS"
    "N"
    "Pascal"
    "R"
    "RD"
    "Scala"
    "SQL"
    "Tcl"
    "TEX"
    "VHDL"
    "Visual Basic"
    "Wiki"
    "XML"
    "X Query"
    "YAML"
    "Plain Text"
)



##########################################################
#                       Functions
##########################################################



function generateSyntaxParser {
    local parser=$1
    local parser_name=$2

    local target_file=${SYNTAX_PARSERS_DIR}/${parser}SyntaxParser.java



    echo "package com.griscom.codereview.review.syntax;"                                                                       >  ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "import android.content.Context;"                                                                                     >> ${target_file}
    echo "import android.graphics.Color;"                                                                                      >> ${target_file}
    echo "import android.graphics.Paint;"                                                                                      >> ${target_file}
    echo "import android.graphics.Typeface;"                                                                                   >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "import com.griscom.codereview.review.TextDocument;"                                                                  >> ${target_file}
    echo "import com.griscom.codereview.review.TextRegion;"                                                                    >> ${target_file}
    echo "import com.griscom.codereview.review.TextRow;"                                                                       >> ${target_file}
    echo "import com.griscom.codereview.util.AppLog;"                                                                          >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "import java.util.HashMap;"                                                                                           >> ${target_file}
    echo "import java.util.List;"                                                                                              >> ${target_file}
    echo "import java.util.Map;"                                                                                               >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "import prettify.PrettifyParser;"                                                                                     >> ${target_file}
    echo "import prettify.parser.Prettify;"                                                                                    >> ${target_file}
    echo "import syntaxhighlight.ParseResult;"                                                                                 >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "public class BashSyntaxParser extends SyntaxParserBase"                                                              >> ${target_file}
    echo "{"                                                                                                                   >> ${target_file}
    echo "    private static final String TAG = \"BashSyntaxParser\";"                                                         >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "    public BashSyntaxParser(Context context)"                                                                        >> ${target_file}
    echo "    {"                                                                                                               >> ${target_file}
    echo "        super(context);"                                                                                             >> ${target_file}
    echo "    }"                                                                                                               >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "    @Override"                                                                                                       >> ${target_file}
    echo "    public TextDocument parseFile(String fileName)"                                                                  >> ${target_file}
    echo "    {"                                                                                                               >> ${target_file}
    echo "        TextDocument res = new TextDocument(this);"                                                                  >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "        try"                                                                                                         >> ${target_file}
    echo "        {"                                                                                                           >> ${target_file}
    echo "            Paint basePaint = new Paint();"                                                                          >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            basePaint.setColor(Color.BLACK);"                                                                        >> ${target_file}
    echo "            basePaint.setTypeface(Typeface.MONOSPACE);"                                                              >> ${target_file}
    echo "            basePaint.setTextSize(getFontSize());"                                                                   >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            Paint keywordPaint     = new Paint(basePaint);"                                                          >> ${target_file}
    echo "            Paint typePaint        = new Paint(basePaint);"                                                          >> ${target_file}
    echo "            Paint literalPaint     = new Paint(basePaint);"                                                          >> ${target_file}
    echo "            mCommentPaint          = new Paint(basePaint);"                                                          >> ${target_file}
    echo "            Paint stringPaint      = new Paint(basePaint);"                                                          >> ${target_file}
    echo "            Paint punctuationPaint = new Paint(basePaint);"                                                          >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            keywordPaint.setColor       (Color.rgb(150, 0,   85));"                                                  >> ${target_file}
    echo "            keywordPaint.setTypeface    (Typeface.create(Typeface.MONOSPACE, Typeface.BOLD));"                       >> ${target_file}
    echo "            keywordPaint.setFakeBoldText(true);"                                                                     >> ${target_file}
    echo "            mCommentPaint.setColor      (Color.rgb(64,  128, 100));"                                                 >> ${target_file}
    echo "            stringPaint.setColor        (Color.rgb(0,   0,   192));"                                                 >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            Map<String, Paint> colorsMap = new HashMap<String, Paint>();"                                            >> ${target_file}
    echo "            colorsMap.put(Prettify.PR_KEYWORD,     keywordPaint);"                                                   >> ${target_file}
    echo "            colorsMap.put(Prettify.PR_TYPE,        typePaint);"                                                      >> ${target_file}
    echo "            colorsMap.put(Prettify.PR_LITERAL,     literalPaint);"                                                   >> ${target_file}
    echo "            colorsMap.put(Prettify.PR_COMMENT,     mCommentPaint);"                                                  >> ${target_file}
    echo "            colorsMap.put(Prettify.PR_STRING,      stringPaint);"                                                    >> ${target_file}
    echo "            colorsMap.put(Prettify.PR_PUNCTUATION, punctuationPaint);"                                               >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            // ---------------------------------------------------------------"                                      >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            StringBuilder codeBuilder = new StringBuilder();"                                                        >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            createReader(fileName);"                                                                                 >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            String line;"                                                                                            >> ${target_file}
    echo "            while ((line = readLine()) != null)"                                                                     >> ${target_file}
    echo "            {"                                                                                                       >> ${target_file}
    echo "                codeBuilder.append(line);"                                                                           >> ${target_file}
    echo "            }"                                                                                                       >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            closeReader();"                                                                                          >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            // ---------------------------------------------------------------"                                      >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            int tabSize = getTabSize();"                                                                             >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            String sourceCode = codeBuilder.toString();"                                                             >> ${target_file}
    echo "            List<ParseResult> results = new PrettifyParser().parse(\"sh\", sourceCode);"                             >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            TextRow row = null;"                                                                                     >> ${target_file}
    echo "            int curColumn = 0;"                                                                                      >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            for (ParseResult result : results)"                                                                      >> ${target_file}
    echo "            {"                                                                                                       >> ${target_file}
    echo "                if (row == null)"                                                                                    >> ${target_file}
    echo "                {"                                                                                                   >> ${target_file}
    echo "                    row = new TextRow();"                                                                            >> ${target_file}
    echo "                }"                                                                                                   >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "                String type    = result.getStyleKeys().get(0);"                                                      >> ${target_file}
    echo "                String content = sourceCode.substring(result.getOffset(), result.getOffset() + result.getLength());" >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "                Paint selectedPaint = colorsMap.get(type);"                                                          >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "                if (selectedPaint == null)"                                                                          >> ${target_file}
    echo "                {"                                                                                                   >> ${target_file}
    echo "                    if (!type.equals(Prettify.PR_PLAIN))"                                                            >> ${target_file}
    echo "                    {"                                                                                               >> ${target_file}
    echo "                        AppLog.wtf(TAG, \"Unhandled syntax type: \" + type);"                                        >> ${target_file}
    echo "                    }"                                                                                               >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "                    selectedPaint = basePaint;"                                                                      >> ${target_file}
    echo "                }"                                                                                                   >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "                boolean lastEnter = content.endsWith(\"\\n\");"                                                      >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "                do"                                                                                                  >> ${target_file}
    echo "                {"                                                                                                   >> ${target_file}
    echo "                    int index = content.indexOf(\'\\n\');"                                                           >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "                    if (index < 0)"                                                                                  >> ${target_file}
    echo "                    {"                                                                                               >> ${target_file}
    echo "                        break;"                                                                                      >> ${target_file}
    echo "                    }"                                                                                               >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "                    String contentPart = content.substring(0, index);"                                               >> ${target_file}
    echo "                    content = content.substring(index + 1);"                                                         >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "                    row.addTextRegion(new TextRegion(contentPart, selectedPaint, curColumn, tabSize));"              >> ${target_file}
    echo "                    res.addTextRow(row);"                                                                            >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "                    row = new TextRow();"                                                                            >> ${target_file}
    echo "                    curColumn = 0;"                                                                                  >> ${target_file}
    echo "                } while (true);"                                                                                     >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "                if (lastEnter || !content.equals(\"\"))"                                                             >> ${target_file}
    echo "                {"                                                                                                   >> ${target_file}
    echo "                    row.addTextRegion(new TextRegion(content, selectedPaint, curColumn, tabSize));"                  >> ${target_file}
    echo "                    curColumn += content.length();"                                                                  >> ${target_file}
    echo "                }"                                                                                                   >> ${target_file}
    echo "            }"                                                                                                       >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "            if (row != null && row.hasRegions())"                                                                    >> ${target_file}
    echo "            {"                                                                                                       >> ${target_file}
    echo "                res.addTextRow(row);"                                                                                >> ${target_file}
    echo "            }"                                                                                                       >> ${target_file}
    echo "        }"                                                                                                           >> ${target_file}
    echo "        catch (Exception e)"                                                                                         >> ${target_file}
    echo "        {"                                                                                                           >> ${target_file}
    echo "            AppLog.e(TAG, \"Impossible to read file: \" + fileName, e);"                                             >> ${target_file}
    echo "        }"                                                                                                           >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "        return res;"                                                                                                 >> ${target_file}
    echo "    }"                                                                                                               >> ${target_file}
    echo ""                                                                                                                    >> ${target_file}
    echo "    @Override"                                                                                                       >> ${target_file}
    echo "    public String getCommentLine()"                                                                                  >> ${target_file}
    echo "    {"                                                                                                               >> ${target_file}
    echo "        return \"#\";"                                                                                               >> ${target_file}
    echo "    }"                                                                                                               >> ${target_file}
    echo "}"                                                                                                                   >> ${target_file}



    return 0
}



##########################################################
#                       Processing
##########################################################



SYNTAX_PARSERS_COUNT=${#SYNTAX_PARSERS[@]}

if [ ${SYNTAX_PARSERS_COUNT} -ne ${#SYNTAX_PARSERS_NAMES[@]} ]; then
    echo "Incorrect number of parsers"

    exit 1
fi



for i in `seq 0 $((${SYNTAX_PARSERS_COUNT} - 1))`
do
    generateSyntaxParser "${SYNTAX_PARSERS[$i]}" "${SYNTAX_PARSERS_NAMES[$i]}"
done



exit 0
