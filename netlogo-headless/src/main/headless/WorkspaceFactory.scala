package org.nlogo.headless

import org.nlogo.workspace.AbstractWorkspace

abstract class WorkspaceFactory extends org.nlogo.workspace.WorkspaceFactory {
  def newInstance: AbstractWorkspace = HeadlessWorkspace.newInstance
}
