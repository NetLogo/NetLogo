// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import javafx.scene.text.{ Font, Text }
import javafx.scene.paint.Color
import javafx.fxml.{ FXML, FXMLLoader }
import javafx.scene.layout.Pane

import org.nlogo.api.{ Color => ApiColor }
import org.nlogo.core.{ TextBox => CoreTextBox }

class TextBox(textBox: CoreTextBox) extends Pane {

  @FXML
  var pane: Pane = _

  @FXML
  var text: Text = _

  locally {
    val loader = new FXMLLoader(getClass.getClassLoader.getResource("TextBox.fxml"))
    loader.setController(this)
    loader.setRoot(this)
    loader.load()

    val font =
      if (System.getProperty("os.name").startsWith("Mac")) Font.font("Lucida Grande", textBox.fontSize)
      else Font.font("Sans-serif", textBox.fontSize)

    text.setText(textBox.display.getOrElse(""))
    text.setFont(font)
    text.setWrappingWidth(textBox.right - textBox.left)
    val c = ApiColor.getColor(Double.box(textBox.color))
    text.setFill(Color.rgb(c.getRed, c.getGreen, c.getBlue))
    pane.setPrefWidth(textBox.right - textBox.left)
    pane.setPrefHeight(textBox.bottom - textBox.top)
    if (textBox.transparent) pane.getStyleClass.add("transparent")
  }

}
