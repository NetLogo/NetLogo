// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Frame
import javax.swing.{ JDialog, WindowConstants }

import javafx.application.Platform
import javafx.beans.value.ObservableValue
import javafx.concurrent.Worker.State
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.layout.VBox
import javafx.scene.web.WebView

import netscape.javascript.JSObject

import org.nlogo.theme.ThemeSync

class RGBAColorDialog(parent: Frame, returnColor: Boolean) extends JDialog(parent) with ThemeSync {
  private class Bridge {
    def selectColor(color: AnyRef): Unit = {
      println(color)
    }
  }

  private val bridge = new Bridge

  setResizable(false)
  setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)

  locally {
    val panel = new JFXPanel

    add(panel)

    // this results in an infinite freeze on Mac, if this file is kept it will need to be fixed (Isaac B 1/14/25)

    Platform.runLater(() => {
      val webView = new WebView

      // webView.getEngine.load(getClass.getResource("/web/color-picker/index.html").toString)

      webView.getEngine.getLoadWorker.stateProperty.addListener(
        (value: ObservableValue[? <: State], oldState: State, newState: State) => {
          webView.getEngine.executeScript("window").asInstanceOf[JSObject].setMember("bridge", bridge)
          webView.getEngine.executeScript("initWithMode(\"RGBA\")")

          val width = webView.getEngine.executeScript("document.body.clientWidth").asInstanceOf[Number].intValue
          val height = webView.getEngine.executeScript("document.body.clientHeight").asInstanceOf[Number].intValue

          setSize(width, height)
        }
      )

      panel.setScene(new Scene(new VBox(webView)))
    })
  }

  override def syncTheme(): Unit = {
    // do this once the new dialog is finalized
  }
}
