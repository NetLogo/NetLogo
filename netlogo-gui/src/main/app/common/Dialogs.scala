// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.Component

import org.nlogo.awt.UserCancelException
import org.nlogo.core.LocaleContext
import org.nlogo.swing.OptionPane

object Dialogs {
  @throws[UserCancelException]
  def userWantsToSaveFirst(file: String, parent: Component) = {
    new OptionPane(parent, loc"NetLogo", loc"Do you want to save the changes you made to $file?",
                   Seq(loc"Save", loc"Discard", loc"Cancel"), OptionPane.Icons.question)
      .getSelectedIndex match {

      case 0 => true
      case 1 => false
      case _ => throw new UserCancelException
    }
  }
}
