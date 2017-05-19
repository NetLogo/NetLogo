// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.event.{ ActionEvent, EventHandler }
import javafx.beans.binding.Bindings
import javafx.beans.property.{ DoubleProperty, ObjectProperty, SimpleBooleanProperty }
import javafx.beans.value.{ ChangeListener, ObservableValue }
import javafx.fxml.{ FXML, FXMLLoader }
import javafx.scene.control.{ ButtonBase, ToggleButton }
import javafx.scene.layout.StackPane
import java.lang.{ Double => JDouble }

import org.nlogo.core.{ Button => CoreButton }
import org.nlogo.internalapi.{ CompiledButton => ApiCompiledButton }

class ButtonControl(compiledButton: ApiCompiledButton) extends StackPane {

  @FXML
  var button: ButtonBase = _

  val buttonModel = compiledButton.widget

  val activeProperty = new SimpleBooleanProperty(false)

  var stoppingTag: Option[String] = None
  var jobTag: Option[String] = None

  val triggerStart = new EventHandler[ActionEvent] {
    def handle(e: ActionEvent): Unit = {
      compiledButton.start()
    }
  }

  val triggerStop = new EventHandler[ActionEvent] {
    def handle(e: ActionEvent): Unit = {
      compiledButton.stop()
    }
  }

  locally {
    val loader = new FXMLLoader(getClass.getClassLoader.getResource(
      if (buttonModel.forever) "ForeverButton.fxml" else "Button.fxml"))
    loader.setController(this)
    loader.setRoot(this)
    loader.load()
    button.setText(buttonModel.display orElse buttonModel.source getOrElse "")
    setPrefSize(buttonModel.right - buttonModel.left, buttonModel.bottom - buttonModel.top)
    button.setOnAction(triggerStart)
    button.onActionProperty.bind(
      Bindings.when(activeProperty).`then`(triggerStop).otherwise(triggerStart))
    if (buttonModel.disableUntilTicksStart) {
      button.setDisable(! compiledButton.ticksEnabled.currentValue)
      compiledButton.ticksEnabled.onUpdate { (enabled) =>
        button.setDisable(! enabled)
      }
    }
    compiledButton.isRunning.onUpdate(runningChanged _)
    compiledButton.isRunning.onError(runningErrored _)
  }

  def runningChanged(isRunning: Boolean) = {
    activeProperty.set(isRunning)
    if (! isRunning)
      popOut()
  }

  def popOut(): Unit = {
    button match {
      case t: ToggleButton => t.setSelected(false)
      case _ =>
    }
  }

  def runningErrored(e: Exception) = {
    println("button errored: " + e)
    e.printStackTrace()
  }
}
