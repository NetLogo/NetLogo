// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo(UTF8)

package org.nlogo.javafx

import javafx.event.{ ActionEvent, EventHandler }
import javafx.beans.binding.Bindings
import javafx.beans.property.SimpleBooleanProperty
import javafx.beans.value.{ ChangeListener, ObservableValue }
import javafx.fxml.{ FXML, FXMLLoader }
import javafx.scene.control.{ ToggleButton }
import javafx.scene.layout.{ HBox, Pane, StackPane, VBox }

import java.lang.{ Boolean => JBoolean }

import scala.collection.JavaConverters._

import org.nlogo.internalapi.WritableGUIWorkspace

class InterfaceArea(val speedControl: BasicSpeedControl) extends StackPane {
  @FXML
  var controlPane: HBox = _

  @FXML
  var controlContainer: VBox = _

  @FXML
  var controlsToggle: ToggleButton = _

  @FXML
  var interfacePane: Pane = _

  val controlsShowing = new SimpleBooleanProperty(false)

  val showControls = new EventHandler[ActionEvent] {
    def handle(e: ActionEvent): Unit = {
      controlsShowing.setValue(true)
    }
  }

  val hideControls = new EventHandler[ActionEvent] {
    def handle(e: ActionEvent): Unit = {
      controlsShowing.setValue(false)
    }
  }

  locally {
    val loader = new FXMLLoader(getClass.getClassLoader.getResource("InterfaceArea.fxml"))
    loader.setController(this)
    loader.setRoot(this)
    loader.load()
    controlsToggle.setOnAction(showControls)
    controlsToggle.onActionProperty.bind(
      Bindings.when(controlsShowing).`then`(hideControls).otherwise(showControls))
    controlsToggle.textProperty.bind(
      Bindings.when(controlsShowing).`then`("Hide Controls").otherwise("Show Controls"))
    controlsShowing.addListener(new ChangeListener[JBoolean] {
      override def changed(observable: ObservableValue[_ <: JBoolean], oldValue: JBoolean, newValue: JBoolean): Unit = {
        if (newValue.booleanValue) {
          controlPane.getChildren.add(speedControl)
        } else {
          controlPane.getChildren.remove(speedControl)
        }
      }
    })
  }

  controlContainer.opacityProperty.bind(Bindings.when(controlsShowing).`then`(1).otherwise(0.75))

  def getViewCanvas: Option[Canvas] =
    interfacePane.getChildren().asScala.collect {
      case c: Canvas => c
    }.headOption

  def registerMouseEventSink(workspace: WritableGUIWorkspace): Unit =
    getViewCanvas.foreach(_.attachToWorkspace(workspace))

  val ticks = speedControl.ticks
}
