// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.api

// this trait allows ProtocolEditable to check the validity of a variable specification
// without needing to depend on the entire GUIWorkspace. it also has the benefit of allowing
// ProtocolEditable tests to continue as before without constructing a real Workspace. (Isaac B 7/3/25)
trait GlobalsIdentifier {
  def isGlobalVariable(name: String): Boolean
}
