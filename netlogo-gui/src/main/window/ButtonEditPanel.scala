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
      colorizer, err = () => target.error())

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

    c.gridy = 3

    add(name, c)

    c.gridy = 4
    c.fill = GridBagConstraints.NONE

    add(actionKey, c)

    c.gridy = 5

    add(oldSize, c)
  }

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(agentOptions, forever, goTime, wrapSource, name, actionKey, oldSize)

  override def isResizable: Boolean = true

  override def requestFocus(): Unit = {
    wrapSource.requestFocus()
  }
}
