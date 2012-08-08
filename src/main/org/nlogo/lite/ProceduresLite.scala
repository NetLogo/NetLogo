// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import org.nlogo.agent.Observer
import org.nlogo.api.{ AgentKind, ModelSection }
import org.nlogo.nvm.Workspace
import org.nlogo.window.{ Event, Events, ProceduresInterface }

// for use from Applet; we don't want the whole CodeTab class in that context, because
// CodeTab depends on the new editor, which we don't want in the lite jar

class ProceduresLite(linkParent: AnyRef, workspace: Workspace) extends ProceduresInterface
with Event.LinkChild with Events.LoadSectionEventHandler
{
  override def classDisplayName = "Code"
  override def kind = AgentKind.Observer
  private var text = ""
  override def headerSource = ""
  override def innerSource = text
  override def source = headerSource + innerSource
  override def innerSource(text: String) { this.text = text }
  override def handle(e: Events.LoadSectionEvent) {
    if(e.section == ModelSection.Code) {
      innerSource(workspace.autoConvert(e.text, false, false, e.version))
      (new Events.CompileAllEvent).raise(this)
    }
  }
  /// Event.LinkChild -- lets us get events out to rest of app
  override def getLinkParent = linkParent
}
