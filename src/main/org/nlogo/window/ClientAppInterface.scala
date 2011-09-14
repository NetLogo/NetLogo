package org.nlogo.window

import org.nlogo.api.CompilerServices
import org.nlogo.hubnet.connection.ClientRole

trait ClientAppInterface {
  def startup(editorFactory: EditorFactory, userid: String, hostip: String, port: Int,
              role: ClientRole, isLocal: Boolean, isRobo: Boolean, waitTime: Long,
              workspace: CompilerServices): Unit
}

