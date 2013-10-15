// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.apache.http.client.HttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.JDialog;
import java.awt.Frame;

public abstract strictfp class LogoutRequest extends PostRequest {

  private JDialog loadingDialog;
  private Frame frame;

  public LogoutRequest(HttpClient http, Frame frame) {
    super(http, ModelingCommons.HOST + "/account/logout");
    this.frame = frame;
  }

  @Override
  protected void onReturn(String response) {
    loadingDialog.dispose();
    if(response == null) {
      onLogout("CONNECTION_ERROR", false);
      return;
    }
    JSONParser json = new JSONParser();
    try {
      JSONObject obj = (JSONObject)(json.parse(response));
      String status = (String)(obj.get("status"));
      if(status.equals("SUCCESS") || status.equals("NOT_LOGGED_IN")) {
        onLogout("SUCCESS", true);
      } else {
        onLogout(status, false);
      }
    } catch(ParseException e) {
      onLogout("INVALID_RESPONSE_FROM_SERVER", false);
    }
  }

  @Override
  public void execute() {
    loadingDialog = new LoadingDialog(frame, "Logging out of Modeling Commons");
    super.execute();
    loadingDialog.setVisible(true);
  }

  protected abstract void onLogout(String status, boolean logoutSuccessful);

}

