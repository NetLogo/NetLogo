// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.core.Token

class ErrorSource(token: Token) {
  /**
   * Throws a CompilerException with the given message.
   * This procedure will never return.
   */
  def signalError(message:String): Nothing = {
    throw new CompilerException(message, token.start,token.end, token.filename)
  }
}
