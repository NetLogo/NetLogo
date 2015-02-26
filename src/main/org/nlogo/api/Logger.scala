// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait Logger {
  def logCustomMessage(msg: String): Unit
  def logCustomGlobals(nameValuePairs: (String, String)*): Unit
}

object Logger {
  var logger: Option[Logger] = None
  def logCustomMessage(msg: String): Unit = logger.collect { case l => l.logCustomMessage(msg) }
  def logCustomGlobals(nameValuePairs: (String, String)*): Unit = logger.collect { case l => l.logCustomGlobals(nameValuePairs: _*) }
}
