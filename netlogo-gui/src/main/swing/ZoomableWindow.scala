// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing

import java.awt.{ Component, Dimension, Window }
import java.awt.event.{ ActionEvent, InputEvent, KeyEvent }
import javax.swing.{ AbstractAction, ActionMap, InputMap, JComponent, KeyStroke, RootPaneContainer }

trait ZoomableWindow(parent: Option[Component]) extends Window with RootPaneContainer with ZoomHelpers {
  private var zoomFactor: Float = 1f

  locally {
    val inputMap: InputMap = getRootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW)

    val mask: Int = {
      if (System.getProperty("os.name").toLowerCase.startsWith("mac")) {
        InputEvent.META_DOWN_MASK
      } else {
        InputEvent.CTRL_DOWN_MASK
      }
    }

    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, mask), "zoomIn")
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_MINUS, mask), "zoomOut")
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_0, mask), "resetZoom")

    val actionMap: ActionMap = getRootPane.getActionMap

    actionMap.put("zoomIn", new AbstractAction {
      override def actionPerformed(e: ActionEvent): Unit = {
        zoomIn()
      }
    })

    actionMap.put("zoomOut", new AbstractAction {
      override def actionPerformed(e: ActionEvent): Unit = {
        zoomOut()
      }
    })

    actionMap.put("resetZoom", new AbstractAction {
      override def actionPerformed(e: ActionEvent): Unit = {
        resetZoom()
      }
    })
  }

  override def getZoomFactor: Float =
    zoomFactor

  override def setVisible(visible: Boolean): Unit = {
    if (visible) {
      zoomFactor = parent.fold(1f) {
        case zoom: ZoomHelpers =>
          zoom.getZoomFactor

        case _ =>
          1f
      }
    }

    zoomWindow()

    super.setVisible(visible)
  }

  def zoomWindow(): Unit = {
    getComponents.foreach(zoomComponents)

    Option(getRootPane.getJMenuBar).foreach {
      case menu: MenuBar =>
        zoomMenuBar(menu)

      case _ =>
    }

    pack()

    val screen: Dimension = getToolkit.getScreenSize

    setLocation(getX.min(screen.width - getWidth).max(0), getY.min(screen.height - getHeight).max(0))
  }

  def zoomIn(): Unit = {
    setZoomFactor(zoomFactor + 0.125f)
  }

  def zoomOut(): Unit = {
    setZoomFactor((zoomFactor - 0.125f).max(0.25f))
  }

  def resetZoom(): Unit = {
    setZoomFactor(1)
  }

  private def setZoomFactor(factor: Float): Unit = {
    zoomFactor = factor

    zoomWindow()
  }
}
