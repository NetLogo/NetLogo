// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

class JOGLLoadingException(message: String, cause: Throwable = null) extends Exception(message) {
  override def getCause = cause
}
