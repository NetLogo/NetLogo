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

import org.nlogo.analytics.Analytics
import org.nlogo.api.{ Color => NLColor }
import org.nlogo.awt.EventQueue
import org.nlogo.core.{ Color => CoreColor, I18N, LogoList }
import org.nlogo.swing.Positioning
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class JFXColorPicker( frame: Frame, modal: Boolean, config: JFXCPConfig, initialValue: Option[NLColorValue] = None
                    , pickCallback: (String) => Unit = (_ => {}), cancelCallback: () => Unit = (() => {}))
  extends JDialog(frame, I18N.gui.get("tools.colorpicker"), modal) with ThemeSync {

  private val nlBabyMonitor = new Bridge
  private val panel         = new JFXPanel

  private var webEngine: Option[WebEngine] = None

  add(panel)

  val osName = System.getProperty("os.name").toLowerCase

  // Window height on Ubuntu doesn't count the title bar, while, on Mac and Windows,
  // it does.  And they have different title bar heights, of course.  Fun, fun! --Jason B. (5/2/25)
  val addedHeight =
    if (osName.contains("win"))
      32
    else if (osName.contains("mac"))
      27
    else
      0

  setSize(new Dimension(800, 570 + addedHeight))
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
          override def changed(ov: ObservableValue[? <: State], oldState: State, newState: State): Unit = {
            if (newState == State.SUCCEEDED) {

              engine.executeScript("window").asInstanceOf[JSObject].setMember("nlBabyMonitor", nlBabyMonitor)

              config match {
                case DoubleOnly => engine.executeScript("window.useNumberOnlyPicker()")
                case CopyOnly   => engine.executeScript("window.useNonPickPicker()")
                case NumAndRGBA => engine.executeScript("window.useNumAndRGBAPicker()")
              }

              // CSS hacks to fix this stupid JFX browser engine go here! --Jason B. (3/27/25)
              engine.executeScript(s"""window.injectCSS(`.dropdown-arrow {
                                                        |  right:   0px;
                                                        |  bottom: -2px;
                                                        |}
                                                        |
                                                        |.hue .slider-knob {
                                                        |  left: -1px;
                                                        |}
                                                        |
                                                        |.copy-button {
                                                        |  border-radius: 0;
                                                        |}
                                                        |`)""".stripMargin)

              initialValue.foreach {
                value =>
                  engine.executeScript(s"""window.setValue(${value.toJSArgs})""")
                  if (!value.isInstanceOf[NLNumber]) {
                    engine.executeScript("window.switchToAdvPicker()")
                  }
              }

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
            |  dialogBackground:            "${color(_.toolbarBackground()            )}"
            |, dialogText:                  "${color(_.dialogText()                   )}"
            |, tabBackground:               "${color(_.tabBackground()                )}"
            |, tabBackgroundHover:          "${color(_.tabBackgroundHover()           )}"
            |, tabBackgroundSelected:       "${color(_.tabBackgroundSelected()        )}"
            |, tabBorder:                   "${color(_.tabBorder()                    )}"
            |, tabText:                     "${color(_.tabText()                      )}"
            |, tabTextSelected:             "${color(_.tabTextSelected()              )}"
            |, controlBackground:           "${color(_.toolbarControlBackground()     )}"
            |, controlBackgroundActive:     "${color(_.toolbarControlFocus()          )}"
            |, controlBackgroundHover:      "${color(_.toolbarControlBackgroundHover())}"
            |, controlBorder:               "${color(_.toolbarControlBorder()         )}"
            |, controlText:                 "${color(_.toolbarText()                  )}"
            |, dropdownArrow:               "${color(_.toolbarText()                  )}"
            |, genericBorder:               "${color(_.plotBorder()                   )}"
            |, outputBackground:            "${color(_.colorPickerOutputBackground())}"
            |, checkmarkColor:              "${color(_.colorPickerCheckmark())}"
            |, copyHover:                   "${color(_.colorPickerCopyHover())}"
            |, okButtonBackground:          "${color(_.primaryButtonBackground())}"
            |, okButtonBackgroundHover:     "${color(_.primaryButtonBackgroundHover())}"
            |, okButtonBorder:              "${color(_.primaryButtonBorder())}"
            |, okButtonText:                "${color(_.primaryButtonText())}"
            |, okButtonActive:              "${color(_.primaryButtonBackgroundPressed())}"
            |, cancelButtonBackground:      "${color(_.secondaryButtonBackground())}"
            |, cancelButtonBackgroundHover: "${color(_.secondaryButtonBackgroundHover())}"
            |, cancelButtonBorder:          "${color(_.secondaryButtonBorder())}"
            |, cancelButtonText:            "${color(_.secondaryButtonText())}"
            |, cancelButtonActive:          "${color(_.secondaryButtonBackgroundPressed())}"
            })""".stripMargin
      ))
    })

  }

  override def setVisible(visible: Boolean): Unit = {
    if (visible)
      Analytics.colorPickerOpen()

    super.setVisible(visible)
  }

  private class Bridge {

    def onPick(x: String): Unit = {
      EventQueue.invokeLater(() => {
        pickCallback(x)
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
        cancelCallback()
        setVisible(false)
      })
    }

  }

}

sealed trait NLColorValue {
  def toColor:  Color
  def toJSArgs: String
}

case class NLNumber(value: Double) extends NLColorValue {
  override def toColor  = NLColor.getColor(Double.box(value))
  override def toJSArgs = s""""number", ${value}"""
}

object NLNumber {

  def fromJavaColor(color: Color): NLNumber = {
    val vec      = Vector(color.getRed, color.getGreen, color.getBlue, color.getAlpha)
    val list     = LogoList.fromVector(vec.map(Int.box))
    val argbMask = CoreColor.getARGBIntByRGBAList(list)
    val colorNum = CoreColor.getClosestColorNumberByARGB(argbMask)
    NLNumber(colorNum)
  }

  def fromMask(rgbMask: Int): NLNumber = {
    fromJavaColor(new Color(rgbMask))
  }

}

case class RGB(r: Double, g: Double, b: Double) extends NLColorValue {
  override def toColor  = new Color(r.toInt, g.toInt, b.toInt)
  override def toJSArgs = s""""rgb", { red: $r, green: $g, blue: $b }"""
}

// All components are in [0, 255] --Jason B. (4/16/25)
case class RGBA(r: Double, g: Double, b: Double, a: Double) extends NLColorValue {
  override def toColor  = new Color(r.toInt, g.toInt, b.toInt, a.toInt)
  override def toJSArgs = s""""rgba", { red: $r, green: $g, blue: $b, alpha: ${Math.round(a / RGBA.MaxAlpha * 100)} }"""
}

object RGBA {

  val MaxAlpha = 255

  def fromJavaColor(color: Color): RGBA = {
    RGBA(color.getRed, color.getGreen, color.getBlue, color.getAlpha)
  }

  def fromMask(rgbMask: Int): RGBA = {
    fromJavaColor(new Color(rgbMask))
  }

}


sealed trait JFXCPConfig

case object DoubleOnly extends JFXCPConfig
case object CopyOnly   extends JFXCPConfig
case object NumAndRGBA extends JFXCPConfig
