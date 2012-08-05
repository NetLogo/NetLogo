// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.{ agent, workspace }

abstract class GUIWorkspace(
  world: agent.World,
  kioskLevel: GUIWorkspaceJ.KioskLevel,
  frame: java.awt.Frame,
  linkParent: java.awt.Component,
  hubNetManagerFactory: workspace.AbstractWorkspace.HubNetManagerFactory,
  externalFileManager: ExternalFileManager,
  listenerManager: NetLogoListenerManager)
extends GUIWorkspaceJ(
  world, kioskLevel, frame, linkParent, hubNetManagerFactory,
  externalFileManager, listenerManager)
