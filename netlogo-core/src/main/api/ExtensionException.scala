// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

/** Wrapper class for exceptions thrown by NetLogo extensions. */
class ExtensionException(message: String, cause: Exception) extends Exception(message) {
  if (cause != null)
    setStackTrace(cause.getStackTrace)
  def this(message: String) = this(message, null)
  def this(cause: Exception) = this(cause.getMessage, cause)
}
