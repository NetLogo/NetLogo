// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.event.{ ActionEvent, EventHandler }
import javafx.beans.binding.Bindings
import javafx.beans.property.{ DoubleProperty, ObjectProperty, SimpleBooleanProperty }
import javafx.beans.value.{ ChangeListener, ObservableValue }
import javafx.fxml.{ FXML, FXMLLoader }
import javafx.scene.control.{ ButtonBase, ToggleButton }
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{ Background, BackgroundFill, StackPane }
import javafx.scene.paint.Color
import java.lang.{ Double => JDouble }

import org.nlogo.core.{ Button => CoreButton }
import org.nlogo.internalapi.{ AddProcedureRun, CompiledButton => ApiCompiledButton, JobDone, JobErrored, ModelAction, ModelUpdate, RunComponent, RunnableModel, StopProcedure }

// TODO: Figure out a way to disable until ticks start (if appropriate)
class ButtonControl(compiledButton: ApiCompiledButton, runnableModel: RunnableModel, foreverInterval: DoubleProperty)
  extends StackPane with RunComponent {

  @FXML
  var button: ButtonBase = _

  val buttonModel = compiledButton.widget

  val activeProperty = new SimpleBooleanProperty(false)

  var stoppingTag: Option[String] = None
  var jobTag: Option[String] = None

  val triggerStart = new EventHandler[ActionEvent] {
    def handle(e: ActionEvent): Unit = {
      activeProperty.set(true)
      startProcedureRun(foreverInterval.doubleValue)
    }
  }

  val triggerStop = new EventHandler[ActionEvent] {
    def handle(e: ActionEvent): Unit = {
      jobTag.foreach { tag => runnableModel.submitAction(StopProcedure(tag), ButtonControl.this) }
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
  }

  def tagAction(action: ModelAction, actionTag: String): Unit = {
    action match {
      case AddProcedureRun(_, _, _) => jobTag = Some(actionTag)
      case _ =>
    }
  }

  def updateReceived(update: ModelUpdate): Unit = {
    update match {
      case JobDone(t) =>
        if (stoppingTag.contains(t)) {
          stoppingTag = None
        }
        if (jobTag.contains(t)) {
          jobTag = None
          activeProperty.set(false)
        }
      case JobErrored(t, _) =>
        if (stoppingTag.contains(t)) {
          stoppingTag = None
        }
        if (jobTag.contains(t)) {
          jobTag = None
          activeProperty.set(false)
        }
      case _ =>
    }
  }

  foreverInterval.addListener(new ChangeListener[Number] {
    override def changed(observable: ObservableValue[_ <: Number], oldValue: Number, newValue: Number): Unit = {
      if (activeProperty.getValue.booleanValue) {
        for {
          tag <- jobTag
        } {
          stoppingTag = Some(tag)
          runnableModel.submitAction(StopProcedure(tag), ButtonControl.this)
          startProcedureRun(newValue.doubleValue)
        }
      }
    }
  })

  private def startProcedureRun(interval: Double): Unit = {
    runnableModel.submitAction(AddProcedureRun(compiledButton.procedureTag, buttonModel.forever, interval.toLong), this)
  }
}
