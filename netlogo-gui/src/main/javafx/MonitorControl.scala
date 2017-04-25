// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.fxml.{ FXML, FXMLLoader }
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleDoubleProperty
import javafx.scene.control.Label
import javafx.scene.layout.VBox
import javafx.scene.text.Font

import org.nlogo.core.{ Monitor => CoreMonitor }
import org.nlogo.internalapi.{ CompiledMonitor => ApiCompiledMonitor, ModelUpdate }

class MonitorControl(compiledMonitor: ApiCompiledMonitor) extends VBox {

  @FXML
  var nameLabel: Label = _

  @FXML
  var valueLabel: Label = _

  val monitorModel = compiledMonitor.widget

  val fontSizeProperty = new SimpleDoubleProperty(11.0)

  val fontProperty =
      Bindings.createObjectBinding({ () => new Font(fontSizeProperty.getValue)}, fontSizeProperty)

  locally {
    val loader = new FXMLLoader(getClass.getClassLoader.getResource("Monitor.fxml"))
    loader.setController(this)
    loader.setRoot(this)
    loader.load()
    setPrefSize(monitorModel.right - monitorModel.left, monitorModel.bottom - monitorModel.top)
    nameLabel.setText(monitorModel.display orElse monitorModel.source getOrElse "")
    nameLabel.fontProperty.bind(fontProperty)
    valueLabel.setText("0")
    valueLabel.fontProperty.bind(fontProperty)
    fontSizeProperty.setValue(monitorModel.fontSize)
    compiledMonitor.onUpdate({ (s: String) =>
      valueLabel.setText(s)
    })
  }
}
