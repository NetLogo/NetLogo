// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.{ CompilerServices, EditorCompiler }

private[nlogo] trait ClientAppInterface {
  def startup(userid: String, hostip: String,
               port: Int, isLocal: Boolean, isRobo: Boolean, waitTime: Long,
               compiler: EditorCompiler): Unit
}
