// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, GridBagLayout, Insets, Point }
import java.awt.event.ActionEvent
import javax.swing.AbstractAction

import org.nlogo.core.{ I18N, Output => CoreOutput }
import org.nlogo.swing.{ MenuItem, PopupMenu }
import org.nlogo.theme.InterfaceColors

class OutputWidget extends SingleErrorWidget with CommandCenterInterface with
  Events.ExportWorldEvent.Handler with Editable {
  type WidgetModel = CoreOutput

  displayName(I18N.gui.get("tabs.run.widgets.output"))

  val outputArea = new OutputArea()

  setLayout(new GridBagLayout)

  val c = new GridBagConstraints

  c.weightx = 1
  c.weighty = 1
  c.fill = GridBagConstraints.BOTH
  c.insets = new Insets(zoom(8), zoom(8), zoom(8), zoom(8))

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

  override def populateContextMenu(menu: PopupMenu, p: Point) {
    // at least on Macs, Command-C to copy may not work, so this
    // is needed - ST 4/21/05
    menu.add(new MenuItem(new AbstractAction(I18N.gui.get("tabs.run.widget.copyselectedtext")) {
      def actionPerformed(e: ActionEvent) {
        outputArea.text.copy
      }
    }))
  }

  override def syncTheme(): Unit = {
    setBackgroundColor(InterfaceColors.outputBackground())

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
    setSize(model.width, model.height)
    fontSize = model.fontSize
    this
  }

  override def model: WidgetModel = {
    val b = getUnzoomedBounds
    CoreOutput(
      x = b.x, y = b.y, width = b.width, height = b.height,
      fontSize = fontSize)
  }
}
