// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.prim.gui

import org.nlogo.api.{ Dump, ReporterRunnable }
import org.nlogo.core.LocaleContext
import org.nlogo.nvm.{ Context, HaltException, Reporter, RuntimePrimitiveException }
import org.nlogo.swing.DropdownOptionPane
import org.nlogo.window.GUIWorkspace

class _useroneof extends Reporter {



  override def report(context: Context) = {
    val choiceMessage = args(0).report(context)
    val list = argEvalList(context, 1)
    workspace match {
      case gw: GUIWorkspace =>
        if(list.isEmpty)
          throw new RuntimePrimitiveException(context, this, loc"List is empty.")
        val items = list.map(Dump.logoObject).toList
        gw.updateUI()
        val choice = workspace.waitForResult(
          new ReporterRunnable[Int] {
            override def run() = {
              gw.view.mouseDown(false)
              new DropdownOptionPane(gw.getFrame, loc"User One Of", Dump.logoObject(choiceMessage), items)
                .getChoiceIndex
            }})
        if (choice == -1)
          throw new HaltException(true)
        list.get(choice)
      case _ =>
        throw new RuntimePrimitiveException(
          context, this, "You can't get user input headless.")
    }
  }

}
