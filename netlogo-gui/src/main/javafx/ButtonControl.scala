// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.event.ActionEvent
import javafx.beans.property.ObjectProperty
import javafx.fxml.{ FXML, FXMLLoader }
import javafx.scene.control.Label
import javafx.scene.image.ImageView
import javafx.scene.input.MouseEvent
import javafx.scene.layout.{ Background, BackgroundFill, GridPane }
import javafx.scene.paint.Color

import org.nlogo.core.{ Button => CoreButton }
import org.nlogo.internalapi.{ AddProcedureRun, CompiledButton => ApiCompiledButton, ModelAction, ModelRunner, ModelUpdate, RunComponent, RunnableModel }

object ButtonControl {
  sealed trait ButtonState
  case object Inactive extends ButtonState
  case object Active extends ButtonState
}

import ButtonControl._

// TODO: Figure out a way to disable until ticks start (if appropriate)
class ButtonControl(compiledButton: ApiCompiledButton, runnableModel: RunnableModel, modelRunner: ModelRunner) extends GridPane with RunComponent {
  val activeBackgroundColor = Color.web("#1F6A99")
  val inactiveBackgroundColor = Color.web("#BACFF3")


  @FXML
  var foreverIcon: ImageView = _

  @FXML
  var label: Label = _

  val button = compiledButton.widget

  var jobActive: Boolean = false
  var jobTag: Option[String] = None

  @FXML
  def handleClickEvent(event: MouseEvent): Unit = {
    if (! jobActive) {
      val bgFill = getBackground.getFills.get(0)
      // TODO: Leaving the screen and coming back seems to revert the background color to
      // inactive, even though it hasn't been set differently...
      setBackground(new Background(new BackgroundFill(activeBackgroundColor, bgFill.getRadii, bgFill.getInsets)))
      runnableModel.submitAction(AddProcedureRun(compiledButton.procedureTag, button.forever), this)
      jobActive = true
      // compiledModel.runnableModel.submitAction(AddProcedureRun(compiledButton.procedureTag, true))
    }
  }

  locally {
    val loader = new FXMLLoader(getClass.getClassLoader.getResource("Button.fxml"))
    loader.setController(this)
    loader.setRoot(this)
    loader.load()
    label.setText(button.display orElse button.source getOrElse "")
    setPrefSize(button.right - button.left, button.bottom - button.top)
    if (button.forever) {
      foreverIcon.setPreserveRatio(true)
      val columnConstraint = getColumnConstraints.get(2)
      val rowConstraint     = getRowConstraints.get(2)
      //only bind on height because height is basically always less than width
      foreverIcon.setFitHeight(rowConstraint.getPrefHeight)
      foreverIcon.fitHeightProperty().bind(rowConstraint.prefHeightProperty())
      foreverIcon.setOpacity(1.0)
    }
  }

  def tagAction(action: ModelAction, actionTag: String): Unit = {
    action match {
      case AddProcedureRun(_, _) => jobTag = Some(actionTag)
      case _ =>
    }
  }

  def updateReceived(update: ModelUpdate): Unit = {
    val bgFill = getBackground.getFills.get(0)
    setBackground(new Background(new BackgroundFill(inactiveBackgroundColor, bgFill.getRadii, bgFill.getInsets)))
    jobTag = None
    jobActive = false
  }
}
