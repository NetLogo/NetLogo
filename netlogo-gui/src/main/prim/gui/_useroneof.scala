// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ Dump, ReporterRunnable, Syntax }
import org.nlogo.core.I18N
import org.nlogo.nvm.{ Context, EngineException, HaltException, Reporter }
import org.nlogo.window.GUIWorkspace

class _useroneof extends Reporter {

  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.WildcardType, Syntax.ListType),
                          Syntax.WildcardType)

  override def report(context: Context) = {
    val choiceMessage = args(0).report(context)
    val list = argEvalList(context, 1)
    workspace match {
      case gw: GUIWorkspace =>
        if(list.isEmpty)
          throw new EngineException(context, this, I18N.errors.get("org.nlogo.prim.etc.$common.emptyList"))
        val items = list.map(Dump.logoObject).toArray[AnyRef]
        gw.updateUI()
        val choice = workspace.waitForResult(
          new ReporterRunnable[AnyRef] {
            override def run() = {
              gw.view.mouseDown(false)
              new org.nlogo.swing.OptionDialog(
                  gw.getFrame, "User One Of", Dump.logoObject(choiceMessage),
                  items, I18N.gui.fn)
                .showOptionDialog()
            }})
        val index =
          Option(choice).map(_.asInstanceOf[java.lang.Integer].intValue).getOrElse(
            throw new HaltException(true))
        list.get(index)
      case _ =>
        throw new EngineException(
          context, this, "You can't get user input headless.")
    }
  }

}
