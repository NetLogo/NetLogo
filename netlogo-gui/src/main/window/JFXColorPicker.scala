// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Dimension, Frame, Toolkit }
import java.awt.datatransfer.StringSelection

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.concurrent.Worker.State
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import javax.swing.{ JDialog, WindowConstants }

import netscape.javascript.JSObject

import org.nlogo.awt.EventQueue
import org.nlogo.swing.Positioning
import org.nlogo.theme.ThemeSync

class JFXColorPicker(frame: Frame, modal: Boolean, config: JFXCPConfig, callback: (Any) => Unit = _ => {})
  extends JDialog(frame, modal) with ThemeSync {

  private val nlBabyMonitor = new Bridge
  private val panel = new JFXPanel

  add(panel)

  setSize(new Dimension(540, 645))
  setResizable(false)
  setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)

  Positioning.center(this, frame)

  Platform.runLater(
    () => {

      val webView   = new WebView()
      val webEngine = webView.getEngine
      val url       = getClass.getResource("/colorpicker/index.html")
      webEngine.load(url.toExternalForm)

      webEngine.getLoadWorker.stateProperty().addListener(
        new ChangeListener[State] {
          override def changed(ov: ObservableValue[_ <: State], oldState: State, newState: State): Unit = {
            if (newState == State.SUCCEEDED) {

              setTitle(webEngine.getTitle)

              webEngine.executeScript("window").asInstanceOf[JSObject].setMember("nlBabyMonitor", nlBabyMonitor)

              config match {
                case DoubleOnly => webEngine.executeScript(s"window.useNumberOnlyPicker();")
                case CopyOnly   => webEngine.executeScript(s"window.useNonPickPicker();")
              }

            }
          }
        }
      )

      val root = new VBox
      root.getChildren.add(webView)

      panel.setScene(new Scene(root))

    }

  )

  override def syncTheme(): Unit = {
    // do later
  }

  private class Bridge {
    def onPick(x: AnyRef): Unit = {
      EventQueue.invokeLater(() => {
        callback(x.asInstanceOf[String].toDouble)
        setVisible(false)
      })
    }

    def onCopy(x: AnyRef): Unit = {
      EventQueue.invokeLater(() => {
        val selection = new StringSelection(x.toString)
        val clipboard = Toolkit.getDefaultToolkit.getSystemClipboard
        clipboard.setContents(selection, selection)
      })
    }

    def onCancel(): Unit = {
      EventQueue.invokeLater(() => {
        setVisible(false)
      })
    }
  }
}

sealed trait JFXCPConfig {
  def isDoubleEnabled: Boolean
}

case object DoubleOnly extends JFXCPConfig { override def isDoubleEnabled =  true }
case object CopyOnly   extends JFXCPConfig { override def isDoubleEnabled = false }
