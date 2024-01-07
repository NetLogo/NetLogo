// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

import org.nlogo.workspace.{ CurrentModelOpener, WorkspaceFactory }

object LabUtilities {
  var workspaceFactory: WorkspaceFactory with CurrentModelOpener = null
}