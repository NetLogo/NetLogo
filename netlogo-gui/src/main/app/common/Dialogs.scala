// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.common

import java.awt.Component

import org.nlogo.awt.UserCancelException
import org.nlogo.core.I18N
import org.nlogo.swing.OptionDialog

object Dialogs {
  @throws[UserCancelException]
  def userWantsToSaveFirst(file: String, parent: Component) = {
    val options = {
      implicit val i18nPrefix = I18N.Prefix("common.buttons")
      Array[AnyRef](I18N.gui("save"), I18N.gui("discard"), I18N.gui("cancel"))
    }
    val message = I18N.gui.getN("file.save.offer.confirm", file)
    OptionDialog.showMessage(parent, "NetLogo", message, options) match {
      case 0 => true
      case 1 => false
      case _ => throw new UserCancelException
    }
  }
}
