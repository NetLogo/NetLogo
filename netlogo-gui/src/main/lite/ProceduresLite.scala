// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.lite

import org.nlogo.core.AgentKind
import org.nlogo.nvm.Workspace
import org.nlogo.window.{ Event, Events, ProceduresInterface }

// for use when we don't want the whole ProceduresTab class, because
// ProceduresTab depends on the new editor, which we don't want in the lite jar

class ProceduresLite(linkParent: AnyRef, workspace: Workspace) extends ProceduresInterface
with Event.LinkChild with Events.LoadModelEvent.Handler
{
  override def classDisplayName = "Code"
  override def kind = AgentKind.Observer
  private var text = ""
  override def headerSource = ""
  override def innerSource = text
  override def source = headerSource + innerSource
  override def innerSource_=(text: String): Unit = { this.text = text }
  override def handle(e: Events.LoadModelEvent): Unit = {
    innerSource = e.model.code
    (new Events.CompileAllEvent).raise(this)
  }
  /// Event.LinkChild -- lets us get events out to rest of app
  override def getLinkParent = linkParent
}
