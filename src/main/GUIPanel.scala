package org.nlogo.ls.gui

import java.awt.{ BorderLayout, Dimension }
import javax.swing.{ JLabel, JPanel, JSlider, JSplitPane, ScrollPaneConstants }
import javax.swing.event.ChangeEvent

import org.nlogo.app.interfacetab.CommandCenter
import org.nlogo.swing.{ ScrollPane, SplitPane, Transparent, Zoomable }
import org.nlogo.theme.InterfaceColors
import org.nlogo.window.{ Events, GUIWorkspace, TickCounterLabel }
import org.nlogo.workspace.AbstractWorkspace

abstract class ModelPanel(ws: AbstractWorkspace, panel: JPanel, verticalScroll: Int, resizeWeight: Int)
extends JPanel with Events.OutputEvent.Handler {
  setLayout(new BorderLayout)

  val controlStrip = new JPanel
  controlStrip.setLayout(new BorderLayout)
  val tickCounterLabel = new TickCounterLabel(ws.world)
  controlStrip.add(tickCounterLabel, BorderLayout.WEST)

  add(controlStrip, BorderLayout.NORTH)

  var cc = new CommandCenter(ws, false)
  val scrollPane = new ScrollPane(panel, verticalScroll, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED)
  val splitPane = new SplitPane(scrollPane, cc, None)

  scrollPane.setBorder(null)

  add(splitPane, BorderLayout.CENTER)

  def handle(outputEvent: Events.OutputEvent): Unit = {
    if (outputEvent.clear)
      cc.output.clear()
    else
      cc.output.append(outputEvent.outputObject, outputEvent.wrapLines)
  }

  def packSplitPane(): Unit = {
    splitPane.setPreferredSize(
      splitPane.getOrientation match {
        case JSplitPane.HORIZONTAL_SPLIT =>
          new Dimension(scrollPane.getPreferredSize.width, scrollPane.getPreferredSize.height + splitPane.getDividerSize
                        + cc.getPreferredSize.height)
        case JSplitPane.VERTICAL_SPLIT =>
          new Dimension(scrollPane.getPreferredSize.width + splitPane.getDividerSize +
                        cc.getPreferredSize.width, scrollPane.getPreferredSize.height)
      })

    splitPane.revalidate()
    resetCommandCenter()
  }

  def resetCommandCenter(): Unit = {
    splitPane.resetToPreferredSizes()
  }

  def syncTheme(): Unit = {
    controlStrip.setBackground(InterfaceColors.toolbarBackground())
    scrollPane.setBackground(InterfaceColors.interfaceBackground())

    splitPane.syncTheme()
    tickCounterLabel.syncTheme()
    cc.syncTheme()

    panel match {
      case component: InterfaceComponent =>
        component.syncTheme()

      case _ =>
    }
  }
}

class GUIPanel(ws: GUIWorkspace, panel: JPanel)
extends ModelPanel(ws, panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS, 1) {
  val speedSliderPanel = new JPanel with Transparent
  val label = new JLabel("speed: ") with Zoomable
  speedSliderPanel.add(label)
  val speedSlider = new JSlider(-110, 112, ws.speedSliderPosition().toInt)
  speedSlider.addChangeListener((_: ChangeEvent) => {
    ws.speedSliderPosition(speedSlider.getValue / 2)
    ws.updateManager.nudgeSleeper()
  })
  speedSliderPanel.add(speedSlider)
  controlStrip.add(speedSliderPanel, BorderLayout.CENTER)

  override def syncTheme(): Unit = {
    super.syncTheme()

    label.setForeground(InterfaceColors.toolbarText())
  }
}

class HeadlessPanel(ws: AbstractWorkspace, panel: JPanel)
extends ModelPanel(ws, panel, ScrollPaneConstants.VERTICAL_SCROLLBAR_NEVER, 0)
