// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

import javax.swing.JComponent;
import java.util.ArrayList;
import java.util.List;

public strictfp class MessageDialog
    extends javax.swing.JDialog {

  private static final int DEFAULT_ROWS = 15;
  private static final int DEFAULT_COLUMNS = 60;

  protected static MessageDialog dialog;
  private final java.awt.Frame parentFrame;
  protected final javax.swing.JTextArea textArea =
      new javax.swing.JTextArea(DEFAULT_ROWS, DEFAULT_COLUMNS);

  public static MessageDialog init(java.awt.Component owner) {
    dialog = new MessageDialog(owner);
    return dialog;
  }

  public static void show(String title, String message) {
    show(title, message, DEFAULT_ROWS, DEFAULT_COLUMNS);
  }

  public static void show(String title, String message, int rows, int columns) {
    dialog.doShow(title, message, rows, columns);
  }

  protected MessageDialog(java.awt.Component owner) {
    super(org.nlogo.awt.Hierarchy.getFrame(owner));
    parentFrame = org.nlogo.awt.Hierarchy.getFrame(owner);
    addWindowListener
        (new java.awt.event.WindowAdapter() {
          @Override
          public void windowClosing(java.awt.event.WindowEvent e) {
            setVisible(false);
          }
        });
    getContentPane().setLayout(new java.awt.BorderLayout());
    textArea.setDragEnabled(false);
    textArea.setLineWrap(true);
    textArea.setWrapStyleWord(true);
    textArea.setEditable(false);
    textArea.setBorder
        (javax.swing.BorderFactory.createEmptyBorder
            (3, 5, 0, 5));
    getContentPane().add(new javax.swing.JScrollPane(textArea),
        java.awt.BorderLayout.CENTER);
    javax.swing.JPanel buttonPanel =
        new ButtonPanel
            (makeButtons().toArray(new JComponent[]{}));
    getContentPane().add(buttonPanel, java.awt.BorderLayout.SOUTH);
    pack();
  }

  protected List<JComponent> makeButtons() {
    javax.swing.Action dismissAction =
        new javax.swing.AbstractAction("Dismiss") {
          public void actionPerformed(java.awt.event.ActionEvent e) {
            setVisible(false);
          }
        };
    javax.swing.JButton dismissButton =
        new javax.swing.JButton(dismissAction);
    List<JComponent> buttons =
        new ArrayList<JComponent>();
    buttons.add(dismissButton);
    getRootPane().setDefaultButton(dismissButton);
    org.nlogo.swing.Utils.addEscKeyAction
        (this, dismissAction);
    return buttons;
  }

  private boolean firstShow = true;

  protected void doShow(String title, String message, int rows, int columns) {
    setTitle(title);
    textArea.setRows(rows);
    textArea.setColumns(columns);
    textArea.setText(message);
    textArea.setCaretPosition(0);
    pack();
    if (firstShow) {
      firstShow = false;
      org.nlogo.awt.Positioning.center(this, parentFrame);
    }
    setVisible(true);
  }

}
