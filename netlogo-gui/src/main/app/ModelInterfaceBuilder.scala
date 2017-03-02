// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.app

import javafx.fxml.FXML
import javafx.event.{ ActionEvent, EventHandler }
import javafx.scene.control.{ Button, ScrollPane, Slider }
import javafx.scene.canvas.Canvas
import javafx.scene.layout.Pane
import javafx.stage.{ FileChooser, Window }

import org.nlogo.core.{
  I18N, Model, Button => CoreButton, Chooser => CoreChooser, InputBox => CoreInputBox,
  Monitor => CoreMonitor, Output => CoreOutput, Plot => CorePlot, Slider => CoreSlider,
  TextBox => CoreTextBox, View => CoreView, Widget => CoreWidget }

object ModelInterfaceBuilder {

  def build(model: Model): Pane = {
    val interfacePane = new Pane()
    model.widgets.foreach {
      case b: CoreButton  =>
        val button = new Button(b.display orElse b.source getOrElse "")
        button.relocate(b.left, b.top)
        interfacePane.getChildren.add(button)
      case s: CoreSlider  =>
        // TODO: This only allows for sliders with constant mins and maxes
        val slider = new Slider(s.min.toDouble, s.max.toDouble, s.default)
        slider.relocate(s.left, s.top)
        interfacePane.getChildren.add(slider)
      case v: CoreView    =>
        import javafx.scene.paint.Color
        val width  = v.right - v.left
        val height = v.bottom - v.top
        val c = new Canvas(width, height)
        c.relocate(v.left, v.top)
        interfacePane.getChildren.add(c)
        val gc = c.getGraphicsContext2D
        gc.setFill(Color.BLACK)
        gc.fillRect(0, 0, c.getWidth, c.getHeight)
      case other      =>
    }
    interfacePane
  }

}
