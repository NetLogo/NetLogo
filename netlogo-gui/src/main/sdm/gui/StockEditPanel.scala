// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import javax.swing.BoxLayout

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ VerticalStrut, ZoomableBorder }
import org.nlogo.window.{ BooleanEditor, EditPanel, IdentifierEditor, NonEmptyCodeEditor, PropertyAccessor,
                          PropertyEditor }

class StockEditPanel(target: StockFigure, compiler: CompilerServices, colorizer: Colorizer,
                     extensionManager: ExtensionManager) extends EditPanel(target) {

  private val nameWrapper =
    new IdentifierEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.sdm.name"),
        () => target.nameWrapper,
        name => target.nameWrapper(name.getOrElse("")),
        () => apply()),
      compiler, extensionManager)

  private val initialValueExpressionWrapper =
    new NonEmptyCodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.sdm.initialValue"),
        () => target.initialValueExpressionWrapper,
        _.foreach(target.initialValueExpressionWrapper),
        () => apply()),
      compiler, colorizer)

  private val allowNegative =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("tools.sdm.allowNegative"),
        () => target.allowNegative,
        _.foreach(target.allowNegative),
        () => apply()))

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(nameWrapper)
  add(new VerticalStrut(6))
  add(initialValueExpressionWrapper)
  add(new VerticalStrut(6))
  add(allowNegative)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, initialValueExpressionWrapper, allowNegative)

  override def isResizable: Boolean = true

  override def requestFocus(): Unit = {
    nameWrapper.requestFocus()
  }
}
