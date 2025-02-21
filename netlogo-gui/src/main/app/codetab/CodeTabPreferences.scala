// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ Frame, GridBagConstraints, GridBagLayout, Insets }
import javax.swing.{ JDialog, WindowConstants }

import org.nlogo.app.common.TabsInterface
import org.nlogo.core.I18N
import org.nlogo.swing.{ CheckBox, Positioning }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class CodeTabPreferences(parent: Frame, tabs: TabsInterface)
  extends JDialog(parent, I18N.gui.get("tabs.code.preferences.title")) with ThemeSync {

  private val tabbing: CheckBox = new CheckBox(I18N.gui.get("tabs.code.indentAutomatically"), () => {
    tabs.smartTabbingEnabled = tabbing.isSelected
  })

  private val lineNumbers: CheckBox = new CheckBox(I18N.gui.get("tabs.code.editorLineNumbers"), () => {
    tabs.lineNumbersVisible = lineNumbers.isSelected
  })

  locally {
    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(6, 6, 6, 6)

    add(tabbing, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(lineNumbers, c)

    tabbing.setSelected(tabs.smartTabbingEnabled)
    lineNumbers.setSelected(tabs.lineNumbersVisible)

    setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)

    pack()
    syncTheme()
  }

  override def setVisible(visible: Boolean): Unit = {
    if (visible && !isVisible)
      Positioning.center(this, parent)

    super.setVisible(visible)
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground)

    tabbing.setForeground(InterfaceColors.toolbarText)
    lineNumbers.setForeground(InterfaceColors.toolbarText)
  }
}
