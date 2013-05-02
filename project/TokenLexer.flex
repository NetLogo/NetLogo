// -*- Java -*- (tell Emacs to use Java mode)
package org.nlogo.lex;

import org.nlogo.api.Token;
import org.nlogo.api.TokenHolder;
import org.nlogo.api.TokenType;

// Since this is automatically generated code it's not surprising
// it'd produce a few warnings - ST 3/6/08
@SuppressWarnings({"unused","fallthrough"})

%%

%{
  private final String fileName;
  private StringBuilder literalBuilder = null;
  private int literalStart = -1;
  private int literalNestingLevel = 0;

  // this is very annoying, but I can't figure out any other way to
  // get at the Scala inner objects from Java - ST 7/7/11
  private static final TokenType TokenType_EOF = getTokenType("EOF");
  private static final TokenType TokenType_OpenParen = getTokenType("OpenParen");
  private static final TokenType TokenType_CloseParen = getTokenType("CloseParen");
  private static final TokenType TokenType_OpenBracket = getTokenType("OpenBracket");
  private static final TokenType TokenType_CloseBracket = getTokenType("CloseBracket");
  private static final TokenType TokenType_OpenBrace = getTokenType("OpenBrace");
  private static final TokenType TokenType_CloseBrace = getTokenType("CloseBrace");
  private static final TokenType TokenType_Constant = getTokenType("Constant");
  private static final TokenType TokenType_Ident = getTokenType("Ident");
  private static final TokenType TokenType_Keyword = getTokenType("Keyword");
  private static final TokenType TokenType_Comma = getTokenType("Comma");
  private static final TokenType TokenType_Comment = getTokenType("Comment");
  private static final TokenType TokenType_Variable = getTokenType("Variable");
  private static final TokenType TokenType_Bad = getTokenType("Bad");
  private static final TokenType TokenType_Literal = getTokenType("Literal");

  private static TokenType getTokenType(String name) {
    try {
      return (TokenType)
        Class.forName("org.nlogo.api.TokenType$" + name + "$")
        .getField("MODULE$").get(null);
    }
    catch(IllegalAccessException ex) {
      throw new IllegalStateException(ex);
    }
    catch(NoSuchFieldException ex) {
      throw new IllegalStateException(ex);
    }
    catch(ClassNotFoundException ex) {
      throw new IllegalStateException(ex);
    }
  }

  void beginLiteral() {
    literalStart = yychar;
    literalBuilder = new StringBuilder();
  }

  void addToLiteral() {
    literalBuilder.append(yytext());
  }

  Token endLiteral() {
    String text = literalBuilder.toString();
    literalBuilder = null;
    return new Token(text, TokenType_Literal, text,
              literalStart, literalStart + text.length(), fileName);
  }

  Token ident() {
    String text = yytext();
    if (Keywords.isKeyword(text)) {
      return new Token(text, TokenType_Keyword, text.toUpperCase(),
                yychar, yychar + text.length(), fileName);
    }
    else if (Variables.isVariable(text)) {
      return new Token(text, TokenType_Variable, text.toUpperCase(),
                yychar, yychar + text.length(), fileName);
    }
    else if (Constants.isConstant(text)) {
      return new Token(text, TokenType_Constant, Constants.get(text),
                yychar, yychar + text.length(), fileName);
    }
    else {
      return new Token(text, TokenType_Ident, text.toUpperCase(),
                yychar, yychar + text.length(), fileName);
    }
  }
%}
/* this option decreases code size; see JFlex documentation */
%switch
%class TokenLexer
%ctorarg String fileName
%init{
  this.fileName = fileName;
%init}
%unicode
%char
%type Token
%state LITERAL

STRING_TEXT=(\\\"|\\r|\\n|\\t|\\\\|\\[^\"]|[^\r\n\"\\])*
NONNEWLINE_WHITE_SPACE_CHAR=[ \t\b\012]
LETTER=[:letter:]
DIGIT=[:digit:]
IDENTIFIER_CHAR={LETTER} | {DIGIT} | [_\.?=\*!<>:#\+/%\$\^\'&-]

%%

<YYINITIAL> \{\{ {
  yybegin(LITERAL);
  beginLiteral();
  addToLiteral();
  literalNestingLevel = 0;
}

<LITERAL> \}\} {
  addToLiteral();
  if (literalNestingLevel == 0) {
    yybegin(YYINITIAL);
    return endLiteral();
  }
  literalNestingLevel--;
 }

<LITERAL> \{\{ {
  literalNestingLevel++;
  addToLiteral();
 }

<LITERAL> . {
  addToLiteral();
}

<LITERAL> \n|\r {
  yybegin(YYINITIAL);
  return new Token("", TokenType_Bad, "End of line reached unexpectedly",
            yychar, yychar, fileName);
}

<LITERAL> <<EOF>> {
  yybegin(YYINITIAL);
  return new Token("", TokenType_Bad, "End of file reached unexpectedly",
            yychar, yychar, fileName);
}


<YYINITIAL> "," { return new Token(yytext(), TokenType_Comma        , null, yychar, yychar + 1, fileName); }
<YYINITIAL> "{" { return new Token(yytext(), TokenType_OpenBrace   , null, yychar, yychar + 1, fileName); }
<YYINITIAL> "}" { return new Token(yytext(), TokenType_CloseBrace  , null, yychar, yychar + 1, fileName); }
<YYINITIAL> "[" { return new Token(yytext(), TokenType_OpenBracket , null, yychar, yychar + 1, fileName); }
<YYINITIAL> "]" { return new Token(yytext(), TokenType_CloseBracket, null, yychar, yychar + 1, fileName); }
<YYINITIAL> "(" { return new Token(yytext(), TokenType_OpenParen   , null, yychar, yychar + 1, fileName); }
<YYINITIAL> ")" { return new Token(yytext(), TokenType_CloseParen  , null, yychar, yychar + 1, fileName); }

<YYINITIAL> {NONNEWLINE_WHITE_SPACE_CHAR}+ { }

<YYINITIAL> \n|\r { }

<YYINITIAL>;.* {
  String text = yytext();
  return new Token(text, TokenType_Comment, null,
            yychar, yychar + text.length(), fileName);
}

<YYINITIAL> -?\.?[0-9]{IDENTIFIER_CHAR}* {
  String text = yytext();
  scala.util.Either<String, Double> result = org.nlogo.api.NumberParser.parse(text);
  TokenType resultType =
    result.isLeft() ? TokenType_Bad : TokenType_Constant;
  Object resultValue =
    result.isLeft() ? result.left().get() : result.right().get();
  return new Token(
    text, resultType, resultValue,
    yychar, yychar + text.length(), fileName);
}

<YYINITIAL> {IDENTIFIER_CHAR}+ {
  return ident();
}

<YYINITIAL> \"{STRING_TEXT}\" {
  String text = yytext();
  try {
    return new Token
      (text, TokenType_Constant,
        org.nlogo.api.StringUtils.unEscapeString(text.substring(1, text.length() - 1)),
        yychar, yychar + text.length(), fileName);
  }
  catch(IllegalArgumentException ex) {
    return new Token(text, TokenType_Bad, "Illegal character after backslash",
              yychar, yychar + text.length(), fileName);
  }
}

<YYINITIAL> \"{STRING_TEXT} {
  String text = yytext();
  return new Token(text, TokenType_Bad, "Closing double quote is missing",
            yychar, yychar + yytext().length(), fileName);
}

. {
  String text = yytext();
  return new Token(text, TokenType_Bad, "This non-standard character is not allowed.",
            yychar, yychar + 1, fileName);
}
