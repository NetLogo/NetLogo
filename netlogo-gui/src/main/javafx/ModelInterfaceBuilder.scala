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
import org.nlogo.internalapi.{ AddProcedureRun, CompiledButton => ApiCompiledButton, CompiledModel }

object ModelInterfaceBuilder {

  def build(compiledModel: CompiledModel): (Pane, Map[String, ButtonControl]) = {
    val model = compiledModel.model
    val interfacePane = new Pane()
    val widgetsMap =
      compiledModel.compiledWidgets.flatMap {
        case compiledButton: ApiCompiledButton =>
          val button = new ButtonControl(compiledButton, compiledModel.runnableModel)
          val b = compiledButton.widget
          button.relocate(b.left, b.top)
          interfacePane.getChildren.add(button)
          Seq(compiledButton.procedureTag -> button)
        case other =>
          other.widget match {
            case s: CoreSlider  =>
              val slider = new SliderControl(s, compiledModel.runnableModel)
              slider.relocate(s.left, s.top)
              interfacePane.getChildren.add(slider)
            case v: CoreView    =>
              import javafx.scene.paint.Color
              val d = compiledModel.model.view.dimensions
              val c = new Canvas(d.width * d.patchSize, d.height * d.patchSize)
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
