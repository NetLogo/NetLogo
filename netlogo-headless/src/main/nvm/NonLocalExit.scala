// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

// this could be an object, but `catch { case NonLocalExit => ... }`
// will sometime change `StackOverflowError`s to `java.lang.NoClassDefFoundError`.
class NonLocalExit extends RuntimeException {
  // for efficiency, don't fill in stack trace
  override def fillInStackTrace = this
}
