// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

// What kind of object is stored in the value slot is will vary depending on the token type.  If the
// type is COMMAND or REPORTER, a Command or Reporter object will be stored.  If the type is
// Literal, the value of the literal is stored (a String or Integer or Double or LogoList or
// whatever).  And so on.  For some tokens, it will be null, for example for OpenBrace and so on.

// There are two argument lists because we won't need to pattern match on the position or filename;
// they're auxiliary information.  Also we don't want them in the toString output either since it
// makes test cases annoying to write.

object Token {
  val Eof = new Token("", TokenType.Eof, "")(SourceLocation(Int.MaxValue, Int.MaxValue, ""))
}

case class Token(text: String, tpe: TokenType, value: AnyRef)(val sourceLocation: SourceLocation) extends SourceLocatable {

  // the automatically generated `copy` method wouldn't copy the auxiliary fields
  def copy(text: String = text, tpe: TokenType = tpe, value: AnyRef = value): Token =
    new Token(text, tpe, value)(sourceLocation)

  def refine(newPrim: Instruction, text: String = text, tpe: TokenType = tpe): Token = {
    // usual ugliness here because prim instances and tokens
    // have mutual references - ST 9/24/14
    val result = copy(text, tpe, value = newPrim)
    newPrim.token = result
    result
  }
}
