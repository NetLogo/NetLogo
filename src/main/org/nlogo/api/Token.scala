// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// What kind of object is stored in the value slot is will vary depending on the token type.  If the
// type is COMMAND or REPORTER, a Command or Reporter object will be stored.  If the type is
// CONSTANT, the value of the constant is stored (a String or Integer or Double or LogoList or
// whatever).  And so on.  For some tokens, it will be null, for example for OPEN_BRACE and so on.

// There are two argument lists because we won't need to pattern match on the position or filename;
// they're auxiliary information.  Also we don't want them in the toString output either since it
// makes test cases annoying to write.

case class Token(name: String, tpe: TokenType, value: AnyRef)
                (val startPos: Int, val endPos: Int, val fileName: String)
object Token {
  val eof = new Token("", TokenType.EOF, "")(0, 0, "")
}
