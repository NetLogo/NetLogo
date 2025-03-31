// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import java.awt.{ GridBagConstraints, GridBagLayout, Insets }

import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer

class ButtonEditPanel(target: ButtonWidget, colorizer: Colorizer) extends WidgetEditPanel(target) {
  private val agentOptions =
    new OptionsEditor[String](
      new PropertyAccessor(
        I18N.gui.get("edit.button.agents"),
        () => target.agentOptions,
        target.agentOptions(_),
        () => apply()))

  private val forever =
    new BooleanEditor(
      new PropertyAccessor(
        I18N.gui.get("edit.button.forever"),
        () => target.forever,
        target.forever(_),
        () => apply()))

  private val goTime =
    new BooleanEditor(
      new PropertyAccessor(
        I18N.gui.get("edit.button.disable"),
        () => target.goTime,
        target.goTime(_),
        () => apply()))

  private val wrapSource =
    new CodeEditor(
      new PropertyAccessor(
        I18N.gui.get("edit.button.commands"),
        () => target.wrapSource,
        target.wrapSource(_),
        () => apply()),
      colorizer)

  private val name =
    new StringEditor(
      new PropertyAccessor(
        I18N.gui.get("edit.button.displayName"),
        () => target.name,
        target.name(_),
        () => apply()))

  private val actionKey =
    new KeyEditor(
      new PropertyAccessor(
        I18N.gui.get("edit.button.actionKey"),
        () => target.actionKey,
        target.actionKey(_),
        () => apply()))

  locally {
    setLayout(new GridBagLayout)

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

  override def syncTheme(): Unit = {
    agentOptions.syncTheme()
    forever.syncTheme()
    goTime.syncTheme()
    wrapSource.syncTheme()
    name.syncTheme()
    actionKey.syncTheme()
  }
}
