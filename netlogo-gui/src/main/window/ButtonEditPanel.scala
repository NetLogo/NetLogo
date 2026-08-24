// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.Dimension
import javax.swing.{ Box, BoxLayout }

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ BoxRow, HorizontalStrut, VerticalStrut, ZoomableBorder }

class ButtonEditPanel(target: ButtonWidget, compiler: CompilerServices, colorizer: Colorizer)
  extends WidgetEditPanel(target) {

  private val agentOptions =
    new OptionsEditor[String](
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.agents"),
        () => target.agentOptions,
        _.foreach(target.setAgentOptions),
        () => apply()))

  private val forever =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.forever"),
        () => target.forever,
        _.foreach(target.setForever),
        () => apply()))

  private val goTime =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.disable"),
        () => target.goTime,
        _.foreach(target.setGoTime),
        () => apply()))

  private val wrapSource =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.commands"),
        () => target.wrapSource,
        _.foreach(target.setWrapSource),
        () => apply()),
      compiler, colorizer, err = () => target.error())

  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.displayName"),
        () => target.name,
        _.foreach(target.setVarName),
        () => apply()))

  private val actionKey =
    new KeyEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.actionKey"),
        () => target.actionKey,
        _.foreach(target.setActionKey),
        () => apply()))

  private val oldSize =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.general.oldSize"),
        () => target.oldSize,
        _.foreach(target.oldSize),
        () => apply()))

  setLayout(new BoxLayout(this, BoxLayout.Y_AXIS))
  setBorder(new ZoomableBorder(6, 6, 6, 6))

  add(new BoxRow(Seq(agentOptions, new HorizontalStrut(6), forever)) {
    override def getMaximumSize: Dimension =
      new Dimension(super.getMaximumSize.width, getPreferredSize.height)
  })
  add(new VerticalStrut(6))
  add(goTime)
  add(new VerticalStrut(6))
  add(wrapSource)
  add(new VerticalStrut(6))
  add(name)
  add(new VerticalStrut(6))
  add(new BoxRow(Seq(actionKey, Box.createHorizontalGlue)))
  add(new VerticalStrut(6))
  add(oldSize)

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(agentOptions, forever, goTime, wrapSource, name, actionKey, oldSize)

  override def isResizable: Boolean = true

  override def requestFocus(): Unit = {
    wrapSource.requestFocus()
  }
}
