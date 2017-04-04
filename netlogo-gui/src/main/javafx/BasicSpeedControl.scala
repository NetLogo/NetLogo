// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.fxml.{ FXML, FXMLLoader }
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleLongProperty
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
}
