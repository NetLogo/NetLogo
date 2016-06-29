// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.app

import java.awt.{ BorderLayout, Frame }
import java.awt.event.{ WindowAdapter, WindowEvent }
import java.util.prefs.Preferences
import javax.swing.{ BorderFactory, Box, BoxLayout, ImageIcon, JButton,
  JComponent, JDialog, JLabel, WindowConstants, SwingConstants }

import org.nlogo.awt.Positioning
import org.nlogo.core.I18N
import org.nlogo.swing.{ OptionDialog, RichAction, TextFieldBox, Utils => SwingUtils }
import org.nlogo.swing.Implicits._

class PreferencesDialog(parent: Frame, preferences: Preference*)
extends JDialog(parent, I18N.gui.get("tools.preferences"), false) {
  private implicit val prefix = I18N.Prefix("tools.preferences")
  private val netLogoPrefs = Preferences.userRoot.node("/org/nlogo/NetLogo")

  private val reset = () => {
    preferences foreach (_.load(netLogoPrefs))
    apply()
  }
  private val ok = () => {
    apply()
    setVisible(false)
  }
  private val apply = () => preferences foreach (_.save(netLogoPrefs))
  private val cancel = () => {
    reset()
    setVisible(false)
  }

  private val preferencesPanel = new TextFieldBox(SwingConstants.TRAILING)
  preferencesPanel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10))
  preferences foreach { pref =>
    val text = (if (pref.restartRequired) I18N.gui("restartRequired") + "  " else "") +
      I18N.gui(pref.i18nKey)
    preferencesPanel.addField(text, pref.component)
  }

  private val buttonsPanel = new Box(BoxLayout.LINE_AXIS)
  buttonsPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 10, 20))
  private val okAction = RichAction(I18N.gui.get("common.buttons.ok"))(_ => ok())
  private val applyAction = RichAction(I18N.gui.get("common.buttons.apply"))(_ => apply())
  private val cancelAction = RichAction(I18N.gui.get("common.buttons.cancel"))(_ => cancel())
  buttonsPanel.add(Box.createHorizontalGlue)
  buttonsPanel.add(new JButton(okAction))
  buttonsPanel.add(Box.createHorizontalGlue)
  buttonsPanel.add(new JButton(applyAction))
  buttonsPanel.add(Box.createHorizontalGlue)
  buttonsPanel.add(new JButton(cancelAction))
  buttonsPanel.add(Box.createHorizontalGlue)

  add(preferencesPanel, BorderLayout.CENTER)
  add(buttonsPanel, BorderLayout.SOUTH)
  pack()

  reset()
  setDefaultCloseOperation(WindowConstants.HIDE_ON_CLOSE)
  SwingUtils.addEscKeyAction(this, cancel)
  addWindowListener(reset)
  Positioning.center(this, parent)
  setResizable(false)
  parent.toFront()
}
