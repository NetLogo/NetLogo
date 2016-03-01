// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
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
import java.util.Calendar;

public strictfp class NewUserDialog extends JDialog {

  //GUI form members
  private JButton loginButton;
  private JButton createAccountButton;
  private JButton cancelButton;
  private JTextField emailAddressField;
  private JRadioButton femaleRadioButton;
  private JRadioButton maleRadioButton;
  private JComboBox<Integer> birthdayYearComboBox;
  private JComboBox<Month> birthdayMonthComboBox;
  private JComboBox<Integer> birthdayDayComboBox;
  private JPasswordField passwordField;
  private JPasswordField passwordConfirmField;
  private JTextField firstNameField;
  private JTextField lastNameField;
  private DisableableComboBox countryComboBox;
  private JLabel errorLabel;
  private JPanel topLevelContainer;
  private JTextPane userAgreementTextPane;
  private JScrollPane userAgreementScrollPane;
  private JRadioButton imageFromFileRadioButton;
  private JRadioButton noProfilePictureRadioButton;
  private FileSelector fileSelector;

  //Data members
  private ModelingCommons communicator;
  private Frame frame;

  public NewUserDialog(Frame frame, ModelingCommons communicator, String errorLabelText) {
    super(frame);
    this.communicator = communicator;
    this.frame = frame;
    initializeGUIComponents();
    errorLabel.setText(errorLabelText);
    getRootPane().setDefaultButton(createAccountButton);
    for(String country : communicator.getPriorityCountries()) {
      countryComboBox.addItem(country, true);
    }
    countryComboBox.addItem("--------", false);
    for(String country : communicator.getUnpriorityCountries()) {
      countryComboBox.addItem(country, true);
    }
    femaleRadioButton.setSelected(true);
    userAgreementTextPane.setText(communicator.getNewUserAgreement());
    userAgreementTextPane.setCaretPosition(0);
    int startYear = 1930;
    birthdayYearComboBox.addItem(null);
    for(int y = Calendar.getInstance().get(Calendar.YEAR); y >= startYear; y--) {
      birthdayYearComboBox.addItem(y);
    }
    birthdayMonthComboBox.addItem(null);
    for(Month month : Month.getMonths()) {
      birthdayMonthComboBox.addItem(month);
    }
    birthdayDayComboBox.addItem(null);
    for(int d = 1; d <= 31; d++) {
      birthdayDayComboBox.addItem(d);
    }
    imageFromFileRadioButton.addChangeListener(new ChangeListener() {

      @Override
      public void stateChanged(ChangeEvent changeEvent) {
        if(imageFromFileRadioButton.isSelected()) {
          fileSelector.setEnabled(true);
        } else {
          fileSelector.setEnabled(false);
        }
      }

    });
    imageFromFileRadioButton.setSelected(true);
    createAccountButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        onOK();
      }

    });
    cancelButton.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        onCancel();
      }

    });
    loginButton.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        dispose();
        NewUserDialog.this.communicator.promptForLogin();
      }

    });
    //call onCancel() when cross is clicked
    setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
    addWindowListener(new WindowAdapter() {

      public void windowClosing(WindowEvent e) {
        onCancel();
      }

    });
    //call onCancel() on ESCAPE
    topLevelContainer.registerKeyboardAction(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        onCancel();
      }

    }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
    setModal(true);
    pack();
    setLocationRelativeTo(frame);
    setResizable(true);
  }

  private boolean isValidInput() {
    if(firstNameField.getText().length() == 0) {
      errorLabel.setText("First name cannot be blank");
      return false;
    }
    if(lastNameField.getText().length() == 0) {
      errorLabel.setText("Last name cannot be blank");
      return false;
    }
    if(emailAddressField.getText().length() == 0) {
      errorLabel.setText("Email address cannot be blank");
      //Probably should do actual email address validation here
      return false;
    }
    if(passwordField.getPassword().length == 0) {
      errorLabel.setText("Password cannot be blank");
      return false;
    }
    if(!(Arrays.equals(passwordField.getPassword(), passwordConfirmField.getPassword()))) {
      errorLabel.setText("Passwords do not match");
      return false;
    }
    return true;
  }

  private void onOK() {
    if(!isValidInput()) {
      return;
    }
    dispose();
    String firstName = firstNameField.getText().trim();
    String lastName = lastNameField.getText().trim();
    String emailAddress = emailAddressField.getText().trim();
    SexOfPerson sexOfPerson;
    if(femaleRadioButton.isSelected()) {
      sexOfPerson = SexOfPerson.FEMALE;
    } else {
      sexOfPerson = SexOfPerson.MALE;
    }
    String country = (String) countryComboBox.getSelectedObject();
    Integer birthdayYear = (Integer) birthdayYearComboBox.getSelectedItem();
    Month birthdayMonth = (Month) birthdayMonthComboBox.getSelectedItem();
    Integer birthdayDay = (Integer) birthdayDayComboBox.getSelectedItem();
    char[] passwordArr = passwordField.getPassword();
    String password = new String(passwordArr);
    Arrays.fill(passwordArr, (char) 0);
    Image profilePicture = null;
    if(imageFromFileRadioButton.isSelected()) {
      if(fileSelector.getFilePath() != null) {
        profilePicture = new FileImage(fileSelector.getFilePath());
      }
    }
    Request request = new CreateUserRequest(
        communicator.getHttpClient(),
        frame,
        firstName,
        lastName,
        emailAddress,
        sexOfPerson,
        country,
        birthdayYear,
        birthdayMonth,
        birthdayDay,
        password,
        profilePicture
    ) {

      @Override
      protected void onCreateUser(String status, Person person) {
        if(status.equals("INVALID_PROFILE_PICTURE")) {
          communicator.promptForCreateAccount("Invalid profile picture");
        } else if(status.equals("ERROR_CREATING_USER")) {
          communicator.promptForCreateAccount("Error creating user");
        } else if(status.equals("CONNECTION_ERROR")) {
          communicator.promptForCreateAccount("Error connecting to Modeling Commons");
        } else if(status.equals("SUCCESS")) {
          communicator.setPerson(person);
          communicator.promptForUpload();
        } else {
          communicator.promptForCreateAccount("Unknown server error");
        }
      }

    };
    request.execute();
  }

  private void onCancel() {
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

    JPanel formPanel = new JPanel();
    JPanel errorPanel = new JPanel();
    JPanel userAgreementNoticePanel = new JPanel();
    JPanel userAgreementPanel = new JPanel();
    JPanel alreadyRegisteredPanel = new JPanel();
    JPanel buttonsPanel = new JPanel();
    topLevelContainer.add(formPanel);
    topLevelContainer.add(errorPanel);
    topLevelContainer.add(Box.createVerticalGlue());
    topLevelContainer.add(userAgreementNoticePanel);
    topLevelContainer.add(userAgreementPanel);
    topLevelContainer.add(alreadyRegisteredPanel);
    topLevelContainer.add(buttonsPanel);

    formPanel.setLayout(new BorderLayout());
    JPanel formLabels = new JPanel(new GridLayout(11, 1));
    formLabels.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 10));
    JPanel formFields = new JPanel(new GridLayout(11, 1));
    formPanel.add(formLabels, BorderLayout.LINE_START);
    formPanel.add(formFields, BorderLayout.CENTER);

    GridBagConstraints constraints = new GridBagConstraints();
    constraints.fill = GridBagConstraints.HORIZONTAL;
    constraints.weightx = 1;

    formLabels.add(new JLabel("First Name"));
    firstNameField = new JTextField();
    JPanel firstNameFieldPanel = new JPanel(new GridBagLayout());
    firstNameFieldPanel.add(firstNameField, constraints);
    formFields.add(firstNameFieldPanel);

    formLabels.add(new JLabel("Last Name"));
    lastNameField = new JTextField();
    JPanel lastNameFieldPanel = new JPanel(new GridBagLayout());
    lastNameFieldPanel.add(lastNameField, constraints);
    formFields.add(lastNameFieldPanel);

    formLabels.add(new JLabel("Email Address"));
    emailAddressField = new JTextField();
    JPanel emailAddressFieldPanel = new JPanel(new GridBagLayout());
    emailAddressFieldPanel.add(emailAddressField, constraints);
    formFields.add(emailAddressFieldPanel);

    formLabels.add(new JLabel(""));
    femaleRadioButton = new JRadioButton("Female");
    formFields.add(femaleRadioButton);

    formLabels.add(new JLabel("Sex"));
    maleRadioButton = new JRadioButton("Male");
    formFields.add(maleRadioButton);

    formLabels.add(new JLabel("Country"));
    countryComboBox = new DisableableComboBox();
    JPanel countryComboBoxPanel = new JPanel(new GridBagLayout());
    countryComboBoxPanel.add(countryComboBox, constraints);
    formFields.add(countryComboBoxPanel);

    formLabels.add(new JLabel("Birthday (Optional)"));
    JPanel birthdayPanel = new JPanel(new GridLayout(1, 3, 10, 0));
    formFields.add(birthdayPanel);
    birthdayYearComboBox = new JComboBox<Integer>();
    birthdayMonthComboBox = new JComboBox<Month>();
    birthdayDayComboBox = new JComboBox<Integer>();
    JPanel birthdayYearComboBoxPanel = new JPanel(new GridBagLayout());
    JPanel birthdayMonthComboBoxPanel = new JPanel(new GridBagLayout());
    JPanel birthdayDayComboBoxPanel = new JPanel(new GridBagLayout());
    birthdayYearComboBoxPanel.add(birthdayYearComboBox, constraints);
    birthdayMonthComboBoxPanel.add(birthdayMonthComboBox, constraints);
    birthdayDayComboBoxPanel.add(birthdayDayComboBox, constraints);
    birthdayPanel.add(birthdayYearComboBoxPanel);
    birthdayPanel.add(birthdayMonthComboBoxPanel);
    birthdayPanel.add(birthdayDayComboBoxPanel);

    formLabels.add(new JLabel("Password"));
    JPanel passwordFieldPanel = new JPanel(new GridBagLayout());
    passwordField = new JPasswordField();
    passwordFieldPanel.add(passwordField, constraints);
    formFields.add(passwordFieldPanel);

    formLabels.add(new JLabel("Password Confirmation"));
    JPanel passwordConfirmFieldPanel = new JPanel(new GridBagLayout());
    passwordConfirmField = new JPasswordField();
    passwordConfirmFieldPanel.add(passwordConfirmField, constraints);
    formFields.add(passwordConfirmFieldPanel);

    formLabels.add(new JLabel("Profile Picture"));
    JPanel filePanel = new JPanel();
    filePanel.setLayout(new BoxLayout(filePanel, BoxLayout.X_AXIS));
    imageFromFileRadioButton = new JRadioButton("Image from file");
    filePanel.add(imageFromFileRadioButton);
    fileSelector = new FileSelector(filePanel);
    filePanel.add(fileSelector);
    formFields.add(filePanel);

    formLabels.add(new JLabel(""));
    noProfilePictureRadioButton = new JRadioButton("No profile picture");
    formFields.add(noProfilePictureRadioButton);

    setMaxHeightToPreferredHeight(formPanel);

    errorPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 5));
    errorLabel = new JLabel("Error message");
    errorLabel.setForeground(Color.RED);
    errorPanel.add(errorLabel);
    setMaxHeightToPreferredHeight(errorPanel);

    userAgreementNoticePanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 5));
    userAgreementNoticePanel.add(new JLabel("By clicking 'Create Account', you agree to the user agreement below:"));
    setMaxHeightToPreferredHeight(userAgreementNoticePanel);

    userAgreementPanel.setLayout(new BorderLayout());
    JScrollPane userAgreementScrollPane = new JScrollPane();
    userAgreementTextPane = new JTextPane();
    userAgreementTextPane.setContentType("text/html");
    userAgreementTextPane.setText("<b>This</b> is some test text!!!");
    userAgreementTextPane.setPreferredSize(new Dimension(userAgreementTextPane.getPreferredSize().width, 150));
    userAgreementScrollPane.setViewportView(userAgreementTextPane);
    userAgreementPanel.add(userAgreementScrollPane, BorderLayout.CENTER);
    setMaxHeightToPreferredHeight(userAgreementPanel);

    alreadyRegisteredPanel.setLayout(new FlowLayout(FlowLayout.LEADING, 0, 5));
    alreadyRegisteredPanel.add(new JLabel("Already registered?"));
    setMaxHeightToPreferredHeight(alreadyRegisteredPanel);

    buttonsPanel.setLayout(new BoxLayout(buttonsPanel, BoxLayout.X_AXIS));
    loginButton = new JButton("Login");
    cancelButton = new JButton("Cancel");
    createAccountButton = new JButton("Create Account");
    buttonsPanel.add(loginButton);
    buttonsPanel.add(Box.createHorizontalGlue());
    buttonsPanel.add(cancelButton);
    buttonsPanel.add(Box.createRigidArea(new Dimension(10, 0)));
    buttonsPanel.add(createAccountButton);
    setMaxHeightToPreferredHeight(buttonsPanel);

    ButtonGroup sexButtonGroup = new ButtonGroup();
    sexButtonGroup.add(maleRadioButton);
    sexButtonGroup.add(femaleRadioButton);

    ButtonGroup profilePictureButtonGroup = new ButtonGroup();
    profilePictureButtonGroup.add(imageFromFileRadioButton);
    profilePictureButtonGroup.add(noProfilePictureRadioButton);
  }

}

