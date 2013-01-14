package org.nlogo.modelingcommons;

import org.nlogo.swing.BrowserLauncher;
import org.nlogo.swing.ModalProgressTask;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

public class ModelingCommonsLoginDialog extends JDialog {
  private JPanel contentPane;
  private JButton cancelButton;
  private JButton loginButton;
  private JPasswordField passwordField;
  private JTextField emailField;
  private JLabel errorLabel;
  private JButton createAccountButton;
  private Frame frame;
  private ModelingCommons communicator;

  ModelingCommonsLoginDialog(Frame frame, final ModelingCommons communicator, String errorLabelText) {
    super(frame, "Login To Modeling Commons", true);
    this.communicator = communicator;
    this.frame = frame;
    setContentPane(contentPane);
    getRootPane().setDefaultButton(loginButton);

    errorLabel.setText(errorLabelText);

    createAccountButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        dispose();
        communicator.promptForCreateAccount();
      }
    });

    loginButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            onOK();
        }
    });

    cancelButton.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            onCancel();
        }
    });


    //call onCancel() when cross is clicked
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

    this.pack();
    this.setLocationRelativeTo(frame);
    this.setResizable(false);
  }

  private void onOK() {
    final String email_address = emailField.getText();
    final String password = passwordField.getText();
    dispose();
    ModalProgressTask.apply(org.nlogo.awt.Hierarchy.getFrame(this), "Connecting to Modeling Commons", new Runnable() {
      public void run() {
        final String status = communicator.login(email_address, password);
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if(status.equals("INVALID_CREDENTIALS")) {
              communicator.promptForLogin("Invalid email address or password");
            } else if(status.equals("MISSING_PARAMETERS")) {
              communicator.promptForLogin("Missing email address or password");
            } else if(status.equals("CONNECTION_ERROR")) {
              communicator.promptForLogin("Error connecting to Modeling Commons");
            } else if(status.equals("SUCCESS")) {
              communicator.promptForUpload();
            } else {
              communicator.promptForLogin("Unknown server error");
            }
          }
        });

      }
    });
  }

  private void onCancel() {
      dispose();
  }

}
