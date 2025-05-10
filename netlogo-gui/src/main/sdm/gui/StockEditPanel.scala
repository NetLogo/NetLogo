// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.window.{ BooleanEditor, EditPanel, IdentifierEditor, NonEmptyCodeEditor, PropertyAccessor,
                          PropertyEditor }

class StockEditPanel(target: StockFigure, compiler: CompilerServices, colorizer: Colorizer)
  extends EditPanel(target) {

  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.sdm.name"),
        () => target.nameWrapper,
        target.nameWrapper(_),
        () => apply()),
      compiler)

  private val initialValueExpressionWrapper =
    new NonEmptyCodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.sdm.initialValue"),
        () => target.initialValueExpressionWrapper,
        target.initialValueExpressionWrapper(_),
        () => apply()),
      colorizer)

  private val allowNegative =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.sdm.allowNegative"),
        () => target.allowNegative,
        target.allowNegative(_),
        () => apply()))

  locally {
    val c = new GridBagConstraints

    c.gridx = 0
    c.gridwidth = 2
    c.fill = GridBagConstraints.HORIZONTAL
    c.weightx = 1
    c.insets = new Insets(6, 6, 6, 6)

    add(nameWrapper, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(initialValueExpressionWrapper, c)

    c.anchor = GridBagConstraints.WEST

    add(allowNegative, c)

    nameWrapper.requestFocus()
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, initialValueExpressionWrapper, allowNegative)

  override def isResizable: Boolean = true
}
