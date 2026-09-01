// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.event.{ ActionEvent, InputEvent, KeyEvent }
import javax.swing.{ AbstractAction, ActionMap, InputMap, JComponent, KeyStroke, RootPaneContainer }

trait ZoomProvider {
  def zoomIn(): Unit = {
    setZoomFactor(Utils.getZoomFactor + 0.125f)
  }

  def zoomOut(): Unit = {
    setZoomFactor((Utils.getZoomFactor - 0.125f).max(0.25f))
  }

  def resetZoom(): Unit = {
    setZoomFactor(1)
  }

  protected def setZoomFactor(factor: Float): Unit
}

object ZoomActions {
  private var provider: Option[ZoomProvider] = None

  def init(provider: ZoomProvider): Unit = {
    this.provider = Option(provider)
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
        ZoomActions.provider.foreach(_.zoomIn())
      }
    })

    actionMap.put("zoomOut", new AbstractAction {
      override def actionPerformed(e: ActionEvent): Unit = {
        ZoomActions.provider.foreach(_.zoomOut())
      }
    })

    actionMap.put("resetZoom", new AbstractAction {
      override def actionPerformed(e: ActionEvent): Unit = {
        ZoomActions.provider.foreach(_.resetZoom())
      }
    })
  }
}
