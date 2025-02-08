// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.Component

import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.swing.OptionPane

object Dialogs {
  @throws[UserCancelException]
  def userWantsToSaveFirst(file: String, parent: Component) = {
    implicit val i18nPrefix = I18N.Prefix("common.buttons")
    new OptionPane(parent, I18N.gui.get("common.netlogo"), I18N.gui.getN("file.save.offer.confirm", file),
                   List(I18N.gui("save"), I18N.gui("discard"), I18N.gui("cancel")),
                   OptionPane.Icons.Info).getSelectedIndex match {
      case 0 => true
      case 1 => false
      case _ => throw new UserCancelException
    }
  }
}
