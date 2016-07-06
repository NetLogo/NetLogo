// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.nlogo.api.CompilerServices
import org.nlogo.core.{ AgentKind, Model => CoreModel }
import org.nlogo.editor.Colorizer
import org.nlogo.window.{ EditDialogFactoryInterface, MenuBarFactory }

class GUIAggregateManager(
  linkParent: java.awt.Component,
  menuBarFactory: MenuBarFactory,
  compiler: CompilerServices,
  colorizer: Colorizer,
  dialogFactory: EditDialogFactoryInterface)
extends org.nlogo.api.AggregateManagerInterface
with org.nlogo.window.Event.LinkChild
with org.nlogo.window.Events.CompiledEvent.Handler
with org.nlogo.window.Events.BeforeLoadEvent.Handler
with org.nlogo.window.Events.LoadModelEvent.Handler {

  private var editor: AggregateModelEditor = null

  override def showEditor() {
    // if it's the first time, make a new aggregate model editor
    if (editor == null)
      editor = new AggregateModelEditor(
        linkParent, colorizer, menuBarFactory, compiler, dialogFactory)
    editor.setVisible(true)
    editor.toFront()
  }

  override def getLinkParent = linkParent

  override def handle(e: org.nlogo.window.Events.BeforeLoadEvent) {
    if (editor != null) {
      editor.dispose()
      editor = null
    }
  }

  override def handle(e: org.nlogo.window.Events.LoadModelEvent) {
    load(e.model, compiler)
  }

  override def load(model: CoreModel, compiler: CompilerServices) = {
    model.optionalSectionValue[AggregateDrawing]("org.nlogo.modelsection.systemdynamics")
      .foreach { drawing =>
        editor = new AggregateModelEditor(
          linkParent, colorizer, menuBarFactory, drawing, compiler, dialogFactory)
        if (drawing.getModel.elements.isEmpty)
          editor.setVisible(false)
      }
  }

  override def updateModel(m: CoreModel): CoreModel = {
    if (editor == null || !editor.drawing.figures.hasNextFigure)
      m
    else
      m.withOptionalSection[AggregateDrawing]("org.nlogo.modelsection.systemdynamics",
        Some(editor.drawing), editor.drawing)
  }

  override def isLoaded: Boolean = editor != null

  override def handle(e: org.nlogo.window.Events.CompiledEvent) {
    if (editor != null)
      editor.setError(
        this,
        if(e.sourceOwner eq this) e.error else null)
  }

  /// from org.nlogo.nvm.SourceOwner

  override def classDisplayName = "aggregate"
  override def kind = AgentKind.Observer
  override def source = innerSource
  override def innerSource =
    Option(editor).map(_.toNetLogoCode).getOrElse("")
  override def innerSource_=(s: String) { }
  override def headerSource = ""

}
