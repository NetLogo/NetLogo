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
    // methods to be called from JavaScript go here
  }

  private val bridge = new Bridge

  setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)

  val panel = new JFXPanel

  add(panel)

  override def setVisible(visible: Boolean) {
    if (visible)
      Platform.runLater(() => { loadHTML() }) // this is only for testing, for release this will only happen once at initialization
    
    super.setVisible(visible)
  }

  private def loadHTML() {
    val webView = new WebView

    webView.getEngine.load(getClass.getResource("/web/color-picker/index.html").toString)

    webView.getEngine.getLoadWorker.stateProperty.addListener(
      (value: ObservableValue[_ <: State], oldState: State, newState: State) => {
        webView.getEngine.executeScript("window").asInstanceOf[JSObject].setMember("bridge", bridge)
        
        val rect = webView.getEngine.executeScript(
          "document.getElementsByClassName(\"code-editor\")[0].getBoundingClientRect()").asInstanceOf[JSObject]
        
        setSize(rect.getMember("width").asInstanceOf[Int], rect.getMember("height").asInstanceOf[Int])
      }
    )

    panel.setScene(new Scene(new VBox(webView)))
  }

  def syncTheme() {
    // will this component obey the color theme? if so that will go here
  }
}
