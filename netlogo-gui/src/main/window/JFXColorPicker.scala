// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ Color, Dimension, Frame, Toolkit }
import java.awt.datatransfer.StringSelection

import javafx.application.Platform
import javafx.beans.value.ChangeListener
import javafx.beans.value.ObservableValue
import javafx.concurrent.Worker.State
import javafx.embed.swing.JFXPanel
import javafx.scene.Scene
import javafx.scene.input.{ KeyCode, KeyEvent }
import javafx.scene.layout.VBox
import javafx.scene.text.Font
import javafx.scene.web.{ WebEngine, WebView }

import javax.swing.{ JDialog, WindowConstants }

import netscape.javascript.JSObject

import org.nlogo.awt.EventQueue
import org.nlogo.core.I18N
import org.nlogo.swing.Positioning
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class JFXColorPicker(frame: Frame, modal: Boolean, config: JFXCPConfig, callback: (String) => Unit = _ => {})
  extends JDialog(frame, I18N.gui.get("tools.colorpicker"), modal) with ThemeSync {

  private val nlBabyMonitor = new Bridge
  private val panel         = new JFXPanel

  private var webEngine: Option[WebEngine] = None

  add(panel)

  setSize(new Dimension(396, 500))
  setResizable(false)
  setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)

  Positioning.center(this, frame)

  Platform.runLater(
    () => {

      Font.loadFont(getClass.getResource("/fonts/OpenSans-Variable.ttf").toExternalForm, 12.0)

      val webView = new WebView()
      val engine  = webView.getEngine
      val url     = getClass.getResource("/colorpicker/index.html")
      engine.load(url.toExternalForm)

      webView.setContextMenuEnabled(false)

      engine.getLoadWorker.stateProperty().addListener(
        new ChangeListener[State] {
          override def changed(ov: ObservableValue[_ <: State], oldState: State, newState: State): Unit = {
            if (newState == State.SUCCEEDED) {

              engine.executeScript("window").asInstanceOf[JSObject].setMember("nlBabyMonitor", nlBabyMonitor)

              config match {
                case DoubleOnly => engine.executeScript("window.useNumberOnlyPicker()")
                case CopyOnly   => engine.executeScript("window.useNonPickPicker()")
                case NumAndRGBA => engine.executeScript("window.useNumAndRGBAPicker()")
              }

              // CSS hacks to fix this stupid JFX browser engine go here! --Jason B. (3/27/25)
              engine.executeScript("""window.injectCSS(`.tab-button:last-child {
                                                       |  border-right-width: 2px;
                                                       |}
                                                       |
                                                       |.dropdown-arrow {
                                                       |  right:  -312px;
                                                       |  bottom: -17px;
                                                       |}`)""".stripMargin)

              webEngine = Option(engine)

              syncTheme()

            }
          }
        }
      )

      val root = new VBox
      root.getChildren.add(webView)

      val scene = new Scene(root)
      panel.setScene(scene)

      val window = scene.getWindow()
      window.addEventHandler(KeyEvent.KEY_RELEASED, (event: KeyEvent) => {
        if (KeyCode.ESCAPE == event.getCode()) {
          JFXColorPicker.this.dispose()
        }
      })

    }

  )

  override def syncTheme(): Unit = {

    val color = (f: (InterfaceColors.type) => Color) => {
      val c = f(InterfaceColors)
      s"rgba(${c.getRed}, ${c.getGreen}, ${c.getBlue}, ${c.getAlpha})"
    }

    Platform.runLater(() => {
      webEngine.foreach(_.executeScript(
        s"""window.syncTheme({
            |  dialogBackground:        "${color(_.dialogBackground             )}"
            |, dialogText:              "${color(_.dialogText                   )}"
            |, tabBackground:           "${color(_.tabBackground                )}"
            |, tabBackgroundHover:      "${color(_.tabBackgroundHover           )}"
            |, tabBackgroundSelected:   "${color(_.tabBackgroundSelected        )}"
            |, tabBorder:               "${color(_.tabBorder                    )}"
            |, tabText:                 "${color(_.tabText                      )}"
            |, tabTextSelected:         "${color(_.tabTextSelected              )}"
            |, controlBackground:       "${color(_.toolbarControlBackground     )}"
            |, controlBackgroundActive: "${color(_.toolbarControlFocus          )}"
            |, controlBackgroundHover:  "${color(_.toolbarControlBackgroundHover)}"
            |, controlBorder:           "${color(_.toolbarControlBorder         )}"
            |, controlText:             "${color(_.toolbarText                  )}"
            |, dropdownArrow:           "${color(_.toolbarText                  )}"
            })""".stripMargin
      ))
    })

  }

  private class Bridge {
    def onPick(x: String): Unit = {
      EventQueue.invokeLater(() => {
        callback(x)
        setVisible(false)
      })
    }

    def onCopy(x: String): Unit = {
      EventQueue.invokeLater(() => {
        val selection = new StringSelection(x)
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

sealed trait JFXCPConfig

case object DoubleOnly extends JFXCPConfig
case object CopyOnly   extends JFXCPConfig
case object NumAndRGBA extends JFXCPConfig
