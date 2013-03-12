// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.nlogo.swing.BrowserLauncher;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public strictfp class UploadSuccessDialog extends JDialog {

  //GUI form members
  private JPanel topLevelContainer;
  private JButton OKButton;
  private JButton openModelButton;
  private JLabel successLabel;
  private JLabel errorLabel;

  //Data members
  private ModelingCommons communicator;
  private String uploadedModelName;
  private String uploadedModelURL;
  private Frame frame;

  public UploadSuccessDialog(Frame frame, ModelingCommons communicator, String errorLabelText, String uploadedModelURL, String uploadedModelName) {
    super(frame, "Upload To Modeling Commons Successful", true);
    this.frame = frame;
    this.communicator = communicator;
    this.uploadedModelURL = uploadedModelURL;
    this.uploadedModelName = uploadedModelName;
    initializeGUIComponents();
    successLabel.setText(String.format("Model '%s' created successfully", uploadedModelName));
    errorLabel.setText(errorLabelText);
    getRootPane().setDefaultButton(OKButton);
    openModelButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        onOpenModel();
      }

    });
    OKButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        onClose();
      }

    });
    //call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {

      public void windowClosing(WindowEvent e) {
        onClose();
      }

    });
    //call onCancel() on ESCAPE
    topLevelContainer.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        onClose();
      }

    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    setModal(true);
    pack();
    setLocationRelativeTo(frame);
    setResizable(true);
  }

  private void onOpenModel() {
    BrowserLauncher.openURL(frame, uploadedModelURL, false);
    dispose();
  }

  private void onClose() {
    dispose();
  }

  private void setMaxHeightToPreferredHeight(JComponent component) {
    component.setMaximumSize(new Dimension((int) component.getMaximumSize().getWidth(), (int) component.getPreferredSize().getHeight()));
  }

  private void initializeGUIComponents() {
    topLevelContainer = new JPanel();
    topLevelContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    topLevelContainer.setLayout(new BoxLayout(topLevelContainer, BoxLayout.Y_AXIS));
    setContentPane(topLevelContainer);

    JPanel successPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

    topLevelContainer.add(successPanel);
    topLevelContainer.add(errorPanel);
    topLevelContainer.add(new Box.Filler(new Dimension(0, 0), new Dimension(400, 80), new Dimension(Integer.MAX_VALUE, Integer.MAX_VALUE)));
    topLevelContainer.add(buttonsPanel);

    successLabel = new JLabel();
    successPanel.add(successLabel);
    setMaxHeightToPreferredHeight(successPanel);

    errorLabel = new JLabel();
    errorLabel.setForeground(Color.RED);
    errorPanel.add(errorLabel);
    setMaxHeightToPreferredHeight(errorPanel);

    openModelButton = new JButton("Open Model");
    OKButton = new JButton("OK");

    buttonsPanel.add(Box.createHorizontalGlue());
    buttonsPanel.add(openModelButton);
    buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonsPanel.add(OKButton);
    setMaxHeightToPreferredHeight(buttonsPanel);
  }

}

