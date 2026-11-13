// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ Dump, ReporterRunnable }
import org.nlogo.core.LocaleContext
import org.nlogo.nvm.{ Context, HaltException, Reporter, RuntimePrimitiveException }
import org.nlogo.swing.OptionPane
import org.nlogo.window.GUIWorkspace

class _useryesorno extends Reporter {

  override def report(context: Context) =
    workspace match {
      case gw: GUIWorkspace =>
        val yesNoMessage = args(0).report(context)
        gw.updateUI()
        val result: Option[java.lang.Boolean] = workspace.waitForResult(
          new ReporterRunnable[Option[java.lang.Boolean]] {
            override def run() = {
              gw.view.mouseDown(false)
              val response = new OptionPane(gw.getFrame, loc"User Yes or No", Dump.logoObject(yesNoMessage),
                                            Seq(loc"Yes", loc"No", loc"Halt"), OptionPane.Icons.question)
                               .getSelectedIndex
              response match {
                case 0 => Option(java.lang.Boolean.TRUE)
                case 1 => Option(java.lang.Boolean.FALSE)
                case _ => None
              }}})
        result.getOrElse(throw new HaltException(true))
      case _ =>
        throw new RuntimePrimitiveException(
          context, this, "You can't get user input headless.")
    }
}
