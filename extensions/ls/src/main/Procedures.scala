package org.nlogo.ls.gui

import org.nlogo.api.{ CompilerManagerInterface, ProceduresInterface }
import org.nlogo.core.{ AgentKind, Model }

// dummy class so LevelSpace doesn't need to depend on (and instantiate) the main CodeTab class (Isaac B 1/25/26)
class Procedures(compilerManager: CompilerManagerInterface) extends ProceduresInterface {
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

  override def loadModel(model: Model): Unit = {
    innerSource = model.code

    compilerManager.compileAll()
  }
}
