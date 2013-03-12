// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;

public strictfp class LoadingDialog extends JDialog {

  //GUI form members
  private JPanel topLevelContainer;
  private JLabel label;
  private JProgressBar progressBar;

  //Data members
  private String loadingText;
  private Frame frame;

  public LoadingDialog(Frame frame, String loadingText) {
    super(frame);
    this.frame = frame;
    this.loadingText = loadingText;
    initializeGUIComponents();
    label.setText(loadingText);
    setModal(true);
    setResizable(false);
    setUndecorated(true);
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    pack();
    setLocationRelativeTo(frame);
  }

  private void initializeGUIComponents() {
    topLevelContainer = new JPanel();
    topLevelContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    topLevelContainer.setLayout(new BoxLayout(topLevelContainer, BoxLayout.Y_AXIS));
    setContentPane(topLevelContainer);

    label = new JLabel();
    label.setAlignmentX(Component.CENTER_ALIGNMENT);
    topLevelContainer.add(label);

    topLevelContainer.add(Box.createRigidArea(new Dimension(0, 10)));

    progressBar = new JProgressBar();
    progressBar.setIndeterminate(true);
    progressBar.setAlignmentX(Component.CENTER_ALIGNMENT);
    topLevelContainer.add(progressBar);
  }

}

