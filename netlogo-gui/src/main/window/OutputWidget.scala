// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.swing.RichJMenuItem
import org.nlogo.api.Editable
import org.nlogo.core.{ I18N, Output => CoreOutput }

class OutputWidget extends SingleErrorWidget with CommandCenterInterface with
  org.nlogo.window.Events.ExportWorldEvent.Handler with Editable {
  type WidgetModel = CoreOutput

  setLayout(new java.awt.BorderLayout())
  setBorder(widgetBorder)
  setBackground(InterfaceColors.MONITOR_BACKGROUND)
  displayName(I18N.gui.get("tabs.run.widgets.output"))
  val output = new OutputArea()
  val outputArea = Some(output)
  add(new javax.swing.JPanel() {
    setLayout(new java.awt.BorderLayout())
    setBackground(InterfaceColors.MONITOR_BACKGROUND)
    setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2))
    add(output, java.awt.BorderLayout.CENTER)
  }, java.awt.BorderLayout.CENTER)

  def propertySet = Properties.output

  originalFont = output.getFont
  def fontSize = originalFont.getSize
  def fontSize_=(newSize: Int): Unit = {
    val zoomDiff = output.fontSize - fontSize
    output.fontSize(newSize + zoomDiff)
    originalFont = originalFont.deriveFont(newSize.toFloat)
  }

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.output")
  override def zoomSubcomponents = true
  override def exportable = true
  override def getDefaultExportName = "output.txt"
  def valueText: String = output.text.getText
  override def hasContextMenu = true
  override def copyable = false

  // satisfy CommandCenterInterface, which we must implement in order
  // to be used in NetLogoComponent - ST 9/13/04
  def repaintPrompt(){}
  def cycleAgentType(forward:Boolean){}

  override def populateContextMenu(menu:javax.swing.JPopupMenu, p:java.awt.Point, source:java.awt.Component) = {
    // at least on Macs, Command-C to copy may not work, so this
    // is needed - ST 4/21/05
    val copyItem = RichJMenuItem(I18N.gui.get("tabs.run.widget.copyselectedtext")){ output.text.copy }
    menu.add(copyItem)
    p
  }

  // these are copied from the TrailDrawer, as is this code for breaking up
  // possible very long text into multiple cells and rows for Excel
  // CLB 7/15/05
  def handle(e:org.nlogo.window.Events.ExportWorldEvent){
    import org.nlogo.api.Dump
    e.writer.println(Dump.csv.encode("OUTPUT"))
    Dump.csv.stringToCSV(e.writer, output.text.getText())
  }

  override def load(model: WidgetModel): AnyRef = {
    setSize(model.right - model.left, model.bottom - model.top)
    fontSize = model.fontSize
    this
  }

  override def model: WidgetModel = {
    val b = getBoundsTuple
    CoreOutput(
      left = b._1, top = b._2, right = b._3, bottom = b._4,
      fontSize = fontSize)
  }
}
