// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ Dump, ReporterRunnable }
import org.nlogo.core.I18N
import org.nlogo.nvm.{ Context, HaltException, Reporter, RuntimePrimitiveException }
import org.nlogo.swing.OptionPane
import org.nlogo.window.GUIWorkspace

class _useryesorno extends Reporter {

  override def report(context: Context) =
    workspace match {
      case gw: GUIWorkspace =>
        val yesNoMessage = args(0).report(context)
        gw.updateUI()
        val result: java.lang.Boolean = workspace.waitForResult(
          new ReporterRunnable[java.lang.Boolean] {
            override def run = {
              gw.view.mouseDown(false)
              val response = new OptionPane(gw.getFrame, I18N.gui.get("dialog.userYesOrNo"),
                                            Dump.logoObject(yesNoMessage),
                                            List(I18N.gui.get("common.buttons.yes"),
                                                 I18N.gui.get("common.buttons.no"),
                                                 I18N.gui.get("common.buttons.halt")),
                                            OptionPane.Icons.Question).getSelectedIndex
              response match {
                case 0 => java.lang.Boolean.TRUE
                case 1 => java.lang.Boolean.FALSE
                case _ => null
              }}})
        Option(result).getOrElse(throw new HaltException(true))
      case _ =>
        throw new RuntimePrimitiveException(
          context, this, "You can't get user input headless.")
    }
}
