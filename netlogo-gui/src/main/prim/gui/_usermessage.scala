// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ Dump, ReporterRunnable, Syntax }
import org.nlogo.core.I18N
import org.nlogo.nvm.{ Command, Context }
import org.nlogo.swing.OptionDialog
import org.nlogo.window.GUIWorkspace

class _usermessage extends Command {

  def syntax =
    Syntax.commandSyntax(Array(Syntax.WildcardType))

  override def perform(context: Context) {
    val message = Dump.logoObject(args(0).report(context))
    workspace match {
      case gw: GUIWorkspace =>
        gw.updateUI()
        val canceled = workspace.waitForResult(
          new ReporterRunnable[java.lang.Boolean] {
            override def run = {
              gw.view.mouseDown(false)
              Boolean.box(1 ==
                OptionDialog.show(gw.getFrame, "User Message", message,
                                  Array(I18N.gui.get("common.buttons.ok"),
                                        I18N.gui.get("common.buttons.halt"))))
            }}).booleanValue
        if(canceled)
          throw new org.nlogo.nvm.HaltException(true)
      case _ =>
        // if not in GUI, just do nothing
    }
    context.ip = next
  }

}
