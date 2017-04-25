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

import org.nlogo.internalapi.{ CompiledSlider => ApiCompiledSlider, Monitorable }

import org.nlogo.core.{ Slider => CoreSlider }
import Utils.{ changeListener, handler }

class SliderControl(compiledSlider: ApiCompiledSlider)
  extends GridPane {

  @FXML
  var slider: Slider = _

  @FXML
  var nameLabel: Label = _

  @FXML
  var valueLabel: Label = _

  val model = compiledSlider.widget

  val data = new SliderData(
    compiledSlider.value.currentValue,
    compiledSlider.min.currentValue,
    compiledSlider.max.currentValue,
    compiledSlider.inc.currentValue)

  var lastValue: Double = model.default

  protected val updateValueFromUI = changeListener { (n: Number) =>
    val d = n.doubleValue
    if (! updatingFromModel) {
      data.inputValueProperty.set(d)
      slider.valueProperty.setValue(data.value)
      if (lastValue != data.value) {
        lastValue = data.value
        compiledSlider.setValue(data.value)
      }
    } else if (! slider.isValueChanging) {
      lastValue = n.doubleValue
    }
  }

  // This variable and all it represents are disgusting.
  // Just like the feedback around slider values in NetLogo.
  private var updatingFromModel: Boolean = false

  protected def updateValueFromModel(updatedValue: Double): Unit = {
    updatingFromModel = true
    slider.setValue(updatedValue)
    updatingFromModel = false
  }

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
    compiledSlider.value.onUpdate(updateValueFromModel _)

    slider.valueProperty.setValue(compiledSlider.value.defaultValue)
    slider.valueProperty.addListener(updateValueFromUI)

    slider.majorTickUnitProperty.bind(data.incrementProperty)
    slider.minProperty.bind(data.minimumProperty)
    slider.maxProperty.bind(data.maximumProperty)

    bindPropertyToMonitorable(data.minimumProperty, compiledSlider.min)
    bindPropertyToMonitorable(data.maximumProperty, compiledSlider.max)
    bindPropertyToMonitorable(data.incrementProperty, compiledSlider.inc)
  }

  protected def bindPropertyToMonitorable(d: DoubleProperty, m: Monitorable[Double]): Unit = {
    d.setValue(m.defaultValue)
    m.onUpdate({ updated => d.setValue(updated) })
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
}
