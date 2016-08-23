// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.api.{ AnonymousProcedure, World }
import org.nlogo.core.I18N
import org.nlogo.awt.UserCancelException
import org.nlogo.swing.OptionDialog

/**
 * Detect if an exported world will contain any anonymous procedures.  If it will, warn the user,
 * since the file won't re-import.
 */
object TaskWarning {
  val buttons = Array[AnyRef](I18N.gui.get("common.buttons.continue"),
                              I18N.gui.get("common.buttons.cancel"))
  val message =
    "Some of your agent variables contain anonymous procedures. You may export anyway, " +
    "but import-world won't be able to read the file."
  def maybeWarn(parent: java.awt.Component, world: World) {
    def confirmed =
      0 == OptionDialog.show(parent, "Exporting Anonymous Procedures", message, buttons)
    if(world.allStoredValues.exists(_.isInstanceOf[AnonymousProcedure]) && !confirmed)
      throw new UserCancelException
  }
}
