// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.AbstractAction

import org.nlogo.core.I18N
import org.nlogo.swing.UserAction.{ EditCategory, EditFormatGroup, KeyBindings, MenuAction }

class ShowUsageAction(editorArea: AdvancedEditorArea, colorizer: Colorizer)
  extends AbstractAction(I18N.gui.get("tabs.code.rightclick.showUsage")) with MenuAction {

  category = EditCategory
  group = EditFormatGroup
  accelerator = KeyBindings.keystroke(KeyEvent.VK_U, withMenu = true)
  mnemonic = KeyEvent.VK_W

  override def actionPerformed(e: ActionEvent): Unit = {
    new ShowUsage(editorArea, colorizer)
  }
}
