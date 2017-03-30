// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.fxml.{ FXML, FXMLLoader }
import javafx.scene.control.Label
import javafx.scene.layout.VBox

import org.nlogo.core.{ Monitor => CoreMonitor }
import org.nlogo.internalapi.{ CompiledMonitor => ApiCompiledMonitor, ModelAction, ModelUpdate, RunComponent, RunnableModel }

class MonitorControl(compiledMonitor: ApiCompiledMonitor, runnableModel: RunnableModel) extends VBox with RunComponent {

  @FXML
  var nameLabel: Label = _

  @FXML
  var valueLabel: Label = _

  val monitorModel = compiledMonitor.widget

  locally {
    val loader = new FXMLLoader(getClass.getClassLoader.getResource("Monitor.fxml"))
    loader.setController(this)
    loader.setRoot(this)
    loader.load()
    setPrefSize(monitorModel.right - monitorModel.left, monitorModel.bottom - monitorModel.top)
    nameLabel.setText(monitorModel.display orElse monitorModel.source getOrElse "")
    valueLabel.setText("0")
  }

  def tagAction(action: ModelAction, actionTag: String): Unit = {
  }

  def updateReceived(update: ModelUpdate): Unit = {
  }
}
