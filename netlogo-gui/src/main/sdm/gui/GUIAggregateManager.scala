// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.nlogo.api.{ AggregateManagerInterface, CompilerServices }
import org.nlogo.core.{ AgentKind, LiteralParser, Model => CoreModel }
import org.nlogo.editor.Colorizer
import org.nlogo.theme.ThemeSync
import org.nlogo.window.{ EditDialogFactory, Event, Events, MenuBarFactory }

class GUIAggregateManager(
  linkParent: java.awt.Component,
  menuBarFactory: MenuBarFactory,
  compiler: CompilerServices,
  colorizer: Colorizer,
  dialogFactory: EditDialogFactory)
extends AggregateManagerInterface
with Event.LinkChild
with Events.CompiledEvent.Handler
with Events.BeforeLoadEvent.Handler
with Events.LoadModelEvent.Handler
with ThemeSync {

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

  override def load(model: CoreModel, compiler: LiteralParser) = {
    model.optionalSectionValue[AggregateDrawing]("org.nlogo.modelsection.systemdynamics.gui")
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
      m.withOptionalSection[AggregateDrawing]("org.nlogo.modelsection.systemdynamics.gui",
        Some(editor.drawing), editor.drawing)
  }

  override def isLoaded: Boolean = editor != null

  override def handle(e: org.nlogo.window.Events.CompiledEvent) {
    if (editor != null)
      editor.setError(
        this,
        if(e.sourceOwner eq this) e.error else null)
  }

  override def syncTheme(): Unit = {
    if (editor != null) {
      editor.syncTheme()
      editor.repaint()
    }
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
