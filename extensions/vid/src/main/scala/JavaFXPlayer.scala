package org.nlogo.extensions.vid

import java.awt.Dimension
import java.awt.event.{ WindowAdapter, WindowEvent }

import javafx.application.Platform
import javafx.beans.binding.Bindings
import javafx.beans.value.{ ChangeListener, ObservableValue }
import javafx.embed.swing.JFXPanel
import javafx.scene.{ Group, Scene }
import javafx.scene.shape.Rectangle

import javax.swing.{ JFrame, SwingUtilities }

import util.FunctionToCallback.function2ChangeListener

class JavaFXPlayer extends Player {
  if (!VidExtension.isHeadless) {
    new JFXPanel() // init JavaFX
  }

  def boundedSize = Option(currentNode).flatMap(_.enforcedBounds)

  private var frame: Option[PlayerFrame] = None
  private var currentNode: BoundedNode = null

  private val resizeListener: ChangeListener[Dimension] =
    function2ChangeListener { (oldDim: Dimension, newDim: Dimension) =>
      onSwing { () =>
        withFrame { f =>
          f.jfxPanel.setPreferredSize(newDim)
          f.pack()
        }
      }
    }

  present(emptyNode(None))

  def emptyNode(bounds: Option[(Double, Double)]): BoundedNode = {
    if (VidExtension.isHeadless) {
      null
    } else {
      val (width, height) = bounds.getOrElse((640d, 480d))
      val rectangle = new Rectangle(width, height)
      val preferredSize: ObservableValue[Dimension] =
        Bindings.createObjectBinding[Dimension](
          () =>
            new Dimension(rectangle.getWidth.toInt, rectangle.getHeight.toInt),
            rectangle.widthProperty, rectangle.heightProperty)
      BoundedNode(rectangle, preferredSize, bounds)
    }
  }

  override def show(): Unit =
    onSwing(() => withFrame(f => f.setVisible(true)))

  override def isShowing: Boolean = frame.exists(_.isVisible)

  override def hide(): Unit =
    frame.foreach { f =>
      onSwing { () =>
        f.dispatchEvent(new WindowEvent(f, WindowEvent.WINDOW_CLOSING))
      }
    }

  override def present(boundedNode: BoundedNode): Unit = {
    if (!VidExtension.isHeadless && boundedNode != currentNode) {
      onJavaFX { () =>
        if (currentNode != null)
          currentNode.preferredSize.removeListener(resizeListener)
        boundedNode.preferredSize.addListener(resizeListener)
        currentNode = boundedNode
        val preferredSize =
          boundedNode.preferredSize.getValue
        onJavaFX { () =>
          val scene = new Scene(new Group(boundedNode.node))
          onSwing { () =>
            withFrame { f =>
              f.jfxPanel.setScene(scene)
              f.jfxPanel.setPreferredSize(preferredSize)
              f.pack()
            }
          }
        }
      }
    }
  }

  class PlayerFrame extends JFrame("NetLogo - vid extension") {
    val jfxPanel = new JFXPanel()

    add(jfxPanel)

    addWindowListener(new WindowAdapter() {
      override def windowClosing(windowEvent: WindowEvent): Unit = {
        frame  = None
      }
    })
  }

  private def withFrame(f: PlayerFrame => Unit) = {
    for {
      currentFrame <- frame orElse Some(new PlayerFrame)
    } {
      frame = Some(currentFrame)
      f(currentFrame)
    }
  }

  private def onJavaFX(runnable: Runnable) =
    Platform.runLater(runnable)

  private def onSwing(runnable: Runnable) =
    SwingUtilities.invokeLater(runnable)
}
