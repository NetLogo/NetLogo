package org.nlogo.headless

import org.nlogo.workspace.AbstractWorkspaceScala

abstract class WorkspaceFactory extends org.nlogo.workspace.WorkspaceFactory {
  def newInstance: AbstractWorkspaceScala = HeadlessWorkspace.newInstance
}
