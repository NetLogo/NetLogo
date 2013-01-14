package org.nlogo.modelingcommons;

import org.nlogo.awt.UserCancelException;
import org.nlogo.swing.FileDialog;
import org.nlogo.swing.ModalProgressTask;

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
import java.awt.FontMetrics;
import java.awt.Frame;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.text.DateFormat;
import java.text.DateFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * Created with IntelliJ IDEA.
 * User: Ben
 * Date: 12/11/12
 * Time: 4:23 PM
 * To change this template use File | Settings | File Templates.
 */
public class ModelingCommonsNewUserDialog extends JDialog {
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
  private DisablableComboBox countryComboBox;
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


  ModelingCommonsNewUserDialog(final Frame frame, final ModelingCommons communicator, final String errorLabelText) {
    super(frame, "Register As A New User", true);
    this.communicator = communicator;
    this.frame = frame;
    errorLabel.setText(errorLabelText);

    setContentPane(contentPane);
    setModal(true);
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
    for(ModelingCommons.Month month : ModelingCommons.Month.getMonths()) {
      birthdayMonthComboBox.addItem(month);
    }

    birthdayDayComboBox.addItem(null);
    for(int d = 1; d <= 31; d++) {
      birthdayDayComboBox.addItem(d);
    }

    selectFileButton.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent actionEvent) {
        try {
          profilePictureFilePath = FileDialog.show(frame, "Select image to use as profile picture", java.awt.FileDialog.LOAD);
          String toSet = profilePictureFilePath;
          FontMetrics metrics = selectedFileLabel.getFontMetrics(selectedFileLabel.getFont());
          System.out.println("" + selectedFileLabel.getMaximumSize().width);
          while(metrics.stringWidth(toSet) > selectedFileLabel.getMaximumSize().width) {
            toSet = "\u2026" + toSet.substring(2);
          }
          selectedFileLabel.setText(toSet);
        } catch(UserCancelException e) {}
      }
    });

    imageFromFileRadioButton.addChangeListener(new ChangeListener() {
      @Override
      public void stateChanged(ChangeEvent changeEvent) {
        if(imageFromFileRadioButton.isSelected()) {
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
    if(passwordField.getText().length() == 0) {
      errorLabel.setText("Password cannot be blank");
      return false;
    }
    if(!( passwordField.getText().equals( passwordConfirmField.getText() ) ) ) {
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
    ModalProgressTask.apply(frame, "Uploading model to Modeling Commons", new Runnable() {
      public void run() {
        String firstName = firstNameField.getText().trim();
        String lastName = lastNameField.getText().trim();
        String emailAddress = emailAddressField.getText().trim();
        ModelingCommons.Sex sex;
        if(femaleRadioButton.isSelected()) {
          sex = ModelingCommons.Sex.FEMALE;
        } else {
          sex = ModelingCommons.Sex.MALE;
        }
        String country = (String)(countryComboBox.getSelectedItem());
        System.out.println(country);
        Integer birthdayYear = (Integer)(birthdayYearComboBox.getSelectedItem());
        ModelingCommons.Month birthdayMonth = (ModelingCommons.Month)(birthdayMonthComboBox.getSelectedItem());
        Integer birthdayDay = (Integer)(birthdayDayComboBox.getSelectedItem());
        String password = passwordField.getText();
        ModelingCommons.Image profilePicture = null;
        if(imageFromFileRadioButton.isSelected()) {
          profilePicture = communicator.new FileImage(profilePictureFilePath);
          System.out.println(profilePictureFilePath);
        }
        final String result = communicator.createUser(
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
        );
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            if(result.equals("INVALID_PROFILE_PICTURE")) {
              communicator.promptForCreateAccount("Invalid profile picture");
            } else if(result.equals("ERROR_CREATING_USER")) {
              communicator.promptForCreateAccount("Error creating user");
            } else if(result.equals("CONNECTION_ERROR")) {
              communicator.promptForCreateAccount("Error connecting to Modeling Commons");
            } else if(result.equals("SUCCESS")) {
              communicator.promptForUpload();
            } else {
              communicator.promptForCreateAccount("Unknown server error");
            }
          }
        });
/*



        final String result = communicator.uploadModel(
            modelName,
            group,
            visibility,
            changeability,
            previewImage
        );
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
            } else if(result.equals("INVALID_PREVIEW_IMAGE")) {
              communicator.promptForUpload("Invalid preview image");
            } else if(result.equals("SUCCESS_PREVIEW_NOT_SAVED")) {
              communicator.promptForSuccess("The model was uploaded, but the preview image was not saved");
            } else {
              communicator.promptForUpload("Unknown server error");
            }
          }
        });*/
      }
    });
  }

  private void onCancel() {
    dispose();
  }

}
