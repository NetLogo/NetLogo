// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.nvm

import org.nlogo.agent.OutputObject

// this trait resolves some dependency issues, allowing GUIWorkspace
// to mirror HeadlessWorkspace output (Isaac B 6/28/25)
trait WorkspaceMirror {
  def mirrorOutput(oo: OutputObject, toOutputArea: Boolean): Unit
}
