// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.apache.http.client.HttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.imageio.ImageIO;
import javax.swing.JDialog;
import java.awt.Frame;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public abstract strictfp class CreateUserRequest extends MultiPartPostRequest {

  private JDialog loadingDialog;
  private String invalid;
  private Frame frame;

  public CreateUserRequest(HttpClient http, Frame frame, String firstName, String lastName, String emailAddress, SexOfPerson sexOfPerson, String country, Integer birthdayYear, Month birthdayMonth, Integer birthdayDay, String password, Image profilePicture) {
    super(http, ModelingCommons.HOST + "/account/create");
    this.frame = frame;
    addStringParam("new_person[first_name]", firstName);
    addStringParam("new_person[last_name]", lastName);
    addStringParam("new_person[email_address]", emailAddress);
    addStringParam("new_person[sex]", sexOfPerson.toString());
    addStringParam("new_person[country_name]", country);
    String birthdayYearString = "";
    if(birthdayYear != null) {
      birthdayYearString = birthdayYear.toString();
    }
    String birthdayMonthString = "";
    if(birthdayMonth != null) {
      birthdayMonthString = "" + birthdayMonth.getMonthNum();
    }
    String birthdayDayString = "";
    if(birthdayDay != null) {
      birthdayDayString = birthdayDay.toString();
    }
    addStringParam("new_person[birthdate(1i)]", birthdayYearString);
    addStringParam("new_person[birthdate(2i)]", birthdayMonthString);
    addStringParam("new_person[birthdate(3i)]", birthdayDayString);
    addStringParam("new_person[password]", password);
    addStringParam("new_person[registration_consent]","1");
    if(profilePicture != null) {
      try {
        ByteArrayOutputStream profilePictureStream = new ByteArrayOutputStream();
        BufferedImage image = profilePicture.getImage();
        if(image == null) {
          invalid = "INVALID_PROFILE_PICTURE";
        } else {
          ImageIO.write(image, "png", profilePictureStream);
          addFileParam("new_person[avatar]", profilePictureStream.toByteArray(), firstName + "_" + lastName + ".png");
        }
      } catch(ImageException e) {
        //Thrown when the picture selected by the user is invalid (ie file that is not an image)
        invalid = "INVALID_PROFILE_PICTURE";
      } catch(IOException e) {
        //Thrown ImageIO.write fails
        //This should probably never be reached
        invalid = "INVALID_PROFILE_PICTURE";
      }
    }
  }

  @Override
  protected void onReturn(String response) {
    loadingDialog.dispose();
    if(response == null) {
      onCreateUser("CONNECTION_ERROR", null);
      return;
    }
    JSONParser json = new JSONParser();
    try {
      JSONObject obj = (JSONObject)(json.parse(response));
      String status = (String)(obj.get("status"));
      Person person = null;
      if(status.equals("SUCCESS")) {
        JSONObject personObj = (JSONObject)(obj.get("person"));
        person = new Person(
            (String) personObj.get("first_name"),
            (String) personObj.get("last_name"),
            ((Number) personObj.get("id")).intValue(),
            (String) personObj.get("avatar"),
            (String) personObj.get("email_address")
        );
      }
      onCreateUser(status, person);
    } catch(ParseException e) {
      onCreateUser("INVALID_RESPONSE_FROM_SERVER", null);
    }
  }

  @Override
  public void execute() {
    if(invalid != null) {
      onCreateUser(invalid, null);
      return;
    }
    loadingDialog = new LoadingDialog(frame, "Creating account on Modeling Commons");
    super.execute();
    loadingDialog.setVisible(true);
  }

  protected abstract void onCreateUser(String status, Person person);

}

