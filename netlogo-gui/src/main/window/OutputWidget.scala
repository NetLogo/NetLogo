// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

import org.nlogo.api.Editable
import org.nlogo.core.{ I18N, Output => CoreOutput }

class OutputWidget extends SingleErrorWidget with CommandCenterInterface with
  org.nlogo.window.Events.ExportWorldEvent.Handler with Editable {
  type WidgetModel = CoreOutput

  displayName(I18N.gui.get("tabs.run.widgets.output"))

  val outputArea = new OutputArea()

  setLayout(new GridBagLayout)

  val c = new GridBagConstraints

  c.weightx = 1
  c.weighty = 1
  c.fill = GridBagConstraints.BOTH
  c.insets = new Insets(6, 6, 6, 6)

  add(outputArea, c)

  def propertySet = Properties.output

  originalFont = outputArea.getFont
  def fontSize = originalFont.getSize
  def fontSize_=(newSize: Int): Unit = {
    val zoomDiff = outputArea.fontSize - fontSize
    outputArea.fontSize(newSize + zoomDiff)
    originalFont = originalFont.deriveFont(newSize.toFloat)
  }

  override def classDisplayName = I18N.gui.get("tabs.run.widgets.output")
  override def setZoomFactor(zoomFactor: Double) {
    super.setZoomFactor(zoomFactor)

    outputArea.zoomFactor = zoomFactor
  }
  override def exportable = true
  override def getDefaultExportName = "output.txt"
  def valueText: String = outputArea.text.getText
  override def hasContextMenu = true
  override def copyable = false

  // satisfy CommandCenterInterface, which we must implement in order
  // to be used in NetLogoComponent - ST 9/13/04
  def repaintPrompt(){}
  def cycleAgentType(forward:Boolean){}

  override def populateContextMenu(menu:javax.swing.JPopupMenu, p:java.awt.Point, source:java.awt.Component) = {
    // at least on Macs, Command-C to copy may not work, so this
    // is needed - ST 4/21/05
    menu.add(new PopupMenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.copyselectedtext")) {
      def actionPerformed(e: ActionEvent) {
        outputArea.text.copy
      }
    }))
    p
  }

  override def syncTheme() {
    setBackgroundColor(InterfaceColors.OUTPUT_BACKGROUND)

    outputArea.syncTheme()
  }

  // these are copied from the TrailDrawer, as is this code for breaking up
  // possible very long text into multiple cells and rows for Excel
  // CLB 7/15/05
  def handle(e:org.nlogo.window.Events.ExportWorldEvent){
    import org.nlogo.api.Dump
    e.writer.println(Dump.csv.encode("OUTPUT"))
    Dump.csv.stringToCSV(e.writer, outputArea.text.getText())
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
