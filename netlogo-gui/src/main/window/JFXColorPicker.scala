package org.nlogo.window

import java.awt.{ Dimension, Toolkit }
import java.awt.datatransfer.StringSelection

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.concurrent.Worker.State
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.scene.web.WebView
import javax.swing.JFrame

import netscape.javascript.JSObject

import org.nlogo.awt.EventQueue

object JFXColorPicker {

  def launch(config: JFXCPConfig)(callback: (Any) => Unit): Unit = {

    val frame = new JFrame
    val panel = new JFXPanel

    frame.add(panel)
    frame.setSize(new Dimension(540, 645))
    frame.setLocationRelativeTo(null) // Center on screen --Jason B. (3/14/25)
    frame.setVisible(true)

    Platform.runLater(
      () => {

        val webView   = new WebView()
        val webEngine = webView.getEngine
        val url       = this.getClass.getResource("/colorpicker/index.html")
        webEngine.load(url.toExternalForm)

        webEngine.getLoadWorker.stateProperty().addListener(
          new ChangeListener[State] {
            override def changed(ov: ObservableValue[_ <: State], oldState: State, newState: State): Unit = {
              if (newState == State.SUCCEEDED) {

                frame.setTitle(webEngine.getTitle)

                val nlBabyMonitor = new {

                  def onPick(x: AnyRef): Unit = {
                    EventQueue.invokeLater(() => {
                      callback(x.asInstanceOf[String].toDouble)
                      frame.dispose()
                    })
                  }

                  def onCopy(x: AnyRef): Unit = {
                    EventQueue.invokeLater(() => {
                      val selection = new StringSelection(x.toString())
                      val clipboard = Toolkit.getDefaultToolkit().getSystemClipboard()
                      clipboard.setContents(selection, selection)
                    })
                  }

                  def onCancel(): Unit = {
                    EventQueue.invokeLater(() => {
                      frame.dispose()
                    })
                  }

                }

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
        root.getChildren().add(webView)

        panel.setScene(new Scene(root))

      }

    )

  }

}

sealed trait JFXCPConfig {
  def isDoubleEnabled: Boolean
}

case object DoubleOnly extends JFXCPConfig { override def isDoubleEnabled =  true }
case object CopyOnly   extends JFXCPConfig { override def isDoubleEnabled = false }
