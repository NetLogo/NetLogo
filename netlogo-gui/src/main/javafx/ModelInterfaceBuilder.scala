// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.fxml.FXML
import javafx.event.{ ActionEvent, EventHandler }
import javafx.scene.control.{ Button, ScrollPane, Slider }
import javafx.scene.layout.Pane
import javafx.scene.text.Text
import javafx.stage.{ FileChooser, Window }

import org.nlogo.core.{
  I18N, Model, Button => CoreButton, Chooser => CoreChooser, InputBox => CoreInputBox,
  Monitor => CoreMonitor, Output => CoreOutput, Plot => CorePlot, Slider => CoreSlider,
  TextBox => CoreTextBox, View => CoreView, Widget => CoreWidget }
import org.nlogo.internalapi.{
  CompiledButton  => ApiCompiledButton, CompiledModel,
  CompiledChooser => ApiCompiledChooser,
  CompiledMonitor => ApiCompiledMonitor,
  CompiledSlider  => ApiCompiledSlider }

object ModelInterfaceBuilder {
  def build(compiledModel: CompiledModel): (InterfaceArea) = {
    val model = compiledModel.model
    val speedControl  = new BasicSpeedControl()
    val interfaceArea = new InterfaceArea(speedControl)
    val widgetsMap =
      compiledModel.compiledWidgets.foreach {
        case compiledButton: ApiCompiledButton =>
          val button = new ButtonControl(compiledButton, speedControl.foreverInterval)
          val b = compiledButton.widget
          button.relocate(b.left, b.top)
          interfaceArea.addControl(button)
        case compiledMonitor: ApiCompiledMonitor =>
          val monitor = new MonitorControl(compiledMonitor)
          val m = compiledMonitor.widget
          monitor.relocate(m.left, m.top)
          interfaceArea.addControl(monitor)
        case compiledSlider: ApiCompiledSlider =>
          val slider = new SliderControl(compiledSlider)
          val s = compiledSlider.widget
          slider.relocate(s.left, s.top)
          interfaceArea.addControl(slider)
        case compiledChooser: ApiCompiledChooser =>
          val chooser = new ChooserControl(compiledChooser)
          val c = compiledChooser.widget
          chooser.relocate(c.left, c.top)
          interfaceArea.addControl(chooser)
        case other =>
          other.widget match {
            case v: CoreView    =>
              val d = compiledModel.model.view.dimensions
              val c = new Canvas(model.view)
              c.relocate(v.left, v.top)
              interfaceArea.addControl(c)
            case coreText: CoreTextBox =>
              val t = new TextBox(coreText)
              t.relocate(coreText.left, coreText.top)
              interfaceArea.addControl(t)
            case _ =>
          }
      }
    interfaceArea
  }
}
