// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.sdm.gui

import org.nlogo.api.{ CompilerServices, ExtensionManager }
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ BoxAlign, BoxRow }
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

  add(nameWrapper)
  add(initialValueExpressionWrapper)
  add(new BoxRow(allowNegative, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(nameWrapper, initialValueExpressionWrapper, allowNegative)

  override def isResizable: Boolean = true

  override def requestFocus(): Unit = {
    nameWrapper.requestFocus()
  }
}
