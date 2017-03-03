// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

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
import org.nlogo.internalapi.{ CompiledButton => ApiCompiledButton, CompiledModel, ModelRunner }

object ModelInterfaceBuilder {

  def build(compiledModel: CompiledModel, modelRunner: ModelRunner): (Pane, Map[String, Button]) = {
    val model = compiledModel.model
    val interfacePane = new Pane()
    val widgetsMap =
      compiledModel.compiledWidgets.flatMap {
        case compiledButton: ApiCompiledButton =>
          val b = compiledButton.widget
          val button = new Button(b.display orElse b.source getOrElse "")
          button.relocate(b.left, b.top)
          button.setOnAction(new EventHandler[ActionEvent] {
            override def handle(a: ActionEvent): Unit = {
              compiledModel.runnableModel.runTag(compiledButton.tag, modelRunner)
            }
          })
          interfacePane.getChildren.add(button)
          Seq(compiledButton.tag -> button)
        case other =>
          other.widget match {
            case s: CoreSlider  =>
              val slider = new SliderControl(s)
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
            case _ =>
          }
        Seq()
      }
    (interfacePane, widgetsMap.toMap)
  }

}
