// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.swing.RichJMenuItem
import org.nlogo.api.{I18N, Editable}

class OutputWidget extends SingleErrorWidget with CommandCenterInterface with
  org.nlogo.window.Events.ExportWorldEvent.Handler with Editable {

  setLayout(new java.awt.BorderLayout())
  setBorder(widgetBorder)
  setBackground(InterfaceColors.MONITOR_BACKGROUND)
  displayName(I18N.gui.get("tabs.run.widgets.output"))
  val outputArea = new OutputArea()
  add(new javax.swing.JPanel() {
    setLayout(new java.awt.BorderLayout())
    setBackground(InterfaceColors.MONITOR_BACKGROUND)
    setBorder(javax.swing.BorderFactory.createEmptyBorder(2, 2, 2, 2))
    add(outputArea, java.awt.BorderLayout.CENTER)
  }, java.awt.BorderLayout.CENTER)

  def propertySet = Properties.output

  originalFont = outputArea.getFont
  def fontSize = originalFont.getSize
  def fontSize_=(newSize: Int): Unit = {
    val zoomDiff = outputArea.fontSize - fontSize
    outputArea.fontSize(newSize + zoomDiff)
    originalFont = originalFont.deriveFont(newSize.toFloat)
  }

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.output")
  override def zoomSubcomponents = true
  override def exportable = true
  override def getDefaultExportName = "output.txt"
  override def export(exportPath:String) = outputArea.export(exportPath)
  override def hasContextMenu = true
  override def copyable = false

  // satisfy CommandCenterInterface, which we must implement in order
  // to be used in NetLogoComponent - ST 9/13/04
  def repaintPrompt(){}
  def cycleAgentType(forward:Boolean){}

  override def populateContextMenu(menu:javax.swing.JPopupMenu, p:java.awt.Point, source:java.awt.Component) = {
    // at least on Macs, Command-C to copy may not work, so this
    // is needed - ST 4/21/05
    val copyItem = RichJMenuItem("Copy Selected Text"){ outputArea.text.copy }
    menu.add(copyItem)
    p
  }

  // these are copied from the TrailDrawer, as is this code for breaking up
  // possible very long text into multiple cells and rows for Excel
  // CLB 7/15/05
  def handle(e:org.nlogo.window.Events.ExportWorldEvent){
    import org.nlogo.api.Dump
    e.writer.println(Dump.csv.encode("OUTPUT"))
    Dump.csv.stringToCSV(e.writer, outputArea.text.getText())
  }

  override def save = {
    val s = new StringBuilder()
    s.append("OUTPUT\n")
    s.append(getBoundsString)
    s.append(fontSize + "\n")
    s.toString
  }
  override def load(strings:Array[String], helper:Widget.LoadHelper): Object = {
    val List(x1,y1,x2,y2) = strings.drop(1).take(4).map(_.toInt).toList
    setSize(x2 - x1, y2 - y1)
    if (strings.length > 5) { fontSize = strings(5).toInt }
    this
  }
}
