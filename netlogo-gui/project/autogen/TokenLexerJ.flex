// -*- Java -*- (tell Emacs to use Java mode)
package org.nlogo.lex ;

import org.nlogo.core.Token;
import org.nlogo.core.TokenType;
import org.nlogo.core.TokenTypeJ$;

// Mostly the following spec is reasonably straightforward.
// There is some slight weirdness about supporting the triple-underscore
// syntax, the one that lets you type ___foo as a shorthand for
// __magic-open "foo".  This is handled by the MAGIC-OPEN state
// with the aid of the distinction between
// IdentIFIER_CHAR_NOT_UNDERSCORE and IDENTIFIER_CHAR. - ST 2/5/07

// Since this is automatically generated code it's not surprising
// it'd produce a few warnings - ST 3/6/08
@SuppressWarnings({"unused","fallthrough"})

%%

%{
  private final TokenMapper tokenMapper ;
  private final String filename ;
  private final boolean allowRemovedPrimitives ;
  private StringBuilder literalBuilder = null ;
  private int literalStart = -1 ;
  private int literalNestingLevel = 0 ;

  // this is very annoying, but I can't figure out any other way to
  // get at the Scala inner objects from Java - ST 7/7/11
  private static final TokenType TokenType_Eof = getTokenType("Eof");
  private static final TokenType TokenType_OpenParen = getTokenType("OpenParen");
  private static final TokenType TokenType_CloseParen = getTokenType("CloseParen");
  private static final TokenType TokenType_OpenBracket = getTokenType("OpenBracket");
  private static final TokenType TokenType_CloseBracket = getTokenType("CloseBracket");
  private static final TokenType TokenType_OpenBrace = getTokenType("OpenBrace");
  private static final TokenType TokenType_CloseBrace = getTokenType("CloseBrace");
  private static final TokenType TokenType_Literal = getTokenType("Literal");
  private static final TokenType TokenType_Ident = getTokenType("Ident");
  private static final TokenType TokenType_Command = getTokenType("Command");
  private static final TokenType TokenType_Reporter = getTokenType("Reporter");
  private static final TokenType TokenType_Keyword = getTokenType("Keyword");
  private static final TokenType TokenType_COMMA = getTokenType("Comma");
  private static final TokenType TokenType_Comment = getTokenType("Comment");
  private static final TokenType TokenType_Bad = getTokenType("Bad");
  private static final TokenType TokenType_Extension = getTokenType("Extension");

  private static TokenType getTokenType(String name) {
    try {
      return (TokenType) Class.forName("org.nlogo.core.TokenType$" + name + "$").getField("MODULE$").get(null);
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

  void beginLiteral()
  {
    literalStart = yychar ;
    literalBuilder = new StringBuilder() ;
  }

  void addToLiteral()
  {
    literalBuilder.append( yytext() ) ;
  }

  Token endLiteral()
  {
    String text = literalBuilder.toString() ;
    literalBuilder = null ;
    return new Token( text , TokenType_Extension , text ,
              literalStart , literalStart + text.length() , filename ) ;
  }

  Token ident()
  {
    String text = yytext() ;
    if( tokenMapper.isKeyword( text ) )
    {
      return new Token( text , TokenType_Keyword , text.toUpperCase() ,
                yychar , yychar + text.length() , filename ) ;
    }
    else if( tokenMapper.isCommand( text.toUpperCase() )
         && ( allowRemovedPrimitives ||
            ! tokenMapper.wasRemoved( text.toUpperCase() ) ) )
    {
      org.nlogo.core.TokenHolder instr = tokenMapper.getCommand( text ) ;
      Token tok = new Token( text , TokenTypeJ$.MODULE$.Command() , instr ,
                   yychar, yychar + text.length() , filename ) ;
      instr.token_$eq( tok ) ;
      return tok ;
    }
    else if( tokenMapper.isReporter( text )
         && ( allowRemovedPrimitives ||
            ! tokenMapper.wasRemoved( text.toUpperCase() ) ) )
    {
      org.nlogo.core.TokenHolder instr = tokenMapper.getReporter( text ) ;
      Token tok = new Token( text , TokenType_Reporter , instr ,
                   yychar , yychar + text.length() , filename ) ;
      instr.token_$eq( tok ) ;
      return tok ;
    }
    else if( tokenMapper.isVariable( text ) )
    {
      return new Token( text , TokenType_Ident , text.toUpperCase() ,
                yychar , yychar + text.length() , filename ) ;
    }
    else if( tokenMapper.isConstant( text ) )
    {
      return new Token( text , TokenType_Literal , tokenMapper.getConstant( text ) ,
                yychar , yychar + text.length() , filename ) ;
    }
    else
    {
      return new Token( text , TokenType_Ident , text.toUpperCase() ,
                yychar , yychar + text.length() , filename ) ;
    }
  }
%}
/* this option decreases code size; see JFlex documentation */
%switch
%class TokenLexerJ
%ctorarg TokenMapper tokenMapper
%ctorarg String filename
%ctorarg boolean allowRemovedPrimitives
%init{
  this.tokenMapper = tokenMapper ;
  this.filename = filename ;
  this.allowRemovedPrimitives = allowRemovedPrimitives ;
%init}
%unicode
%char
%type Token
%state MAGIC_OPEN
%state LITERAL

STRING_TEXT=(\\\"|\\r|\\n|\\t|\\\\|\\[^\"]|[^\r\n\"\\])*
NONNEWLINE_WHITE_SPACE_CHAR=[ \t\b\012]
LETTER=[:letter:]
DIGIT=[:digit:]
IDENTIFIER_CHAR_NOT_UNDERSCORE={LETTER} | {DIGIT} | [\.?=\*!<>:#\+/%\$\^\'&-]
IDENTIFIER_CHAR={IDENTIFIER_CHAR_NOT_UNDERSCORE} | _

%%

<YYINITIAL> \{\{ {
  yybegin( LITERAL ) ;
  beginLiteral() ;
  addToLiteral() ;
  literalNestingLevel = 0 ;
}

<LITERAL> \}\} {
  addToLiteral() ;
  if( literalNestingLevel == 0 )
  {
    yybegin( YYINITIAL ) ;
    return endLiteral() ;
  }
  literalNestingLevel-- ;
 }

<LITERAL> \{\{ {
  literalNestingLevel++ ;
  addToLiteral() ;
 }

<LITERAL> . {
  addToLiteral() ;
}

<LITERAL> \n|\r {
  yybegin( YYINITIAL ) ;
  return new Token( "" , TokenType_Bad , "End of line reached unexpectedly" ,
            yychar , yychar , filename ) ;
}

<LITERAL> <<EOF>> {
  yybegin( YYINITIAL ) ;
  return new Token( "" , TokenType_Bad , "End of file reached unexpectedly" ,
            yychar , yychar , filename ) ;
}


<YYINITIAL> "," { return new Token( yytext() , TokenType_COMMA         , null , yychar , yychar + 1, filename ) ; }
<YYINITIAL> "{" { return new Token( yytext() , TokenType_OpenBrace    , null , yychar , yychar + 1, filename ) ; }
<YYINITIAL> "}" { return new Token( yytext() , TokenType_CloseBrace   , null , yychar , yychar + 1, filename ) ; }
<YYINITIAL> "[" { return new Token( yytext() , TokenType_OpenBracket  , null , yychar , yychar + 1, filename ) ; }
<YYINITIAL> "]" { return new Token( yytext() , TokenType_CloseBracket , null , yychar , yychar + 1, filename ) ; }
<YYINITIAL> "(" { return new Token( yytext() , TokenType_OpenParen    , null , yychar , yychar + 1, filename ) ; }
<YYINITIAL> ")" { return new Token( yytext() , TokenType_CloseParen   , null , yychar , yychar + 1, filename ) ; }

<YYINITIAL> {NONNEWLINE_WHITE_SPACE_CHAR}+ { }

<YYINITIAL> \n|\r { }

<YYINITIAL> ;.* {
  String text = yytext() ;
  return new Token( text , TokenType_Comment , null ,
            yychar , yychar + text.length() , filename ) ;
}

<YYINITIAL> -?\.?[0-9]{IDENTIFIER_CHAR}* {
  String text = yytext() ;
  scala.util.Either<String, Double> result = org.nlogo.api.NumberParser.parse( text ) ;
  TokenType resultType =
    result.isLeft() ? TokenType_Bad : TokenType_Literal ;
  Object resultValue =
    result.isLeft() ? result.left().get() : result.right().get() ;
  return new Token(
    text , resultType , resultValue ,
    yychar , yychar + text.length() , filename ) ;
}

<YYINITIAL> ___ {
  yybegin( MAGIC_OPEN ) ;
  org.nlogo.core.TokenHolder cmd = tokenMapper.getCommand( "__magic-open" ) ;
  Token token = new Token( "__magic-open" , TokenTypeJ$.MODULE$.Command() , cmd , yychar , yychar + 3 , filename ) ;
  cmd.token_$eq( token ) ;
  return token ;
}

<MAGIC_OPEN> \n {
  yybegin( YYINITIAL ) ;
}
<MAGIC_OPEN> {IDENTIFIER_CHAR}* {
  String text = yytext() ;
  yybegin( YYINITIAL ) ;
  return new Token
    ( "\"" + text + "\"" , TokenType_Literal , text ,
      yychar , yychar + text.length() , filename ) ;
}

<YYINITIAL> {IDENTIFIER_CHAR_NOT_UNDERSCORE}{IDENTIFIER_CHAR}* {
  return ident() ;
}

<YYINITIAL> _{IDENTIFIER_CHAR_NOT_UNDERSCORE}{IDENTIFIER_CHAR}* {
  return ident() ;
}

<YYINITIAL> __{IDENTIFIER_CHAR_NOT_UNDERSCORE}{IDENTIFIER_CHAR}* {
  return ident() ;
}

<YYINITIAL> \"{STRING_TEXT}\" {
  String text = yytext() ;
  try
  {
    return new Token
      ( text , TokenType_Literal ,
        org.nlogo.api.StringUtils.unEscapeString( text.substring( 1 , text.length() - 1 ) ) ,
        yychar , yychar + text.length() , filename ) ;
  }
  catch( IllegalArgumentException ex )
  {
    return new Token( text , TokenType_Bad , "Illegal character after backslash" ,
              yychar , yychar + text.length() , filename ) ;
  }
}

<YYINITIAL> \"{STRING_TEXT} {
  String text = yytext() ;
  return new Token( text , TokenType_Bad , "Closing double quote is missing" ,
            yychar , yychar + yytext().length() , filename ) ;
}

. {
  String text = yytext() ;
  return new Token( text , TokenType_Bad , "This non-standard character is not allowed." ,
            yychar , yychar + 1 , filename ) ;
}
