// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing
import com.formdev.flatlaf.FlatDarkLaf  //FlatDarkLaf
import java.awt.{ Color, Frame, Insets }
import java.awt.event.KeyEvent

import javax.swing.{ Action, BorderFactory, ImageIcon, InputMap, JComponent, JDialog,
  JWindow, KeyStroke, UIManager, UnsupportedLookAndFeelException }

final object Utils {
  val utilsClass = getClass
  def icon(path: String): ImageIcon = new ImageIcon(utilsClass.getResource(path))
  def icon(path: String, w: Int, h: Int): ImageIcon = new CenteredImageIcon(icon(path), w, h)

  def alert(message: String, continueText: String): Unit = {
    val bogusFrame = new Frame
    bogusFrame.pack() // otherwise OptionDialog will fail to get font metrics
    OptionDialog.showMessage(bogusFrame, "Notice", message, Array(continueText))
  }

  def alert(title: String, message: String, details: String, continueText: String): Unit = {
    val bogusFrame = new Frame
    bogusFrame.pack() // otherwise OptionDialog will fail to get font metrics
    OptionDialog.showMessage(bogusFrame, title, s"$message\n\n$details", Array(continueText))
  }

  /// Swing look & feel

  def setSystemLookAndFeel(): Unit = {
    try {
      // this slider thing is a workaround for Java bug parade bug #6465237 - ST 1/20/09
      UIManager.put("Slider.paintValue", false)
      if (System.getProperty("os.name").startsWith("Mac")) {
        //val lookAndFeel = System.getProperty("netlogo.swing.laf", UIManager.getSystemLookAndFeelClassName)
        UIManager.getDefaults.put("TabbedPane.foreground", new Color(0, 0, 0))
        UIManager.getDefaults.put("TabbedPane.selectedTabPadInsets", new Insets(0,0,-2,0))
        UIManager.getDefaults.put("TabbedPane.contentBorderInsets", new Insets(0,-10,-13,-9))
        // The java defaults specify this as black on my system,
        // which was distractingly bad for the name field of the "PlotPen" JTable when
        // that field was clicked, moved off and then released - RG 7/1/16
        UIManager.getDefaults.put("Table.focusCellBackground", new Color(202, 202, 202))
        //UIManager.setLookAndFeel(lookAndFeel)

       // UIManager.setLookAndFeel(new FlatDarkLaf())
       FlatDarkLaf.setup();

      } else if (System.getProperty("os.name").startsWith("Windows")) {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
      } else if (System.getProperty("swing.defaultlaf") == null) {
        try {
          // On Linux (the likely system for this switch), or at least on Gnome, when dark
          // mode is enabled, NetLogo looks very wonky as it's not setup to properly
          // handle it with all its custom UI elements.  So we need to fall back to good
          // ol' Nimbus.  -Jeremy B September 2022 SEO: dark mode, dark theme
          UIManager.setLookAndFeel(new FlatDarkLaf())
        } catch {
          case _: Exception => UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName)
        }
      }
      // else do nothing, if swing.defaultlaf is set it should be respected.
      // this allows Linux/others users to set whatever L&F they want from
      // netlogo.sh - ST 12/15/09
    } catch {
      case ex @ (_:UnsupportedLookAndFeelException | _:IllegalAccessException |
                 _:ClassNotFoundException | _:InstantiationException) =>
        throw new IllegalStateException(ex)
    }
  }

  /// borders

  private val WidgetBorder = BorderFactory.createCompoundBorder(
    BorderFactory.createMatteBorder(1, 1, 0, 0, Color.GRAY),
    BorderFactory.createRaisedBevelBorder)

  private val WidgetPressedBorder = BorderFactory.createCompoundBorder(
    BorderFactory.createMatteBorder(1, 1, 0, 0, java.awt.Color.GRAY),
    BorderFactory.createLoweredBevelBorder)

  def createWidgetBorder() = WidgetBorder
  def createWidgetPressedBorder() = WidgetPressedBorder

  /// Esc key handling in dialogs

  def addEscKeyAction(dialog: JDialog, action: Action): Unit =
    addEscKeyAction(dialog.getRootPane, action)

  def addEscKeyAction(window: JWindow, action: Action): Unit =
    addEscKeyAction(window.getRootPane, action)

  def addEscKeyAction(component: JComponent, action: Action): Unit =
    addEscKeyAction(component, component.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW), action)

  def addEscKeyAction(component: JComponent, inputMap: InputMap, action: Action): Unit = {
    inputMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0, false), "ESC_ACTION")
    component.getActionMap.put("ESC_ACTION", action)
  }
}
