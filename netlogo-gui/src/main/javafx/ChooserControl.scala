// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import javafx.scene.control.{ ChoiceBox, Label }
import javafx.fxml.{ FXML, FXMLLoader }
import javafx.scene.layout.GridPane
import javafx.util.StringConverter

import org.nlogo.core.{ Chooser => CoreChooser, Chooseable, ChooseableBoolean, ChooseableDouble, ChooseableList, ChooseableString }
import org.nlogo.internalapi.{ CompiledChooser => ApiCompiledChooser }
import org.nlogo.api.Dump

import Utils.changeListener

class ChooserControl(compiledChooser: ApiCompiledChooser) extends GridPane {
  val chooser = compiledChooser.widget

  private var updatingFromModel: Boolean = false

  @FXML
  var label: Label = _

  @FXML
  var choice: ChoiceBox[Chooseable] = _

  val converter =
    new StringConverter[Chooseable] {
      def fromString(s: String) = ChooseableString(s)
      def toString(c: Chooseable): String =
        c match {
          case ChooseableBoolean(b) => b.toString
          case ChooseableDouble(d)  => d.toString
          case ChooseableString(s)  => s
          case ChooseableList(l)    => Dump.logoObject(l, true, false)
        }
    }

  protected val updateValueFromUI =
    changeListener { (c: Chooseable) =>
      if (! updatingFromModel)
        compiledChooser.setValue(c)
    }

  locally {
    val loader = new FXMLLoader(getClass.getClassLoader.getResource("Chooser.fxml"))
    loader.setController(this)
    loader.setRoot(this)
    loader.load()
    setPrefWidth(chooser.right - chooser.left)
    setPrefHeight(chooser.bottom - chooser.top)
    label.setText((chooser.display orElse chooser.variable).getOrElse(""))
    choice.setConverter(converter)
    choice.getItems.addAll(chooser.choices: _*)
    choice.setValue(chooser.choices(chooser.currentChoice))
    choice.valueProperty.addListener(updateValueFromUI)
    compiledChooser.value.onUpdate(updateValueFromModel _)
  }

  protected def updateValueFromModel(updatedValue: Chooseable): Unit = {
    updatingFromModel = true
    choice.setValue(updatedValue)
    updatingFromModel = false
  }
}

