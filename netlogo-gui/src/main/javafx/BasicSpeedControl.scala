// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import java.lang.{ Boolean => JBoolean }

import javafx.fxml.{ FXML, FXMLLoader }
import javafx.beans.binding.{ Bindings, DoubleBinding }
import javafx.beans.property.{ SimpleDoubleProperty, SimpleIntegerProperty, SimpleLongProperty }
import javafx.collections.FXCollections
import javafx.scene.layout.{ GridPane }
import javafx.scene.control.{ Label, Slider }

import Utils.changeListener

class BasicSpeedControl extends GridPane {
  val SecondsDelayWhenSpeedAtZero = 1
  val MillisPerSecond = 1000
  val HalfStepCount = 5
  val MaxFPSPerPosition = FXCollections.observableIntegerArray(1000, 100, 25, 20, 10, 5)

  // The SpeedSlider value property ranges between 0 and 100 (it's currently set to snap to intervals of 10,
  // but that may change in the future)
  //
  // foreverInterval is the milliseconds between runs of a foreverButtons job (always approximate...)
  // we want this to be ~ 1 seconds (= 1000 ms) when speed is 0 and 0 when speed is 50
  val foreverInterval: SimpleDoubleProperty = new SimpleDoubleProperty(0)
  // updatesPerSecond is the max FPS sent to the view. We want this to be FullSpeedMaxFPS
  // when the slider is 100 and 1000 (all frames) when speed is 50.
  // Note the point here is to use the extra frames to slow down the engine
  // (this isn't the right way to put brakes on the engine, but it works well).
  // The view has separate code (see FilterUpdateThread) to ensure it isn't drawing
  // more frames than necessary per second.
  // Note that this is an extremely crude method of limiting speed. We should
  // really be taking into account the typical length between updates and taking a multiple
  // of that. Nonetheless, this is easy to implement, so in it goes for now.
  val updatesPerSecond: SimpleIntegerProperty = new SimpleIntegerProperty(0)

  @FXML
  var speedSlider: Slider = _

  @FXML
  var tickCount: Label = _

  @FXML
  var normalSpeed: Label = _

  @FXML
  var slower: Label = _

  @FXML
  var faster: Label = _

  val ticks = new SimpleLongProperty(-1)

  locally {
    val loader = new FXMLLoader(getClass.getClassLoader.getResource("BasicSpeedControl.fxml"))
    loader.setController(this)
    loader.setRoot(this)
    loader.load()
    tickCount.textProperty.bind(
      Bindings.when(Bindings.equal(ticks, -1))
        .`then`("")
        .otherwise(Bindings.concat(ticks)))
    normalSpeed.visibleProperty.bind(
      Bindings.and(
        Bindings.greaterThan(speedSlider.valueProperty, 30),
        Bindings.lessThan(speedSlider.valueProperty, 70)))
    slower.visibleProperty.bind(Bindings.lessThanOrEqual(speedSlider.valueProperty, 30))
    faster.visibleProperty.bind(Bindings.greaterThanOrEqual(speedSlider.valueProperty, 70))

    speedSlider.valueProperty.addListener(changeListener { (newValue: Number) =>
      if (! speedSlider.isValueChanging) {
        val delayInMillis = SecondsDelayWhenSpeedAtZero * MillisPerSecond
        foreverInterval.set(
          0d max (delayInMillis -
            (delayInMillis * newValue.doubleValue / (HalfStepCount * 10))))
      }
    })

    updatesPerSecond.bind(
      Bindings.integerValueAt(MaxFPSPerPosition,
        Bindings.max(0, speedSlider.valueProperty.divide(10).subtract(5))))
  }
}
