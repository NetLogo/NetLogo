// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.core

/**
 * Exception thrown by various methods that accept NetLogo code as input and cause that code to be
 * compiled; indicates the code was invalid.  May be inspected to discover the location and nature
 * of the error.
 */
class CompilerException(message: String, val start: Int, val end: Int, val filename: String)
extends RuntimeException(s"$message at location ($start, $end) in $filename") {
  def this(token: Token) = this(token.ensuring(_.tpe == TokenType.Bad).value.asInstanceOf[String],
                                token.start, token.end, token.filename)

  override def toString =
    getMessage
}
object CompilerException {
  val RuntimeErrorAtCompileTimePrefix = "Runtime error: "
}
