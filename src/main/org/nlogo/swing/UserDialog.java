// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.swing;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.util.Iterator;
import java.util.List;

public strictfp class UserDialog
    extends javax.swing.JDialog {
  int selection = 0;
  static final int DIALOG_WIDTH = 400;

  private final scala.Function1<String, String> i18n;

  public UserDialog(java.awt.Frame owner, String title, scala.Function1<String, String> i18n) {
    super(owner, title, true);
    this.i18n = i18n;
  }

  void addComponents(java.awt.Component comp, String message) {
    GridBagLayout layout = new GridBagLayout();
    GridBagConstraints c = new GridBagConstraints();
    setLayout(layout);

    c.gridwidth = 1;
    c.gridheight = 1;
    c.gridx = 2;
    c.gridy = 1;
    c.anchor = GridBagConstraints.WEST;
    c.ipadx = 10;
    c.ipady = 0;
    c.insets = new java.awt.Insets(2, 10, 5, 5);
    List<String> brokenLines = org.nlogo.awt.LineBreaker.breakLines
        (message,
            getFontMetrics(getFont()), DIALOG_WIDTH);
    for (Iterator<String> iter = brokenLines.iterator();
         iter.hasNext();) {
      javax.swing.JLabel m = new javax.swing.JLabel(iter.next());
      layout.setConstraints(m, c);
      add(m);
      c.gridy++;
    }
    c.fill = GridBagConstraints.HORIZONTAL;
    layout.setConstraints(comp, c);
    add(comp);
    javax.swing.JPanel buttons = new javax.swing.JPanel();
    buttons.add(new javax.swing.JButton(new ButtonPressAction(i18n.apply("common.buttons.halt"), 2)));
    buttons.add(new javax.swing.JButton(new ButtonPressAction(i18n.apply("common.buttons.ok"), 0)));
    c.fill = GridBagConstraints.NONE;
    c.gridy++;
    c.anchor = GridBagConstraints.EAST;
    layout.setConstraints(buttons, c);
    add(buttons);
    javax.swing.ImageIcon icon = new javax.swing.ImageIcon
        (UserDialog.class.getResource("/images/arrowhead.gif"));
    javax.swing.JLabel label = new javax.swing.JLabel(icon);
    c.gridheight = c.gridy;
    c.gridx = 1;
    c.gridy = 1;
    c.anchor = GridBagConstraints.CENTER;
    layout.setConstraints(label, c);
    add(label);
    pack();
    setResizable(false);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
  }

  private class ButtonPressAction
      extends javax.swing.AbstractAction {
    int i;

    ButtonPressAction(String name, int i) {
      super(name);
      this.i = i;
    }

    public void actionPerformed(java.awt.event.ActionEvent e) {
      selection = i;
      UserDialog.this.dispose();
    }
  }
}
