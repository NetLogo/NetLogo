// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import org.nlogo.api.{ I18N, Task, World }
import org.nlogo.awt.UserCancelException
import org.nlogo.swing.OptionDialog

/**
 * Detect if an exported world will contain any tasks.  If it will, warn the user,
 * since the file won't re-import.
 */
object TaskWarning {
  val buttons = Array[AnyRef](I18N.gui.get("common.buttons.continue"),
                              I18N.gui.get("common.buttons.cancel"))
  val message =
    "Some of your agent variables contain tasks. You may export anyway, " +
    "but import-world won't be able to read the file."
  def maybeWarn(parent: java.awt.Component, world: World) {
    def confirmed =
      0 == OptionDialog.show(parent, "Exporting Tasks", message, buttons)
    if(world.allStoredValues.exists(_.isInstanceOf[Task]) && !confirmed)
      throw new UserCancelException
  }
}
