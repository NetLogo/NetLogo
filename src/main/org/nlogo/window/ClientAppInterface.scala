// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.ParserServices

trait ClientAppInterface {
  def startup(editorFactory: EditorFactory, userid: String, hostip: String, port: Int,
    isLocal: Boolean, isRobo: Boolean, waitTime: Long, workspace: ParserServices): Unit
}
