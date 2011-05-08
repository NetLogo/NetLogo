package org.nlogo.api ;

public enum TokenType {
    EOF , OPEN_PAREN , CLOSE_PAREN , OPEN_BRACKET ,
    CLOSE_BRACKET , OPEN_BRACE , CLOSE_BRACE , CONSTANT , IDENT ,
    COMMAND , REPORTER , KEYWORD , COMMA , COMMENT ,
    VARIABLE , // built in variables only
    BAD , // characters the tokenizer couldn't digest
    LITERAL // a literal, untokened string (for external type dumps)
}
