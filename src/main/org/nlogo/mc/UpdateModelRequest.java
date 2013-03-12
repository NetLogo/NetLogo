// (C) Uri Wilensky. https://github.com/NetLogo/NetLogo

package org.nlogo.mc;

import org.apache.http.client.HttpClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import javax.swing.JDialog;
import java.awt.Frame;

public abstract strictfp class UpdateModelRequest extends MultiPartPostRequest {

  private JDialog loadingDialog;
  private String invalid;
  private Frame frame;

  public UpdateModelRequest(HttpClient http, Frame frame, int existingModelId, String newModelName, String modelBody, String description, NewModelType newModelType) {
    super(http, ModelingCommons.HOST + "/upload/update_model");
    this.frame = frame;
    if(!(newModelType == NewModelType.NEW_VERSION || newModelType == NewModelType.CHILD)) {
      throw new IllegalArgumentException("Invalid upload type - must be child or new version");
    }
    addStringParam("new_version[name]", newModelName);
    addStringParam("new_version[description]", description);
    addStringParam("new_version[node_id]", "" + existingModelId);
    addStringParam("fork", newModelType.toString());
    String modelName;
    if(newModelType == NewModelType.NEW_VERSION) {
      modelName = "" + existingModelId + "_new";
    } else {
      modelName = newModelName;
    }
    addFileParam("new_version[uploaded_body]", modelBody, modelName + ".nlogo");
  }

  @Override
  protected void onReturn(String response) {
    loadingDialog.dispose();
    if(response == null) {
      onUploaded("CONNECTION_ERROR", null, null);
      return;
    }
    JSONParser json = new JSONParser();
    try {
      JSONObject obj = (JSONObject)(json.parse(response));
      String status = (String)(obj.get("status"));
      String uploadedModelURL = null;
      String uploadedModelName = null;
      if(status.equals("SUCCESS")) {
        JSONObject model = ((JSONObject)(obj.get("model")));
        uploadedModelURL = (String)(model.get("url"));
        uploadedModelName = (String)(model.get("name"));
      }
      onUploaded(status, uploadedModelURL, uploadedModelName);
    } catch(ParseException e) {
      onUploaded("INVALID_RESPONSE_FROM_SERVER", null, null);
    }
  }

  @Override
  public void execute() {
    if(invalid != null) {
      onUploaded(invalid, null, null);
      return;
    }
    loadingDialog = new LoadingDialog(frame, "Uploading model to Modeling Commons");
    super.execute();
    loadingDialog.setVisible(true);
  }

  protected abstract void onUploaded(String status, String uploadedModelURL, String uploadedModelName);

}

