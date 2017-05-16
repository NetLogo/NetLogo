// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import javafx.scene.control.{ ChoiceBox, Label }
import javafx.fxml.{ FXML, FXMLLoader }
import javafx.scene.layout.GridPane
import javafx.util.StringConverter

import org.nlogo.core.{ Chooser => CoreChooser, Chooseable, ChooseableBoolean, ChooseableDouble, ChooseableList, ChooseableString }
import org.nlogo.api.Dump

class ChooserControl(chooser: CoreChooser) extends GridPane {

  @FXML
  var label: Label = _

  @FXML
  var choice: ChoiceBox[Chooseable] = _

  val converter =
    new StringConverter[Chooseable] {
      def fromString(s: String) = ???
      def toString(c: Chooseable): String =
        c match {
          case ChooseableBoolean(b) => b.toString
          case ChooseableDouble(d)  => d.toString
          case ChooseableString(s)  => s
          case ChooseableList(l)    => Dump.logoObject(l, true, false)
        }
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
  }
}

