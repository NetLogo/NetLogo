// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ Dump, ReporterRunnable }
import org.nlogo.nvm.{ Context, HaltException, Reporter, RuntimePrimitiveException }
import org.nlogo.swing.InputOptionPane
import org.nlogo.window.GUIWorkspace

class _userinput extends Reporter {
  override def report(context: Context) = {
    val inputMessage = args(0).report(context)
    workspace match {
      case gw: GUIWorkspace =>
        gw.updateUI()
        val result = workspace.waitForResult(
          new ReporterRunnable[Option[String]] {
            override def run(): Option[String] = {
              gw.view.mouseDown(false)
              Option(new InputOptionPane(gw.getFrame, "User Input", Dump.logoObject(inputMessage),
                                         if (args.size > 1) args(1).report(context).toString else "").getInput)
            }})
        result.getOrElse(
          throw new HaltException(true))
      case _ =>
        throw new RuntimePrimitiveException(
          context, this, "You can't get user input headless.")
    }
  }
}
