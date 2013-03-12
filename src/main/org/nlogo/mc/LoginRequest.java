// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.apache.http.client.HttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.JDialog;
import java.awt.Frame;

public abstract strictfp class LoginRequest extends PostRequest {

  private JDialog loadingDialog;
  private Frame frame;

  public LoginRequest(HttpClient http, Frame frame, String email, String password) {
    super(http, ModelingCommons.HOST + "/account/login_action");
    this.frame = frame;
    addStringParam("email_address", email);
    addStringParam("password", password);
  }

  @Override
  protected void onReturn(String response) {
    loadingDialog.dispose();
    if(response == null) {
      onLogin("CONNECTION_ERROR", null);
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
      onLogin(status, person);
    } catch(ParseException e) {
      onLogin("INVALID_RESPONSE_FROM_SERVER", null);
    }
  }

  @Override
  public void execute() {
    loadingDialog = new LoadingDialog(frame, "Logging in to Modeling Commons");
    super.execute();
    loadingDialog.setVisible(true);
  }

  protected abstract void onLogin(String status, Person person);

}

