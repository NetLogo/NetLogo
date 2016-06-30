// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

import java.awt.Color;
import javax.swing.UIManager;

public final strictfp class Utils {

  // this class is not instantiable
  private Utils() {
    throw new IllegalStateException();
  }

  public static void alert(String message, String continueText) {
    java.awt.Frame bogusFrame = new java.awt.Frame();
    bogusFrame.pack(); // otherwise OptionDialog will fail to get font metrics
    OptionDialog.show
        (bogusFrame, "Notice",
            message,
            new String[]{continueText});
  }

  public static void alert(String title, String message, String details, String continueText) {
    java.awt.Frame bogusFrame = new java.awt.Frame();
    bogusFrame.pack(); // otherwise OptionDialog will fail to get font metrics
    message = message + "\n\n" + details;
    OptionDialog.show
        (bogusFrame, title,
            message,
            new String[]{continueText});
  }

  /// Swing look & feel

  public static void setSystemLookAndFeel() {
    try {
      // this slider thing is a workaround for Java bug parade bug #6465237 - ST 1/20/09
      javax.swing.UIManager.put("Slider.paintValue", Boolean.FALSE);
      if (System.getProperty("os.name").startsWith("Mac")) {
        String lookAndFeel = System.getProperty("netlogo.swing.laf", javax.swing.UIManager.getSystemLookAndFeelClassName());
        UIManager.getDefaults().put("TabbedPane.foreground", new Color(0, 0, 0));
        UIManager.getDefaults().put("TabbedPane.selectedTabPadInsets", new java.awt.Insets(0,0,-2,0));
        UIManager.getDefaults().put("TabbedPane.contentBorderInsets", new java.awt.Insets(0,-10,-13,-9));
        // The java defaults specify this as black on my system,
        // which was distractingly bad for the name field of the "PlotPen" JTable when
        // that field was clicked, moved off and then released - RG 7/1/16
        UIManager.getDefaults().put("Table.focusCellBackground", new java.awt.Color(202, 202, 202));
        UIManager.setLookAndFeel(lookAndFeel);
      } else if (System.getProperty("os.name").startsWith("Windows")) {
        javax.swing.UIManager.setLookAndFeel
            (javax.swing.UIManager.getSystemLookAndFeelClassName());
      } else if (System.getProperty("swing.defaultlaf") == null) {
        try {
          javax.swing.UIManager.setLookAndFeel
              ("com.sun.java.swing.plaf.nimbus.NimbusLookAndFeel");
        } catch (Exception e)  // NOPMD
        {
          javax.swing.UIManager.setLookAndFeel
              (javax.swing.UIManager.getSystemLookAndFeelClassName());
        }
      }
      // else do nothing, if swing.defaultlaf is set it should be respected.
      // this allows Linux/others users to set whatever L&F they want from
      // netlogo.sh - ST 12/15/09
    } catch (javax.swing.UnsupportedLookAndFeelException ex) {
      throw new IllegalStateException(ex);
    } catch (IllegalAccessException ex) {
      throw new IllegalStateException(ex);
    } catch (ClassNotFoundException ex) {
      throw new IllegalStateException(ex);
    } catch (InstantiationException ex) {
      throw new IllegalStateException(ex);
    }
  }

  /// borders

  private static final javax.swing.border.Border WIDGET_BORDER =
      javax.swing.BorderFactory.createCompoundBorder
          (javax.swing.BorderFactory.createMatteBorder
              (1, 1, 0, 0, java.awt.Color.GRAY),
              javax.swing.BorderFactory.createRaisedBevelBorder());

  private static final javax.swing.border.Border WIDGET_PRESSED_BORDER =
      javax.swing.BorderFactory.createCompoundBorder
          (javax.swing.BorderFactory.createMatteBorder
              (1, 1, 0, 0, java.awt.Color.GRAY),
              javax.swing.BorderFactory.createLoweredBevelBorder());

  public static javax.swing.border.Border createWidgetBorder() {
    return WIDGET_BORDER;
  }

  public static javax.swing.border.Border createWidgetPressedBorder() {
    return WIDGET_PRESSED_BORDER;
  }

  /// Esc key handling in dialogs

  public static void addEscKeyAction(javax.swing.JDialog dialog,
                                     javax.swing.Action action) {
    addEscKeyAction(dialog.getRootPane(), action);
  }

  public static void addEscKeyAction(javax.swing.JWindow window,
                                     javax.swing.Action action) {
    addEscKeyAction(window.getRootPane(), action);
  }

  public static void addEscKeyAction(javax.swing.JComponent component,
                                     javax.swing.Action action) {
    addEscKeyAction(component,
        component.getInputMap(
            javax.swing.JComponent.WHEN_IN_FOCUSED_WINDOW),
        action);
  }

  public static void addEscKeyAction(javax.swing.JComponent component,
                                     javax.swing.InputMap inputMap,
                                     javax.swing.Action action) {
    inputMap.put
        (javax.swing.KeyStroke.getKeyStroke
            (java.awt.event.KeyEvent.VK_ESCAPE, 0, false),
            "ESC_ACTION");
    component
        .getActionMap()
        .put
            ("ESC_ACTION", action);
  }

}
