package org.nlogo.nvm

object NonLocalExit extends RuntimeException {
  // for efficiency, don't fill in stack trace
  override def fillInStackTrace = this
}
