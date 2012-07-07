// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

class ErrorSource(token:Token) {
  /**
   * Throws a CompilerException with the given message.
   * This procedure will never return.
   */
  @throws(classOf[CompilerException])
  def signalError(message:String): Nothing = {
    throw new CompilerException(message, token.startPos,token.endPos, token.fileName)
  }
}
