// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import org.nlogo.agent.Observer
import org.nlogo.api.ModelSection
import org.nlogo.nvm.Workspace
import org.nlogo.window.{ Event, Events, ProceduresInterface }

// for use from Applet; we don't want the whole ProceduresTab class in that context, because
// ProceduresTab depends on the new editor, which we don't want in the lite jar

class ProceduresLite(linkParent: AnyRef, workspace: Workspace) extends ProceduresInterface
with Event.LinkChild with Events.LoadSectionEvent.Handler
{
  override def classDisplayName = "Code"
  override def agentClass = classOf[Observer]
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
