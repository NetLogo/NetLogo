// -*- Java -*- (tell Emacs to use Java mode)

package org.nlogo.agent ;

// Since this is automatically generated code it's not surprising
// it'd produce a few warnings - ST 3/6/08
@SuppressWarnings({"unused","fallthrough"})

%%

/* this option decreases code size; see JFlex documentation */
%switch

%unicode

%yylexthrow{
  ImportLexer.LexerException
%yylexthrow}

%{
  static String[] lex( String source )
    throws LexerException
  {
    ImportLexer yy = new ImportLexer( new java.io.StringReader( source + "," ) ) ;
    java.util.List<String> result = new java.util.ArrayList<String>() ;
    try
    {
      while( true )
      {
        String s = yy.yylex() ;
        if( s == null )
        {
          break ;
        }
        result.add( s ) ;
      }
    }
    // we should never get one of these since we're using StringReaders,
    // but the Yylex stuff uses generic BufferedReaders, so we have to
    // declare that we handle this exception
    catch( java.io.IOException ex )
    {
      throw new IllegalStateException( ex ) ;
    }
    return result.toArray( new String[ result.size() ] ) ;
  }

  static class LexerException
    extends Exception
  {
    public LexerException( String details )
    {
      super( details ) ;
    }
  }

  private static String unescape( String s )
  {
    if( s.indexOf( "\"\"" ) == -1 )
    {
      return s ;
    }
    StringBuilder result = new StringBuilder() ;
    for( int i = 0 ; i < s.length() ; i++ )
    {
      char c = s.charAt( i ) ;
      if( c == '"' && i < s.length() - 1 && s.charAt( i + 1 ) == '"' )
      {
        result.append( '"' ) ;
        i++ ;
      }
      else
      {
        result.append( c ) ;
      }
    }
    return result.toString() ;
  }
%}

%class ImportLexer
%type String
%state COMMA,QUOTED

NORMAL=[^,\"]
SPACE=[\ \t\b\012]
STRING_CONTENTS=([^\"]|\"\")

%%

<YYINITIAL> {
  , { return "" ; }
  {NORMAL}+ {
  yybegin( COMMA ) ;
  return yytext().trim() ;
  }
  {SPACE}*\" { yybegin( QUOTED ) ; }
}

<COMMA> {
  , { yybegin( YYINITIAL ) ; }
  . {
  throw new ImportLexer.LexerException
    ( "Quoted fields must be followed by comma or end of line" ) ;
  }
}

<QUOTED> {
  \"{SPACE}* {
    yybegin( COMMA ) ;
  return "" ;
  }
  {STRING_CONTENTS}+\"{SPACE}* {
  yybegin( COMMA ) ;
  String text = yytext() ;
  return unescape( text.substring( 0 , text.lastIndexOf( '"' ) ) ) ;
  }
  . {
    throw new ImportLexer.LexerException( "Unclosed double quote" ) ;
  }
}
