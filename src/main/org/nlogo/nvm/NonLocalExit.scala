// (C) 2011 Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

object NonLocalExit extends RuntimeException {
  // for efficiency, don't fill in stack trace
  override def fillInStackTrace = this
}
