// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ Dump, ReporterRunnable }
import org.nlogo.core.I18N
import org.nlogo.nvm.{ Command, Context }
import org.nlogo.swing.OptionPane
import org.nlogo.window.GUIWorkspace

class _usermessage extends Command {



  override def perform(context: Context) {
    val message = Dump.logoObject(args(0).report(context))
    workspace match {
      case gw: GUIWorkspace =>
        gw.updateUI()
        val canceled = workspace.waitForResult(
          new ReporterRunnable[java.lang.Boolean] {
            override def run = {
              gw.view.mouseDown(false)
              Boolean.box(new OptionPane(gw.getFrame, I18N.gui.get("common.messages.userMessage"), message,
                                         List(I18N.gui.get("common.buttons.ok"), I18N.gui.get("common.buttons.halt")),
                                         OptionPane.Icons.INFO).getSelectedIndex == 1)
            }}).booleanValue
        if(canceled)
          throw new org.nlogo.nvm.HaltException(true)
      case _ =>
        // if not in GUI, just do nothing
    }
    context.ip = next
  }

}
