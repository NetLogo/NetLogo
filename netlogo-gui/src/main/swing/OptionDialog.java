// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

import java.util.Arrays;
import java.util.List;

public strictfp class OptionDialog<T>
    extends UserDialog {
  javax.swing.JComboBox<T> options;

  public OptionDialog(java.awt.Frame owner, String title,
                      String message, T[] choices, scala.Function1<String, String> i18n) {
    super(owner, title, i18n);
    options = new javax.swing.JComboBox<T>(choices);
    addComponents(options, message);
  }

  public Object showOptionDialog() {
    java.awt.Rectangle r = getOwner().getBounds();
    setLocation
        (r.x + (r.width / 2) - (getWidth() / 2),
            r.y + (r.height / 2) - (getHeight() / 2));
    setVisible(true);
    if (selection == 0) {
      return Integer.valueOf(options.getSelectedIndex());
    } else {
      return null;
    }
  }

  public static int show(java.awt.Component owner, String title,
                         String message, Object[] options) {
    List<String> brokenLines = org.nlogo.awt.LineBreaker.breakLines
        (message,
            owner.getFontMetrics(owner.getFont()), DIALOG_WIDTH);
    StringBuilder brokenMessage = new StringBuilder();
    for (int i = 0; i < brokenLines.size(); i++) {
      brokenMessage.append(brokenLines.get(i));
      if (i + 1 < brokenLines.size()) {
        brokenMessage.append("\n");
      }
    }
    return javax.swing.JOptionPane.showOptionDialog
        (org.nlogo.awt.Hierarchy.getFrame(owner),
            brokenMessage.toString(), title,
            javax.swing.JOptionPane.DEFAULT_OPTION,
            javax.swing.JOptionPane.QUESTION_MESSAGE,
            null, options, options[0]);
  }

  public static int showIgnoringCloseBox(java.awt.Component owner, String title,
                                         String message, Object[] options, boolean asList) {
    int result = -1;
    while (result == -1) {
      if (asList) {
        result = showAsList(owner, title, message, options);
      } else {
        result = show(owner, title, message, options);
      }
    }
    return result;
  }

  public static int showAsList(java.awt.Component owner, String title,
                               String message, Object[] options) {
    return Arrays.asList(options).indexOf
        (javax.swing.JOptionPane.showInputDialog
            (org.nlogo.awt.Hierarchy.getFrame(owner),
                message, title,
                javax.swing.JOptionPane.QUESTION_MESSAGE,
                null, options, options[0]));
  }

}
