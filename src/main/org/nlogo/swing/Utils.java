// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

import ch.randelshofer.quaqua.QuaquaManager;

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
        preloadQuaquaNativeLibraries();
        String lookAndFeel = System.getProperty("netlogo.quaqua.laf", QuaquaManager.getLookAndFeelClassName());
        javax.swing.UIManager.setLookAndFeel(lookAndFeel);
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

  // For full Quaqua functionality, including having our application icon appear
  // when we bring up a Swing alert dialog, Quaqua needs native libraries.
  // If we can't get them, it's not the end of the world, but we want them.
  // It might be possible somehow to just allow Quaqua to load them itself,
  // but that might require fiddling with java.library.path and I'm terrified
  // to do that because Esther has struggled a bunch with that in the past
  // and has a bunch of code (JavaLibraryPath, etc.) she put together in order
  // to make JOGL work, and also make extensions with native libraries (such
  // as QTJ) work, and I don't understand any of it and I'm scared of it.
  // So I think the safest thing to do is to manually load the libraries
  // ourselves here, without relying on the value of java.library.path or
  // any other settings. - ST 4/29/10
  private static void preloadQuaquaNativeLibraries() {
    try {
      String libraryName = (QuaquaManager.getOS() >= QuaquaManager.LEOPARD)
          && QuaquaManager.getProperty("os.arch").equals("x86_64")
          ? "quaqua64"
          : "quaqua";
      System.load(new java.io.File(".").getCanonicalPath() + "/lib/Mac OS X/lib" + libraryName + ".jnilib");
      System.setProperty("Quaqua.jniIsPreloaded", "true");
    } catch (java.io.IOException ex) // NOPMD
    {
      // ignore
    } catch (UnsatisfiedLinkError ex) // NOPMD
    {
      // ignore
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
