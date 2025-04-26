// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app.codetab

import java.awt.{ Frame, GridBagConstraints, GridBagLayout, Insets }
import java.awt.event.{ WindowAdapter, WindowEvent }
import java.util.prefs.Preferences
import javax.swing.{ JDialog, JLabel, WindowConstants }

import org.nlogo.app.common.TabsInterface
import org.nlogo.core.I18N
import org.nlogo.swing.{ ButtonPanel, CheckBox, DialogButton, Positioning }
import org.nlogo.theme.{ InterfaceColors, ThemeSync }

class CodeTabPreferences(parent: Frame, tabs: TabsInterface)
  extends JDialog(parent, I18N.gui.get("tabs.code.preferences.title")) with ThemeSync {

  private val prefs = Preferences.userRoot.node("/org/nlogo/NetLogo")

  private val message = new JLabel(I18N.gui.get("tabs.code.preferences.message"))

  private val tabbing = new CheckBox(I18N.gui.get("tabs.code.indentAutomatically"), (selected) => {
    tabs.smartTabbingEnabled = selected
  })

  private val lineNumbers = new CheckBox(I18N.gui.get("tabs.code.editorLineNumbers"), (selected) => {
    tabs.lineNumbersVisible = selected
  })

  private val okButton = new DialogButton(true, I18N.gui.get("common.buttons.ok"), () => apply())
  private val cancelButton = new DialogButton(false, I18N.gui.get("common.buttons.cancel"), () => revert())

  locally {
    setLayout(new GridBagLayout)

    val c = new GridBagConstraints

    c.gridx = 0
    c.anchor = GridBagConstraints.WEST
    c.insets = new Insets(6, 6, 6, 6)

    add(message, c)

    c.insets = new Insets(0, 6, 6, 6)

    add(tabbing, c)
    add(lineNumbers, c)

    c.anchor = GridBagConstraints.CENTER

    add(new ButtonPanel(Seq(okButton, cancelButton)), c)

    getRootPane.setDefaultButton(okButton)

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE)

    addWindowListener(new WindowAdapter {
      override def windowClosing(e: WindowEvent): Unit = {
        revert()
      }
    })

    pack()
    syncTheme()
  }

  override def setVisible(visible: Boolean): Unit = {
    if (visible && !isVisible) {
      tabbing.setSelected(tabs.smartTabbingEnabled)
      lineNumbers.setSelected(tabs.lineNumbersVisible)

      Positioning.center(this, parent)
    }

    super.setVisible(visible)
  }

  def apply(): Unit = {
    prefs.putBoolean("indentAutomatically", tabbing.isSelected)
    prefs.putBoolean("lineNumbers", lineNumbers.isSelected)

    setVisible(false)
  }

  def revert(): Unit = {
    tabs.smartTabbingEnabled = prefs.getBoolean("indentAutomatically", true)
    tabs.lineNumbersVisible = prefs.getBoolean("lineNumbers", true)

    setVisible(false)
  }

  override def syncTheme(): Unit = {
    getContentPane.setBackground(InterfaceColors.dialogBackground())

    message.setForeground(InterfaceColors.toolbarText())
    tabbing.setForeground(InterfaceColors.toolbarText())
    lineNumbers.setForeground(InterfaceColors.toolbarText())

    okButton.syncTheme()
    cancelButton.syncTheme()
  }
}
