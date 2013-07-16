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
  private final String filename;
  private ExtensionLiteral extensionLiteral;
%}
/* this option decreases code size; see JFlex documentation */
%switch
%class TokenLexer
%ctorarg String filename
%init{
  this.filename = filename;
%init}
%unicode
%char
%type Token
%state EXTENSION_LITERAL

STRING_TEXT=(\\\"|\\r|\\n|\\t|\\\\|\\[^\"]|[^\r\n\"\\])*
NONNEWLINE_WHITE_SPACE_CHAR=[ \t\b\012]
LETTER=[:letter:]
DIGIT=[:digit:]
IDENTIFIER_CHAR={LETTER} | {DIGIT} | [_\.?=\*!<>:#\+/%\$\^\'&-]

%%

<YYINITIAL> \{\{ {
  yybegin(EXTENSION_LITERAL);
  extensionLiteral = new ExtensionLiteral(yychar, filename);
  extensionLiteral.add(yytext());
}

<EXTENSION_LITERAL> \}\} {
  extensionLiteral.add(yytext());
  if (extensionLiteral.nestingLevel() == 0) {
    yybegin(YYINITIAL);
    return extensionLiteral.done();
  }
  extensionLiteral.pop();
 }

<EXTENSION_LITERAL> \{\{ {
  extensionLiteral.push();
  extensionLiteral.add(yytext());
 }

<EXTENSION_LITERAL> . {
  extensionLiteral.add(yytext());
}

<EXTENSION_LITERAL> \n|\r {
  yybegin(YYINITIAL);
  return new Token("", TokenTypeJ.Bad(), "End of line reached unexpectedly",
            yychar, yychar, filename);
}

<EXTENSION_LITERAL> <<EOF>> {
  yybegin(YYINITIAL);
  return new Token("", TokenTypeJ.Bad(), "End of file reached unexpectedly",
            yychar, yychar, filename);
}


<YYINITIAL> "," { return new Token(yytext(), TokenTypeJ.Comma()       , null, yychar, yychar + 1, filename); }
<YYINITIAL> "{" { return new Token(yytext(), TokenTypeJ.OpenBrace()   , null, yychar, yychar + 1, filename); }
<YYINITIAL> "}" { return new Token(yytext(), TokenTypeJ.CloseBrace()  , null, yychar, yychar + 1, filename); }
<YYINITIAL> "[" { return new Token(yytext(), TokenTypeJ.OpenBracket() , null, yychar, yychar + 1, filename); }
<YYINITIAL> "]" { return new Token(yytext(), TokenTypeJ.CloseBracket(), null, yychar, yychar + 1, filename); }
<YYINITIAL> "(" { return new Token(yytext(), TokenTypeJ.OpenParen()   , null, yychar, yychar + 1, filename); }
<YYINITIAL> ")" { return new Token(yytext(), TokenTypeJ.CloseParen()  , null, yychar, yychar + 1, filename); }

<YYINITIAL> {NONNEWLINE_WHITE_SPACE_CHAR}+ { }

<YYINITIAL> \n|\r { }

<YYINITIAL>;.* {
  String text = yytext();
  return new Token(text, TokenTypeJ.Comment(), null,
            yychar, yychar + text.length(), filename);
}

<YYINITIAL> -?\.?[0-9]{IDENTIFIER_CHAR}* {
  String text = yytext();
  scala.util.Either<String, Double> result = org.nlogo.api.NumberParser.parse(text);
  TokenType resultType =
    result.isLeft() ? TokenTypeJ.Bad() : TokenTypeJ.Literal();
  Object resultValue =
    result.isLeft() ? result.left().get() : result.right().get();
  return new Token(
    text, resultType, resultValue,
    yychar, yychar + text.length(), filename);
}

<YYINITIAL> {IDENTIFIER_CHAR}+ {
  String text = yytext();
  return new Token(text, TokenTypeJ.Ident(), text.toUpperCase(),
                   yychar, yychar + text.length(), filename);
}

<YYINITIAL> \"{STRING_TEXT}\" {
  String text = yytext();
  try {
    return new Token
      (text, TokenTypeJ.Literal(),
        org.nlogo.api.StringUtils.unescapeString(text.substring(1, text.length() - 1)),
        yychar, yychar + text.length(), filename);
  }
  catch(IllegalArgumentException ex) {
    return new Token(text, TokenTypeJ.Bad(), "Illegal character after backslash",
              yychar, yychar + text.length(), filename);
  }
}

<YYINITIAL> \"{STRING_TEXT} {
  String text = yytext();
  return new Token(text, TokenTypeJ.Bad(), "Closing double quote is missing",
            yychar, yychar + yytext().length(), filename);
}

. {
  String text = yytext();
  return new Token(text, TokenTypeJ.Bad(), "This non-standard character is not allowed.",
            yychar, yychar + 1, filename);
}
