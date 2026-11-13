// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.editor

import java.awt.event.{ ActionEvent, KeyEvent }
import javax.swing.AbstractAction

import org.nlogo.core.LocaleContext
import org.nlogo.swing.UserAction.{ EditCategory, EditFormatGroup, KeyBindings, MenuAction }

class ShowUsageAction(editorArea: AdvancedEditorArea, colorizer: Colorizer)
  extends AbstractAction(loc"Show Usage") with MenuAction {

  category = EditCategory
  group = EditFormatGroup
  accelerator = KeyBindings.keystroke(KeyEvent.VK_U, withMenu = true)
  mnemonic = 'W'

  override def actionPerformed(e: ActionEvent): Unit = {
    new ShowUsage(editorArea, colorizer)
  }
}
