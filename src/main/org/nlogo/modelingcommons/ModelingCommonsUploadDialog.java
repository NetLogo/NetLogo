package org.nlogo.modelingcommons;

import org.nlogo.swing.ModalProgressTask;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ModelingCommonsUploadDialog extends JDialog {
  private JPanel contentPane;
  private JButton uploadModelButton;
  private JButton cancelButton;
  private JButton logoutButton;
  private JTextField modelNameField;
  private JLabel errorLabel;
  private JLabel personNameLabel;
  private ModelingCommons communicator;
  private Frame frame;
  ModelingCommonsUploadDialog(final Frame frame, final ModelingCommons communicator, String errorLabelText) {
    super(frame, "Upload Model to Modeling Commons", true);
    this.communicator = communicator;
    this.frame = frame;
    errorLabel.setText(errorLabelText);
    personNameLabel.setText("Hello " + communicator.getPerson().getFirstName() + " " + communicator.getPerson().getLastName());
    setSize(400, 200);
    setResizable(false);

    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(uploadModelButton);

    uploadModelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onOK();
      }
    });

    cancelButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    });

    logoutButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
        ModalProgressTask.apply(org.nlogo.awt.Hierarchy.getFrame(ModelingCommonsUploadDialog.this), "Logging out of Modeling Commons", new Runnable() {
          public void run() {
            communicator.logout();
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                communicator.promptForLogin();
              }
            });
          }
        });
      }
    });

// call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        onCancel();
      }
    });

// call onCancel() on ESCAPE
    contentPane.registerKeyboardAction(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        onCancel();
      }
    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
  }
  private boolean isValidInput() {
    if(modelNameField.getText().trim().length() == 0) {
      errorLabel.setText("Missing model name");
      return false;
    }
    return true;
  }
  private void onOK() {
    if(!isValidInput()) {
      return;
    }
    dispose();
    ModalProgressTask.apply(frame, "Uploading model to Modeling Commons", new Runnable() {
      public void run() {
        final String result = communicator.uploadModel(modelNameField.getText().trim());
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if(result.equals("NOT_LOGGED_IN")) {
              communicator.promptForLogin();
            } else if(result.equals("MISSING_PARAMETERS")) {
              communicator.promptForUpload("Missing model name");
            } else if(result.equals("MODEL_NOT_SAVED")) {
              communicator.promptForUpload("Server error");
            } else if(result.equals("CONNECTION_ERROR")) {
              communicator.promptForUpload("Error connecting to Modeling Commons");
            } else if(result.equals("SUCCESS")) {
              communicator.promptForSuccess();
            } else {
              communicator.promptForUpload("Unknown server error");
            }
          }
        });
      }
    });
  }

  private void onCancel() {
// add your code here if necessary
    dispose();
  }
}
