package org.nlogo.window

import org.nlogo.api.CompilerServices
import org.nlogo.hubnet.connection.ClientRoles

trait ClientAppInterface {
  def startup(editorFactory: EditorFactory, userid: String, hostip: String, port: Int,
              role: ClientRoles.Value, isLocal: Boolean, isRobo: Boolean, waitTime: Long,
              workspace: CompilerServices): Unit
}

