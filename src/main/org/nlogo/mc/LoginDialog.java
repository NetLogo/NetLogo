// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.SwingConstants;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;

public strictfp class LoginDialog extends JDialog {

  //GUI form members
  private JPanel topLevelContainer;
  private JButton cancelButton;
  private JButton loginButton;
  private JPasswordField passwordField;
  private JTextField emailField;
  private JLabel errorLabel;
  private JButton createAccountButton;

  //Data members
  private ModelingCommons communicator;
  private Frame frame;

  public LoginDialog(Frame frame, final ModelingCommons communicator, String errorLabelText) {
    super(frame, "Login To Modeling Commons", true);
    this.communicator = communicator;
    this.frame = frame;
    initializeGUIComponents();
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
    topLevelContainer.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        onCancel();
      }

    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    this.pack();
    this.setLocationRelativeTo(frame);
    this.setResizable(true);
  }

  private void onOK() {
    final String emailAddress = emailField.getText();
    char[] passwordArr = passwordField.getPassword();
    final String password = new String(passwordArr);
    Arrays.fill(passwordArr, (char) 0);
    dispose();
    LoginRequest request = new LoginRequest(communicator.getHttpClient(), frame, emailAddress, password) {

      @Override
      protected void onLogin(String status, Person person) {
        if(status.equals("INVALID_CREDENTIALS")) {
          communicator.promptForLogin("Invalid email address or password");
        } else if(status.equals("MISSING_PARAMETERS")) {
          communicator.promptForLogin("Missing email address or password");
        } else if(status.equals("INVALID_RESPONSE_FROM_SERVER")) {
          communicator.promptForLogin("Invalid response from Modeling Commons");
        } else if(status.equals("CONNECTION_ERROR")) {
          communicator.promptForLogin("Could not connect to Modeling Commons");
        } else if(status.equals("SUCCESS")) {
          communicator.setPerson(person);
          communicator.promptForUpload();
        } else {
          communicator.promptForLogin("Unknown server error");
        }
      }

    };
    //Request.execute MUST come before the call to JDialog.setVisible because the setVisible call does not return if the
    //dialog is modal (which it is).
    request.execute();
  }

  private void onCancel() {
    dispose();
  }

  private void setMaxHeightToPreferredHeight(JComponent component) {
    component.setMaximumSize(new Dimension((int) component.getMaximumSize().getWidth(), (int) component.getPreferredSize().getHeight()));
  }

  private void initializeGUIComponents() {
    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1;

    topLevelContainer = new JPanel();
    topLevelContainer.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    topLevelContainer.setLayout(new BoxLayout(topLevelContainer, BoxLayout.Y_AXIS));
    setContentPane(topLevelContainer);

    JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JPanel formPanel = new JPanel(new BorderLayout());
    JPanel errorPanel = new JPanel(new FlowLayout(FlowLayout.LEADING));
    JPanel buttonsPanel = new JPanel();
    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));

    topLevelContainer.add(titlePanel);
    topLevelContainer.add(formPanel);
    topLevelContainer.add(Box.createVerticalGlue());
    topLevelContainer.add(errorPanel);
    topLevelContainer.add(buttonsPanel);

    titlePanel.add(new JLabel("You must login to upload to the Modeling Commons"));
    setMaxHeightToPreferredHeight(titlePanel);

    JPanel formLabels = new JPanel(new GridLayout(2, 1));
    formLabels.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
    JPanel formFields = new JPanel(new GridLayout(2, 1));
    formPanel.add(formLabels, BorderLayout.LINE_START);
    formPanel.add(formFields, BorderLayout.CENTER);

    formLabels.add(new JLabel("Email Address", SwingConstants.TRAILING));
    emailField = new JTextField();
    JPanel emailFieldPanel = new JPanel(new GridBagLayout());
    emailFieldPanel.add(emailField, constraints);
    formFields.add(emailFieldPanel);

    formLabels.add(new JLabel("Password", SwingConstants.TRAILING));
    passwordField = new JPasswordField();
    JPanel passwordFieldPanel = new JPanel(new GridBagLayout());
    passwordFieldPanel.add(passwordField, constraints);
    formFields.add(passwordFieldPanel);

    setMaxHeightToPreferredHeight(formPanel);

    errorLabel = new JLabel("Error");
    errorLabel.setForeground(Color.RED);
    errorPanel.add(errorLabel);
    setMaxHeightToPreferredHeight(errorPanel);

    createAccountButton = new JButton("Create Account");
    cancelButton = new JButton("Cancel");
    loginButton = new JButton("Login");

    buttonsPanel.add(createAccountButton);
    buttonsPanel.add(Box.createHorizontalGlue());
    buttonsPanel.add(cancelButton);
    buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonsPanel.add(loginButton);
    setMaxHeightToPreferredHeight(buttonsPanel);
  }

}

