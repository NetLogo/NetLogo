// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.{ ActionEvent, InputEvent, KeyEvent }
import javax.swing.{ AbstractAction, ActionMap, InputMap, JComponent, KeyStroke, RootPaneContainer }

object ZoomActions {
  private var zoomIn: Option[() => Unit] = None
  private var zoomOut: Option[() => Unit] = None
  private var resetZoom: Option[() => Unit] = None

  def init(zoomIn: () => Unit, zoomOut: () => Unit, resetZoom: () => Unit): Unit = {
    this.zoomIn = Option(zoomIn)
    this.zoomOut = Option(zoomOut)
    this.resetZoom = Option(resetZoom)
  }
}

trait ZoomActions extends RootPaneContainer {
  locally {
    val inputMap: InputMap = getRootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, InputEvent.CTRL_DOWN_MASK), "zoomIn")
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, InputEvent.CTRL_DOWN_MASK), "zoomOut")
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, InputEvent.CTRL_DOWN_MASK), "resetZoom")

    val actionMap: ActionMap = getRootPane.getActionMap

    actionMap.put("zoomIn", new AbstractAction {
      override def actionPerformed(e: ActionEvent): Unit = {
        ZoomActions.zoomIn.foreach(_())
      }
    })

    actionMap.put("zoomOut", new AbstractAction {
      override def actionPerformed(e: ActionEvent): Unit = {
        ZoomActions.zoomOut.foreach(_())
      }
    })

    actionMap.put("resetZoom", new AbstractAction {
      override def actionPerformed(e: ActionEvent): Unit = {
        ZoomActions.resetZoom.foreach(_())
      }
    })
  }
}
