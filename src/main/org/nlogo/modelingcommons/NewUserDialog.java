package org.nlogo.modelingcommons;

import com.intellij.uiDesigner.core.GridConstraints;
import com.intellij.uiDesigner.core.GridLayoutManager;
import com.intellij.uiDesigner.core.Spacer;
import org.nlogo.awt.UserCancelException;
import org.nlogo.swing.FileDialog;
import org.nlogo.swing.ModalProgressTask;

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
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Arrays;
import java.util.Calendar;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 12/11/12
 * Time: 4:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class NewUserDialog extends JDialog {
  private JButton loginButton;
  private JButton createAccountButton;
  private JButton cancelButton;
  private JTextField emailAddressField;
  private JRadioButton femaleRadioButton;
  private JRadioButton maleRadioButton;
  private JComboBox birthdayYearComboBox;
  private JComboBox birthdayMonthComboBox;
  private JComboBox birthdayDayComboBox;
  private JPasswordField passwordField;
  private JPasswordField passwordConfirmField;
  private JButton selectFileButton;
  private JTextField firstNameField;
  private JTextField lastNameField;
  private DisableableComboBox countryComboBox;
  private JLabel selectedFileLabel;
  private JLabel errorLabel;
  private JPanel contentPane;
  private JTextPane userAgreementTextPane;
  private JScrollPane userAgreementScrollPane;
  private JRadioButton imageFromFileRadioButton;
  private JRadioButton noProfilePictureRadioButton;
  private String profilePictureFilePath;
  private ModelingCommons communicator;
  private Frame frame;


  NewUserDialog(final Frame frame, final ModelingCommons communicator, final String errorLabelText) {
    super(frame, "Register As A New User", true);
    this.communicator = communicator;
    this.frame = frame;
    errorLabel.setText(errorLabelText);

    setContentPane(contentPane);
    setModal(true);
    getRootPane().setDefaultButton(createAccountButton);

    for (String country : communicator.getPriorityCountries()) {
      countryComboBox.addItem(country, true);
    }
    countryComboBox.addItem("--------", false);
    for (String country : communicator.getUnpriorityCountries()) {
      countryComboBox.addItem(country, true);
    }

    femaleRadioButton.setSelected(true);

    userAgreementTextPane.setText(communicator.getNewUserAgreement());
    userAgreementTextPane.setCaretPosition(0);

    int startYear = 1930;
    birthdayYearComboBox.addItem(null);
    for (int y = Calendar.getInstance().get(Calendar.YEAR); y >= startYear; y--) {
      birthdayYearComboBox.addItem(y);
    }


    birthdayMonthComboBox.addItem(null);
    for (Month month : Month.getMonths()) {
      birthdayMonthComboBox.addItem(month);
    }

    birthdayDayComboBox.addItem(null);
    for (int d = 1; d <= 31; d++) {
      birthdayDayComboBox.addItem(d);
    }

    selectFileButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        try {
          profilePictureFilePath = FileDialog.show(frame, "Select image to use as profile picture", java.awt.FileDialog.LOAD);
          String toSet = profilePictureFilePath;
          FontMetrics metrics = selectedFileLabel.getFontMetrics(selectedFileLabel.getFont());
          while (metrics.stringWidth(toSet) > selectedFileLabel.getMaximumSize().width) {
            toSet = "\u2026" + toSet.substring(2);
          }
          selectedFileLabel.setText(toSet);
        } catch (UserCancelException e) {
        }
      }
    });

    imageFromFileRadioButton.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent changeEvent) {
        if (imageFromFileRadioButton.isSelected()) {
          selectFileButton.setEnabled(true);
          selectedFileLabel.setEnabled(true);
        } else {
          selectFileButton.setEnabled(false);
          selectedFileLabel.setEnabled(false);
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
        communicator.promptForLogin();
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

    this.pack();
    this.setLocationRelativeTo(frame);
    this.setResizable(false);
  }

  private boolean isValidInput() {
    if (firstNameField.getText().length() == 0) {
      errorLabel.setText("First name cannot be blank");
      return false;
    }
    if (lastNameField.getText().length() == 0) {
      errorLabel.setText("Last name cannot be blank");
      return false;
    }
    if (emailAddressField.getText().length() == 0) {
      errorLabel.setText("Email address cannot be blank");
      //Probably should do actual email address validation here
      return false;
    }
    if (passwordField.getPassword().length == 0) {
      errorLabel.setText("Password cannot be blank");
      return false;
    }
    if (!(Arrays.equals(passwordField.getPassword(), passwordConfirmField.getPassword()))) {
      errorLabel.setText("Passwords do not match");
      return false;
    }


    return true;
  }

  private void onOK() {
    if (!isValidInput()) {
      return;
    }
    dispose();

    String firstName = firstNameField.getText().trim();
    String lastName = lastNameField.getText().trim();
    String emailAddress = emailAddressField.getText().trim();
    Sex sex;
    if (femaleRadioButton.isSelected()) {
      sex = Sex.FEMALE;
    } else {
      sex = Sex.MALE;
    }
    String country = (String) (countryComboBox.getSelectedObject());
    Integer birthdayYear = (Integer) (birthdayYearComboBox.getSelectedItem());
    Month birthdayMonth = (Month) (birthdayMonthComboBox.getSelectedItem());
    Integer birthdayDay = (Integer) (birthdayDayComboBox.getSelectedItem());

    char[] passwordArr = passwordField.getPassword();
    String password = new String(passwordArr);
    Arrays.fill(passwordArr, (char) 0);

    Image profilePicture = null;
    if (imageFromFileRadioButton.isSelected()) {
      profilePicture = new FileImage(profilePictureFilePath);
    }

    ModelingCommons.CreateUserRequest request = communicator.new CreateUserRequest(
        firstName,
        lastName,
        emailAddress,
        sex,
        country,
        birthdayYear,
        birthdayMonth,
        birthdayDay,
        password,
        profilePicture
    ) {
      @Override
      protected void onCreateUser(String status) {
        if (status.equals("INVALID_PROFILE_PICTURE")) {
          communicator.promptForCreateAccount("Invalid profile picture");
        } else if (status.equals("ERROR_CREATING_USER")) {
          communicator.promptForCreateAccount("Error creating user");
        } else if (status.equals("CONNECTION_ERROR")) {
          communicator.promptForCreateAccount("Error connecting to Modeling Commons");
        } else if (status.equals("SUCCESS")) {
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

  {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
    $$$setupUI$$$();
  }

  /**
   * Method generated by IntelliJ IDEA GUI Designer
   * >>> IMPORTANT!! <<<
   * DO NOT edit this method OR call it in your code!
   *
   * @noinspection ALL
   */
  private void $$$setupUI$$$() {
    contentPane = new JPanel();
    contentPane.setLayout(new GridLayoutManager(4, 1, new Insets(10, 10, 10, 10), -1, -1));
    final JPanel panel1 = new JPanel();
    panel1.setLayout(new GridLayoutManager(10, 2, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final JLabel label1 = new JLabel();
    label1.setText("First Name");
    panel1.add(label1, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    firstNameField = new JTextField();
    firstNameField.setText("");
    panel1.add(firstNameField, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label2 = new JLabel();
    label2.setText("Last Name");
    panel1.add(label2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    lastNameField = new JTextField();
    panel1.add(lastNameField, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label3 = new JLabel();
    label3.setText("Email Address");
    panel1.add(label3, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label4 = new JLabel();
    label4.setText("Sex");
    panel1.add(label4, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label5 = new JLabel();
    label5.setText("Country");
    panel1.add(label5, new GridConstraints(4, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label6 = new JLabel();
    label6.setText("Birthday (Optional)");
    panel1.add(label6, new GridConstraints(5, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label7 = new JLabel();
    label7.setText("Password");
    panel1.add(label7, new GridConstraints(6, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label8 = new JLabel();
    label8.setText("Password Comfirmation");
    panel1.add(label8, new GridConstraints(7, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    emailAddressField = new JTextField();
    panel1.add(emailAddressField, new GridConstraints(2, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JPanel panel2 = new JPanel();
    panel2.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
    panel1.add(panel2, new GridConstraints(3, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    femaleRadioButton = new JRadioButton();
    femaleRadioButton.setText("Female");
    panel2.add(femaleRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    maleRadioButton = new JRadioButton();
    maleRadioButton.setText("Male");
    panel2.add(maleRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    countryComboBox = new DisableableComboBox();
    panel1.add(countryComboBox, new GridConstraints(4, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel3 = new JPanel();
    panel3.setLayout(new GridLayoutManager(1, 3, new Insets(0, 0, 0, 0), -1, -1));
    panel1.add(panel3, new GridConstraints(5, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    birthdayYearComboBox = new JComboBox();
    panel3.add(birthdayYearComboBox, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    birthdayMonthComboBox = new JComboBox();
    panel3.add(birthdayMonthComboBox, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    birthdayDayComboBox = new JComboBox();
    panel3.add(birthdayDayComboBox, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    passwordField = new JPasswordField();
    panel1.add(passwordField, new GridConstraints(6, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    passwordConfirmField = new JPasswordField();
    panel1.add(passwordConfirmField, new GridConstraints(7, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_FIXED, null, new Dimension(150, -1), null, 0, false));
    final JLabel label9 = new JLabel();
    label9.setText("Profile Picture");
    panel1.add(label9, new GridConstraints(8, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    errorLabel = new JLabel();
    errorLabel.setForeground(new Color(-65536));
    errorLabel.setText(" ");
    panel1.add(errorLabel, new GridConstraints(9, 0, 1, 2, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel4 = new JPanel();
    panel4.setLayout(new GridLayoutManager(2, 3, new Insets(0, 0, 0, 0), -1, -1));
    panel1.add(panel4, new GridConstraints(8, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_VERTICAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    imageFromFileRadioButton = new JRadioButton();
    imageFromFileRadioButton.setText("Image from file");
    panel4.add(imageFromFileRadioButton, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    noProfilePictureRadioButton = new JRadioButton();
    noProfilePictureRadioButton.setText("No profile picture");
    panel4.add(noProfilePictureRadioButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    selectedFileLabel = new JLabel();
    selectedFileLabel.setMaximumSize(new Dimension(250, 16));
    selectedFileLabel.setText("No file selected");
    panel4.add(selectedFileLabel, new GridConstraints(0, 2, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    selectFileButton = new JButton();
    selectFileButton.setText("Select File");
    panel4.add(selectFileButton, new GridConstraints(0, 1, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JPanel panel5 = new JPanel();
    panel5.setLayout(new GridLayoutManager(2, 4, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel5, new GridConstraints(3, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    loginButton = new JButton();
    loginButton.setText("Login");
    panel5.add(loginButton, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    createAccountButton = new JButton();
    createAccountButton.setText("Create Account");
    panel5.add(createAccountButton, new GridConstraints(1, 3, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer1 = new Spacer();
    panel5.add(spacer1, new GridConstraints(1, 1, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_WANT_GROW, 1, null, null, null, 0, false));
    cancelButton = new JButton();
    cancelButton.setText("Cancel");
    panel5.add(cancelButton, new GridConstraints(1, 2, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_HORIZONTAL, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final JLabel label10 = new JLabel();
    label10.setText("Already registered?");
    panel5.add(label10, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    final Spacer spacer2 = new Spacer();
    contentPane.add(spacer2, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_VERTICAL, 1, GridConstraints.SIZEPOLICY_WANT_GROW, null, null, null, 0, false));
    final JPanel panel6 = new JPanel();
    panel6.setLayout(new GridLayoutManager(2, 1, new Insets(0, 0, 0, 0), -1, -1));
    contentPane.add(panel6, new GridConstraints(2, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_CAN_GROW, null, null, null, 0, false));
    final JLabel label11 = new JLabel();
    label11.setText("By clicking 'Create Account', you agree to the user agreement below:");
    panel6.add(label11, new GridConstraints(0, 0, 1, 1, GridConstraints.ANCHOR_WEST, GridConstraints.FILL_NONE, GridConstraints.SIZEPOLICY_FIXED, GridConstraints.SIZEPOLICY_FIXED, null, null, null, 0, false));
    userAgreementScrollPane = new JScrollPane();
    panel6.add(userAgreementScrollPane, new GridConstraints(1, 0, 1, 1, GridConstraints.ANCHOR_CENTER, GridConstraints.FILL_BOTH, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, GridConstraints.SIZEPOLICY_CAN_SHRINK | GridConstraints.SIZEPOLICY_WANT_GROW, null, new Dimension(700, 150), null, 0, false));
    userAgreementTextPane = new JTextPane();
    userAgreementTextPane.setContentType("text/html");
    userAgreementTextPane.setText("<html>\n  <head>\n\n  </head>\n  <body>\n    <p style=\"margin-top: 0\">\n      \n    </p>\n  </body>\n</html>\n");
    userAgreementScrollPane.setViewportView(userAgreementTextPane);
    ButtonGroup buttonGroup;
    buttonGroup = new ButtonGroup();
    buttonGroup.add(femaleRadioButton);
    buttonGroup.add(maleRadioButton);
    buttonGroup = new ButtonGroup();
    buttonGroup.add(imageFromFileRadioButton);
    buttonGroup.add(noProfilePictureRadioButton);
  }

  /**
   * @noinspection ALL
   */
  public JComponent $$$getRootComponent$$$() {
    return contentPane;
  }
}
