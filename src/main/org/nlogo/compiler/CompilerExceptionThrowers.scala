// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.compiler

import org.nlogo.api.{CompilerException, Token}

object CompilerExceptionThrowers {

  // "assert" is in Predef, so...
  def cAssert(condition: Boolean, desc: String, token: Token) =
    if(!condition)
      exception(desc, token)
  def cAssert(condition: Boolean, desc: String, node: AstNode) =
    if(!condition)
      exception(desc, node)

  def exception(message: String, startPos: Int, endPos: Int, fileName: String) =
    throw new CompilerException(message, startPos, endPos, fileName)
  def exception(message: String, token: Token) =
    throw new CompilerException(message, token.startPos, token.endPos, token.fileName)
  def exception(message: String, node: AstNode) =
    throw new CompilerException(message, node.start, node.end, node.file)

}
