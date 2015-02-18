// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.log.{ Logger => L }

object Logger {
  def logCustomMessage(msg: String):                       Unit = L.logCustomMessage(msg)
  def logCustomGlobals(nameValuePairs: (String, String)*): Unit = L.logCustomGlobals(nameValuePairs: _*)
}
