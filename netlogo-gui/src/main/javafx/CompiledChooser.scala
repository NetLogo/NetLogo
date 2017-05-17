// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.javafx

import
  org.nlogo.{ core, internalapi },
    core.{ AgentKind, Chooseable, ChooseableBoolean, ChooseableDouble, ChooseableList, ChooseableString, Chooser => CoreChooser },
    internalapi.{ CompiledChooser => ApiCompiledChooser, Monitorable, UpdateVariable }

case class CompiledChooser(
  val widget: CoreChooser,
  val value: Monitorable[Chooseable] with ReporterMonitorable,
  widgetActions: WidgetActions) extends ApiCompiledChooser {

    def anyRefToChooseable(a: AnyRef): AnyRef = Chooseable(a)

    def chooseableToAnyRef(c: Chooseable): AnyRef = {
      c match {
        case ChooseableString(s)  => s
        case ChooseableDouble(d)  => Double.box(d)
        case ChooseableBoolean(b) => Boolean.box(b)
        case ChooseableList(l)    => l
      }
    }

    def setValue(update: Chooseable): Unit = {
      for {
        name <- widget.variable
      } {
        widgetActions.runOperation(
          UpdateVariable(name, AgentKind.Observer, 0,
            chooseableToAnyRef(value.currentValue),
            chooseableToAnyRef(update)), value)
      }
    }
}
