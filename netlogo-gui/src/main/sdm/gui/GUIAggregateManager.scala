// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.nlogo.api.{ CompilerServices, ModelSection }
import org.nlogo.core.{ AgentKind, TokenType }
import org.nlogo.editor.Colorizer
import org.nlogo.window.{ EditDialogFactoryInterface, MenuBarFactory }

class GUIAggregateManager(linkParent: java.awt.Component,
                          menuBarFactory: MenuBarFactory,
                          compiler: CompilerServices,
                          colorizer: Colorizer[TokenType],
                          dialogFactory: EditDialogFactoryInterface)
extends org.nlogo.api.AggregateManagerInterface
with org.nlogo.window.Event.LinkChild
with org.nlogo.window.Events.CompiledEvent.Handler
with org.nlogo.window.Events.BeforeLoadEvent.Handler
with org.nlogo.window.Events.LoadSectionEvent.Handler {

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

  override def save: String = {
    if (editor == null || !editor.view.drawing.figures.hasNextFigure)
      null
    else {
      val s = new java.io.ByteArrayOutputStream
      val output = new org.jhotdraw.util.StorableOutput(s)
      output.writeDouble(editor.getModel.getDt)
      output.writeStorable(editor.view.drawing)
      output.close()
      // JHotDraw has an annoying habit of including spaces at the end of lines.  we have stripped
      // those out of the models in version control, so to prevent spurious diffs, we need to keep
      // them from coming back - ST 3/10/09
      s.toString.replaceAll(" *\n", "\n").trim
    }
  }

  override def handle(e: org.nlogo.window.Events.BeforeLoadEvent) {
    if (editor != null) {
      editor.dispose()
      editor = null
    }
  }

  override def handle(e: org.nlogo.window.Events.LoadSectionEvent) {
    if (e.section == ModelSection.SystemDynamics)
      load(e.text, compiler)
  }

  override def load(text: String, compiler: CompilerServices) {
    if(text.trim.nonEmpty) {
      var text2 = org.nlogo.sdm.Loader.mungeClassNames(text)
      // first parse out dt on our own as jhotdraw does not deal with scientific notation
      // properly. ev 10/11/05
      val br = new java.io.BufferedReader(new java.io.StringReader(text2))
      val dt = br.readLine().toDouble
      val str = br.readLine()
      text2 = text2.substring(text2.indexOf(str))
      val s = new java.io.ByteArrayInputStream(text2.getBytes())
      val input = new org.jhotdraw.util.StorableInput(s)
      val drawing = input.readStorable.asInstanceOf[AggregateDrawing]
      drawing.getModel.setDt(dt)
      editor = new AggregateModelEditor(
        linkParent, colorizer, menuBarFactory, drawing, compiler, dialogFactory)
      if (drawing.getModel.elements.isEmpty)
        editor.setVisible(false)
    }
  }

  override def handle(e: org.nlogo.window.Events.CompiledEvent) {
    if (editor != null)
      editor.setError(
        this,
        if(e.sourceOwner eq this) e.error else null)
  }

  /// from org.nlogo.nvm.SourceOwner

  override def classDisplayName = "Aggregate"
  override def kind = AgentKind.Observer
  override def source = innerSource
  override def innerSource =
    Option(editor).map(_.toNetLogoCode).getOrElse("")
  override def innerSource(s: String) { }
  override def headerSource = ""

}
