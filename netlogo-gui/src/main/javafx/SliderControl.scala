// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import java.lang.{ Boolean => JBoolean, Double => JDouble }
import java.util.concurrent.atomic.AtomicReference

import javafx.beans.value.ObservableValue
import javafx.beans.property.DoubleProperty
import javafx.event.{ Event, EventHandler }
import javafx.fxml.{ FXML, FXMLLoader }
import javafx.geometry.Orientation
import javafx.scene.control.{ Label, Slider }
import javafx.scene.layout.GridPane
import javafx.scene.input.TouchEvent

import com.sun.javafx.scene.control.skin.SliderSkin

import org.nlogo.internalapi.{ CompiledSlider => ApiCompiledSlider, Monitorable,
  RunComponent, RunnableModel, UpdateInterfaceGlobal }

import org.nlogo.core.{ Slider => CoreSlider }
import Utils.{ changeListener, handler }

// TODO: This only allows for sliders with constant mins and maxes
class SliderControl(compiledSlider: ApiCompiledSlider, runnableModel: RunnableModel)
  extends GridPane
  with RunComponent {

  @FXML
  var slider: Slider = _

  @FXML
  var nameLabel: Label = _

  @FXML
  var valueLabel: Label = _

  val model = compiledSlider.widget

  val currentValue = new AtomicReference[JDouble](Double.box(model.default))

  protected var actionTags = Set.empty[String]

  locally {
    val loader = new FXMLLoader(getClass.getClassLoader.getResource("Slider.fxml"))
    loader.setController(this)
    loader.setRoot(this)
    loader.load()
    setPrefSize(model.right - model.left, model.bottom - model.top)
    nameLabel.setText(model.display orElse model.variable getOrElse "")
    slider.setSnapToTicks(true)
    slider.setMinorTickCount(0)

    bindSliderToLabel(slider, valueLabel)
    slider.valueProperty.setValue(compiledSlider.value.defaultValue)
    compiledSlider.value.onUpdate(updateValue _)
    bindPropertyToMonitorable(slider.minProperty,           compiledSlider.min)
    bindPropertyToMonitorable(slider.maxProperty,           compiledSlider.max)
    bindPropertyToMonitorable(slider.majorTickUnitProperty, compiledSlider.inc)

    slider.valueProperty.addListener(changeListener {
      (o: ObservableValue[_ <: Number], oldValue: Number, newValue: Number) =>
        if (! slider.isValueChanging) {
        currentValue.set(Double.box(newValue.doubleValue))
        model.variable.foreach { variableName =>
          runnableModel.submitAction(UpdateInterfaceGlobal(variableName.toUpperCase, currentValue))
        }
      }
    })
  }

  protected def bindPropertyToMonitorable(d: DoubleProperty, m: Monitorable[Double]): Unit = {
    d.setValue(m.defaultValue)
    m.onUpdate({ updated => d.setValue(updated) })
  }

  protected def updateValue(updatedValue: Double): Unit = {
    if (! slider.isValueChanging && actionTags.isEmpty) {
      slider.setValue(updatedValue)
    }
  }

  protected def bindSliderToLabel(s: Slider, l: Label): Unit = {
    def adjustSlider(te: TouchEvent): Unit = {
      s.getSkin match {
        case ss: SliderSkin =>
          if (ss.getSkinnable.getOrientation == Orientation.HORIZONTAL)
            s.adjustValue((te.getTouchPoint.getX / s.getWidth) * (s.getMax - s.getMin) + s.getMin)
          else
            s.adjustValue((te.getTouchPoint.getY / s.getHeight) * (s.getMax - s.getMin) + s.getMin)
        case _ =>
      }
    }
    s.setOnTouchMoved(handler(adjustSlider _))
    s.setOnTouchPressed(handler(adjustSlider _))
    s.valueProperty.addListener(changeListener {
      (observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number) =>
        l.textProperty.setValue(newValue.toString.take(5) + model.units.map(" " + _).getOrElse(""))
    })
    l.setText(s.getValue.toString.take(5) + model.units.map(" " + _).getOrElse(""))
  }

  def tagAction(action: org.nlogo.internalapi.ModelAction,actionTag: String): Unit = {
    actionTags = actionTags + actionTag
  }

  def updateReceived(update: org.nlogo.internalapi.ModelUpdate): Unit = {
    actionTags = actionTags - update.tag
  }
}
