// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.apache.http.client.HttpClient;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.JDialog;
import java.awt.Frame;
import java.util.ArrayList;
import java.util.List;

public abstract strictfp class DownloadNewUserParametersRequest extends PostRequest {

  private JDialog loadingDialog;
  private Frame frame;

  public DownloadNewUserParametersRequest(HttpClient http, Frame frame) {
    super(http, ModelingCommons.HOST + "/account/new");
    this.frame = frame;
  }

  @Override
  protected void onReturn(String response) {
    loadingDialog.dispose();
    if(response == null) {
      onDownloaded("CONNECTION_ERROR", null, null, null);
      return;
    }
    JSONParser json = new JSONParser();
    try {
      List<String> priorityCountries = new ArrayList<String>();
      List<String> unpriorityCountries = new ArrayList<String>();
      JSONObject obj = (JSONObject) json.parse(response);
      JSONArray countries = (JSONArray) obj.get("countries");
      for(Object countryObj : countries) {
        JSONObject country = (JSONObject) countryObj;
        String countryName = (String) country.get("name");
        Boolean isPriority = (Boolean) country.get("priority");
        if(isPriority) {
          priorityCountries.add(countryName);
        } else {
          unpriorityCountries.add(countryName);
        }
      }
      String userAgreement = (String)(obj.get("user_agreement"));
      onDownloaded("SUCCESS", userAgreement, priorityCountries, unpriorityCountries);
    } catch(ParseException e) {
      onDownloaded("INVALID_RESPONSE_FROM_SERVER", null, null, null);
    }
  }

  @Override
  public void execute() {
    loadingDialog = new LoadingDialog(frame, "Loading new user information");
    super.execute();
    loadingDialog.setVisible(true);
  }

  protected abstract void onDownloaded(String status, String newUserAgreement, List<String> priorityCountries, List<String> unpriorityCountries);

}

