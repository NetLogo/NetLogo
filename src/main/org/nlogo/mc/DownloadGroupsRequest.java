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

public abstract strictfp class DownloadGroupsRequest extends PostRequest {

  private Frame frame;
  private JDialog loadingDialog;

  public DownloadGroupsRequest(HttpClient http, Frame frame) {
    super(http, ModelingCommons.HOST + "/account/list_groups");
    this.frame = frame;
  }

  @Override
  protected void onReturn(String response) {
    loadingDialog.dispose();
    if(response == null) {
      onDownloaded("CONNECTION_ERROR", null);
      return;
    }
    JSONParser json = new JSONParser();
    try {
      List<Group> newGroups = new ArrayList<Group>();
      JSONObject obj = (JSONObject) json.parse(response);
      JSONArray groupsArr = (JSONArray) obj.get("groups");
      for(Object groupObj : groupsArr) {
        JSONObject group = (JSONObject) groupObj;
        int id = ((Number) group.get("id")).intValue();
        String name = (String) group.get("name");
        newGroups.add(new Group(id, name));
      }
      onDownloaded("SUCCESS", newGroups);
    } catch(ParseException e) {
      onDownloaded("INVALID_RESPONSE_FROM_SERVER", null);
    }
  }

  @Override
  public void execute() {
    loadingDialog = new LoadingDialog(frame, "Loading groups you belong to");
    super.execute();
    loadingDialog.setVisible(true);
  }

  protected abstract void onDownloaded(String status, List<Group> groups);

}

