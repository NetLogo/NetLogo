package org.nlogo.headless

import org.nlogo.workspace.AbstractWorkspace

class WorkspaceFactory extends org.nlogo.workspace.WorkspaceFactory {
  def newInstance(is3D: Boolean): AbstractWorkspace = HeadlessWorkspace.newInstance(is3D)
  def fromPath(path: String): AbstractWorkspace = HeadlessWorkspace.fromPath(path)
}
