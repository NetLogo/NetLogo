// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

trait Logger {
  def logCustomMessage(msg: String):                       Unit
  def logCustomGlobals(nameValuePairs: (String, String)*): Unit
}
