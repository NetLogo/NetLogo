// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, Insets }

import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer

class ButtonEditPanel(target: ButtonWidget, colorizer: Colorizer) extends WidgetEditPanel(target) {
  private val agentOptions =
    new OptionsEditor[String](
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.agents"),
        () => target.agentOptions,
        target.setAgentOptions(_),
        () => apply()))

  private val forever =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.forever"),
        () => target.forever,
        target.setForever(_),
        () => apply()))

  private val goTime =
    new BooleanEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.disable"),
        () => target.goTime,
        target.setGoTime(_),
        () => apply()))

  private val wrapSource =
    new CodeEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.commands"),
        () => target.wrapSource,
        target.setWrapSource(_),
        () => apply()),
      colorizer)

  private val name =
    new StringEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.displayName"),
        () => target.name,
        target.setVarName(_),
        () => apply()))

  private val actionKey =
    new KeyEditor(
      new PropertyAccessor(
        target,
        I18N.gui.get("edit.button.actionKey"),
        () => target.actionKey,
        target.setActionKey(_),
        () => apply()))

  locally {
    val c = new GridBagConstraints

    c.gridy = 0
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(6, 6, 6, 6)

    add(agentOptions, c)

    c.weightx = 1
    c.insets = new Insets(6, 0, 6, 6)

    add(forever, c)

    c.gridy = 1
    c.gridwidth = 2
    c.insets = new Insets(0, 6, 6, 6)

    add(goTime, c)

    c.gridy = 2
    c.fill = GridBagConstraints.HORIZONTAL

    add(wrapSource, c)

    wrapSource.requestFocus()

    c.gridy = 3

    add(name, c)

    c.gridy = 4
    c.fill = GridBagConstraints.NONE

    add(actionKey, c)
  }

  override def propertyEditors: Seq[PropertyEditor[_]] =
    Seq(agentOptions, forever, goTime, wrapSource, name, actionKey)

  override def isResizable: Boolean = true
}
