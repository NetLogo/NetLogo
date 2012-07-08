// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ Dump, I18N, ReporterRunnable, Syntax }
import org.nlogo.nvm.{ Context, EngineException, HaltException, Reporter }
import org.nlogo.swing.OptionDialog
import org.nlogo.window.GUIWorkspace

class _useryesorno extends Reporter {
  override def syntax =
    Syntax.reporterSyntax(Array(Syntax.WildcardType),
                          Syntax.BooleanType)
  override def report(context: Context) =
    workspace match {
      case gw: GUIWorkspace =>
        val yesNoMessage = args(0).report(context)
        gw.updateUI()
        val result: java.lang.Boolean = workspace.waitForResult(
          new ReporterRunnable[java.lang.Boolean] {
            override def run = {
              gw.view.mouseDown(false)
              val response = OptionDialog.showIgnoringCloseBox(
                gw.getFrame, "User Yes or No",
                Dump.logoObject(yesNoMessage),
                Array(I18N.gui.get("common.buttons.yes"),
                      I18N.gui.get("common.buttons.no"),
                      I18N.gui.get("common.buttons.halt")),
                false)
              response match {
                case 0 => java.lang.Boolean.TRUE
                case 1 => java.lang.Boolean.FALSE
                case _ => null
              }}})
        Option(result).getOrElse(throw new HaltException(true))
      case _ =>
        throw new EngineException(
          context, this, "You can't get user input headless.")
    }
}
