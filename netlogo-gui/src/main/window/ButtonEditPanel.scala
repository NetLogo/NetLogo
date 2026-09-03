// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.window

import org.nlogo.api.CompilerServices
import org.nlogo.core.I18N
import org.nlogo.editor.Colorizer
import org.nlogo.swing.{ BoxAlign, BoxRow, MaximumHeight }

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

  add(new BoxRow(Seq(agentOptions, forever), 6, BoxAlign.Start) with MaximumHeight)
  add(new BoxRow(goTime, BoxAlign.Start))
  add(wrapSource)
  add(name)
  add(new BoxRow(actionKey, BoxAlign.Start))
  add(new BoxRow(oldSize, BoxAlign.Start))

  override def propertyEditors: Seq[PropertyEditor[?]] =
    Seq(agentOptions, forever, goTime, wrapSource, name, actionKey, oldSize)

  override def isResizable: Boolean = true

  override def requestFocus(): Unit = {
    wrapSource.requestFocus()
  }
}
