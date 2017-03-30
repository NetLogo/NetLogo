// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.event.{ ActionEvent, EventHandler }
import javafx.beans.binding.Bindings
import javafx.beans.property.{ ObjectProperty, SimpleBooleanProperty }
import javafx.fxml.{ FXML, FXMLLoader }
import javafx.scene.control.{ ButtonBase, ToggleButton }
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{ Background, BackgroundFill, StackPane }
import javafx.scene.paint.Color

import org.nlogo.core.{ Button => CoreButton }
import org.nlogo.internalapi.{ AddProcedureRun, CompiledButton => ApiCompiledButton, ModelAction, ModelUpdate, RunComponent, RunnableModel, StopProcedure }

object ButtonControl {
  sealed trait ButtonState
  case object Inactive extends ButtonState
  case object Active extends ButtonState
}

import ButtonControl._

// TODO: Figure out a way to disable until ticks start (if appropriate)
class ButtonControl(compiledButton: ApiCompiledButton, runnableModel: RunnableModel) extends StackPane with RunComponent {
  val activeBackgroundColor = Color.web("#1F6A99")
  val inactiveBackgroundColor = Color.web("#BACFF3")

  @FXML
  var button: ButtonBase = _

  val buttonModel = compiledButton.widget

  val activeProperty = new SimpleBooleanProperty(false)

  var jobTag: Option[String] = None

  val triggerStart = new EventHandler[ActionEvent] {
    def handle(e: ActionEvent): Unit = {
      activeProperty.set(true)
      runnableModel.submitAction(AddProcedureRun(compiledButton.procedureTag, buttonModel.forever), ButtonControl.this)
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
      case AddProcedureRun(_, _) => jobTag = Some(actionTag)
      case _ =>
    }
  }

  def updateReceived(update: ModelUpdate): Unit = {
    jobTag = None
    activeProperty.set(false)
  }
}
