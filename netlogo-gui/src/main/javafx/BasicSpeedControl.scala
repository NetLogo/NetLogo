// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.fxml.{ FXML, FXMLLoader }
import javafx.beans.binding.{ Bindings, DoubleBinding }
import javafx.beans.property.{ SimpleDoubleProperty, SimpleIntegerProperty, SimpleLongProperty }
import javafx.scene.layout.{ GridPane }
import javafx.scene.control.{ Label, Slider }

class BasicSpeedControl extends GridPane {
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
  }

  val SecondsDelayWhenSpeedAtZero = 5
  val FramesSkippedWhenSpeedAtFull = 100

  // The SpeedSlider value property ranges between 0 and 100 (it's currently set to snap to intervals of 10,
  // but that may change in the future)
  //
  // foreverInterval is the milliseconds between runs of a foreverButtons job (always approximate...)
  // we want this to be ~ 5 seconds (= 5000 ms) when speed is 0 and 0 when speed is 50
  val foreverInterval: SimpleDoubleProperty = new SimpleDoubleProperty(0)
  val frameSkips: SimpleIntegerProperty = new SimpleIntegerProperty(0)

  foreverInterval.bind(Bindings.max(0d, speedSlider.valueProperty.negate.multiply(100).add(SecondsDelayWhenSpeedAtZero * 1000)))
  frameSkips.bind(Bindings.max(0, speedSlider.valueProperty.add(-50).multiply(FramesSkippedWhenSpeedAtFull.toFloat / 50.0f)))
}
