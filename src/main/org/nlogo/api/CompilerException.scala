// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/**
 * Exception thrown by various methods that accept NetLogo code as input and cause that code to be
 * compiled; indicates the code was invalid.  May be inspected to discover the location and nature
 * of the error.
 */
class CompilerException(message: String, val startPos: Int, val endPos: Int, val fileName: String)
extends Exception(message) {
  def this(token: Token) = this({assert(token.tyype == TokenType.BAD); token.value.asInstanceOf[String]},
                                 token.startPos, token.endPos, token.fileName)

  override def toString =
    getMessage + " at position " + startPos + " in " + fileName
}
object CompilerException {
  val RuntimeErrorAtCompileTimePrefix = "Runtime error: "
}
