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
    local filename=$1
    local parser=$2



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
