package org.nlogo.modelingcommons;

import org.nlogo.swing.BrowserLauncher;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ModelingCommonsUploadSuccessDialog extends JDialog {
  private JPanel contentPane;
  private JButton OKButton;
  private JButton openModelButton;
  private JLabel successLabel;
  private JLabel errorLabel;
  private Frame frame;
  private ModelingCommons communicator;
  ModelingCommonsUploadSuccessDialog(Frame frame, ModelingCommons communicator, String errorLabelText) {
    super(frame, "Upload To Modeling Commons Successful", true);

    setSize(400, 200);
    setResizable(false);
    this.frame = frame;
    this.communicator = communicator;
    successLabel.setText("Model '" + communicator.getUploadedModelName() + "' created successfully");
    errorLabel.setText(errorLabelText);
    setContentPane(contentPane);
    setModal(true);
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

// call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onClose();
      }
    });

// call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onClose();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }

  private void onOpenModel() {
    BrowserLauncher.openURL(frame, communicator.getUploadedModelURL(), false);
    dispose();
  }

  private void onClose() {
// add your code here if necessary
    dispose();
  }
}
