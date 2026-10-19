package org.nlogo.ls.gui

import org.nlogo.core.AgentKind
import org.nlogo.window.{ Event, Events, ProceduresInterface }

// dummy class so LevelSpace doesn't need to depend on (and instantiate) the main CodeTab class (Isaac B 1/25/26)
class Procedures(linkParent: AnyRef)
  extends ProceduresInterface with Event.LinkChild with Events.LoadModelEvent.Handler {

  private var text = ""

  override def classDisplayName: String =
    "Code"

  override def kind: AgentKind =
    AgentKind.Observer

  override def headerSource: String =
    ""

  override def innerSource: String =
    text

  override def source: String =
    headerSource + innerSource

  override def innerSource_=(text: String): Unit = {
    this.text = text
  }

  override def handle(e: Events.LoadModelEvent): Unit = {
    innerSource = e.model.code

    new Events.CompileAllEvent().raise(this)
  }

  override def getLinkParent: AnyRef =
    linkParent
}
