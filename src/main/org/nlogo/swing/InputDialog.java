package org.nlogo.swing;

public strictfp class InputDialog
    extends UserDialog {
  private final javax.swing.JTextField field = new javax.swing.JTextField();

  public InputDialog(java.awt.Frame owner, String title, String message, scala.Function1<String, String> i18n) {
    super(owner, title, i18n);
    addComponents(field, message);
  }

  public static String show(java.awt.Component owner, String title,
                            String message, String defaultInput, String[] options) {
    return (String) javax.swing.JOptionPane.showInputDialog
        (org.nlogo.awt.Utils.getFrame(owner),
            message, title,
            javax.swing.JOptionPane.QUESTION_MESSAGE,
            null, null, defaultInput);
  }

  public String showInputDialog() {
    java.awt.Rectangle r = getOwner().getBounds();
    setLocation
        (r.x + (r.width / 2) - (getWidth() / 2),
            r.y + (r.height / 2) - (getHeight() / 2));
    setVisible(true);
    if (selection == 0) {
      return field.getText();
    } else {
      return null;
    }
  }
}
